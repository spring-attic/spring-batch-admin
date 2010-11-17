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

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.MetricType;

@ManagedResource
public class SimpleJobExecutionMetrics implements JobExecutionMetrics {

	private final JobService jobService;

	private final String jobName;

	public SimpleJobExecutionMetrics(JobService jobService, String jobName) {
		this.jobService = jobService;
		this.jobName = jobName;
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Count")
	public int getExecutionCount() {
		try {
			return jobService.countJobExecutionsForJob(jobName);
		}
		catch (NoSuchJobException e) {
			throw new IllegalStateException("Cannot locate job=" + jobName, e);
		}
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Failure Count")
	public int getFailureCount() {

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
	public double getLatestDuration() {
		return computeHistory(jobName, 1).getDuration().getMean();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Mean Duration")
	public double getMeanDuration() {
		JobExecutionHistory history = computeHistory(jobName);
		return history.getDuration().getMean();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Max Duration")
	public double getMaxDuration() {
		JobExecutionHistory history = computeHistory(jobName);
		return history.getDuration().getMax();
	}

	@ManagedAttribute(description = "Latest Start Time")
	public Date getLatestStartTime() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		return jobExecution==null ? null : jobExecution.getStartTime();
	}

	@ManagedAttribute(description = "Latest End Time")
	public Date getLatestEndTime() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		return jobExecution==null ? null : jobExecution.getEndTime();
	}

	@ManagedAttribute(description = "Latest Exit Code")
	public String getLatestExitCode() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		return jobExecution==null ? "NONE" : jobExecution.getExitStatus().getExitCode();
	}

	@ManagedAttribute(description = "Latest Status")
	public String getLatestStatus() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		return jobExecution==null ? "NONE" : jobExecution.getStatus().toString();
	}

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Job Execution ID")
	public long getLatestExecutionId() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		return jobExecution==null ? -1 : jobExecution.getId();
	}

	@ManagedAttribute(description = "Latest Step Execution Exit Decription")
	public String getLatestStepExitDescription() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
		StepExecution stepExecution = null;
		if (!stepExecutions.isEmpty()) {
			Date latest = new Date(0L);
			for (StepExecution candidate : stepExecutions) {
				Date stepDate = candidate.getEndTime();
				stepDate = stepDate==null ? new Date() : stepDate;
				if (stepDate.after(latest)) {
					latest = stepDate;
					stepExecution = candidate;
				}				
			}
		}
		return stepExecution==null ? "" : stepExecution.getExitStatus().getExitDescription();
	}

	@ManagedAttribute(description = "Check if there is a Running Job Execution")
	public boolean isJobRunning() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		return jobExecution==null ? false : jobExecution.isRunning();
	}

	private JobExecutionHistory computeHistory(String jobName) {
		// Running average over last 10 executions...
		return computeHistory(jobName, 10);
	}

	private JobExecution getLatestJobExecution(String jobName) {
		try {
			Collection<JobExecution> jobExecutions = jobService.listJobExecutionsForJob(jobName, 0, 1);
			if (jobExecutions.isEmpty()) {
				return null;
			}
			return jobExecutions.iterator().next();
		}
		catch (NoSuchJobException e) {
			throw new IllegalStateException("Cannot locate job=" + jobName, e);
		}
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