/*
 * Copyright 2013-2014 the original author or authors.
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

package org.springframework.batch.admin.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.batch.admin.domain.JobExecutionInfo;
import org.springframework.batch.admin.domain.JobExecutionInfoResource;
import org.springframework.batch.admin.domain.NoSuchBatchJobException;
import org.springframework.batch.admin.domain.support.JobParametersExtractor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.PagedResources.PageMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for batch job executions.
 *
 * @author Dave Syer
 * @author Ilayaperumal Gopinathan
 * @author Gunnar Hillert
 * @since 2.0
 */
@RestController
@RequestMapping("/batch/executions")
@ExposesResourceFor(JobExecutionInfoResource.class)
public class BatchJobExecutionsController extends AbstractBatchJobsController {

	@Autowired
	private ListableJobLocator jobLocator;

	/**
	 * List all job executions in a given range. If no pagination is provided,
	 * the default {@code PageRequest(0, 20)} is passed in. See {@link org.springframework.data.web.PageableHandlerMethodArgumentResolver}
	 * for details.
	 *
	 * @param pageable If not provided will default to page 0 and a page size of 20
	 * @return Collection of JobExecutionInfoResource
	 */
	@RequestMapping(value = { "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	public PagedResources<JobExecutionInfoResource> list(Pageable pageable) throws NoSuchJobException {

		Collection<JobExecutionInfoResource> resources = new ArrayList<JobExecutionInfoResource>();

		for (JobExecution jobExecution : jobService.listJobExecutions(pageable.getOffset(), pageable.getPageSize())) {
			Job job = jobLocator.getJob(jobExecution.getJobInstance().getJobName());

			final JobExecutionInfoResource jobExecutionInfoResource = getJobExecutionInfoResource(jobExecution,
					job.isRestartable());
			resources.add(jobExecutionInfoResource);
		}

		return new PagedResources<JobExecutionInfoResource>(resources,
				new PageMetadata(pageable.getPageSize(), pageable.getPageNumber(),
						jobService.countJobExecutions()));
	}

	/**
	 * Return a paged collection of job executions for a given job.
	 *
	 * @param jobName name of the job
	 * @param startJobExecution start index for the job execution list
	 * @param pageSize page size for the list
	 * @return collection of JobExecutionInfo
	 */
	@RequestMapping(value = "", method = RequestMethod.GET, params = "jobname", produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	public Collection<JobExecutionInfoResource> executionsForJob(@RequestParam("jobname") String jobName,
			@RequestParam(defaultValue = "0") int startJobExecution,
			@RequestParam(defaultValue = "20") int pageSize) {

		Collection<JobExecutionInfoResource> result = new ArrayList<JobExecutionInfoResource>();
		try {
			for (JobExecution jobExecution : jobService.listJobExecutionsForJob(jobName, startJobExecution, pageSize)) {
				result.add(jobExecutionInfoResourceAssembler.toResource(new JobExecutionInfo(jobExecution, timeZone)));
			}
		}
		catch (NoSuchJobException e) {
			throw new NoSuchBatchJobException(jobName);
		}
		return result;
	}

	/**
	 * Send the request to launch Job. Job has to be deployed first.
	 *
	 * @param name the name of the job
	 * @param jobParameters the job parameters in comma delimited form
	 */
	@RequestMapping(value = "", method = RequestMethod.POST, params = "jobname")
	@ResponseStatus(HttpStatus.CREATED)
	public void launchJob(@RequestParam("jobname") String name, @RequestParam(required = false) String jobParameters) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, NoSuchJobException {
		JobParameters params = new JobParameters();
		if(jobParameters != null) {
			JobParametersExtractor extractor = new JobParametersExtractor();
			extractor.fromString(jobParameters);
		}

		jobService.launch(name, params);
	}

	/**
	 * Get all existing job definition names.
	 *
	 * @return the collection of job definition names
	 */
	private Collection<String> getJobDefinitionNames() {
		int totalJobs = jobService.countJobs();
		return jobService.listJobs(0, totalJobs);
	}

	/**
	 * Check if the {@link org.springframework.batch.core.JobInstance} corresponds to the given {@link org.springframework.batch.core.JobExecution}
	 * has any of the JobExecutions in {@link org.springframework.batch.core.BatchStatus#COMPLETED} status
	 * @param jobExecution the jobExecution to check for
	 * @return boolean flag to set if this job execution can be restarted
	 */
	private boolean isJobExecutionRestartable(JobExecution jobExecution) {
		JobInstance jobInstance = jobExecution.getJobInstance();
		BatchStatus status = jobExecution.getStatus();
		try {
			List<JobExecution> jobExecutionsForJobInstance = (List<JobExecution>) jobService.getJobExecutionsForJobInstance(
					jobInstance.getJobName(), jobInstance.getId());
			for (JobExecution jobExecutionForJobInstance : jobExecutionsForJobInstance) {
				if (jobExecutionForJobInstance.getStatus() == BatchStatus.COMPLETED) {
					return false;
				}
			}
		}
		catch (NoSuchJobException e) {
			throw new NoSuchBatchJobException(jobInstance.getJobName());
		}
		return status.isGreaterThan(BatchStatus.STOPPING) && status.isLessThan(BatchStatus.ABANDONED);
	}

	/**
	 * @param executionId Id of the {@link org.springframework.batch.core.JobExecution}
	 * @return JobExecutionInfo for the given job name
	 * @throws org.springframework.batch.core.launch.NoSuchJobExecutionException Thrown if the {@link org.springframework.batch.core.JobExecution} does not exist
	 */
	@RequestMapping(value = "/{executionId}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public JobExecutionInfoResource getJobExecutionInfo(@PathVariable long executionId) throws NoSuchJobExecutionException {

		final JobExecution jobExecution;

		try {
			jobExecution = jobService.getJobExecution(executionId);
		}
		catch (org.springframework.batch.core.launch.NoSuchJobExecutionException e) {
			throw new NoSuchJobExecutionException(String.format("Could not find jobExecution with id %s", String.valueOf(executionId)));
		}

		final Job job;
		String jobName = jobExecution.getJobInstance().getJobName();

		try {
			job = jobLocator.getJob(jobName);
		}
		catch (NoSuchJobException e1) {
			throw new NoSuchBatchJobException("The job '" + jobName + "' does not exist.");
		}

		return getJobExecutionInfoResource(jobExecution, job.isRestartable());
	}

	private JobExecutionInfoResource getJobExecutionInfoResource(JobExecution jobExecution,
			boolean restartable) {

		final JobExecutionInfoResource jobExecutionInfoResource = jobExecutionInfoResourceAssembler.toResource(new JobExecutionInfo(
				jobExecution,
				timeZone));
		if (restartable) {
			// Set restartable flag for the JobExecutionResource based on the actual JobInstance
			// If any one of the jobExecutions for the jobInstance is complete, set the restartable flag for
			// all the jobExecutions to false.
			if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
				jobExecutionInfoResource.setRestartable(isJobExecutionRestartable(jobExecution));
			}
		}
		else {
			// Set false for this job execution irrespective its status.
			jobExecutionInfoResource.setRestartable(false);
		}

		return jobExecutionInfoResource;
	}

	/**
	 * Stop Job Execution by the given executionId.
	 *
	 * @param jobExecutionId the executionId of the job execution to stop
	 */
	@RequestMapping(value = { "/{executionId}" }, method = RequestMethod.PUT, params = "stop=true")
	@ResponseStatus(HttpStatus.OK)
	public void stopJobExecution(@PathVariable("executionId") long jobExecutionId) throws JobExecutionNotRunningException, NoSuchJobExecutionException {
		try {
			jobService.stop(jobExecutionId);
		}
		catch (org.springframework.batch.core.launch.JobExecutionNotRunningException e) {
			throw new JobExecutionNotRunningException(String.format("Job execution with executionId %s is not running.", String.valueOf(jobExecutionId)));
		}
		catch (org.springframework.batch.core.launch.NoSuchJobExecutionException e) {
			throw new NoSuchJobExecutionException(String.format("Could not find jobExecution with id %s", String.valueOf(jobExecutionId)));
		}
	}

	/**
	 * Restart the Job Execution with the given executionId.
	 *
	 * @param jobExecutionId the executionId of the job execution to restart
	 */
	@RequestMapping(value = { "/{executionId}" }, method = RequestMethod.PUT, params = "restart=true")
	@ResponseStatus(HttpStatus.OK)
	public void restartJobExecution(@PathVariable("executionId") long jobExecutionId) throws NoSuchJobExecutionException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobInstanceAlreadyCompleteException, JobRestartException, NoSuchJobException {

		final JobExecution jobExecution;
		try {
			jobExecution = jobService.getJobExecution(jobExecutionId);
		}
		catch (org.springframework.batch.core.launch.NoSuchJobExecutionException e) {
			throw new NoSuchJobExecutionException(String.format("Could not find jobExecution with id %s", String.valueOf(jobExecutionId)));
		}

		if (jobExecution.isRunning()) {
			throw new JobExecutionAlreadyRunningException(
					"Job Execution for this job is already running: " + jobExecution.getJobInstance());
		}

		final JobInstance lastInstance = jobExecution.getJobInstance();
		final JobParameters jobParameters = jobExecution.getJobParameters();

		final Job job;
		try {
			job = jobLocator.getJob(lastInstance.getJobName());
		}
		catch (NoSuchJobException e1) {
			throw new NoSuchBatchJobException("The job '" + lastInstance.getJobName()
					+ "' does not exist.");
		}
		try {
			job.getJobParametersValidator().validate(jobParameters);
		}
		catch (JobParametersInvalidException e) {
			throw new JobParametersInvalidException(
					"The Job Parameters for Job Execution " + jobExecution.getId()
					+ " are invalid.");
		}

		final BatchStatus status = jobExecution.getStatus();

		if (status == BatchStatus.COMPLETED || status == BatchStatus.ABANDONED) {
			throw new JobInstanceAlreadyCompleteException(
					"Job Execution " + jobExecution.getId() + " is already complete.");
		}

		if (!job.isRestartable()) {
			throw new JobRestartException(
					"The job '" + lastInstance.getJobName() + "' is not restartable.");
		}

		jobService.launch(lastInstance.getJobName(), jobParameters);
	}

	/**
	 * Stop all job executions.
	 */
	@RequestMapping(value = { "" }, method = RequestMethod.PUT, params = "stop=true")
	@ResponseStatus(HttpStatus.OK)
	public void stopAll() throws NoSuchJobExecutionException, JobExecutionNotRunningException {
		jobService.stopAll();
	}
}
