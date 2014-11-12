/*
 * Copyright 2009-2014 the original author or authors.
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
import java.util.TimeZone;

import org.springframework.batch.admin.domain.JobExecutionInfo;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;

public class JobInstanceInfo {

	private final JobInstance jobInstance;

	private final Long id;

	private final Collection<JobExecutionInfo> jobExecutionInfos;

	public JobInstanceInfo(JobInstance jobInstance, Collection<JobExecution> jobExecutions, TimeZone timeZone) {
		this.jobInstance = jobInstance;
		this.jobExecutionInfos = new ArrayList<JobExecutionInfo>();

		if (jobExecutions != null) {
			for (JobExecution jobExecution : jobExecutions) {
				jobExecutionInfos.add(new JobExecutionInfo(jobExecution, timeZone));
			}
		}

		this.id = jobInstance.getId();
	}

	public JobInstanceInfo(JobInstance jobInstance, Collection<JobExecution> jobExecutions) {
		this(jobInstance, jobExecutions, TimeZone.getDefault());
	}

	public JobInstance getJobInstance() {
		return jobInstance;
	}

	public Long getId() {
		return id;
	}

	public int getJobExecutionCount() {
		return jobExecutionInfos.size();
	}

	public Collection<JobExecution> getJobExecutions() {
		Collection<JobExecution> jobExecutions = new ArrayList<JobExecution>();

		for (JobExecutionInfo jobExecutionInfo : jobExecutionInfos) {
			jobExecutions.add(jobExecutionInfo.getJobExecution());
		}

		return jobExecutions;
	}

	public JobExecution getLastJobExecution() {
		return jobExecutionInfos.isEmpty() ? null : jobExecutionInfos.iterator().next().getJobExecution();
	}

	public JobExecutionInfo getLastJobExecutionInfo() {
		return jobExecutionInfos.isEmpty() ? null : jobExecutionInfos.iterator().next();
	}
}
