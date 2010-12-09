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
package org.springframework.batch.admin.history;

import java.util.Date;

import org.springframework.batch.core.JobExecution;

public class JobExecutionHistory {

	private final String jobName;

	private CumulativeHistory duration = new CumulativeHistory();

	public JobExecutionHistory(String jobName) {
		this.jobName = jobName;
	}

	public String getJobName() {
		return jobName;
	}

	public CumulativeHistory getDuration() {
		return duration;
	}

	public void append(JobExecution jobExecution) {
		if (jobExecution.getEndTime() == null) {
			// ignore unfinished executions
			return;
		}
		Date startTime = jobExecution.getStartTime();
		Date endTime = jobExecution.getEndTime();
		long time = endTime.getTime() - startTime.getTime();
		duration.append(time);
	}

}