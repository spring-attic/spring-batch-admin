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
package org.springframework.batch.admin.partition.remote;

import java.util.List;
import java.util.Set;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;

public class JobExplorerSupport implements JobExplorer {

	public List<JobExecution> getJobExecutions(JobInstance jobInstance) {

		return null;
	}

	public Set<JobExecution> findRunningJobExecutions(String jobName) {

		return null;
	}

	public JobExecution getJobExecution(Long executionId) {

		return null;
	}

	public JobInstance getJobInstance(Long instanceId) {

		return null;
	}

	public List<JobInstance> getJobInstances(String jobName, int start, int count) {

		return null;
	}

	public StepExecution getStepExecution(Long jobExecutionId, Long stepExecutionId) {

		return null;
	}
	
	public List<String> getJobNames() {

		return null;
	}

}
