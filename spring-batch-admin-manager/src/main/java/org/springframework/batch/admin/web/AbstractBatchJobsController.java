/*
 * Copyright 2013-2015 the original author or authors.
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

package org.springframework.batch.admin.web;

import java.util.TimeZone;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.web.resource.DetailedJobInfoResourceAssembler;
import org.springframework.batch.admin.web.resource.FileInfoResourceAssembler;
import org.springframework.batch.admin.web.resource.JobExecutionInfoResourceAssembler;
import org.springframework.batch.admin.web.resource.JobInstanceInfoResourceAssembler;
import org.springframework.batch.admin.web.resource.StepExecutionInfoResourceAssembler;
import org.springframework.batch.admin.web.resource.StepExecutionProgressInfoResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


/**
 * Abstract controller that all the XD batch admin controllers extend.
 *
 * @author Ilayaperumal Gopinathan
 * @since 2.0
 */
public abstract class AbstractBatchJobsController {

	@Autowired
	protected JobService jobService;

	protected TimeZone timeZone = TimeZone.getTimeZone("UTC");

	protected final DetailedJobInfoResourceAssembler jobInfoResourceAssembler = new DetailedJobInfoResourceAssembler();

	protected final JobExecutionInfoResourceAssembler jobExecutionInfoResourceAssembler = new JobExecutionInfoResourceAssembler();

	protected final JobInstanceInfoResourceAssembler jobInstanceInfoResourceAssembler = new JobInstanceInfoResourceAssembler();

	protected final StepExecutionInfoResourceAssembler stepExecutionInfoResourceAssembler = new StepExecutionInfoResourceAssembler();

	protected final StepExecutionProgressInfoResourceAssembler progressInfoResourceAssembler = new StepExecutionProgressInfoResourceAssembler();

	protected final FileInfoResourceAssembler fileInfoResourceAssembler = new FileInfoResourceAssembler();

	/**
	 * @param timeZone the timeZone to set
	 */
	@Autowired(required = false)
	@Qualifier("userTimeZone")
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
}
