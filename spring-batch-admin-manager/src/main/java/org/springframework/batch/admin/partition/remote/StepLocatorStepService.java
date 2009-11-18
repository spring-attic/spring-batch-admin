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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service("stepService")
public class StepLocatorStepService implements StepService {

	private Log logger = LogFactory.getLog(getClass());

	private StepLocator stepLocator;

	private Collection<StepExecution> stepExecutions = new ArrayList<StepExecution>();

	private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

	private float weight = 1;

	private JobExplorer jobExplorer;

	/**
	 * Public setter for the {@link JobExplorer}.
	 * 
	 * @param jobExplorer
	 */
	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}

	/**
	 * The weight of this service in a balanced cluster. More powerful nodes
	 * should set a weight greater than he default, to enable a load balancing
	 * client to send more requests. A load balancer should attempt to make the
	 * average load on this node <code>weight</code> times that of a default
	 * node.
	 * 
	 * @param weight the weight factor to set (default 1.0)
	 */
	public void setWeight(float weight) {
		this.weight = weight;
	}

	/**
	 * @param taskExecutor
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void setStepLocator(StepLocator stepLocator) {
		this.stepLocator = stepLocator;
	}

	public StepServiceStatus getStatus() {
		return new StepServiceStatus(calculateScore());
	}

	public StepExecutionResponse execute(StepExecutionRequest wrapper) throws NoSuchStepExecutionException,
			NoSuchStepException {

		float score = calculateScore();
		logger.info(String.format("Received step execution request (score=%f, weight=%f): %s", score, weight, wrapper));
		StepExecutionResponse response = new StepExecutionResponse(wrapper.getThreshold() * weight - calculateScore());

		// The threshold is multiplied by a weight factor to allow more powerful
		// nodes to accept more work.
		if (score + 1 > wrapper.getThreshold() * weight) {
			logger.info(String.format("Rejected step execution request (score=%f, weight=%f): %s", score, weight,
					wrapper));
			response.setRejected(true);
			return response;
		}

		Long jobExecutionId = wrapper.getJobExecutionId();
		Long stepExecutionId = wrapper.getStepExecutionId();
		final StepExecution stepExecution = jobExplorer.getStepExecution(jobExecutionId, stepExecutionId);
		if (stepExecution == null) {
			throw new NoSuchStepExecutionException(String.format(
					"Either the JobExecution with id=%d or its step execution named id=%d does not exist",
					jobExecutionId, stepExecutionId));
		}
		stepExecutions.add(stepExecution);

		final Step step = stepLocator.getStep(wrapper.getStepName());

		Runnable task = new Runnable() {
			public void run() {
				try {
					logger.info("Starting step execution: " + stepExecution);
					step.execute(stepExecution);
				}
				catch (JobInterruptedException e) {
					logger.warn("Step interrupted during execution.", e);
				}
				finally {
					stepExecutions.remove(stepExecution);
				}
			}
		};

		taskExecutor.execute(task);
		logger.info("Executing step execution request: " + wrapper);

		try {
			// Fast steps can clear the decks right away and won't affect the
			// capacity estimate...
			Thread.sleep(100L);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		response.setCapacity(wrapper.getThreshold() * weight - calculateScore());

		return response;

	}

	private float calculateScore() {
		return stepExecutions.size();
	}

}
