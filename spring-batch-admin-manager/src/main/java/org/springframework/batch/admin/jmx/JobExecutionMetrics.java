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
package org.springframework.batch.admin.jmx;

import java.util.Date;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.support.MetricType;

/**
 * @author Dave Syer
 *
 */
public interface JobExecutionMetrics {

	@ManagedMetric(metricType = MetricType.COUNTER, description = "Job Execution Count")
	int getExecutionCount();

	@ManagedMetric(metricType = MetricType.COUNTER, description = "Job Execution Failure Count")
	int getFailureCount();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Latest Duration Milliseconds")
	double getLatestDuration();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Mean Duration Milliseconds")
	double getMeanDuration();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Max Duration Milliseconds")
	double getMaxDuration();

	@ManagedAttribute(description = "Latest Job Execution ID")
	long getLatestExecutionId();

	@ManagedAttribute(description = "Latest Start Time")
	Date getLatestStartTime();

	@ManagedAttribute(description = "Latest End Time")
	Date getLatestEndTime();

	@ManagedAttribute(description = "Latest Exit Code")
	String getLatestExitCode();

	@ManagedAttribute(description = "Latest Status")
	String getLatestStatus();

	@ManagedAttribute(description = "Latest Step Execution Exit Description")
	String getLatestStepExitDescription();

	@ManagedAttribute(description = "Latest Step Execution Step Name")
	String getLatestStepName();

	@ManagedAttribute(description = "Check if there is a Running Job Execution")
	boolean isJobRunning();

}