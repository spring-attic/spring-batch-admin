/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.admin.history;

import java.util.Date;

import org.springframework.batch.core.StepExecution;


public class StepExecutionHistory {

	private final String stepName;
	private int count = 0;
	private CumulativeHistory commitCount = new CumulativeHistory();
	private CumulativeHistory rollbackCount = new CumulativeHistory();
	private CumulativeHistory readCount = new CumulativeHistory();
	private CumulativeHistory writeCount = new CumulativeHistory();
	private CumulativeHistory filterCount = new CumulativeHistory();
	private CumulativeHistory readSkipCount = new CumulativeHistory();
	private CumulativeHistory writeSkipCount = new CumulativeHistory();
	private CumulativeHistory processSkipCount = new CumulativeHistory();
	private CumulativeHistory duration = new CumulativeHistory();
	private CumulativeHistory durationPerRead = new CumulativeHistory();

	public StepExecutionHistory(String stepName) {
		this.stepName = stepName;
	}

	public void append(StepExecution stepExecution) {
		if (stepExecution.getEndTime()==null) {
			// ignore unfinished executions
			return;
		}
		Date startTime = stepExecution.getStartTime();
		Date endTime = stepExecution.getEndTime();
		long time = endTime.getTime()-startTime.getTime();
		duration.append(time);
		if (stepExecution.getReadCount()>0) {
			durationPerRead.append(time/stepExecution.getReadCount());
		}
		count++;
		commitCount.append(stepExecution.getCommitCount());
		rollbackCount.append(stepExecution.getRollbackCount());
		readCount.append(stepExecution.getReadCount());
		writeCount.append(stepExecution.getWriteCount());
		filterCount.append(stepExecution.getFilterCount());
		readSkipCount.append(stepExecution.getReadSkipCount());
		writeSkipCount.append(stepExecution.getWriteSkipCount());
		processSkipCount.append(stepExecution.getProcessSkipCount());
	}

	public String getStepName() {
		return stepName;
	}
	
	public int getCount() {
		return count;
	}

	public CumulativeHistory getCommitCount() {
		return commitCount;
	}

	public CumulativeHistory getRollbackCount() {
		return rollbackCount;
	}

	public CumulativeHistory getReadCount() {
		return readCount;
	}

	public CumulativeHistory getWriteCount() {
		return writeCount;
	}

	public CumulativeHistory getFilterCount() {
		return filterCount;
	}

	public CumulativeHistory getReadSkipCount() {
		return readSkipCount;
	}

	public CumulativeHistory getWriteSkipCount() {
		return writeSkipCount;
	}

	public CumulativeHistory getProcessSkipCount() {
		return processSkipCount;
	}

	public CumulativeHistory getDuration() {
		return duration;
	}
	
	public CumulativeHistory getDurationPerRead() {
		return durationPerRead;
	}
	
}
