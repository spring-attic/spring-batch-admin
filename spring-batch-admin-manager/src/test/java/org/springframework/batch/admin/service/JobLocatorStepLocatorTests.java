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
package org.springframework.batch.admin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.step.StepLocator;

/**
 * @author Dave Syer
 * 
 */
public class JobLocatorStepLocatorTests {

	private interface JobStepLocator extends Job, StepLocator {
	}

	private JobLocatorStepLocator stepLocator = new JobLocatorStepLocator();

	@Mock
	private ListableJobLocator jobLocator;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		stepLocator.setJobLocator(jobLocator);
	}

	@Test
	public void testJobLocatorStepLocator() {
		stepLocator = new JobLocatorStepLocator(jobLocator);
		assertNotNull(stepLocator);
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.admin.service.JobLocatorStepLocator#getStep(java.lang.String)}
	 * .
	 * @throws NoSuchJobException
	 */
	@Test
	public void testGetStep() throws NoSuchJobException {
		Step step = mock(Step.class);
		JobStepLocator job = mock(JobStepLocator.class);
		when(jobLocator.getJob("job")).thenReturn(job);
		when(job.getStepNames()).thenReturn(Arrays.asList("step"));
		when(job.getStep("step")).thenReturn(step);

		assertEquals(step, stepLocator.getStep("job/step"));
	}

	@Test
	public void testGetStepWithJobPrefix() throws NoSuchJobException {
		Step step = mock(Step.class);
		JobStepLocator job = mock(JobStepLocator.class);
		when(jobLocator.getJob("job")).thenReturn(job);
		when(job.getStepNames()).thenReturn(Arrays.asList("job.step"));
		when(job.getStep("job.step")).thenReturn(step);

		assertEquals(step, stepLocator.getStep("job/step"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.admin.service.JobLocatorStepLocator#getStepNames()}
	 * .
	 * @throws Exception 
	 */
	@Test
	public void testGetStepNames() throws Exception {
		when(jobLocator.getJobNames()).thenReturn(Arrays.asList("job"));
		JobStepLocator job = mock(JobStepLocator.class);
		when(jobLocator.getJob("job")).thenReturn(job);
		when(job.getStepNames()).thenReturn(Arrays.asList("step"));

		assertEquals("[job/step]", stepLocator.getStepNames().toString());
	}

}
