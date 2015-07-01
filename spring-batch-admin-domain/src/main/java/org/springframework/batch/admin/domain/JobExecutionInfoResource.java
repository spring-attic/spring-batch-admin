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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceSupport;


/**
 * Represents job execution info resource.
 *
 * @author Dave Syer
 * @author Ilayaperumal Gopinathan
 * @author Michael Minella
 * @since 2.0
 */
@XmlRootElement
public class JobExecutionInfoResource extends ResourceSupport {

	private DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime();

	private Long executionId;

	private int stepExecutionCount;

	private Long jobId;

	private Integer version;

	@JsonProperty("name")
	private String jobName;

	private String startTime = "";

	private String endTime = "";

	private String createDate = "";

	private String lastUpdated = "";

	private JobParameters jobParameters;

	private boolean restartable = false;

	private boolean abandonable = false;

	private boolean stoppable = false;

	private final TimeZone timeZone;

	private BatchStatus status;

	private ExitStatus exitStatus;

	private String jobConfigurationName;

	private List<Throwable> failureExceptions;

	private Map<String, Object> executionContext;

	private Collection<StepExecutionInfoResource> stepExecutions;

	public JobExecutionInfoResource() {
		this.timeZone = TimeZone.getTimeZone("UTC");
	}

	public JobExecutionInfoResource(JobExecution jobExecution, TimeZone timeZone) {

		if(timeZone != null) {
			this.timeZone = timeZone;
		}
		else {
			this.timeZone = TimeZone.getTimeZone("UTC");
		}

		this.executionId = jobExecution.getId();
		this.jobId = jobExecution.getJobId();
		this.stepExecutionCount = jobExecution.getStepExecutions().size();
		this.jobParameters = jobExecution.getJobParameters();
		this.status = jobExecution.getStatus();
		this.exitStatus = jobExecution.getExitStatus();
		this.jobConfigurationName = jobExecution.getJobConfigurationName();
		this.failureExceptions = jobExecution.getFailureExceptions();
		Map<String, Object> executionContextEntires =
				new HashMap<String, Object>(jobExecution.getExecutionContext().size());

		for (Map.Entry<String, Object> stringObjectEntry : jobExecution.getExecutionContext().entrySet()) {
			executionContextEntires.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
		}

		this.executionContext = executionContextEntires;

		this.version = jobExecution.getVersion();

		JobInstance jobInstance = jobExecution.getJobInstance();
		if (jobInstance != null) {
			this.jobName = jobInstance.getJobName();
			BatchStatus status = jobExecution.getStatus();
			this.restartable = status.isGreaterThan(BatchStatus.STOPPING) && status.isLessThan(BatchStatus.ABANDONED);
			this.abandonable = status.isGreaterThan(BatchStatus.STARTED) && status != BatchStatus.ABANDONED;
			this.stoppable = status.isLessThan(BatchStatus.STOPPING) && status != BatchStatus.COMPLETED;
		}
		else {
			this.jobName = "?";
		}

		this.dateFormat = this.dateFormat.withZone(DateTimeZone.forTimeZone(timeZone));

		this.createDate = dateFormat.print(jobExecution.getCreateTime().getTime());
		this.lastUpdated = dateFormat.print(jobExecution.getLastUpdated().getTime());

		if (jobExecution.getStartTime() != null) {
			this.startTime = dateFormat.print(jobExecution.getStartTime().getTime());

			if(!jobExecution.isRunning()) {
				this.endTime = dateFormat.print(jobExecution.getEndTime().getTime());
			}
			else {
				this.endTime = "N/A";
			}
		}
	}

	public void setStepExecutions(Collection<StepExecutionInfoResource> stepExecutions) {
		this.stepExecutions = stepExecutions;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	@JsonProperty
	public String getName() {
		return (jobName.endsWith(".job") ? jobName.substring(0, jobName.lastIndexOf(".job")) : jobName);
	}

	public Long getExecutionId() {
		return executionId;
	}

	public int getStepExecutionCount() {
		return stepExecutionCount;
	}

	public Long getJobId() {
		return jobId;
	}

	public String getStartTime() {
		return startTime;
	}

	public boolean isRestartable() {
		return restartable;
	}

	public boolean isAbandonable() {
		return abandonable;
	}

	public boolean isStoppable() {
		return stoppable;
	}

	public JobParameters getJobParameters() {
		return jobParameters;
	}

	public Map<String, Object> getExecutionContext() {
		return executionContext;
	}

	public List<Throwable> getFailureExceptions() {
		return failureExceptions;
	}

	public String getJobConfigurationName() {
		return jobConfigurationName;
	}

	public ExitStatus getExitStatus() {
		return exitStatus;
	}

	public BatchStatus getStatus() {
		return status;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public String getCreateDate() {
		return createDate;
	}

	public Collection<StepExecutionInfoResource> getStepExecutions() {
		return stepExecutions;
	}

	public Integer getVersion() {
		return version;
	}

	public String getEndTime() {
		return endTime;
	}

	/**
	 * Set restartable flag explicitly based on the job executions status of the same job instance.
	 *
	 * @param restartable flag to identify if the job execution can be restarted
	 */
	public void setRestartable(boolean restartable) {
		this.restartable = restartable;
	}

	/**
	 * Dedicated subclass to workaround type erasure.
	 *
	 * @author Gunnar Hillert
	 */
	public static class Page extends PagedResources<JobExecutionInfoResource> {

	}
}
