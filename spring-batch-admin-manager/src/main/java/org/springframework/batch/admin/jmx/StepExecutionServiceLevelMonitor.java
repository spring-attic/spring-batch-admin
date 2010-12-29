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

import javax.management.ObjectName;
import javax.management.monitor.GaugeMonitor;
import javax.management.monitor.MonitorMBean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Monitors executions of a given step and sends JMX notifications if it takes
 * too long. The monitor is a {@link MonitorMBean} so it can be automatically
 * exposed to an existing MBeanServer using Spring JMX. JMX clients subscribe to
 * notifications and receive them whenever the thresholds are crossed.
 * 
 * @author Dave Syer
 * 
 */
public class StepExecutionServiceLevelMonitor implements FactoryBean<GaugeMonitor>, InitializingBean {

	private String defaultDomain = BatchMBeanExporter.DEFAULT_DOMAIN;

	private String stepName;

	private String jobName;

	private int upperThreshold = 0;

	private int lowerThreshold = 0;

	private boolean autoStart = true;

	private String observedAttribute = "LatestDuration";

	/**
	 * The name of the attribute to monitor on the step. Defaults to
	 * <code>LatestDuration</code>. This can be changed at runtime, but note
	 * that if the type of the observed metric changes (e.g. from double to
	 * integer) then the thresholds will also have to be changed so their type
	 * matches.
	 * 
	 * @param observedAttribute the observed attribute to set
	 */
	public void setObservedAttribute(String observedAttribute) {
		this.observedAttribute = observedAttribute;
	}

	/**
	 * Should the monitor start immediately or wait to be started manually?
	 * 
	 * @param autoStart the auto start flag to set
	 */
	public void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
	}

	/**
	 * The domain name to use in constructing object names for the monitored
	 * step. Default to <code>org.springframework.batch</code> (same as
	 * {@link BatchMBeanExporter}).
	 * 
	 * @param defaultDomain the default domain to set
	 */
	public void setDefaultDomain(String defaultDomain) {
		this.defaultDomain = defaultDomain;
	}

	/**
	 * @param stepName the stepName to set
	 */
	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	/**
	 * @param jobName the jobName to set
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * Upper threshold for observed attribute. Mandatory with no default.
	 * 
	 * @param upperThreshold the upper threshold to set
	 */
	public void setUpperThreshold(int upperThreshold) {
		this.upperThreshold = upperThreshold;
	}

	/**
	 * Optional lower threshold. Defaults to 80% of the upper threshold.
	 * 
	 * @param lowerThreshold the lower threshold to set
	 */
	public void setLowerThreshold(int lowerThreshold) {
		this.lowerThreshold = lowerThreshold;
	}

	public GaugeMonitor getObject() throws Exception {
		GaugeMonitor monitor = new GaugeMonitor();
		monitor.setNotifyHigh(true);
		monitor.addObservedObject(new ObjectName(String.format("%s:type=JobExecution,name=%s,step=%s", defaultDomain,
				jobName, stepName)));
		monitor.setObservedAttribute(observedAttribute);
		if (observedAttribute.endsWith("Duration")) {
			monitor.setThresholds(new Double(upperThreshold), new Double(lowerThreshold));
		}
		else {
			monitor.setThresholds(new Integer(upperThreshold), new Integer(lowerThreshold));
		}
		if (autoStart) {
			monitor.start();
		}
		return monitor;
	}

	public Class<?> getObjectType() {
		return GaugeMonitor.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.state(jobName != null, "A Job name must be provided");
		Assert.state(stepName != null, "A Step name must be provided");
		Assert.state(upperThreshold > 0, "A threshold must be provided");
		Assert.state(lowerThreshold < upperThreshold, "A threshold must be provided");
		if (lowerThreshold == 0) {
			lowerThreshold = upperThreshold * 8 / 10;
		}
	}

}
