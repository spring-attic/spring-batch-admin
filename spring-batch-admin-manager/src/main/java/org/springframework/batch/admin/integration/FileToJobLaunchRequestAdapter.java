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

import java.io.File;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.util.Assert;

/**
 * Adapt a {@link File} to a {@link JobLaunchRequest} with a job parameter
 * <code>input.file</code> equal to the path of the file.
 * 
 * @author Dave Syer
 * 
 */
@MessageEndpoint
public class FileToJobLaunchRequestAdapter implements InitializingBean {

	private Job job;

	public void setJob(Job job) {
		this.job = job;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(job, "A Job must be provided");
	}

	@ServiceActivator
	public JobLaunchRequest adapt(File file) throws NoSuchJobException {

		String fileName = file.getAbsolutePath();

		if (!fileName.startsWith("/")) {
			fileName = "/" + fileName;
		}

		fileName = "file://" + fileName;

		JobParameters jobParameters = new JobParametersBuilder().addString(
				"input.file", fileName).toJobParameters();

		if (job.getJobParametersIncrementer() != null) {
			jobParameters = job.getJobParametersIncrementer().getNext(jobParameters);
		}

		return new JobLaunchRequest(job, jobParameters);

	}

}
