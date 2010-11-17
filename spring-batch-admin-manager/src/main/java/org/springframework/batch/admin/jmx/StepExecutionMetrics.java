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

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.support.MetricType;

/**
 * @author Dave Syer
 *
 */
public interface StepExecutionMetrics {

	@ManagedMetric(metricType = MetricType.COUNTER, description = "Step Execution Count")
	int getExecutionCount();

	@ManagedMetric(metricType = MetricType.COUNTER, description = "Step Execution Failure Count")
	int getFailureCount();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Latest Duration Milliseconds")
	double getLatestDuration();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Mean Duration Milliseconds")
	double getMeanDuration();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Max Duration Milliseconds")
	double getMaxDuration();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Latest Step Execution ID")
	long getLatestExecutionId();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Latest Read Count")
	int getLatestReadCount();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Latest Write Count")
	int getLatestWriteCount();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Latest Filter Count")
	int getLatestFilterCount();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Latest Skip Count")
	int getLatestSkipCount();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Latest Commit Count")
	int getLatestCommitCount();

	@ManagedMetric(metricType = MetricType.GAUGE, description = "Latest Rollback Count")
	int getLatestRollbackCount();

	@ManagedAttribute(description  = "Latest Status")
	String getLatestStatus();

	@ManagedAttribute(description  = "Latest Exit Code")
	String getLatestExitCode();

	@ManagedAttribute(description  = "Latest Exit Description")
	String getLatestExitDescription();

}