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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.util.Assert;


/**
 * Represents the step execution info resource.
 * 
 * @author Gunnar Hillert
 * @author Michael Minella
 * @since 2.0
 */
@XmlRootElement
public class StepExecutionInfoResource extends ResourceSupport {

	private DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime();

	private Long executionId;

	private Long jobExecutionId;

	private String stepType;

	private String stepName;

	private BatchStatus status;

	private int readCount;

	private int writeCount;

	private int commitCount;

	private int rollbackCount;

	private int readSkipCount;

	private int processSkipCount;

	private int writeSkipCount;

	private String startTime;

	private String endTime;

	private String lastUpdated;

	private Map<String, Object> executionContext;

	private ExitStatus exitStatus;

	private boolean terminateOnly;

	private int filterCount;

	private List<Throwable> failureExceptions;

	private Integer version;

	private final TimeZone timeZone;

	/**
	 * @param stepExecution Must not be null
	 * @param timeZone timeZone dates are represented in.
	 */
	public StepExecutionInfoResource(StepExecution stepExecution, TimeZone timeZone) {
		Assert.notNull(stepExecution, "stepExecution must not be null.");

		if(timeZone != null){
			this.timeZone = timeZone;
		}
		else {
			this.timeZone = TimeZone.getTimeZone("UTC");
		}

		this.dateFormat = this.dateFormat.withZone(DateTimeZone.forTimeZone(this.timeZone));

		this.jobExecutionId = stepExecution.getJobExecutionId();
		if(stepExecution.getExecutionContext().containsKey(Step.STEP_TYPE_KEY)) {
			this.stepType = (String) stepExecution.getExecutionContext().get(Step.STEP_TYPE_KEY);
		}

		this.executionId = stepExecution.getId();
		this.stepName = stepExecution.getStepName();
		this.status = stepExecution.getStatus();
		this.readCount = stepExecution.getReadCount();
		this.writeCount = stepExecution.getWriteCount();
		this.commitCount = stepExecution.getCommitCount();
		this.rollbackCount = stepExecution.getRollbackCount();
		this.readSkipCount = stepExecution.getReadSkipCount();
		this.processSkipCount = stepExecution.getProcessSkipCount();
		this.writeSkipCount = stepExecution.getWriteSkipCount();
		this.startTime = dateFormat.print(stepExecution.getStartTime().getTime());

		if(stepExecution.getEndTime() != null) {
			this.endTime = dateFormat.print(stepExecution.getEndTime().getTime());
		}
		else {
			this.endTime = "N/A";
		}

		this.lastUpdated = dateFormat.print(stepExecution.getLastUpdated().getTime());
		HashMap<String, Object> executionContextValues = new HashMap<String, Object>();

		for (Map.Entry<String, Object> stringObjectEntry : stepExecution.getExecutionContext().entrySet()) {
			executionContextValues.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
		}

		this.executionContext = executionContextValues;
		this.exitStatus = stepExecution.getExitStatus();
		this.terminateOnly = stepExecution.isTerminateOnly();
		this.filterCount = stepExecution.getFilterCount();
		this.failureExceptions = stepExecution.getFailureExceptions();
		this.version = stepExecution.getVersion();
	}

	public StepExecutionInfoResource() {
		this.timeZone = TimeZone.getTimeZone("UTC");
	}

	/**
	 * @return The jobExecutionId, which will never be null
	 */
	public Long getJobExecutionId() {
		return this.jobExecutionId;
	}

	public String getStepType() { return this.stepType; }

	public String getStepName() {
		return stepName;
	}

	public BatchStatus getStatus() {
		return status;
	}

	public int getReadCount() {
		return readCount;
	}

	public int getWriteCount() {
		return writeCount;
	}

	public int getCommitCount() {
		return commitCount;
	}

	public int getRollbackCount() {
		return rollbackCount;
	}

	public int getReadSkipCount() {
		return readSkipCount;
	}

	public int getProcessSkipCount() {
		return processSkipCount;
	}

	public int getWriteSkipCount() {
		return writeSkipCount;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public Map<String, Object> getExecutionContext() {
		return executionContext;
	}

	public ExitStatus getExitStatus() {
		return exitStatus;
	}

	public boolean isTerminateOnly() {
		return terminateOnly;
	}

	public int getFilterCount() {
		return filterCount;
	}

	public List<Throwable> getFailureExceptions() {
		return failureExceptions;
	}

	public Integer getVersion() {
		return version;
	}

	public Long getExecutionId() {
		return executionId;
	}
}
