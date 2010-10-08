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

import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.support.MetricType;

/**
 * @author Dave Syer
 *
 */
public interface JobExecutionMetrics {

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Count")
	int getJobExecutionCount();

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Failure Count")
	int getJobExecutionFailureCount();

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Duration")
	double getLatestJobExecutionDuration();

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Mean Duration")
	double getMeanJobExecutionDuration();

	@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Max Duration")
	double getMaxJobExecutionDuration();

}