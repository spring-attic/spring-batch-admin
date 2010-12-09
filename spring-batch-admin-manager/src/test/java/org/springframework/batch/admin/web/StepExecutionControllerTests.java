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
package org.springframework.batch.admin.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.batch.admin.history.StepExecutionHistory;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.ui.ExtendedModelMap;


public class StepExecutionControllerTests {

	private JobService jobService = EasyMock.createMock(JobService.class);

	private StepExecutionController controller = new StepExecutionController(
			jobService);

	@Test
	public void testDetailSunnyDay() throws Exception {

		jobService.getStepExecution(123L, 1234L);
		EasyMock.expectLastCall().andReturn(
				MetaDataInstanceFactory.createStepExecution());
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.detail(model, 123L, 1234L, null, null);
		// StepExecution
		assertEquals(1, model.size());
		assertEquals("jobs/executions/step", result);

		assertTrue(model.containsKey("stepExecutionInfo"));

		EasyMock.verify(jobService);

	}

	@Test
	public void testProgressSunnyDay() throws Exception {

		jobService.getStepExecution(123L, 1234L);
		EasyMock.expectLastCall().andReturn(
				MetaDataInstanceFactory.createStepExecution());
		jobService.countStepExecutionsForStep("job", "step");
		EasyMock.expectLastCall().andReturn(1200);
		jobService.listStepExecutionsForStep("job", "step", 0, 1000);
		EasyMock.expectLastCall().andReturn(
				Arrays.asList(MetaDataInstanceFactory.createStepExecution()));
		jobService.listStepExecutionsForStep("job", "step", 1000, 1000);
		EasyMock.expectLastCall().andReturn(
				Arrays.asList(MetaDataInstanceFactory.createStepExecution()));
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.history(model, 123L, 1234L, null, null);
		// StepExecution, history and progress
		assertEquals(3, model.size());
		assertEquals("jobs/executions/step/progress", result);

		assertTrue(model.containsKey("stepExecutionHistory"));

		EasyMock.verify(jobService);

	}

	@Test
	public void testProgressPartitionSunnyDay() throws Exception {

		jobService.getStepExecution(123L, 1234L);
		EasyMock.expectLastCall().andReturn(
				MetaDataInstanceFactory.createStepExecution("step:partition1", 0L));
		jobService.countStepExecutionsForStep("job", "step:partition*");
		EasyMock.expectLastCall().andReturn(1200);
		jobService.listStepExecutionsForStep("job", "step:partition*", 0, 1000);
		EasyMock.expectLastCall().andReturn(
				Arrays.asList(MetaDataInstanceFactory.createStepExecution()));
		jobService.listStepExecutionsForStep("job", "step:partition*", 1000, 1000);
		EasyMock.expectLastCall().andReturn(
				Arrays.asList(MetaDataInstanceFactory.createStepExecution()));
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.history(model, 123L, 1234L, null, null);
		// StepExecution, history and progress
		assertEquals(3, model.size());
		assertEquals("jobs/executions/step/progress", result);

		assertTrue(model.containsKey("stepExecutionHistory"));
		StepExecutionHistory history = (StepExecutionHistory) model.get("stepExecutionHistory");
		// The wildcard is intentional:
		assertEquals("step:partition*", history.getStepName());

		EasyMock.verify(jobService);

	}

	@Test
	public void testListForJobExecutionSunnyDay() throws Exception {

		jobService.getStepExecutions(123L);
		StepExecution stepExecution = MetaDataInstanceFactory
				.createStepExecution();
		EasyMock.expectLastCall().andReturn(Arrays.asList(stepExecution));
		jobService.getJobExecution(123L);
		EasyMock.expectLastCall().andReturn(stepExecution.getJobExecution());
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.list(model, 123L, null, null);
		// StepExecutions, JobExecution
		assertEquals(2, model.size());
		assertEquals("jobs/executions/steps", result);

		assertTrue(model.containsKey("jobExecutionInfo"));

		EasyMock.verify(jobService);

	}

}
