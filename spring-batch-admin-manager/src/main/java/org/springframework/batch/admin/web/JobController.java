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
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.support.PropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for launching jobs.
 * 
 * @author Dave Syer
 * 
 */
@Controller
public class JobController {

	private final JobService jobService;

	private JobParametersConverter converter = new DefaultJobParametersConverter();

	@Autowired
	public JobController(JobService jobService) {
		super();
		this.jobService = jobService;
	}

	@RequestMapping(value = "/jobs/{jobName}", method = RequestMethod.POST)
	public String launch(ModelMap model, @PathVariable String jobName,
			@ModelAttribute("launchRequest") LaunchRequest launchRequest, Errors errors,
			@RequestParam(defaultValue = "job") String origin) {

		launchRequest.setJobName(jobName);
		String params = launchRequest.jobParameters;

		Properties properties = PropertiesConverter.stringToProperties(params);
		JobParameters jobParameters = converter.getJobParameters(properties);

		try {
			JobExecution jobExecution = jobService.launch(jobName, jobParameters);
			model.addAttribute(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));
		}
		catch (NoSuchJobException e) {
			errors.reject("no.such.job", new Object[] { jobName }, "No such job: " + jobName);
		}
		catch (JobExecutionAlreadyRunningException e) {
			errors.reject("job.already.running", "A job with this name and parameters is already running.");
		}
		catch (JobRestartException e) {
			errors.reject("job.could.not.restart", "The job was not able to restart.");
		}
		catch (JobInstanceAlreadyCompleteException e) {
			errors
					.reject("job.already.complete",
							"A job with this name and parameters already completed successfully.");
		}
		catch (JobParametersInvalidException e) {
			errors.reject("job.parameters.invalid", "The job parameters are invalid according to the configuration.");
		}

		if ("execution".equals(origin)) {
			return "jobs/execution";
		}
		else {
			return details(model, jobName, new Date(), errors, 0, 20);
		}

		// Not a redirect because normally it is requested by an Ajax call so
		// there's less of a pressing need for one (the browser history won't
		// contain the request).

	}

	@RequestMapping(value = "/jobs/{jobName}", method = RequestMethod.GET)
	public String details(ModelMap model, @PathVariable String jobName, @ModelAttribute("date") Date date,
			Errors errors, @RequestParam(defaultValue = "0") int startJobInstance,
			@RequestParam(defaultValue = "20") int pageSize) {

		model.addAttribute("launchable", jobService.isLaunchable(jobName));

		try {

			Collection<JobInstance> result = jobService.listJobInstances(jobName, startJobInstance, pageSize);
			Collection<JobInstanceInfo> jobInstances = new ArrayList<JobInstanceInfo>();
			for (JobInstance jobInstance : result) {
				jobInstances.add(new JobInstanceInfo(jobInstance, jobService.getJobExecutionsForJobInstance(jobName,
						jobInstance.getId())));
			}

			model.addAttribute("jobInstances", jobInstances);
			model.addAttribute("jobParameters", getLastJobParameters(jobInstances));
			int total = jobService.countJobInstances(jobName);
			TableUtils.addPagination(model, total, startJobInstance, pageSize, "JobInstance");
			int count = jobService.countJobExecutionsForJob(jobName);
			model.addAttribute("job", new JobInfo(jobName, count));

		}
		catch (NoSuchJobException e) {
			errors.reject("no.such.job", new Object[] { jobName }, "There is no such job (" + jobName + ")");
		}

		return "jobs/job";

	}

	/**
	 * @param lastInstances the latest job instances
	 * @return a String representation for rendering the job parameters from the last instance
	 */
	private String getLastJobParameters(Collection<JobInstanceInfo> lastInstances) {

		JobInstance lastInstance = null;
		if (!lastInstances.isEmpty()) {
			lastInstance = lastInstances.iterator().next().getJobInstance();
		}

		JobParameters oldParameters = new JobParameters();
		if (lastInstance != null) {
			oldParameters = lastInstance.getJobParameters();
		}
		
		return PropertiesConverter.propertiesToString(converter.getProperties(oldParameters));
	}

	@RequestMapping(value = "/jobs", method = RequestMethod.GET)
	public void jobs(ModelMap model, @RequestParam(defaultValue = "0") int startJob,
			@RequestParam(defaultValue = "20") int pageSize) {
		int total = jobService.countJobs();
		TableUtils.addPagination(model, total, startJob, pageSize, "Job");
		Collection<String> names = jobService.listJobs(startJob, pageSize);
		List<JobInfo> jobs = new ArrayList<JobInfo>();
		for (String name : names) {
			int count = 0;
			try {
				count = jobService.countJobExecutionsForJob(name);
			}
			catch (NoSuchJobException e) {
				// shouldn't happen
			}
			boolean launchable = jobService.isLaunchable(name);
			boolean incrementable = jobService.isIncrementable(name);
			jobs.add(new JobInfo(name, count, null, launchable, incrementable));
		}
		model.addAttribute("jobs", jobs);
	}

}
