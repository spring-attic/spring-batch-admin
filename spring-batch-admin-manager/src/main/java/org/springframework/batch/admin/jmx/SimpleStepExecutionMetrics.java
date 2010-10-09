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
	public int getStepExecutionCount() {
		return jobService.countStepExecutionsForStep(jobName, stepName);
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Step Execution Failure Count")
	public int getStepExecutionFailureCount() {
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
	public double getLatestStepExecutionDuration() {
		return computeHistory(stepName, 1).getDuration().getMean();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Mean Duration")
	public double getMeanStepExecutionDuration() {
		StepExecutionHistory history = computeHistory(stepName);
		return history.getDuration().getMean();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Max Duration")
	public double getMaxStepExecutionDuration() {
		StepExecutionHistory history = computeHistory(stepName);
		return history.getDuration().getMax();
	}

	private StepExecutionHistory computeHistory(String stepName) {
		// Running average over last 10 executions...
		return computeHistory(stepName, 10);
	}

	private StepExecutionHistory computeHistory(String stepName, int total) {
		StepExecutionHistory stepExecutionHistory = new StepExecutionHistory(stepName);
		for (StepExecution stepExecution : jobService.listStepExecutionsForStep(jobName, stepName, 0, total)) {
			stepExecutionHistory.append(stepExecution);
		}
		return stepExecutionHistory;
	}

}