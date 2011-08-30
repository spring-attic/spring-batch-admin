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

import org.springframework.batch.admin.history.JobExecutionHistory;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class SimpleJobExecutionMetrics implements JobExecutionMetrics {

	private final JobService jobService;

	private final String jobName;

	public SimpleJobExecutionMetrics(JobService jobService, String jobName) {
		this.jobService = jobService;
		this.jobName = jobName;
	}

	public int getExecutionCount() {
		try {
			return jobService.countJobExecutionsForJob(jobName);
		}
		catch (NoSuchJobException e) {
			throw new IllegalStateException("Cannot locate job=" + jobName, e);
		}
	}

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

	public double getLatestDuration() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		JobExecutionHistory history = new JobExecutionHistory(jobName);
		history.append(jobExecution);
		return history.getDuration().getMean();
	}

	public double getMeanDuration() {
		JobExecutionHistory history = computeHistory(jobName);
		return history.getDuration().getMean();
	}

	public double getMaxDuration() {
		JobExecutionHistory history = computeHistory(jobName);
		return history.getDuration().getMax();
	}

	public Date getLatestStartTime() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		return jobExecution==null ? null : jobExecution.getStartTime();
	}

	public Date getLatestEndTime() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		return jobExecution==null ? null : jobExecution.getEndTime();
	}

	public String getLatestExitCode() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		return jobExecution==null ? "NONE" : jobExecution.getExitStatus().getExitCode();
	}

	public String getLatestStatus() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		return jobExecution==null ? "NONE" : jobExecution.getStatus().toString();
	}

	public long getLatestExecutionId() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		return jobExecution==null ? -1 : jobExecution.getId();
	}

	public String getLatestStepExitDescription() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		StepExecution stepExecution = getLatestStepExecution(jobExecution);
		return stepExecution==null ? "" : stepExecution.getExitStatus().getExitDescription();
	}

	public String getLatestStepName() {
		JobExecution jobExecution = getLatestJobExecution(jobName);
		StepExecution stepExecution = getLatestStepExecution(jobExecution);
		return stepExecution==null ? "" : stepExecution.getStepName();
	}

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
			// On the cautious side: grab the last 4 executions by ID and look for
			// the one that was last created...
			Collection<JobExecution> jobExecutions = jobService.listJobExecutionsForJob(jobName, 0, 4);
			if (jobExecutions.isEmpty()) {
				return null;
			}
			long lastUpdated = 0L;
			JobExecution result = null;
			for (JobExecution jobExecution : jobExecutions) {
				long updated = jobExecution.getCreateTime().getTime();
				if (updated > lastUpdated) {
					result = jobExecution;
					lastUpdated = updated;
				}
				else if (result!=null && updated == lastUpdated && jobExecution.getId() > result.getId()) {
					// Tie breaker using ID
					result = jobExecution;
				}
			}
			return result;
		}
		catch (NoSuchJobException e) {
			throw new IllegalStateException("Cannot locate job=" + jobName, e);
		}
	}

	private StepExecution getLatestStepExecution(JobExecution jobExecution) {
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
				else if (stepExecution!=null && stepDate.equals(latest) && candidate.getId()>stepExecution.getId()) {
					// Tie breaker using ID
					stepExecution = candidate;						
				}
			}
		}
		return stepExecution;
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