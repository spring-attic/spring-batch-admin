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
package org.springframework.batch.admin.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller for job executions.
 * 
 * @author Dave Syer
 * 
 */
@Controller
public class JobExecutionController {

	private static Log logger = LogFactory.getLog(JobExecutionController.class);

	public static class StopRequest {
		private Long jobExecutionId;

		public Long getJobExecutionId() {
			return jobExecutionId;
		}

		public void setJobExecutionId(Long jobExecutionId) {
			this.jobExecutionId = jobExecutionId;
		}

	}

	private JobService jobService;

	@Autowired
	public JobExecutionController(JobService jobService) {
		super();
		this.jobService = jobService;
	}

	@RequestMapping(value = "/jobs/executions/{jobExecutionId}", method = RequestMethod.DELETE)
	public String stop(Model model, @ModelAttribute("stopRequest") StopRequest stopRequest, Errors errors,
			@PathVariable Long jobExecutionId) {

		stopRequest.jobExecutionId = jobExecutionId;
		try {
			JobExecution jobExecution = jobService.stop(jobExecutionId);
			model.addAttribute(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));
		}
		catch (NoSuchJobExecutionException e) {
			errors.reject("no.such.job.execution", new Object[] { jobExecutionId }, "No job exection with id="
					+ jobExecutionId);
		}
		catch (JobExecutionNotRunningException e) {
			errors.reject("job.execution.not.running", "Job exection with id=" + jobExecutionId + " is not running.");
			JobExecution jobExecution;
			try {
				jobExecution = jobService.getJobExecution(jobExecutionId);
				model.addAttribute(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));
			}
			catch (NoSuchJobExecutionException e1) {
				// safe
			}
		}

		return "jobs/execution";

	}

	@RequestMapping(value = "/jobs/executions/{jobExecutionId}", method = RequestMethod.DELETE, params = "abandon")
	public String abandon(Model model, @ModelAttribute("stopRequest") StopRequest stopRequest, Errors errors,
			@PathVariable Long jobExecutionId) {

		stopRequest.jobExecutionId = jobExecutionId;
		try {
			JobExecution jobExecution = jobService.abandon(jobExecutionId);
			model.addAttribute(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));
		}
		catch (NoSuchJobExecutionException e) {
			errors.reject("no.such.job.execution", new Object[] { jobExecutionId }, "No job exection with id="
					+ jobExecutionId);
		}
		catch (JobExecutionAlreadyRunningException e) {
			errors.reject("job.execution.running", "Job exection with id=" + jobExecutionId + " is running.");
			JobExecution jobExecution;
			try {
				jobExecution = jobService.getJobExecution(jobExecutionId);
				model.addAttribute(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));
			}
			catch (NoSuchJobExecutionException e1) {
				// safe
			}
		}

		return "jobs/execution";

	}

	@RequestMapping(value = {"/jobs/executions", "/jobs/executions.*"}, method = RequestMethod.GET)
	public @ModelAttribute("jobExecutions")
	Collection<JobExecutionInfo> list(ModelMap model, @RequestParam(defaultValue = "0") int startJobExecution,
			@RequestParam(defaultValue = "20") int pageSize) {

		int total = jobService.countJobExecutions();
		TableUtils.addPagination(model, total, startJobExecution, pageSize, "JobExecution");

		Collection<JobExecutionInfo> result = new ArrayList<JobExecutionInfo>();
		for (JobExecution jobExecution : jobService.listJobExecutions(startJobExecution, pageSize)) {
			result.add(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));
		}

		return result;

	}

	@RequestMapping(value = "/jobs/{jobName}/{jobInstanceId}/executions", method = RequestMethod.GET)
	public String listForInstance(Model model, @PathVariable String jobName, @PathVariable long jobInstanceId) {

		Collection<JobExecutionInfo> result = new ArrayList<JobExecutionInfo>();
		try {
			for (JobExecution jobExecution : jobService.getJobExecutionsForJobInstance(jobName, jobInstanceId)) {
				result.add(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));
			}
		}
		catch (NoSuchJobException e) {
			// TODO error message
		}
		model.addAttribute(new JobInfo(jobName, result.size(), jobInstanceId, null));
		model.addAttribute("jobExecutions", result);
		return "jobs/executions";

	}

	@RequestMapping(value = "/jobs/{jobName}/{jobInstanceId}/executions", method = RequestMethod.POST)
	public String restart(Model model, @PathVariable String jobName, @PathVariable long jobInstanceId) {

		try {

			Collection<JobExecution> jobExecutions = jobService.getJobExecutionsForJobInstance(jobName, jobInstanceId);
			model.addAttribute(new JobInfo(jobName, jobExecutions.size()+1));
			JobExecution jobExecution = jobExecutions.iterator().next();
			model.addAttribute(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));

			try {

				jobExecution = jobService.restart(jobExecution.getId());
				model.addAttribute(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));

			}
			catch (NoSuchJobExecutionException e) {
				// TODO error message
			}
			catch (JobExecutionAlreadyRunningException e) {
				// TODO error message
			}
			catch (JobRestartException e) {
				// TODO error message
			}
			catch (JobInstanceAlreadyCompleteException e) {
				// TODO error message
			}
			catch (JobParametersInvalidException e) {
				// TODO error message
			}

		}
		catch (NoSuchJobException e) {
			// TODO error message
		}

		return "jobs/execution";

	}

	@RequestMapping(value = "/jobs/executions", method = RequestMethod.DELETE)
	public @ModelAttribute("jobExecutions")
	Collection<JobExecutionInfo> stopAll(ModelMap model, @RequestParam(defaultValue = "0") int startJobExecution,
			@RequestParam(defaultValue = "20") int pageSize) {

		model.addAttribute("stoppedCount", jobService.stopAll());
		return list(model, startJobExecution, pageSize);

	}

	@RequestMapping(value = "/jobs/{jobName}/executions", method = RequestMethod.GET)
	public String listForJob(ModelMap model, @PathVariable String jobName,
			@RequestParam(defaultValue = "0") int startJobExecution, @RequestParam(defaultValue = "20") int pageSize) {

		int total = startJobExecution;
		try {
			total = jobService.countJobExecutionsForJob(jobName);
		}
		catch (NoSuchJobException e) {
			// TODO: add an error message
			logger.warn("Could not locate Job with name=" + jobName);
		}
		TableUtils.addPagination(model, total, startJobExecution, pageSize, "JobExecution");

		Collection<JobExecutionInfo> result = new ArrayList<JobExecutionInfo>();
		try {

			for (JobExecution jobExecution : jobService.listJobExecutionsForJob(jobName, startJobExecution, pageSize)) {
				result.add(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));
			}
			int count = jobService.countJobExecutionsForJob(jobName);
			model.addAttribute(new JobInfo(jobName, count));
			model.addAttribute("jobExecutions", result);

		}
		catch (NoSuchJobException e) {
			// TODO: add an error message
			logger.warn("Could not locate Job with name=" + jobName);
		}

		return "jobs/executions";

	}

	@RequestMapping(value = "/jobs/executions/{jobExecutionId}", method = RequestMethod.GET)
	public String detail(Model model, @PathVariable Long jobExecutionId) {

		try {
			JobExecution jobExecution = jobService.getJobExecution(jobExecutionId);
			model.addAttribute(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));
		}
		catch (NoSuchJobExecutionException e) {
			// TODO: add an error message
		}

		return "jobs/execution";

	}

}
