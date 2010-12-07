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
import java.util.Properties;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;

public class JobInstanceInfo {

	private final JobInstance jobInstance;

	private final Long id;

	private final Collection<JobExecution> jobExecutions;

	private final Properties jobParameters;

	private final String jobParametersString;

	public JobInstanceInfo(JobInstance jobInstance, Collection<JobExecution> jobExecutions) {
		this.jobInstance = jobInstance;
		this.jobExecutions = jobExecutions != null ? jobExecutions : new ArrayList<JobExecution>();
		this.id = jobInstance.getId();
		this.jobParameters = new DefaultJobParametersConverter().getProperties(jobInstance.getJobParameters());
		this.jobParametersString = new JobParametersExtractor().fromJobParameters(jobInstance.getJobParameters());
	}

	public JobInstance getJobInstance() {
		return jobInstance;
	}

	public Long getId() {
		return id;
	}

	public int getJobExecutionCount() {
		return jobExecutions.size();
	}

	public Collection<JobExecution> getJobExecutions() {
		return jobExecutions;
	}

	public JobExecution getLastJobExecution() {
		return jobExecutions.isEmpty() ? null : jobExecutions.iterator().next();
	}
	
	public Properties getJobParameters() {
		return jobParameters;
	}

	public String getJobParametersString() {
		return jobParametersString;
	}

}
