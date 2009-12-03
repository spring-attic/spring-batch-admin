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

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Adapt a String to a {@link JobLaunchRequest} consisting of a reference to a
 * {@link Job} and some {@link JobParameters}. The input String is in the
 * format: <code>jobname([(key=value(,key=value)*])</code>, where
 * <ul>
 * <li>jobname = the name of a {@link Job} to launch</li>
 * <li>key = the name of a {@link JobParameter}</li>
 * <li>value = the value of the parameter</li>
 * <ul>
 * Job parameter values are optional, and if provided are separated by commas
 * and enclosed in square brackets. If no parameters are provided the empty set
 * will be used.
 * 
 * @author Dave Syer
 * 
 */
@MessageEndpoint
public class StringToJobLaunchRequestAdapter implements InitializingBean {

	private JobLocator jobLocator;

	private JobParametersConverter converter = new DefaultJobParametersConverter();

	private static String PATTERN = "([\\w-_]*)(\\[(.*)\\]|.*)";

	public void setJobLocator(JobLocator jobLocator) {
		this.jobLocator = jobLocator;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.state(jobLocator != null, "A JobLocator must be provided");
	}

	@ServiceActivator
	public JobLaunchRequest adapt(String request) throws NoSuchJobException {
		request = request.trim();
		Assert.isTrue(request.matches(PATTERN), "Input in wrong format ("
				+ request + "): use jobname([(key=value(,key=value)*])");
		String jobName = request.replaceAll(PATTERN, "$1");
		Job job = jobLocator.getJob(jobName);
		String paramsText = request.replaceAll(PATTERN, "$3");
		JobParameters jobParameters = converter.getJobParameters(StringUtils
				.splitArrayElementsIntoProperties(paramsText.split(","), "="));
		return new JobLaunchRequest(job, jobParameters);
	}
}
