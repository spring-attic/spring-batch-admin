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
package org.springframework.batch.admin.launch;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;

/**
 * Wrapper for a {@link JobLauncher} that synchronizes jobs globally so that
 * only one execution of a given Job can be active at once.
 * 
 * @author Dave Syer
 * 
 */
@Aspect
@ManagedResource
public class JobLauncherSynchronizer implements InitializingBean {

	private static final Log logger = LogFactory.getLog(JobLauncherSynchronizer.class);

	private JobExplorer jobExplorer;

	private JobRepository jobRepository;

	private Set<String> jobNames = new HashSet<String>();

	/**
	 * The {@link JobExplorer} to use to inspect existing executions.
	 * 
	 * @param jobExplorer a {@link JobExplorer}
	 */
	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}

	/**
	 * The {@link JobRepository} needed for updates to execution data.
	 * 
	 * @param jobRepository a {@link JobRepository}
	 */
	public void setJobRepository(JobRepository jobRepository) {
		this.jobRepository = jobRepository;
	}

	/**
	 * Set of job names that will be synchronized. Others are ignored.
	 * 
	 * @param jobNames the job names
	 */
	public void setJobNames(Set<String> jobNames) {
		this.jobNames = jobNames;
	}
	
	/**
	 * A job name that will be synchronized.
	 * 
	 * @param jobName the job name
	 */
	@ManagedOperation
	public void addJobName(String jobName) {
		this.jobNames.add(jobName);
	}
	
	/**
	 * Remove a job name from the list to synchronize.
	 * 
	 * @param jobName the job name
	 */
	@ManagedOperation
	public void removeJobName(String jobName) {
		this.jobNames.remove(jobName);
	}
	
	/**
	 * @return the jobNames
	 */
	@ManagedAttribute
	public Set<String> getJobNames() {
		return jobNames;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(jobExplorer, "A JobExplorer must be provided");
		Assert.notNull(jobRepository, "A JobRepository must be provided");
	}

	@Before("execution(* org.springframework.batch..JobLauncher+.*(..)) && args(job,..)")
	public void checkJobBeforeLaunch(Job job) throws JobExecutionAlreadyRunningException {
		String jobName = job.getName();
		logger.debug("Checking for synchronization on Job: " + jobName);
		if (!jobNames.contains(jobName)) {
			logger.debug("Not synchronizing Job: " + jobName);
			return;
		}
		Set<JobExecution> running = jobExplorer.findRunningJobExecutions(jobName);
		if (!running.isEmpty()) {
			throw new JobExecutionAlreadyRunningException("An instance of this job is already active: "+jobName);
		}
		logger.debug("Job checked and no duplicates detected: " + jobName);
	}

	@AfterReturning(value = "execution(* org.springframework.batch..JobRepository+.createJobExecution(..)) && args(jobName,..)", returning = "jobExecution")
	public void checkJobDuringLaunch(String jobName, JobExecution jobExecution)
			throws JobExecutionAlreadyRunningException {
		logger.debug("Re-checking for synchronization on JobExecution: " + jobExecution);
		if (!jobNames.contains(jobName)) {
			logger.debug("Not re-checking for synchronization of Job: " + jobName);
			return;
		}
		Set<JobExecution> running = jobExplorer.findRunningJobExecutions(jobName);
		if (running.size() > 1) {
			jobExecution.setEndTime(new Date());
			jobExecution.upgradeStatus(BatchStatus.ABANDONED);
			jobExecution.setExitStatus(jobExecution.getExitStatus().and(ExitStatus.NOOP).addExitDescription(
					"Not executed because another execution was detected for the same Job."));
			jobRepository.update(jobExecution);
			throw new JobExecutionAlreadyRunningException("An instance of this job is already active: "+jobName);
		}
	}
}
