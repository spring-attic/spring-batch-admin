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

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.web.StepExecutionHistory;
import org.springframework.batch.core.StepExecution;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.MetricType;

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

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Step Execution Count")
	public int getExecutionCount() {
		return jobService.countStepExecutionsForStep(jobName, stepName);
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Step Execution Failure Count")
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

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Duration")
	public double getLatestDuration() {
		return computeHistory(stepName, 1).getDuration().getMean();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Mean Duration")
	public double getMeanDuration() {
		StepExecutionHistory history = computeHistory(stepName);
		return history.getDuration().getMean();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Max Duration")
	public double getMaxDuration() {
		StepExecutionHistory history = computeHistory(stepName);
		return history.getDuration().getMax();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Read Count")
	public int getLatestReadCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getReadCount();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Write Count")
	public int getLatestWriteCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getWriteCount();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Filter Count")
	public int getLatestFilterCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getFilterCount();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Skip Count")
	public int getLatestSkipCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getSkipCount();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Commit Count")
	public int getLatestCommitCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getCommitCount();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Rollback Count")
	public int getLatestRollbackCount() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? 0 : stepExecution.getRollbackCount();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Step Execution ID")
	public long getLatestExecutionId() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? -1 : stepExecution.getId();
	}

	@ManagedAttribute(description = "Latest Status")
	public String getLatestStatus() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? "NON" : stepExecution.getStatus().toString();
	}

	@ManagedAttribute(description = "Latest Exit Code")
	public String getLatestExitCode() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? "NONE" : stepExecution.getExitStatus().getExitCode();
	}

	@ManagedAttribute(description = "Latest Exit Description")
	public String getLatestExitDescription() {
		StepExecution stepExecution = getLatestStepExecution(stepName);
		return stepExecution == null ? "" : stepExecution.getExitStatus().getExitDescription();
	}

	private StepExecutionHistory computeHistory(String stepName) {
		// Running average over last 10 executions...
		return computeHistory(stepName, 10);
	}

	private StepExecution getLatestStepExecution(String stepName) {
		Collection<StepExecution> stepExecutions = jobService.listStepExecutionsForStep(jobName, stepName, 0, 1);
		if (stepExecutions.isEmpty()) {
			return null;
		}
		return stepExecutions.iterator().next();
	}

	private StepExecutionHistory computeHistory(String stepName, int total) {
		StepExecutionHistory stepExecutionHistory = new StepExecutionHistory(stepName);
		for (StepExecution stepExecution : jobService.listStepExecutionsForStep(jobName, stepName, 0, total)) {
			stepExecutionHistory.append(stepExecution);
		}
		return stepExecutionHistory;
	}

}