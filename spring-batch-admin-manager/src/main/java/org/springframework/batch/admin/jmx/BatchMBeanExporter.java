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

@ManagedResource
public class BatchMBeanExporter extends MBeanExporter implements SmartLifecycle {

	private static final Log logger = LogFactory.getLog(BatchMBeanExporter.class);

	public static final String DEFAULT_DOMAIN = "org.springframework.batch";

	private volatile boolean autoStartup = true;

	private volatile int phase = 0;

	private volatile boolean running;

	private final ReentrantLock lifecycleLock = new ReentrantLock();

	private Set<String> stepKeys = new HashSet<String>();

	private Set<String> jobKeys = new HashSet<String>();

	private final AnnotationJmxAttributeSource attributeSource = new AnnotationJmxAttributeSource();

	private JobService jobService;

	private String domain = DEFAULT_DOMAIN;

	public BatchMBeanExporter() {
		super();
		setAutodetect(false);
		setNamingStrategy(new MetadataNamingStrategy(attributeSource));
		setAssembler(new MetadataMBeanInfoAssembler(attributeSource));
	}

	/**
	 * The JMX domain to use for MBeans registered. Defaults to <code>org.springframework.batch</code> (which is useful
	 * in SpringSource HQ).
	 * 
	 * @param domain the domain name to set
	 */
	public void setDefaultDomain(String domain) {
		this.domain = domain;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	protected void registerBeans() {
		// Completely disable super class registration to avoid duplicates
	}

	private void registerSteps() {
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
					if (!stepKeys.contains(stepName)) {
						stepKeys.add(stepName);
						String beanKey = getBeanKeyForStepExecution(jobName, stepName);
						logger.info("Registering step execution " + stepName);
						registerBeanNameOrInstance(new SimpleStepExecutionMetrics(jobService, stepName), beanKey);
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
				registerBeanNameOrInstance(new SimpleJobExecutionMetrics(jobService, jobName),
						getBeanKeyForJobExecution(jobName));
			}
		}
	}

	private String getBeanKeyForJobExecution(String jobName) {
		return String.format(domain + ":type=JobExecution,name=%s", jobName);
	}

	private String getBeanKeyForStepExecution(String jobName, String stepName) {
		return String.format(domain + ":type=StepExecution,name=%s/%s", jobName, stepName);
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Step Count")
	public int getStepCount() {
		// TODO: only do this if necessary
		registerSteps();
		return stepKeys.size();
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Count")
	public int getJobCount() {
		// TODO: only do this if necessary
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

}
