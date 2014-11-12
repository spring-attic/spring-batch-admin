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

import org.springframework.batch.admin.domain.StepExecutionHistory;
import org.springframework.batch.admin.domain.StepExecutionInfo;
import org.springframework.batch.admin.domain.StepExecutionInfoResource;
import org.springframework.batch.admin.domain.StepExecutionProgressInfo;
import org.springframework.batch.admin.domain.StepExecutionProgressInfoResource;
import org.springframework.batch.admin.service.NoSuchStepExecutionException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for returning Batch {@link org.springframework.batch.core.StepExecution}s.
 *
 * @author Gunnar Hillert
 * @author Dave Syer
 * @author Ilayaperumal Gopinathan
 * @since 2.0
 */
@RestController
@RequestMapping("/batch/executions/{jobExecutionId}/steps")
@ExposesResourceFor(StepExecutionInfoResource.class)
public class BatchStepExecutionsController extends AbstractBatchJobsController {

	/**
	 * List all step executions.
	 *
	 * @param jobExecutionId Id of the {@link org.springframework.batch.core.JobExecution}, must not be null
	 * @return Collection of {@link StepExecutionInfoResource} for the given jobExecutionId
	 * @throws org.springframework.batch.core.launch.NoSuchJobExecutionException Thrown if the respective {@link org.springframework.batch.core.JobExecution} does not exist
	 */
	@RequestMapping(value = { "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	public Collection<StepExecutionInfoResource> list(@PathVariable("jobExecutionId") long jobExecutionId) throws NoSuchJobExecutionException {

		final Collection<StepExecution> stepExecutions;

		try {
			stepExecutions = jobService.getStepExecutions(jobExecutionId);
		}
		catch (NoSuchJobExecutionException e) {
			throw new NoSuchJobExecutionException(String.format("Could not find jobExecution with id %s", String.valueOf(jobExecutionId)));
		}

		final Collection<StepExecutionInfoResource> result = new ArrayList<StepExecutionInfoResource>();

		for (StepExecution stepExecution : stepExecutions) {
			// Band-Aid to prevent Hateos crash - see XD-1206
			if (stepExecution.getId() != null) {
				result.add(stepExecutionInfoResourceAssembler.toResource(new StepExecutionInfo(stepExecution, timeZone)));
			}
		}

		return result;
	}

	/**
	 * Inspect the StepExecution with the provided Step Execution Id
	 *
	 * @param jobExecutionId Id of the {@link org.springframework.batch.core.JobExecution}, must not be null
	 * @param stepExecutionId Id of the {@link org.springframework.batch.core.StepExecution}, must not be null
	 * @return {@link StepExecutionInfoResource} that has the details on the given {@link org.springframework.batch.core.StepExecution}.
	 * @throws NoSuchJobExecutionException Thrown if the respective {@link org.springframework.batch.core.JobExecution} does not exist
	 * @throws org.springframework.batch.admin.service.NoSuchStepExecutionException Thrown if the respective {@link org.springframework.batch.core.StepExecution} does not exist
	 */
	@RequestMapping(value = "/{stepExecutionId}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public StepExecutionInfoResource details(@PathVariable long jobExecutionId,
			@PathVariable long stepExecutionId) throws NoSuchStepExecutionException, NoSuchJobExecutionException {
		try {
			StepExecution stepExecution = jobService.getStepExecution(jobExecutionId, stepExecutionId);
			return this.stepExecutionInfoResourceAssembler.toResource(new StepExecutionInfo(stepExecution,
					this.timeZone));
		}
		catch (org.springframework.batch.admin.service.NoSuchStepExecutionException e) {
			throw new NoSuchStepExecutionException(String.format("Could not find step execution with id %s", String.valueOf(stepExecutionId)));
		}
		catch (org.springframework.batch.core.launch.NoSuchJobExecutionException e) {
			throw new NoSuchJobExecutionException(String.format("Could not find jobExecution with id %s", String.valueOf(jobExecutionId)));
		}
	}

	/**
	 * Get the step execution progress for the given jobExecutions step.
	 *
	 * @param jobExecutionId Id of the {@link org.springframework.batch.core.JobExecution}, must not be null
	 * @param stepExecutionId Id of the {@link org.springframework.batch.core.StepExecution}, must not be null
	 * @return {@link StepExecutionInfoResource} that has the progress info on the given {@link org.springframework.batch.core.StepExecution}.
	 * @throws NoSuchJobExecutionException Thrown if the respective {@link org.springframework.batch.core.JobExecution} does not exist
	 * @throws NoSuchStepExecutionException Thrown if the respective {@link org.springframework.batch.core.StepExecution} does not exist
	 */
	@RequestMapping(value = "/{stepExecutionId}/progress", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public StepExecutionProgressInfoResource progress(@PathVariable long jobExecutionId,
			@PathVariable long stepExecutionId) throws NoSuchStepExecutionException, NoSuchJobExecutionException {
		try {
			StepExecution stepExecution = jobService.getStepExecution(jobExecutionId, stepExecutionId);
			String stepName = stepExecution.getStepName();
			if (stepName.contains(":partition")) {
				// assume we want to compare all partitions
				stepName = stepName.replaceAll("(:partition).*", "$1*");
			}
			String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
			StepExecutionHistory stepExecutionHistory = computeHistory(jobName, stepName);
			return progressInfoResourceAssembler.toResource(new StepExecutionProgressInfo(stepExecution,
					stepExecutionHistory));
		}
		catch (org.springframework.batch.admin.service.NoSuchStepExecutionException e) {
			throw new NoSuchStepExecutionException(String.format("Could not find step execution with id %s", String.valueOf(stepExecutionId)));
		}
		catch (org.springframework.batch.core.launch.NoSuchJobExecutionException e) {
			throw new NoSuchJobExecutionException(String.format("Could not find jobExecution with id %s", String.valueOf(jobExecutionId)));
		}
	}

	/**
	 * Compute step execution history for the given jobs step.
	 * 
	 * @param jobName the name of the job
	 * @param stepName the name of the step
	 * @return the step execution history for the given step
	 */
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
