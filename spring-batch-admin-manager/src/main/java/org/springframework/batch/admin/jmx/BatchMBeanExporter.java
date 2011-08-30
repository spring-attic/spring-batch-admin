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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.jmx.export.naming.MetadataNamingStrategy;
import org.springframework.jmx.support.MetricType;
import org.springframework.util.Assert;

@ManagedResource
public class BatchMBeanExporter extends MBeanExporter implements SmartLifecycle {

	private static final Log logger = LogFactory.getLog(BatchMBeanExporter.class);

	public static final String DEFAULT_DOMAIN = "org.springframework.batch";

	private volatile boolean autoStartup = true;

	private volatile int phase = 0;

	private volatile boolean running;

	private final Map<String, String> objectNameStaticProperties = new LinkedHashMap<String, String>();

	private final ReentrantLock lifecycleLock = new ReentrantLock();

	private Set<String> stepKeys = new HashSet<String>();

	private Set<String> jobKeys = new HashSet<String>();

	private final AnnotationJmxAttributeSource attributeSource = new AnnotationJmxAttributeSource();

	private JobService jobService;

	private String domain = DEFAULT_DOMAIN;

	private boolean registerSteps = true;

	private JobExecutionMetricsFactory jobExecutionMetricsFactory = new ExecutionMetricsFactory();

	private StepExecutionMetricsFactory stepExecutionMetricsFactory = new ExecutionMetricsFactory();

	public BatchMBeanExporter() {
		super();
		setAutodetect(false);
		setNamingStrategy(new MetadataNamingStrategy(attributeSource));
		setAssembler(new MetadataMBeanInfoAssembler(attributeSource));
	}

	/**
	 * Flag to determine if any metrics at all should be exposed for step
	 * executions (default true). Set to fals eto only expose job-level metrics.
	 * 
	 * @param registerSteps the flag to set
	 */
	public void setRegisterSteps(boolean registerSteps) {
		this.registerSteps = registerSteps;
	}

	/**
	 * The JMX domain to use for MBeans registered. Defaults to
	 * <code>org.springframework.batch</code> (which is useful in SpringSource
	 * HQ).
	 * 
	 * @param domain the domain name to set
	 */
	public void setDefaultDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Help method for extensions which need access to the default domain.
	 * 
	 * @return the default domain used to construct object names
	 */
	protected String getDefaultDomain() {
		return this.domain;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	/**
	 * Static properties that will be added to all object names.
	 * 
	 * @param objectNameStaticProperties the objectNameStaticProperties to set
	 */
	public void setObjectNameStaticProperties(Map<String, String> objectNameStaticProperties) {
		this.objectNameStaticProperties.putAll(objectNameStaticProperties);
	}

	/**
	 * Factory for {@link JobExecutionMetrics}. Can be used to customize and
	 * extend the metrics exposed.
	 * 
	 * @param stepExecutionMetricsFactory the {@link StepExecutionMetricsFactory} to set
	 */
	public void setStepExecutionMetricsFactory(StepExecutionMetricsFactory stepExecutionMetricsFactory) {
		this.stepExecutionMetricsFactory = stepExecutionMetricsFactory;
	}

	/**
	 * Factory for {@link StepExecutionMetrics}. Can be used to customize and
	 * extend the metrics exposed.
	 * 
	 * @param jobExecutionMetricsFactory the {@link JobExecutionMetricsFactory} to set
	 */
	public void setJobExecutionMetricsFactory(JobExecutionMetricsFactory jobExecutionMetricsFactory) {
		this.jobExecutionMetricsFactory = jobExecutionMetricsFactory;
	}

	@Override
	public void afterPropertiesSet() {
		Assert.state(jobService != null, "A JobService must be provided");
		super.afterPropertiesSet();
	}

	protected void registerBeans() {
		// Completely disable super class registration to avoid duplicates
	}

	private void registerSteps() {
		if (!registerSteps) {
			return;
		}
		for (String jobName : jobService.listJobs(0, Integer.MAX_VALUE)) {
			Collection<JobExecution> jobExecutions = Collections.emptySet();
			try {
				jobExecutions = jobService.listJobExecutionsForJob(jobName, 0, 1);
			}
			catch (NoSuchJobException e) {
				// do-nothing
				logger.error("Job listed but does not exist", e);
			}
			for (JobExecution jobExecution : jobExecutions) {
				for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
					String stepName = stepExecution.getStepName();
					String stepKey = String.format("%s/%s", jobName, stepName);
					String beanKey = getBeanKeyForStepExecution(jobName, stepName);
					if (!stepKeys.contains(stepKey)) {
						stepKeys.add(stepKey);
						logger.info("Registering step execution " + stepKey);
						registerBeanNameOrInstance(stepExecutionMetricsFactory.createMetricsForStep(jobName, stepName),
								beanKey);
					}
				}
			}
		}
	}

	private void registerJobs() {
		for (String jobName : jobService.listJobs(0, Integer.MAX_VALUE)) {
			if (!jobKeys.contains(jobName)) {
				jobKeys.add(jobName);
				logger.info("Registering job execution " + jobName);
				registerBeanNameOrInstance(jobExecutionMetricsFactory.createMetricsForJob(jobName),
						getBeanKeyForJobExecution(jobName));
			}
		}
	}

	/**
	 * Encode the job name into an ObjectName in the form
	 * <code>[domain]:type=JobExecution,name=[jobName]</code>.
	 * 
	 * @param jobName the name of the job
	 * @return a String representation of an ObjectName
	 */
	protected String getBeanKeyForJobExecution(String jobName) {
		jobName = escapeForObjectName(jobName);
		return String.format("%s:type=JobExecution,name=%s", domain, jobName) + getStaticNames();
	}

	/**
	 * Encode the job and step name into an ObjectName in the form
	 * <code>[domain]:type=JobExecution,name=[jobName],step=[stepName]</code>.
	 * 
	 * @param jobName the name of the job
	 * @param stepName the name of the step
	 * @return a String representation of an ObjectName
	 */
	protected String getBeanKeyForStepExecution(String jobName, String stepName) {
		jobName = escapeForObjectName(jobName);
		stepName = escapeForObjectName(stepName);
		return String.format("%s:type=JobExecution,name=%s,step=%s", domain, jobName, stepName) + getStaticNames();
	}

	private String getStaticNames() {
		if (objectNameStaticProperties.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (String key : objectNameStaticProperties.keySet()) {
			builder.append("," + key + "=" + objectNameStaticProperties.get(key));
		}
		return builder.toString();
	}

	private String escapeForObjectName(String value) {
		value = value.replaceAll(":", "@");
		value = value.replaceAll(",", ";");
		value = value.replaceAll("=", "~");
		return value;
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Step Count")
	public int getStepCount() {
		registerSteps();
		return stepKeys.size();
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Count")
	public int getJobCount() {
		registerJobs();
		return jobKeys.size();
	}

	@ManagedAttribute
	public String[] getJobNames() {
		return jobKeys.toArray(new String[0]);
	}

	@ManagedAttribute
	public String[] getStepNames() {
		return stepKeys.toArray(new String[0]);
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Failure Count")
	public int getJobExecutionFailureCount() {
		int count = 0;
		int start = 0;
		int pageSize = 100;
		Collection<JobExecution> jobExecutions;
		do {
			jobExecutions = jobService.listJobExecutions(start, pageSize);
			start += pageSize;
			for (JobExecution jobExecution : jobExecutions) {
				if (jobExecution.getStatus().isUnsuccessful()) {
					count++;
				}
			}
		} while (!jobExecutions.isEmpty());
		return count;
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Count")
	public int getJobExecutionCount() {
		return jobService.countJobExecutions();
	}

	public final boolean isAutoStartup() {
		return this.autoStartup;
	}

	public final int getPhase() {
		return this.phase;
	}

	public final boolean isRunning() {
		this.lifecycleLock.lock();
		try {
			return this.running;
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	public final void start() {
		this.lifecycleLock.lock();
		try {
			if (!this.running) {
				this.doStart();
				this.running = true;
				if (logger.isInfoEnabled()) {
					logger.info("started " + this);
				}
			}
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	public final void stop() {
		this.lifecycleLock.lock();
		try {
			if (this.running) {
				this.doStop();
				this.running = false;
				if (logger.isInfoEnabled()) {
					logger.info("stopped " + this);
				}
			}
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	public final void stop(Runnable callback) {
		this.lifecycleLock.lock();
		try {
			this.stop();
			callback.run();
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	protected void doStop() {
		unregisterBeans();
		jobKeys.clear();
		stepKeys.clear();
	}

	protected void doStart() {
		registerJobs();
		registerSteps();
	}

	private class ExecutionMetricsFactory implements JobExecutionMetricsFactory, StepExecutionMetricsFactory {

		public StepExecutionMetrics createMetricsForStep(String jobName, String stepName) {
			return new SimpleStepExecutionMetrics(jobService, jobName, stepName);
		}

		public JobExecutionMetrics createMetricsForJob(String jobName) {
			return new SimpleJobExecutionMetrics(jobService, jobName);
		}

	}

}
