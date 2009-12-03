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

import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

/**
 * Adapt a job name to a {@link JobLaunchRequest} for restarting the last failed
 * execution of the {@link Job}. The parameters of the last execution are pulled
 * out of the {@link JobExplorer}.
 * 
 * @author Dave Syer
 * 
 */
@MessageEndpoint
public class JobNameToJobRestartRequestAdapter {

	private JobLocator jobLocator;

	private JobExplorer jobExplorer;

	public void setJobLocator(JobLocator jobLocator) {
		this.jobLocator = jobLocator;
	}

	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}

	@ServiceActivator
	public JobLaunchRequest adapt(String jobName) throws NoSuchJobException,
			JobParametersNotFoundException {
		jobName = jobName.trim();
		Job job = jobLocator.getJob(jobName);
		JobParameters jobParameters = getLastFailedJobParameters(jobName);
		return new JobLaunchRequest(job, jobParameters);
	}

	/**
	 * @param jobName
	 * @return
	 * @throws JobParametersNotFoundException
	 */
	private JobParameters getLastFailedJobParameters(String jobName)
			throws JobParametersNotFoundException {

		int start = 0;
		int count = 100;
		List<JobInstance> lastInstances = jobExplorer.getJobInstances(jobName,
				start, count);

		JobParameters jobParameters = null;

		if (lastInstances.isEmpty()) {
			throw new JobParametersNotFoundException(
					"No job instance found for job=" + jobName);
		}

		while (!lastInstances.isEmpty()) {

			for (JobInstance jobInstance : lastInstances) {
				List<JobExecution> jobExecutions = jobExplorer
						.getJobExecutions(jobInstance);
				if (jobExecutions == null || jobExecutions.isEmpty()) {
					continue;
				}
				JobExecution jobExecution = jobExecutions.get(jobExecutions
						.size() - 1);
				if (jobExecution.getStatus()
						.isGreaterThan(BatchStatus.STOPPING)) {
					jobParameters = jobInstance.getJobParameters();
					break;
				}
			}

			if (jobParameters != null) {
				break;
			}

			start += count;
			lastInstances = jobExplorer.getJobInstances(jobName, start, count);

		}

		if (jobParameters == null) {
			throw new JobParametersNotFoundException(
					"No failed or stopped execution found for job=" + jobName);
		}
		return jobParameters;

	}

}
