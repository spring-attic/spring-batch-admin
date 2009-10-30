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
package org.springframework.batch.admin.partition.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.admin.partition.remote.NoSuchStepExecutionException;
import org.springframework.batch.admin.partition.remote.SimpleStepLocator;
import org.springframework.batch.admin.partition.remote.StepExecutionRequest;
import org.springframework.batch.admin.partition.remote.StepExecutionResponse;
import org.springframework.batch.admin.partition.remote.StepLocatorStepService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.core.task.TaskExecutor;

public class StepLocatorStepServiceTests {

	private StepLocatorStepService service = new StepLocatorStepService();

	private StepSupport step;

	private StepExecutionRequest wrapper;

	@Before
	public void setUpFactory() {
		wrapper = new StepExecutionRequest(123L, 321L, "step");
		wrapper.setThreshold(1);
		step = new StepSupport("step");
		service.setStepLocator(new SimpleStepLocator(step));
		service.setJobExplorer(new JobExplorerSupport() {
			@Override
			public StepExecution getStepExecution(Long jobExecutionId, Long stepExecutionId) {
				return new StepExecution("step:partition1", new JobExecution(stepExecutionId), stepExecutionId);
			}
		});
	}

	@Test(expected = NoSuchStepExecutionException.class)
	public void testNoSuchStepExecution() throws Exception {
		service.setJobExplorer(new JobExplorerSupport());
		service.execute(wrapper);
	}

	@Test(expected=NoSuchStepException.class)
	public void testStepNameNotMatching() throws Exception {
		wrapper = new StepExecutionRequest(123L, 321L, "not-a-step");
		wrapper.setThreshold(1);
		service.execute(wrapper);
	}

	@Test
	public void testSetWeight() throws Exception {
		service.setWeight(2);
		StepExecutionResponse result = service.execute(wrapper);
		assertEquals(2, result.getCapacity(), 0.01);
		assertFalse(result.isRejected());
	}

	@Test
	public void testCapacityWhenRejected() throws Exception {
		service.setWeight(0.5f);
		StepExecutionResponse result = service.execute(wrapper);
		assertEquals(0.5, result.getCapacity(), 0.01);
		assertTrue(result.isRejected());
	}

	@Test
	public void testSetTaskExecutor() throws Exception {
		service.setTaskExecutor(new TaskExecutor() {
			public void execute(Runnable task) {
				wrapper.setThreshold(20);
			}
		});
		service.execute(wrapper);
		assertEquals(20, wrapper.getThreshold(), 0.01);
	}

}
