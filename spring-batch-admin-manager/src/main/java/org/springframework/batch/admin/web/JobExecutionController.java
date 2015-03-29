/*
 * Copyright 2009-2014 the original author or authors.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.batch.admin.domain.JobExecutionInfo;
import org.springframework.batch.admin.domain.JobInfo;
import org.springframework.batch.admin.domain.StepExecutionInfo;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
	private ObjectMapper objectMapper;
	private TimeZone timeZone = TimeZone.getDefault();

	/**
	 * @param timeZone the timeZone to set
	 */
	@Autowired(required = false)
	@Qualifier("userTimeZone")
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

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
			model.addAttribute(new JobExecutionInfo(jobExecution, timeZone));
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
				model.addAttribute(new JobExecutionInfo(jobExecution, timeZone));
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
			model.addAttribute(new JobExecutionInfo(jobExecution, timeZone));
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
				model.addAttribute(new JobExecutionInfo(jobExecution, timeZone));
			}
			catch (NoSuchJobExecutionException e1) {
				// safe
			}
		}

		return "jobs/execution";

	}

	@RequestMapping(value = { "/jobs/executions", "/jobs/executions.*" }, method = RequestMethod.GET)
	public @ModelAttribute("jobExecutions")
	Collection<JobExecutionInfo> list(ModelMap model, @RequestParam(defaultValue = "0") int startJobExecution,
			@RequestParam(defaultValue = "20") int pageSize) {

		int total = jobService.countJobExecutions();
		TableUtils.addPagination(model, total, startJobExecution, pageSize, "JobExecution");

		Collection<JobExecutionInfo> result = new ArrayList<JobExecutionInfo>();
		for (JobExecution jobExecution : jobService.listJobExecutions(startJobExecution, pageSize)) {
			result.add(new JobExecutionInfo(jobExecution, timeZone));
		}

		return result;

	}

	@RequestMapping(value = { "/jobs/{jobName}/{jobInstanceId}/executions", "/jobs/{jobName}/{jobInstanceId}" }, method = RequestMethod.GET)
	public String listForInstance(Model model, @PathVariable String jobName, @PathVariable long jobInstanceId,
			@ModelAttribute("date") Date date, Errors errors) {

		JobInstance jobInstance = null;
		try {
			jobInstance = jobService.getJobInstance(jobInstanceId);
			if (!jobInstance.getJobName().equals(jobName)) {
				errors.reject("wrong.job.name", new Object[] { jobInstanceId, jobInstance.getJobName(), jobName },
						"The JobInstance with id=" + jobInstanceId + " has the wrong name (" + jobInstance.getJobName()
						+ " not " + jobName);
			}
		}
		catch (NoSuchJobInstanceException e) {
			errors.reject("no.such.job.instance", new Object[] { jobInstanceId }, "There is no such job instance ("
					+ jobInstanceId + ")");
		}
		if (jobInstance != null && (errors == null || !errors.hasErrors())) {
			Collection<JobExecutionInfo> result = new ArrayList<JobExecutionInfo>();
			try {
				Collection<JobExecution> jobExecutions = jobService.getJobExecutionsForJobInstance(jobName,
						jobInstanceId);
				for (JobExecution jobExecution : jobExecutions) {
					result.add(new JobExecutionInfo(jobExecution, timeZone));
				}
			}
			catch (NoSuchJobException e) {
				errors.reject("no.such.job", new Object[] { jobName }, "There is no such job (" + jobName + ")");
			}
			model.addAttribute(new JobInfo(jobName, result.size(), jobInstanceId, jobService.isLaunchable(jobName), jobService.isIncrementable(jobName)));
			model.addAttribute("jobExecutions", result);
		}
		return "jobs/executions";

	}

	@RequestMapping(value = "/jobs/{jobName}/{jobInstanceId}/executions", method = RequestMethod.POST)
	public String restart(Model model, @PathVariable String jobName, @PathVariable long jobInstanceId,
			@ModelAttribute("date") Date date, Errors errors) {

		try {

			Collection<JobExecution> jobExecutions = jobService.getJobExecutionsForJobInstance(jobName, jobInstanceId);
			model.addAttribute(new JobInfo(jobName, jobExecutions.size() + 1));
			JobExecution jobExecution = jobExecutions.iterator().next();
			model.addAttribute(new JobExecutionInfo(jobExecution, timeZone));

			Long jobExecutionId = jobExecution.getId();

			try {

				jobExecution = jobService.restart(jobExecutionId);
				model.addAttribute(new JobExecutionInfo(jobExecution, timeZone));

			}
			catch (NoSuchJobExecutionException e) {
				errors.reject("no.such.job.execution", new Object[] { jobExecutionId },
						"There is no such job execution (" + jobExecutionId + ")");
			}
			catch (JobExecutionAlreadyRunningException e) {
				errors.reject("job.execution.already.running", new Object[] { jobExecutionId },
						"This job execution is already running (" + jobExecutionId + ")");
			}
			catch (JobRestartException e) {
				errors.reject("job.restart.exception", new Object[] { jobName },
						"There was a problem restarting the job (" + jobName + ")");
			}
			catch (JobInstanceAlreadyCompleteException e) {
				errors.reject("job.instance.already.complete", new Object[] { jobName },
						"The job instance is already complete for (" + jobName
						+ "). Use different job parameters to launch it again.");
			}
			catch (JobParametersInvalidException e) {
				errors.reject("job.parameters.invalid", new Object[] { jobName },
						"The job parameters are invalid according to the job (" + jobName + ")");
			}

		}
		catch (NoSuchJobException e) {
			errors.reject("no.such.job", new Object[] { jobName }, "There is no such job (" + jobName + ")");
		}

		return "jobs/execution";

	}

	@RequestMapping(value = "/jobs/executions", method = RequestMethod.DELETE)
	public @ModelAttribute("jobExecutions")
	Collection<JobExecutionInfo> stopAll(ModelMap model, @RequestParam(defaultValue = "0") int startJobExecution,
			@RequestParam(defaultValue = "20") int pageSize) throws NoSuchJobExecutionException, JobExecutionNotRunningException {

		model.addAttribute("stoppedCount", jobService.stopAll());
		return list(model, startJobExecution, pageSize);

	}

	@RequestMapping(value = "/jobs/{jobName}/executions", method = RequestMethod.GET)
	public String listForJob(ModelMap model, @PathVariable String jobName, @ModelAttribute("date") Date date,
			Errors errors, @RequestParam(defaultValue = "0") int startJobExecution,
			@RequestParam(defaultValue = "20") int pageSize) {

		int total = startJobExecution;
		try {
			total = jobService.countJobExecutionsForJob(jobName);
		}
		catch (NoSuchJobException e) {
			errors.reject("no.such.job", new Object[] { jobName }, "There is no such job (" + jobName + ")");
			logger.warn("Could not locate Job with name=" + jobName);
			return "jobs/executions";
		}
		TableUtils.addPagination(model, total, startJobExecution, pageSize, "JobExecution");

		Collection<JobExecutionInfo> result = new ArrayList<JobExecutionInfo>();
		try {

			for (JobExecution jobExecution : jobService.listJobExecutionsForJob(jobName, startJobExecution, pageSize)) {
				result.add(new JobExecutionInfo(jobExecution, timeZone));
			}
			int count = jobService.countJobExecutionsForJob(jobName);
			model.addAttribute(new JobInfo(jobName, count, null, jobService.isLaunchable(jobName), jobService
					.isIncrementable(jobName)));
			model.addAttribute("jobExecutions", result);

		}
		catch (NoSuchJobException e) {
			errors.reject("no.such.job", new Object[] { jobName }, "There is no such job (" + jobName + ")");
			logger.warn("Could not locate Job with name=" + jobName);
		}

		return "jobs/executions";

	}

	@RequestMapping(value = "/jobs/executions/{jobExecutionId}", method = RequestMethod.GET)
	public String detail(Model model, @PathVariable Long jobExecutionId, @ModelAttribute("date") Date date,
			Errors errors) {

		try {
			JobExecution jobExecution = jobService.getJobExecution(jobExecutionId);
			model.addAttribute(new JobExecutionInfo(jobExecution, timeZone));
			String jobName = jobExecution.getJobInstance().getJobName();
//			Collection<String> stepNames = new HashSet<String>(jobService.getStepNamesForJob(jobName));
//			Collection<StepExecution> stepExecutions = new ArrayList<StepExecution>(jobExecution.getStepExecutions());
			List<StepExecutionInfo> stepExecutionInfos = new ArrayList<StepExecutionInfo>();

			for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
				stepExecutionInfos.add(new StepExecutionInfo(stepExecution, timeZone));
			}

//			for (String name : stepNames) {
//				boolean found = false;
//				for (Iterator<StepExecution> iterator = stepExecutions.iterator(); iterator.hasNext();) {
//					StepExecution stepExecution = iterator.next();
//					if (stepExecution.getStepName().equals(name)) {
//						stepExecutionInfos.add(new StepExecutionInfo(stepExecution, timeZone));
//						iterator.remove();
//						found = true;
//					}
//				}
//				if (!found) {
//					stepExecutionInfos.add(new StepExecutionInfo(jobName, jobExecutionId, name, timeZone));
//				}
//			}

			Collections.sort(stepExecutionInfos, new Comparator<StepExecutionInfo>() {
				@Override
				public int compare(StepExecutionInfo o1, StepExecutionInfo o2) {
					return o1.getId().compareTo(o2.getId());
				}
			});

			model.addAttribute("stepExecutionInfos", stepExecutionInfos);
		}
		catch (NoSuchJobExecutionException e) {
			errors.reject("no.such.job.execution", new Object[] { jobExecutionId }, "There is no such job execution ("
					+ jobExecutionId + ")");
		}
//		catch (NoSuchJobException e) {
//			errors.reject("no.such.job", new Object[] { jobExecutionId }, "There is no such job with exeuction id ("
//					+ jobExecutionId + ")");
//		}

		return "jobs/execution";

	}

	@RequestMapping(value = "/jobs/executions/{jobExecutionId}/execution-context", method = RequestMethod.GET)
	public String getExecutionContext(Model model, @PathVariable Long jobExecutionId, @ModelAttribute("date") Date date,
									  Errors errors) {
		try {
			JobExecution jobExecution = jobService.getJobExecution(jobExecutionId);
			Map<String, Object> executionMap = new HashMap<String, Object>();

			for (Map.Entry<String, Object> entry : jobExecution.getExecutionContext().entrySet()) {
				executionMap.put(entry.getKey(), entry.getValue());
			}

			model.addAttribute("jobExecutionContext", objectMapper.writeValueAsString(executionMap));
			model.addAttribute("jobExecutionId", jobExecutionId);
		}
		catch (NoSuchJobExecutionException e) {
			errors.reject("no.such.job.execution", new Object[] { jobExecutionId }, "There is no such job execution ("
					+ jobExecutionId + ")");
		} catch (IOException e) {
			errors.reject("serialization.error", new Object[] { jobExecutionId }, "Error serializing execution context for job execution ("
					+ jobExecutionId + ")");
		}

		return "jobs/executions/execution-context";
	}
}
