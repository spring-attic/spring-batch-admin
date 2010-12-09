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
import java.util.TimeZone;

import org.springframework.batch.admin.history.StepExecutionHistory;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.service.NoSuchStepExecutionException;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for step executions.
 * 
 * @author Dave Syer
 * 
 */
@Controller
public class StepExecutionController {

	private JobService jobService;

	private TimeZone timeZone = TimeZone.getDefault();

	/**
	 * @param timeZone the timeZone to set
	 */
	@Autowired(required=false)
	@Qualifier("userTimeZone")
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	@Autowired
	public StepExecutionController(JobService jobService) {
		super();
		this.jobService = jobService;
	}

	@RequestMapping(value = "/jobs/executions/{jobExecutionId}/steps", method = RequestMethod.GET)
	public String list(Model model, @PathVariable Long jobExecutionId, @ModelAttribute("date") Date date, Errors errors) {

		Collection<StepExecutionInfo> result = new ArrayList<StepExecutionInfo>();
		try {
			for (StepExecution stepExecution : jobService.getStepExecutions(jobExecutionId)) {
				result.add(new StepExecutionInfo(stepExecution, TimeZone.getTimeZone("GMT")));
			}
			JobExecution jobExecution = jobService.getJobExecution(jobExecutionId);
			model.addAttribute(new JobExecutionInfo(jobExecution, timeZone));
		}
		catch (NoSuchJobExecutionException e) {
			errors.reject("no.such.job.execution", new Object[] { jobExecutionId }, "There is no such job execution ("
					+ jobExecutionId + ")");
		}
		model.addAttribute("stepExecutions", result);

		return "jobs/executions/steps";

	}

	@RequestMapping(value = "/jobs/executions/{jobExecutionId}/steps/{stepExecutionId}", method = RequestMethod.GET)
	public String detail(Model model, @PathVariable Long jobExecutionId, @PathVariable Long stepExecutionId,
			@ModelAttribute("date") Date date, Errors errors) {

		try {
			StepExecution stepExecution = jobService.getStepExecution(jobExecutionId, stepExecutionId);
			model.addAttribute(new StepExecutionInfo(stepExecution, TimeZone.getTimeZone("GMT")));
		}
		catch (NoSuchStepExecutionException e) {
			errors.reject("no.such.step.execution", new Object[] { stepExecutionId }, "There is no such step execution ("
					+ stepExecutionId + ")");
		}
		catch (NoSuchJobExecutionException e) {
			errors.reject("no.such.job.execution", new Object[] { jobExecutionId }, "There is no such job execution ("
					+ jobExecutionId + ")");
		}

		return "jobs/executions/step";

	}

	@RequestMapping(value = "/jobs/executions/{jobExecutionId}/steps/{stepExecutionId}/progress", method = RequestMethod.GET)
	public String history(Model model, @PathVariable Long jobExecutionId, @PathVariable Long stepExecutionId,
			@ModelAttribute("date") Date date, Errors errors) {

		try {
			StepExecution stepExecution = jobService.getStepExecution(jobExecutionId, stepExecutionId);
			model.addAttribute(new StepExecutionInfo(stepExecution, TimeZone.getTimeZone("GMT")));
			String stepName = stepExecution.getStepName();
			if (stepName.contains(":partition")) {
				// assume we want to compare all partitions
				stepName = stepName.replaceAll("(:partition).*", "$1*");
			}
			String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
			StepExecutionHistory stepExecutionHistory = computeHistory(jobName, stepName);
			model.addAttribute(stepExecutionHistory);
			model.addAttribute(new StepExecutionProgress(stepExecution, stepExecutionHistory));
		}
		catch (NoSuchStepExecutionException e) {
			errors.reject("no.such.step.execution", new Object[] { stepExecutionId }, "There is no such step execution ("
					+ stepExecutionId + ")");
		}
		catch (NoSuchJobExecutionException e) {
			errors.reject("no.such.job.execution", new Object[] { jobExecutionId }, "There is no such job execution ("
					+ jobExecutionId + ")");
		}

		return "jobs/executions/step/progress";

	}

	private StepExecutionHistory computeHistory(String jobName, String stepName) {
		int total = jobService.countStepExecutionsForStep(jobName, stepName);
		StepExecutionHistory stepExecutionHistory = new StepExecutionHistory(stepName);
		for (int i = 0; i < total; i += 1000) {
			for (StepExecution stepExecution : jobService.listStepExecutionsForStep(jobName, stepName, i, 1000)) {
				stepExecutionHistory.append(stepExecution);
			}
		}
		return stepExecutionHistory;
	}

}
