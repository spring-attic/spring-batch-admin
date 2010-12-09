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
package org.springframework.batch.admin.history;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;
import org.springframework.batch.admin.history.StepExecutionHistory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

public class StepExecutionHistoryTests {

	private StepExecutionHistory history = new StepExecutionHistory("step");

	@Test
	public void testGetStepName() {
		assertEquals("step", history.getStepName());
	}

	@Test
	public void testGetDurationWithRunningExecution() {
		assertEquals(0, history.getCount());
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.setStartTime(new Date(System.currentTimeMillis()-1000));
		history.append(stepExecution);
		assertEquals("Incomplete history should have no duration: "+history.getDuration(), 0, history.getDuration().getMean(), 0.01);
	}

	@Test
	public void testGetCountWithRunningExecution() {
		assertEquals(0, history.getCount());
		history.append(MetaDataInstanceFactory.createStepExecution());
		assertEquals(0, history.getCount());
	}

	@Test
	public void testGetCountWithCompletedExecution() {
		assertEquals(0, history.getCount());
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.setEndTime(new Date());
		history.append(stepExecution);
		assertEquals(1, history.getCount());
	}

}
