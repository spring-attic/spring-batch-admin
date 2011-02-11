/*
 * Copyright 2006-2011 the original author or authors.
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

package org.springframework.batch.admin.sample;

import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.item.ExecutionContext;

/**
 * Workaround for BATCH-1692, BATCH-1693.
 * 
 * @author Dave Syer
 * 
 */
public class RemoteStep implements Step {

	private Step step;

	private JobExplorer jobExplorer;

	private String name;

	private int startLimit = Integer.MAX_VALUE;

	private boolean allowStartIfComplete = false;

	/**
	 * Set the name property. Always overrides the default value if this object
	 * is a Spring bean.
	 * 
	 * @see #setBeanName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public int getStartLimit() {
		return this.startLimit;
	}

	/**
	 * Public setter for the startLimit.
	 * 
	 * @param startLimit the startLimit to set
	 */
	public void setStartLimit(int startLimit) {
		this.startLimit = startLimit;
	}

	public boolean isAllowStartIfComplete() {
		return this.allowStartIfComplete;
	}

	/**
	 * Public setter for flag that determines whether the step should start
	 * again if it is already complete. Defaults to false.
	 * 
	 * @param allowStartIfComplete the value of the flag to set
	 */
	public void setAllowStartIfComplete(boolean allowStartIfComplete) {
		this.allowStartIfComplete = allowStartIfComplete;
	}

	/**
	 * @param delegate the delegate to set
	 */
	public void setStep(Step delegate) {
		this.step = delegate;
	}

	/**
	 * @param jobExplorer the jobExplorer to set
	 */
	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}

	public void execute(StepExecution stepExecution) throws JobInterruptedException {
		step.execute(stepExecution);
		copy(jobExplorer.getStepExecution(stepExecution.getJobExecutionId(), stepExecution.getId()), stepExecution);
	}

	private void copy(final StepExecution source, final StepExecution target) {
		target.setStatus(source.getStatus());
		target.setExitStatus(source.getExitStatus());
		target.setReadCount(source.getReadCount());
		target.setReadCount(source.getReadCount());
		target.setWriteCount(source.getWriteCount());
		target.setFilterCount(source.getFilterCount());
		target.setCommitCount(source.getCommitCount());
		target.setRollbackCount(source.getRollbackCount());
		target.setReadSkipCount(source.getReadSkipCount());
		target.setWriteSkipCount(source.getWriteSkipCount());
		target.setProcessSkipCount(source.getProcessSkipCount());
		target.setExecutionContext(new ExecutionContext(source.getExecutionContext()));
	}

}
