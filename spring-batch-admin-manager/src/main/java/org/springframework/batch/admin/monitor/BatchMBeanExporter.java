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
package org.springframework.batch.admin.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.util.CumulativeHistory;
import org.springframework.batch.admin.web.StepExecutionHistory;
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
						registerBeanNameOrInstance(new StepExecutionMonitor(jobService, stepName), beanKey);
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
				registerBeanNameOrInstance(new JobExecutionMonitor(jobService, jobName),
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

	@ManagedResource
	public static class StepExecutionMonitor {

		private final JobService jobService;

		private final String stepName;

		public StepExecutionMonitor(JobService jobService, String stepName) {
			this.jobService = jobService;
			this.stepName = stepName;
		}

		@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Step Execution Count")
		public int getStepExecutionCount() {
			return jobService.countStepExecutionsForStep(stepName);
		}

		@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Step Execution Failure Count")
		public int getStepExecutionFailureCount() {
			int count = 0;
			int start = 0;
			int pageSize = 100;
			Collection<StepExecution> stepExecutions;
			do {
				stepExecutions = jobService.listStepExecutionsForStep(stepName, start, pageSize);
				start += pageSize;
				for (StepExecution stepExecution : stepExecutions) {
					if (stepExecution.getStatus().isUnsuccessful()) {
						count++;
					}
				}
			} while (!stepExecutions.isEmpty());
			return count;
		}

		@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Duration")
		public double getLatestStepExecutionDuration() {
			return computeHistory(stepName, 1).getDuration().getMean();
		}

		@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Mean Duration")
		public double getMeanStepExecutionDuration() {
			StepExecutionHistory history = computeHistory(stepName);
			return history.getDuration().getMean();
		}

		@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Max Duration")
		public double getMaxStepExecutionDuration() {
			StepExecutionHistory history = computeHistory(stepName);
			return history.getDuration().getMax();
		}

		private StepExecutionHistory computeHistory(String stepName) {
			// Running average over last 10 executions...
			return computeHistory(stepName, 10);
		}

		private StepExecutionHistory computeHistory(String stepName, int total) {
			StepExecutionHistory stepExecutionHistory = new StepExecutionHistory(stepName);
			for (StepExecution stepExecution : jobService.listStepExecutionsForStep(stepName, 0, total)) {
				stepExecutionHistory.append(stepExecution);
			}
			return stepExecutionHistory;
		}

	}

	@ManagedResource
	public static class JobExecutionMonitor {

		private final JobService jobService;

		private final String jobName;

		public JobExecutionMonitor(JobService jobService, String stepName) {
			this.jobService = jobService;
			this.jobName = stepName;
		}

		@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Count")
		public int getJobExecutionCount() {
			try {
				return jobService.countJobExecutionsForJob(jobName);
			}
			catch (NoSuchJobException e) {
				throw new IllegalStateException("Cannot locate job=" + jobName, e);
			}
		}

		@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Failure Count")
		public int getJobExecutionFailureCount() {

			int pageSize = 100;
			int start = 0;
			int count = 0;

			Collection<JobExecution> jobExecutions;
			do {

				try {
					jobExecutions = jobService.listJobExecutionsForJob(jobName, start, pageSize);
					start += pageSize;
				}
				catch (NoSuchJobException e) {
					throw new IllegalStateException("Cannot locate job=" + jobName, e);
				}
				for (JobExecution jobExecution : jobExecutions) {
					if (jobExecution.getStatus().isUnsuccessful()) {
						count++;
					}
				}
			} while (!jobExecutions.isEmpty());

			return count;

		}

		@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Latest Duration")
		public double getLatestJobExecutionDuration() {
			return computeHistory(jobName, 1).getDuration().getMean();
		}

		@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Mean Duration")
		public double getMeanJobExecutionDuration() {
			JobExecutionHistory history = computeHistory(jobName);
			return history.getDuration().getMean();
		}

		@ManagedMetric(metricType = MetricType.GAUGE, displayName = "Max Duration")
		public double getMaxJobExecutionDuration() {
			JobExecutionHistory history = computeHistory(jobName);
			return history.getDuration().getMax();
		}

		private JobExecutionHistory computeHistory(String jobName) {
			// Running average over last 10 executions...
			return computeHistory(jobName, 10);
		}

		private JobExecutionHistory computeHistory(String jobName, int total) {
			JobExecutionHistory jobExecutionHistory = new JobExecutionHistory(jobName);
			try {
				for (JobExecution jobExecution : jobService.listJobExecutionsForJob(jobName, 0, total)) {
					jobExecutionHistory.append(jobExecution);
				}
			}
			catch (NoSuchJobException e) {
				throw new IllegalStateException("Cannot locate job=" + jobName, e);
			}
			return jobExecutionHistory;
		}

	}

	public static class JobExecutionHistory {

		private final String jobName;

		private CumulativeHistory duration = new CumulativeHistory();

		public JobExecutionHistory(String jobName) {
			this.jobName = jobName;
		}

		public String getJobName() {
			return jobName;
		}

		public CumulativeHistory getDuration() {
			return duration;
		}

		public void append(JobExecution jobExecution) {
			if (jobExecution.getEndTime() == null) {
				// ignore unfinished executions
				return;
			}
			Date startTime = jobExecution.getStartTime();
			Date endTime = jobExecution.getEndTime();
			long time = endTime.getTime() - startTime.getTime();
			duration.append(time);
		}

	}

}
