/*
 * Copyright 2006-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.admin.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.junit.Test;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.test.MetaDataInstanceFactory;

/**
 * @author Dave Syer
 * 
 */
public class BatchMBeanExporterTests {

	private BatchMBeanExporter exporter = new BatchMBeanExporter();

	@Test
	public void testJobObjectNameStaticProperties() throws Exception {
		exporter.setObjectNameStaticProperties(Collections.singletonMap("foo", "bar"));
		assertEquals("org.springframework.batch:type=JobExecution,name=job,foo=bar",
				exporter.getBeanKeyForJobExecution("job"));
	}

	@Test
	public void testStepObjectNameStaticProperties() throws Exception {
		exporter.setObjectNameStaticProperties(Collections.singletonMap("foo", "bar"));
		assertEquals("org.springframework.batch:type=JobExecution,name=job,step=step,foo=bar",
				exporter.getBeanKeyForStepExecution("job", "step"));
	}

	@Test
	public void testMetricsFactory() throws Exception {
		
		final MyStepExecutionMetrics stepExecutionMetrics = new MyStepExecutionMetrics("foo", "step");
		final AtomicBoolean stepCalled = new AtomicBoolean(false);
		
		StepExecutionMetricsFactory stepExecutionMetricsFactory = new StepExecutionMetricsFactory() {
			public StepExecutionMetrics createMetricsForStep(String jobName, String stepName) {
				stepCalled.set(true);
				return stepExecutionMetrics;
			}
		};
		exporter.setStepExecutionMetricsFactory(stepExecutionMetricsFactory);
		
		final MyJobExecutionMetrics jobExecutionMetrics = new MyJobExecutionMetrics("foo");
		final AtomicBoolean jobCalled = new AtomicBoolean(false);
		
		JobExecutionMetricsFactory jobExecutionMetricsFactory = new JobExecutionMetricsFactory() {
			public JobExecutionMetrics createMetricsForJob(String jobName) {
				jobCalled.set(true);
				return jobExecutionMetrics;
			}
		};
		exporter.setJobExecutionMetricsFactory(jobExecutionMetricsFactory);

		MBeanServer server = mock(MBeanServer.class);
		exporter.setServer(server);
		when(server.registerMBean(anyObject(), isA(ObjectName.class))).thenReturn(new ObjectInstance(new ObjectName(exporter.getBeanKeyForJobExecution("foo")), SimpleJobExecutionMetrics.class.getName()));
		when(server.registerMBean(anyObject(), isA(ObjectName.class))).thenReturn(new ObjectInstance(new ObjectName(exporter.getBeanKeyForStepExecution("foo", "step")), SimpleStepExecutionMetrics.class.getName()));

		JobService jobService = mock(JobService.class);
		exporter.setJobService(jobService);


		when(jobService.listJobs(0, Integer.MAX_VALUE)).thenReturn(Arrays.asList("foo"));
		when(jobService.listJobExecutionsForJob("foo", 0, 1)).thenReturn(Arrays.asList(MetaDataInstanceFactory.createJobExecutionWithStepExecutions(123L, Arrays.asList("step"))));

		exporter.doStart();
		
		assertTrue(stepCalled.get());
		assertTrue(jobCalled.get());
	}

	public static class MyStepExecutionMetrics extends SimpleStepExecutionMetrics {
		public MyStepExecutionMetrics(String jobName, String stepName) {
			super(null, jobName, stepName);
		}

	}

	public static class MyJobExecutionMetrics extends SimpleJobExecutionMetrics {
		public MyJobExecutionMetrics(String jobName) {
			super(null, jobName);
		}

	}

}

