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
import static org.junit.Assert.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.batch.admin.partition.remote.BalancedStepServiceHandler;
import org.springframework.batch.admin.partition.remote.StepExecutionRequest;
import org.springframework.batch.admin.partition.remote.StepExecutionRequestRejectedException;
import org.springframework.batch.admin.partition.remote.StepExecutionResponse;

public class BalancedStepServiceHandlerTests {

	private BalancedStepServiceHandler service = new BalancedStepServiceHandler();

	private StubStepService stepService = new StubStepService();

	@Test
	public void testThreshold() {
		assertEquals(1, service.getThreshold(), 0.001);
	}

	@Test
	public void testVanillaExecute() throws Exception {
		service.handle(stepService, new StepExecutionRequest());
		assertEquals(0, service.getRejectedCount());
		assertEquals(0, service.getScore(), 0.001);
	}

	@Test(expected = StepExecutionRequestRejectedException.class)
	public void testRejectedExecute() throws Exception {
		stepService.score = 2;
		service.handle(stepService, new StepExecutionRequest());
	}

	@Test
	public void testRejectedAndRetry() throws Exception {
		service.handle(stepService, new StepExecutionRequest());
		assertEquals(1, service.getThreshold(), 0.01);
		try {
			service.handle(stepService, new StepExecutionRequest());
			fail("Expected StepExecutionRequestRejectedException");
		}
		catch (StepExecutionRequestRejectedException e) {
			// expected
		}
		try {
			service.handle(stepService, new StepExecutionRequest());
			fail("Expected StepExecutionRequestRejectedException");
		}
		catch (StepExecutionRequestRejectedException e) {
			// expected
		}
		service.handle(stepService, new StepExecutionRequest());
		assertEquals(2, service.getThreshold(), 0.01);
		assertEquals(2, service.getRejectedCount());
	}

	@Test
	public void testRejectedResetsGridSize() throws Exception {
		service.setRejectionFrequencyInitialEstimate(2);
		stepService.score = 0;
		service.handle(stepService, new StepExecutionRequest());
		stepService.score = 0;
		service.handle(stepService, new StepExecutionRequest());
		assertEquals(2, service.getRejectionFrequency(), 0.001);
		assertEquals(1, service.getThreshold(), 0.01);
		try {
			stepService.score = 1;
			service.handle(stepService, new StepExecutionRequest());
			fail("Expected StepExecutionRequestRejectedException");
		}
		catch (StepExecutionRequestRejectedException e) {
			// expected
		}
		try {
			stepService.score = 1;
			service.handle(stepService, new StepExecutionRequest());
			fail("Expected StepExecutionRequestRejectedException");
		}
		catch (StepExecutionRequestRejectedException e) {
			// expected
		}
		assertEquals(2, service.getThreshold(), 0.01);
		assertEquals(1, service.getRejectionFrequency(), 0.001);

		stepService.score = 0;
		service.handle(stepService, new StepExecutionRequest());
		assertEquals(1, service.getRejectionFrequency(), 0.001);
		try {
			stepService.score = 2;
			service.handle(stepService, new StepExecutionRequest());
			fail("Expected StepExecutionRequestRejectedException");
		}
		catch (StepExecutionRequestRejectedException e) {
			// expected
		}
		assertEquals(1, service.getRejectionFrequency(), 0.001);

		stepService.score = 0;
		service.handle(stepService, new StepExecutionRequest());
		stepService.score = 0;
		service.handle(stepService, new StepExecutionRequest());
		assertEquals(1, service.getRejectionFrequency(), 0.001);
		try {
			stepService.score = 3;
			service.handle(stepService, new StepExecutionRequest());
			fail("Expected StepExecutionRequestRejectedException");
		}
		catch (StepExecutionRequestRejectedException e) {
			// expected
		}
		assertEquals(1.25, service.getRejectionFrequency(), 0.001);
	}

	private static class StubStepService extends StepServiceSupport {

		private Log logger = LogFactory.getLog(getClass());

		private int score = 0;

		public StepExecutionResponse execute(StepExecutionRequest wrapper) {
			float threshold = wrapper.getThreshold();
			boolean rejected = (score + 1) > threshold;
			if (!rejected) {
				score++;
			}
			logger.debug(String.format("score=%d, threshold=%f, rejected=%b", score, threshold, rejected));
			return new StepExecutionResponse(threshold - score, rejected);
		}

	}

}
