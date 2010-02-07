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
package org.springframework.batch.admin.integration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;
import org.springframework.util.Assert;

/**
 * Adapt a {@link JobLaunchRequest} so that it picks up the job parameters from
 * the last execution if possible.
 * 
 * @author Dave Syer
 * 
 */
@MessageEndpoint
public class LastJobParametersJobLaunchRequestEnhancer implements InitializingBean {

	private JobService jobService;

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(jobService, "A JobService must be provided");
	}

	@Transformer
	public JobLaunchRequest adapt(JobLaunchRequest request) throws NoSuchJobException {

		Map<String, JobParameter> jobParameters = request.getJobParameters().getParameters();
		Map<String, JobParameter> oldParameters = jobService.getLastJobParameters(request.getJob().getName())
				.getParameters();

		Map<String, JobParameter> map = new HashMap<String, JobParameter>();

		for (String key : oldParameters.keySet()) {
			map.put(key, oldParameters.get(key));
		}
		for (String key : jobParameters.keySet()) {
			map.put(key, jobParameters.get(key));
		}

		return new JobLaunchRequest(request.getJob(), new JobParameters(map));

	}

}
