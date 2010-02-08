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

import java.util.Arrays;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
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

	private ListableJobLocator jobLocator = EasyMock.createMock(ListableJobLocator.class);

	@Before
	public void init() {
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
		Step step = EasyMock.createMock(Step.class);
		JobStepLocator job = EasyMock.createMock(JobStepLocator.class);
		EasyMock.expect(jobLocator.getJob("job")).andReturn(job);
		EasyMock.expect(job.getStepNames()).andReturn(Arrays.asList("step"));
		EasyMock.expect(job.getStep("step")).andReturn(step);
		EasyMock.replay(jobLocator, job);
		assertEquals(step, stepLocator.getStep("job/step"));
		EasyMock.verify(jobLocator, job);
	}

	@Test
	public void testGetStepWithJobPrefix() throws NoSuchJobException {
		Step step = EasyMock.createMock(Step.class);
		JobStepLocator job = EasyMock.createMock(JobStepLocator.class);
		EasyMock.expect(jobLocator.getJob("job")).andReturn(job);
		EasyMock.expect(job.getStepNames()).andReturn(Arrays.asList("job.step")).anyTimes();
		EasyMock.expect(job.getStep("job.step")).andReturn(step);
		EasyMock.replay(jobLocator, job);
		assertEquals(step, stepLocator.getStep("job/step"));
		EasyMock.verify(jobLocator, job);
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.admin.service.JobLocatorStepLocator#getStepNames()}
	 * .
	 * @throws Exception 
	 */
	@Test
	public void testGetStepNames() throws Exception {
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Arrays.asList("job"));
		JobStepLocator job = EasyMock.createMock(JobStepLocator.class);
		EasyMock.expect(jobLocator.getJob("job")).andReturn(job);
		EasyMock.expect(job.getStepNames()).andReturn(Arrays.asList("step"));
		EasyMock.replay(jobLocator, job);
		assertEquals("[job/step]", stepLocator.getStepNames().toString());
		EasyMock.verify(jobLocator, job);
	}

}
