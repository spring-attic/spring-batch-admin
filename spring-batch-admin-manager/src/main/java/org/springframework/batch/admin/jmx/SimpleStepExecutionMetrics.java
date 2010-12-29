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
package org.springframework.batch.admin.jmx;

import java.util.Collection;
import java.util.Date;

import org.springframework.batch.admin.history.StepExecutionHistory;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.StepExecution;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class SimpleStepExecutionMetrics implements StepExecutionMetrics {

	private final JobService jobService;

	private final String stepName;

	private final String jobName;

	public SimpleStepExecutionMetrics(JobService jobService, String jobName, String stepName) {
		this.jobService = jobService;
		this.jobName = jobName;
		this.stepName = stepName;
	}

	public int getExecutionCount() {
		return jobService.countStepExecutionsForStep(jobName, stepName);
	}

	public int getFailureCount() {
		int count = 0;
		int start = 0;
		int pageSize = 100;
		Collection<StepExecution> stepExecutions;
		do {
			stepExecutions = jobService.listStepExecutionsForStep(jobName, stepName, start, pageSize);
			start += pageSize;
			for (StepExecution stepExecution : stepExecutions) {
				if (stepExecution.getStatus().isUnsuccessful()) {
					count++;
				}
			}
		} while (!stepExecutions.isEmpty());
		return count;
	}

	public double getLatestDuration() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		if (stepExecution==null) {
			return 0;
		}
		Date endTime = stepExecution.getEndTime();
		return (endTime != null ? endTime.getTime() : System.currentTimeMillis())
				- stepExecution.getStartTime().getTime();
	}

	public double getMeanDuration() {
		StepExecutionHistory history = computeHistory(stepName);
		return history.getDuration().getMean();
	}

	public double getMaxDuration() {
		StepExecutionHistory history = computeHistory(stepName);
		return history.getDuration().getMax();
	}

	public int getLatestReadCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getReadCount();
	}

	public int getLatestWriteCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getWriteCount();
	}

	public int getLatestFilterCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getFilterCount();
	}

	public int getLatestSkipCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getSkipCount();
	}

	public int getLatestCommitCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getCommitCount();
	}

	public int getLatestRollbackCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getRollbackCount();
	}

	public long getLatestExecutionId() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? -1 : stepExecution.getId();
	}

	public String getLatestStatus() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? "NON" : stepExecution.getStatus().toString();
	}

	public String getLatestExitCode() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? "NONE" : stepExecution.getExitStatus().getExitCode();
	}

	public String getLatestExitDescription() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? "" : stepExecution.getExitStatus().getExitDescription();
	}

	private StepExecutionHistory computeHistory(String stepName) {
		// Running average over last 10 executions...
		return computeHistory(stepName, 10);
	}

	private StepExecution getLatestStepExecution(String stepName) {
		// On the cautious side: grab the last 4 executions by ID and look for
		// the one that was last started...
		Collection<StepExecution> stepExecutions = jobService.listStepExecutionsForStep(jobName, stepName, 0, 4);
		if (stepExecutions.isEmpty()) {
			return null;
		}
		long lastUpdated = 0L;
		StepExecution result = null;
		for (StepExecution stepExecution : stepExecutions) {
			long updated = stepExecution.getStartTime().getTime();
			if (updated > lastUpdated) {
				result = stepExecution;
				lastUpdated = updated;
			}
		}
		return result;
	}

	private StepExecutionHistory computeHistory(String stepName, int total) {
		StepExecutionHistory stepExecutionHistory = new StepExecutionHistory(stepName);
		for (StepExecution stepExecution : jobService.listStepExecutionsForStep(jobName, stepName, 0, total)) {
			stepExecutionHistory.append(stepExecution);
		}
		return stepExecutionHistory;
	}

}