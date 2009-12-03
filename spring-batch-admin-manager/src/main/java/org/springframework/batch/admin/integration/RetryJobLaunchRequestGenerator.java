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
package org.springframework.batch.admin.integration;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

/**
 * Service endpoint to convert a recent failed job execution into a restart
 * request, if the failure is identifiable as a remote failure.
 * 
 * @author Dave Syer
 * 
 */
@MessageEndpoint
public class RetryJobLaunchRequestGenerator {

	private static Log logger = LogFactory.getLog(RetryJobLaunchRequestGenerator.class);

	private Map<Long, JobExecution> executions = new WeakHashMap<Long, JobExecution>();

	private JobLocator jobLocator;

	private JobExplorer jobExplorer;

	public void setJobLocator(JobLocator jobLocator) {
		this.jobLocator = jobLocator;
	}

	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}

	@ServiceActivator
	public JobLaunchRequest retry(JobExecution jobExecution) throws NoSuchJobException {

		if (jobExecution.getStatus().isLessThan(BatchStatus.STOPPED)) {
			logger.debug("No restart needed or necessary for " + jobExecution);
			return null;
		}

		if (jobExplorer.getJobExecutions(jobExecution.getJobInstance()).size() > 1) {
			logger.debug("Already restarted according to repository so nothing to do " + jobExecution);
			return null;
		}

		if (executions.containsKey(jobExecution.getId())) {
			logger.debug("Already processed so nothing to do " + jobExecution);
			return null;
		}

		boolean retryable = false;
		for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
			if (stepExecution.getStatus().isLessThan(BatchStatus.STOPPED)) {
				continue;
			}
			if (stepExecution.getExitStatus().getExitDescription().contains("SuspectedTimeoutException")) {
				retryable = true;
				break;
			}
		}
		if (!retryable) {
			logger.debug("Not a retryable failure so cannot restart " + jobExecution);
			return null;			
		}

		logger.debug("Restart requested for " + jobExecution);
		executions.put(jobExecution.getId(), jobExecution);
		Job job;
		job = jobLocator.getJob(jobExecution.getJobInstance().getJobName());
		JobParameters jobParameters = jobExecution.getJobInstance().getJobParameters();
		return new JobLaunchRequest(job, jobParameters);

	}
}
