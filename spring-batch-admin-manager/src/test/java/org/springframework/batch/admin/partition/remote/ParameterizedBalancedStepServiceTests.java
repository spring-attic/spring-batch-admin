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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.batch.admin.partition.remote.BalancedStepServiceHandler;
import org.springframework.batch.admin.partition.remote.StepExecutionRequest;
import org.springframework.batch.admin.partition.remote.StepExecutionRequestRejectedException;
import org.springframework.batch.admin.partition.remote.StepExecutionResponse;
import org.springframework.util.StringUtils;

@RunWith(Parameterized.class)
public class ParameterizedBalancedStepServiceTests {

	private BalancedStepServiceHandler service = new BalancedStepServiceHandler();

	private StubStepService stepService = new StubStepService();

	private final TestData[] inputs;

	@Parameters
	public static List<Object[]> data() {
		List<Object[]> params = new ArrayList<Object[]>();
		// 0: 1 request to empty node
		params.add(new Object[] { 1, getTestData("0:0:1:f") });
		// 1: 2 requests to empty nodes
		params.add(new Object[] { 1, getTestData("0:0:1:f, *") });
		// 2: 3 requests to empty nodes
		params.add(new Object[] { 1, getTestData("0:0:1:f, *, *") });
		// 3: pass, barf, pass
		params.add(new Object[] { 1, getTestData("0:0:1:f, 1:0:1:t, *") });
		// 4: 3 requests to empty nodes
		params.add(new Object[] { 1, getTestData("0:0:1:f, *, *") });
		// 5: Round robin, 1 cycle (+ simulates overcrowding and
		// rejection on 4th request)
		params.add(new Object[] { 3, getTestData("0:0:1:f, *, *, +, *") });
		// 6: Round robin, 2 cycles
		params.add(new Object[] { 3,
				getTestData("0:0:1:f, *, *, +, *, 1:0:2:f, *, *, +") });
		// 7: Round robin, 3 cycles
		params
				.add(new Object[] {
						2,
						getTestData("0:0:1:f, *, +, *, 1:0:2:f, *, +, *, 2:0:3:f, *, +") });
		// 8: Third request goes to a busy node and is
		// rejected. The retry succeeds. Then a round robin cycle.
		params
				.add(new Object[] {
						3,
						getTestData("0:0:1:f, *, 1:0:1:t, *, 0:1:2:f, 1:0:2:f, 1:0:2:f, *, *, +") });
		// 9: 2 successful requests, followed by four successive failures (going
		// to the same node which is still busy), then hitting the empty node
		// and completing successfully, followed by 1 round robin cycle.
		params
				.add(new Object[] {
						3,
						getTestData("0:0:1:f, *, 1:0:1:t, *, 1:0:2:f, *, *, 0:1:2:f, 1:0:2:f, 1:0:2:f, *, *, +") });
		// 10: Success, busy, more busy, busy, success, round robin.
		params
				.add(new Object[] {
						3,
						getTestData("0:0:1:f, 1:0:1:t, 2:-1:1:t, 1:1:3:f, 0:2:3:f, 2:0:3:f, 1:1:3:f, *, *, 2:0:3:f") });
		return params;
	}

	public ParameterizedBalancedStepServiceTests(int gridSize, TestData[] inputs) {
		this.inputs = inputs;
		service.setRejectionFrequencyInitialEstimate(gridSize);
	}

	@Test
	public void testRejectedAndRetryWithGridSize() throws Exception {

		for (int i = 0; i < inputs.length; i++) {

			TestData data = inputs[i];

			stepService.score = data.inputScore;

			boolean rejected = false;
			try {
				service.handle(stepService, new StepExecutionRequest());
			} catch (StepExecutionRequestRejectedException e) {
				rejected = true;
			}

			TestData result = new TestData(data.inputScore,
					stepService.capacity, stepService.threshold, rejected);
			String msg = String.format("i=%d: ", i);

			assertEquals(msg, data.toString(), result.toString());

		}

	}

	private static TestData[] getTestData(String str) {
		String[] array = StringUtils.trimArrayElements(StringUtils
				.commaDelimitedListToStringArray(str));
		TestData[] result = new TestData[array.length];
		TestData last = new TestData(0, 1, 1, false);
		for (int i = 0; i < array.length; i++) {

			String[] values;
			if (array[i].equals("*")) {
				result[i] = last;
			} else
			// Increment the input and output score, but not the threshold =>
			// rejected
			if ("+".equals(array[i])) {
				result[i] = new TestData(last.inputScore + 1, last.capacity,
						last.threshold, true);
			} else {
				values = StringUtils.delimitedListToStringArray(array[i], ":");
				result[i] = TestData.parse(values);
			}

			last = result[i];

		}
		return result;
	}

	private static class StubStepService extends StepServiceSupport {

		private Log logger = LogFactory.getLog(getClass());

		private int score = 0;

		private float threshold = 0;

		private float capacity;

		public StepExecutionResponse execute(StepExecutionRequest wrapper) {
			threshold = wrapper.getThreshold();
			boolean rejected = (score + 1) > threshold;
			logger.debug(String.format("score=%d, threshold=%f, rejected=%b",
					score, threshold, rejected));
			if (!rejected) {
				score++;
			}
			capacity = threshold - score;
			return new StepExecutionResponse(capacity, rejected);
		}

	}

	private static class TestData {

		private final int inputScore;

		private final float threshold;

		private final boolean rejected;

		private final float capacity;

		public TestData(int inputScore, float capacity, float threshold,
				boolean rejected) {
			this.inputScore = inputScore;
			this.capacity = capacity;
			this.threshold = threshold;
			this.rejected = rejected;

		}

		public static TestData parse(String[] values) {
			return new TestData(Integer.parseInt(values[0]), Integer
					.parseInt(values[1]), Float.parseFloat(values[2]),
					values[3].equals("t") ? true : false);
		}

		/**
		 * Get a string representation of the test data in the form that it
		 * could be parsed back into an instance, e.g. "1:1:1:t" for input=1,
		 * output=1, threshold=1, rejected.
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("%d:%.0f:%.0f:%s", inputScore, capacity,
					threshold, rejected ? "t" : "f");
		}
	}

}
