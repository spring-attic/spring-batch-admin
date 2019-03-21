/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.admin.web.resource;

import org.springframework.batch.admin.domain.DetailedJobInfo;
import org.springframework.batch.admin.domain.DetailedJobInfoResource;
import org.springframework.batch.admin.domain.JobExecutionInfo;
import org.springframework.batch.admin.domain.JobExecutionInfoResource;
import org.springframework.batch.admin.web.BatchJobsController;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;


/**
 * Knows how to build a REST resource out of our domain model {@link org.springframework.batch.admin.domain.DetailedJobInfo}.
 * 
 * @author Ilayaperumal Gopinathan
 */
public class DetailedJobInfoResourceAssembler extends
		ResourceAssemblerSupport<DetailedJobInfo, DetailedJobInfoResource> {

	private JobExecutionInfoResourceAssembler jobExecutionInfoResourceAssembler = new JobExecutionInfoResourceAssembler();

	public DetailedJobInfoResourceAssembler() {
		super(BatchJobsController.class, DetailedJobInfoResource.class);
	}

	@Override
	public DetailedJobInfoResource toResource(DetailedJobInfo entity) {
		return createResourceWithId(entity.getName(), entity);
	}

	@Override
	protected DetailedJobInfoResource instantiateResource(DetailedJobInfo entity) {
		JobExecutionInfoResource jobExecutionInfoResource;
		if (entity.getLastExecutionInfo() != null) {
			JobExecutionInfo jobExecutionInfo = new JobExecutionInfo(
					entity.getLastExecutionInfo().getJobExecution(),
					entity.getLastExecutionInfo().getTimeZone());
			jobExecutionInfoResource = jobExecutionInfoResourceAssembler.instantiateResource(jobExecutionInfo);
		}
		else {
			jobExecutionInfoResource = null;
		}
		return new DetailedJobInfoResource(entity.getName(), entity.getExecutionCount(),
				entity.isLaunchable(), entity.isIncrementable(), jobExecutionInfoResource);
	}
}
