/*
 * Copyright 2013-2015 the original author or authors.
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

package org.springframework.batch.admin.domain;

import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameters;


/**
 * Represents Expanded Batch job info that has more details on batch jobs.
 * 
 * @author Ilayaperumal Gopinathan
 * @author Michael Minella
 * @since 2.0
 */
@XmlRootElement
public class DetailedJobInfoResource extends JobInfoResource {

	private JobParameters jobParameters;

	private String startTime;

	private String endTime;

	private int stepExecutionCount;

	private ExitStatus exitStatus;

	/**
	 * Default constructor for serialization frameworks.
	 */
	@SuppressWarnings("unused")
	private DetailedJobInfoResource() {
		super();
	}

	public DetailedJobInfoResource(String name, int executionCount, boolean launchable, boolean incrementable,
			JobExecutionInfoResource lastExecution) {
		super(name, executionCount, lastExecution != null ? lastExecution.getJobId() : null , launchable, incrementable);
		if (lastExecution != null) {
			this.jobParameters = lastExecution.getJobParameters();
			this.startTime = lastExecution.getStartTime();
			this.endTime = lastExecution.getEndTime();
			this.stepExecutionCount = lastExecution.getStepExecutionCount();
			this.exitStatus = lastExecution.getExitStatus();
		}
	}

	public JobParameters getJobParameters() {
		return jobParameters;
	}

	public String getStartTime() {
		return startTime;
	}

	public int getStepExecutionCount() {
		return stepExecutionCount;
	}

	public ExitStatus getExitStatus() {
		return exitStatus;
	}

	public String getEndTime() {
		return endTime;
	}
}
