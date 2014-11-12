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
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.batch.admin.domain.StepExecutionHistory;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.ui.ExtendedModelMap;


public class StepExecutionControllerTests {

	@Mock
	private JobService jobService;

	private StepExecutionController controller;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		controller = new StepExecutionController(jobService);
	}

	@Test
	public void testDetailSunnyDay() throws Exception {

		when(jobService.getStepExecution(123L, 1234L)).thenReturn(MetaDataInstanceFactory.createStepExecution());

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.detail(model, 123L, 1234L, null, null);
		// StepExecution
		assertEquals(1, model.size());
		assertEquals("jobs/executions/step", result);

		assertTrue(model.containsKey("stepExecutionInfo"));
	}

	@Test
	public void testProgressSunnyDay() throws Exception {

		when(jobService.getStepExecution(123L, 1234L)).thenReturn(MetaDataInstanceFactory.createStepExecution());
		when(jobService.countStepExecutionsForStep("job", "step")).thenReturn(1200);
		when(jobService.listStepExecutionsForStep("job", "step", 0, 1000)).thenReturn(Arrays.asList(MetaDataInstanceFactory.createStepExecution()));
		when(jobService.listStepExecutionsForStep("job", "step", 1000, 1000)).thenReturn(Arrays.asList(MetaDataInstanceFactory.createStepExecution()));

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.history(model, 123L, 1234L, null, null);
		// StepExecution, history and progress
		assertEquals(3, model.size());
		assertEquals("jobs/executions/step/progress", result);

		assertTrue(model.containsKey("stepExecutionHistory"));
	}

	@Test
	public void testProgressPartitionSunnyDay() throws Exception {

		when(jobService.getStepExecution(123L, 1234L)).thenReturn(MetaDataInstanceFactory.createStepExecution("step:partition1", 0L));
		when(jobService.countStepExecutionsForStep("job", "step:partition*")).thenReturn(1200);
		when(jobService.listStepExecutionsForStep("job", "step:partition*", 0, 1000)).thenReturn(Arrays.asList(MetaDataInstanceFactory.createStepExecution()));
		when(jobService.listStepExecutionsForStep("job", "step:partition*", 1000, 1000)).thenReturn(Arrays.asList(MetaDataInstanceFactory.createStepExecution()));

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.history(model, 123L, 1234L, null, null);
		// StepExecution, history and progress
		assertEquals(3, model.size());
		assertEquals("jobs/executions/step/progress", result);

		assertTrue(model.containsKey("stepExecutionHistory"));
		StepExecutionHistory history = (StepExecutionHistory) model.get("stepExecutionHistory");
		// The wildcard is intentional:
		assertEquals("step:partition*", history.getStepName());
	}

	@Test
	public void testListForJobExecutionSunnyDay() throws Exception {

		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		when(jobService.getStepExecutions(123L)).thenReturn(Arrays.asList(stepExecution));
		when(jobService.getJobExecution(123L)).thenReturn(stepExecution.getJobExecution());

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.list(model, 123L, null, null);
		// StepExecutions, JobExecution
		assertEquals(2, model.size());
		assertEquals("jobs/executions/steps", result);

		assertTrue(model.containsKey("jobExecutionInfo"));
	}

}
