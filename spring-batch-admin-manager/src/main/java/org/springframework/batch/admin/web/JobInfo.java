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
/**
 * 
 */
package org.springframework.batch.admin.web;

public class JobInfo {
	private final String name;

	private final int executionCount;
	
	private Boolean launchable;

	private Boolean incrementable;

	private final Long jobInstanceId;

	public JobInfo(String name, int executionCount) {
		this(name, executionCount, null);
	}

	public JobInfo(String name, int executionCount, Boolean launchable) {
		this(name, executionCount, null, launchable, null);
	}

	public JobInfo(String name, int executionCount, Long jobInstanceId, Boolean launchable, Boolean incrementable) {
		super();
		this.name = name;
		this.executionCount = executionCount;
		this.jobInstanceId = jobInstanceId;
		this.launchable = launchable;
		this.incrementable = incrementable;
	}

	public String getName() {
		return name;
	}

	public int getExecutionCount() {
		return executionCount;
	}
	
	public Long getJobInstanceId() {
		return jobInstanceId;
	}
	
	public Boolean getLaunchable() {
		return launchable;
	}
	
	public Boolean getIncrementable() {
		return incrementable;
	}
	
	@Override
	public String toString() {
		return name;
	}

}