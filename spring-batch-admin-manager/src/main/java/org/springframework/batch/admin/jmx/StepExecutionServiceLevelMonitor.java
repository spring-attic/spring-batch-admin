/*
 * Copyright 2006-2010 the original author or authors.
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
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.Notification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;
import org.springframework.jmx.support.MetricType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.util.StopWatch;

/**
 * Monitors executions of a given step and sends JMX notifications if it takes too long. 
 * 
 * TODO: use messaging instead of notifications?
 * TODO: use JMX monitor instead of AOP?
 * 
 * @author Dave Syer
 * 
 */
@ManagedResource
public class StepExecutionServiceLevelMonitor implements NotificationPublisherAware {

	private static final Log logger = LogFactory.getLog(StepExecutionServiceLevelMonitor.class);

	private NotificationPublisher notificationPublisher;

	private long timeout = 0;

	private double warningMargin = 0.2;

	private long sequence = 0;

	private int overruns = 0;

	private TaskScheduler taskScheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void setWarningMargin(double warningMargin) {
		this.warningMargin = warningMargin;
	}

	/**
	 * A scheduler that can be used to poll a step execution in a background thread.
	 * 
	 * @param taskScheduler the task scheduler to set
	 */
	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public void invoke(ProceedingJoinPoint joinPoint, final StepExecution stepExecution, Step step) throws Throwable {

		final AtomicBoolean finished = new AtomicBoolean(false);
		final StopWatch timer = new StopWatch(stepExecution.getStepName() + ":execution");

		try {

			if (timeout > 0) {

				if (notificationPublisher != null) {
					Notification notification = new Notification("INFO", this, sequence++, "Starting:" + stepExecution);
					notificationPublisher.sendNotification(notification);
				}

				timer.start("StepExecution.Id:" + stepExecution.getId());
				final long threshold = (long) (timeout * (1 - warningMargin));
				Date warningTime = new Date(System.currentTimeMillis() + threshold);
				logger.debug("Scheduling warning after (ms) " + threshold);
				taskScheduler.schedule(new Runnable() {

					public void run() {
						if (!finished.get()) {
							logger.debug("Sending warning (step not complete after " + threshold + " ms): "
									+ stepExecution);
							if (notificationPublisher != null) {
								Notification notification = new Notification("WARN",
										StepExecutionServiceLevelMonitor.this, sequence++, "Warning:" + stepExecution);
								notificationPublisher.sendNotification(notification);
							}
						}
						else {
							logger.debug("No warning necessary for " + stepExecution);
						}
					}

				}, warningTime);
			}

			joinPoint.proceed();

		}
		finally {

			finished.set(true);

			if (timeout > 0) {
				timer.stop();
				Executors.newSingleThreadScheduledExecutor().shutdown();
				if (timer.getLastTaskTimeMillis() > timeout) {
					overruns++;
					logger.debug("Notifying overrun " + stepExecution);
					if (notificationPublisher != null) {
						Notification notification = new Notification("ERROR", this, sequence++, "Overrun:"
								+ stepExecution);
						notificationPublisher.sendNotification(notification);
					}
				}
			}

		}

	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Step Execution Overruns")
	public int getTotalOverruns() {
		return overruns;
	}

	public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
		this.notificationPublisher = notificationPublisher;
	}

}
