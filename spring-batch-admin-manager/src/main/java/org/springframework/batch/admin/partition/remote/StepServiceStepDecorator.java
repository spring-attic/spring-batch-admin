/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.admin.partition.remote;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class StepServiceStepDecorator implements InitializingBean {

	public static final long DEFAULT_POLL_INTERVAL = 200;

	// 1 HOUR
	public static final long DEFAULT_TIMEOUT = 3600 * 1000;

	private JobExplorer jobExplorer;

	private StepService stepService;

	private long pollInterval = DEFAULT_POLL_INTERVAL;

	private long timeout = DEFAULT_TIMEOUT;

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(jobExplorer);
		Assert.notNull(stepService);
	}

	public void setPollInterval(long pollInterval) {
		this.pollInterval = pollInterval;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}

	public void setStepService(StepService stepService) {
		this.stepService = stepService;
	}

	public void execute(String remoteStepName, StepExecution stepExecution) throws Exception {
		StepExecutionRequest request = new StepExecutionRequest(stepExecution.getJobExecutionId(), stepExecution
				.getId(), remoteStepName);
		// TODO: add flag for optional balancing
		new BalancedStepServiceHandler().handle(stepService, request);
		waitForResult(stepExecution);
	}

	// TODO: does it make sense to use Spring Integration for polling?
	private void waitForResult(StepExecution request) throws Exception {

		long maxCount = (timeout + pollInterval) / pollInterval;
		long count = 0;

		StepExecution stepExecution = request;
		while (stepExecution.getStatus().isRunning() && ++count <= maxCount) {
			stepExecution = getResult(request);
			// TODO: this should be unregistered, but then we need a stack in
			// the synchronization manager
			StepExecutionSynchronizationManager.register(stepExecution);
			try {
				// TODO: use wait/notify for short-lived steps?
				Thread.sleep(pollInterval);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new UnexpectedJobExecutionException("Interrupted waiting for stepExecution: id="
						+ request.getId());
			}
		}

		if (stepExecution.getStatus().isRunning() && count >= maxCount) {
			throw new UnexpectedJobExecutionException("Timed out waiting for stepExecution: id=" + request.getId());
		}

	}

	private StepExecution getResult(StepExecution request) {
		return jobExplorer.getStepExecution(request.getJobExecutionId(), request.getId());
	}
}
