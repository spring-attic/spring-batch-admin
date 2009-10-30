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

import java.io.Serializable;

public class StepExecutionRequest implements Serializable {

	private Long jobExecutionId;

	private String stepName;

	private Long stepExecutionId;

	private float threshold = 1;

	public StepExecutionRequest() {
		super();
	}

	public StepExecutionRequest(Long jobExecutionId, Long stepExecutionId, String stepName) {
		super();
		this.jobExecutionId = jobExecutionId;
		this.stepName = stepName;
		this.stepExecutionId = stepExecutionId;
	}

	public Long getJobExecutionId() {
		return jobExecutionId;
	}

	public void setJobExecutionId(Long jobExecutionId) {
		this.jobExecutionId = jobExecutionId;
	}

	public String getStepName() {
		return stepName;
	}

	public Long getStepExecutionId() {
		return stepExecutionId;
	}

	public float getThreshold() {
		return threshold;
	}

	public void setThreshold(float f) {
		this.threshold = f;
	}

	@Override
	public String toString() {
		return String.format("%s: stepName=%s, jobExecutionId=%d, stepExecutionId=%d, threshold=%f", getClass()
				.getSimpleName(), stepName, jobExecutionId, stepExecutionId, threshold);
	}

}
