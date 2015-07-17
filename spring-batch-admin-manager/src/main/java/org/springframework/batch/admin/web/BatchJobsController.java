/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import org.springframework.batch.admin.domain.DetailedJobInfo;
import org.springframework.batch.admin.domain.DetailedJobInfoResource;
import org.springframework.batch.admin.domain.JobExecutionInfo;
import org.springframework.batch.admin.domain.NoSuchBatchJobException;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Controller for batch jobs and job instances, job executions on a given batch job.
 * 
 * @author Dave Syer
 * @author Ilayaperumal Gopinathan
 * @author Andrew Eisenberg
 * @since 2.0
 * 
 */
@Controller
@RequestMapping("/batch/configurations")
@ExposesResourceFor(DetailedJobInfoResource.class)
public class BatchJobsController extends AbstractBatchJobsController {

	/**
	 * Get the paged resources of {@link org.springframework.batch.admin.domain.DetailedJobInfoResource}
	 * 
	 * @param pageable the paging metadata
	 * @param assembler the paged resource assembler of type {@link org.springframework.batch.admin.domain.DetailedJobInfo}
	 *
	 * @return a page of {@link DetailedJobInfoResource}
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public PagedResources<DetailedJobInfoResource> jobs(Pageable pageable,
			PagedResourcesAssembler<DetailedJobInfo> assembler) {
		int total = jobService.countJobs();
		Collection<String> names = jobService.listJobs(pageable.getOffset(), pageable.getPageSize());
		List<DetailedJobInfo> detailedJobs = new ArrayList<DetailedJobInfo>();
		for (String name : names) {
			detailedJobs.add(getJobInfo(name));
		}
		return assembler.toResource(
				new PageImpl<DetailedJobInfo>(detailedJobs, pageable, total),
				jobInfoResourceAssembler);
	}

	/**
	 * @param jobName name of the job
	 * @return ExpandedJobInfo for the given job name
	 */
	@RequestMapping(value = "/{jobName}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public DetailedJobInfoResource jobinfo(@PathVariable String jobName) {
		return getJobInfoResource(jobName);
	}

	/**
	 * @param jobName
	 * @return the detailed job info resource for the given job name.
	 */
	private DetailedJobInfoResource getJobInfoResource(String jobName) {
		return jobInfoResourceAssembler.toResource(getJobInfo(jobName));
	}

	/**
	 * Get detailed job info
	 *
	 * @param jobName name of the job
	 * @return a job info for this job
	 */
	private DetailedJobInfo getJobInfo(String jobName) {
		boolean launchable = jobService.isLaunchable(jobName);
		try {
			int count = jobService.countJobExecutionsForJob(jobName);
			return new DetailedJobInfo(jobName, count, launchable,
					jobService.isIncrementable(jobName),
					getLastExecution(jobName));
		}
		catch (NoSuchJobException e) {
			throw new NoSuchBatchJobException(jobName);
		}
	}

	/**
	 * @param jobName name of the batch job
	 * @return Last job execution info
	 * @throws org.springframework.batch.core.launch.NoSuchJobException
	 */
	private JobExecutionInfo getLastExecution(String jobName) throws NoSuchJobException {
		Collection<JobExecution> executions = jobService.listJobExecutionsForJob(jobName, 0, 1);
		if (executions.size() > 0) {
			return new JobExecutionInfo(executions.iterator().next(), timeZone);
		}
		else {
			return null;
		}
	}

}
