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
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.MetricType;

@ManagedResource
public class SimpleJobExecutionMetrics implements JobExecutionMetrics {

	private final JobService jobService;

	private final String jobName;

	public SimpleJobExecutionMetrics(JobService jobService, String stepName) {
		this.jobService = jobService;
		this.jobName = stepName;
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Count")
	public int getJobExecutionCount() {
		try {
			return jobService.countJobExecutionsForJob(jobName);
		}
		catch (NoSuchJobException e) {
			throw new IllegalStateException("Cannot locate job=" + jobName, e);
		}
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Failure Count")
	public int getJobExecutionFailureCount() {

		int pageSize = 100;
		int start = 0;
		int count = 0;

		Collection<JobExecution> jobExecutions;
		do {

			try {
				jobExecutions = jobService.listJobExecutionsForJob(jobName, start, pageSize);
				start += pageSize;
			}
			catch (NoSuchJobException e) {
				throw new IllegalStateException("Cannot locate job=" + jobName, e);
			}
			for (JobExecution jobExecution : jobExecutions) {
				if (jobExecution.getStatus().isUnsuccessful()) {
					count++;
				}
			}
		} while (!jobExecutions.isEmpty());

		return count;

	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Duration")
	public double getLatestJobExecutionDuration() {
		return computeHistory(jobName, 1).getDuration().getMean();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Mean Duration")
	public double getMeanJobExecutionDuration() {
		JobExecutionHistory history = computeHistory(jobName);
		return history.getDuration().getMean();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Max Duration")
	public double getMaxJobExecutionDuration() {
		JobExecutionHistory history = computeHistory(jobName);
		return history.getDuration().getMax();
	}

	private JobExecutionHistory computeHistory(String jobName) {
		// Running average over last 10 executions...
		return computeHistory(jobName, 10);
	}

	private JobExecutionHistory computeHistory(String jobName, int total) {
		JobExecutionHistory jobExecutionHistory = new JobExecutionHistory(jobName);
		try {
			for (JobExecution jobExecution : jobService.listJobExecutionsForJob(jobName, 0, total)) {
				jobExecutionHistory.append(jobExecution);
			}
		}
		catch (NoSuchJobException e) {
			throw new IllegalStateException("Cannot locate job=" + jobName, e);
		}
		return jobExecutionHistory;
	}

}