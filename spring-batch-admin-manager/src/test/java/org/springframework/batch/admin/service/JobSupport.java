/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.admin.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersIncrementer;

/**
 * @author Dave Syer
 *
 */
public class JobSupport implements Job {

	private String name;

	public JobSupport(String name) {
		super();
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.Job#execute(org.springframework.batch.core.JobExecution)
	 */
	public void execute(JobExecution execution) {
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.Job#getJobParametersIncrementer()
	 */
	public JobParametersIncrementer getJobParametersIncrementer() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.Job#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.Job#isRestartable()
	 */
	public boolean isRestartable() {
		return true;
	}

}
