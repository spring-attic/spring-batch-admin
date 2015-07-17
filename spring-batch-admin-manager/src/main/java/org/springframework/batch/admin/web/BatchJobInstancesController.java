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
import org.springframework.batch.admin.domain.JobInstanceInfoResource;
import org.springframework.batch.admin.domain.NoSuchBatchJobException;
import org.springframework.batch.admin.domain.NoSuchBatchJobInstanceException;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller for batch job instances.
 * 
 * @author Ilayaperumal Gopinathan
 * @since 2.0
 */
@Controller
@RequestMapping("/batch/instances")
@ExposesResourceFor(JobInstanceInfoResource.class)
public class BatchJobInstancesController extends AbstractBatchJobsController {

	/**
	 * Return job instance info by the given instance id.
	 * 
	 * @param instanceId job instance id
	 * @return job instance info
	 */
	@RequestMapping(value = "/{instanceId}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public JobInstanceInfoResource getJobInstance(@PathVariable long instanceId) {
		try {
			JobInstance jobInstance = jobService.getJobInstance(instanceId);
			String jobName = jobInstance.getJobName();

			try {
				List<JobExecution> jobExecutions = (List<JobExecution>) jobService.getJobExecutionsForJobInstance(
						jobInstance.getJobName(), jobInstance.getId());
				List<JobExecutionInfo> jobExecutionInfos = new ArrayList<JobExecutionInfo>();
				for (JobExecution jobExecution : jobExecutions) {
					jobExecutionInfos.add(new JobExecutionInfo(jobExecution, timeZone));
				}

				return jobInstanceInfoResourceAssembler.toResource(new JobInstanceInfo(jobInstance, jobExecutions));
			}
			catch (NoSuchJobException e) {
				throw new NoSuchBatchJobException(jobName);
			}
		}
		catch (NoSuchJobInstanceException e) {
			throw new NoSuchBatchJobInstanceException(instanceId);
		}
	}

	/**
	 * Return a paged collection of job instances for a given job.
	 *
	 * @param pageable page request
	 * @param assembler used to construct resources
	 * @param jobName name of the batch job
	 * @return collection of JobInstances by job name
	 */
	@RequestMapping(value = "", method = RequestMethod.GET, params = "jobname")
	@ResponseStatus(HttpStatus.OK)
	public PagedResources<JobInstanceInfoResource> instancesForJob(Pageable pageable, PagedResourcesAssembler<JobInstanceInfo> assembler, @RequestParam("jobname") String jobName) {

		try {
			List<JobInstanceInfo> result = new ArrayList<JobInstanceInfo>();
			long total = jobService.countJobInstances(jobName);

			Collection<JobInstance> jobInstances = jobService.listJobInstances(jobName, pageable.getOffset(), pageable.getPageSize());
			for (JobInstance jobInstance : jobInstances) {
				List<JobExecution> jobExecutions = (List<JobExecution>) jobService.getJobExecutionsForJobInstance(
						jobName, jobInstance.getId());
				result.add(new JobInstanceInfo(jobInstance, jobExecutions));
			}

			return assembler.toResource(new PageImpl<JobInstanceInfo>(result, pageable, total), jobInstanceInfoResourceAssembler);
		}
		catch (NoSuchJobException e) {
			throw new NoSuchBatchJobException(jobName);
		}
	}
}
