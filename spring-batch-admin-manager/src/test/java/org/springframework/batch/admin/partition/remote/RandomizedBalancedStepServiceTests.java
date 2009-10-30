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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.batch.admin.partition.remote.BalancedStepServiceHandler;
import org.springframework.batch.admin.partition.remote.NoSuchStepExecutionException;
import org.springframework.batch.admin.partition.remote.StepExecutionRequest;
import org.springframework.batch.admin.partition.remote.StepExecutionRequestRejectedException;
import org.springframework.batch.admin.partition.remote.StepExecutionResponse;
import org.springframework.batch.admin.partition.remote.StepService;
import org.springframework.batch.core.step.NoSuchStepException;

@RunWith(Parameterized.class)
public class RandomizedBalancedStepServiceTests {

	private Log logger = LogFactory.getLog(getClass());

	private enum PoolType {
		ROUND_ROBIN, RANDOM;
	}

	private BalancedStepServiceHandler service = new BalancedStepServiceHandler();

	private StubPooledStepService pooledService;

	private final OutputData data;

	private final int maxIterations;

	@Parameters
	public static List<Object[]> data() {

		List<Object[]> params = new ArrayList<Object[]>();

		// 0:
		params.add(new Object[] { new InputData(1, 1, 100, PoolType.ROUND_ROBIN), new OutputData(0.1, 66) });

		// 1:
		params.add(new Object[] { new InputData(3, 3, 100, PoolType.ROUND_ROBIN), new OutputData(0.1, 40) });

		// 2:
		params.add(new Object[] { new InputData(1, 1, 100, PoolType.RANDOM), new OutputData(0.1, 66) });

		// 3:
		params.add(new Object[] { new InputData(3, 3, 100, PoolType.RANDOM), new OutputData(0.1, 50) });

		// 4:
		params.add(new Object[] { new InputData(3, 1, 100, PoolType.RANDOM), new OutputData(0.1, 50) });

		// 5:
		params.add(new Object[] { new InputData(12, 12, 300, PoolType.RANDOM), new OutputData(0.25, 150) });

		// 6:
		params.add(new Object[] { new InputData(12, 1, 300, PoolType.RANDOM), new OutputData(0.25, 150) });

		// 7:
		params.add(new Object[] { new InputData(12, 1, 100, PoolType.ROUND_ROBIN, 10), new OutputData(0.1, 25) });

		// 8:
		params.add(new Object[] { new InputData(12, 1, 100, PoolType.RANDOM, 10), new OutputData(0.25, 50) });

		// 9:
		params.add(new Object[] { new InputData(3, 3, 100, PoolType.ROUND_ROBIN, new float[] { 1, 2, 1 }),
				new OutputData(0.1, 50) });

		return params;

	}

	public RandomizedBalancedStepServiceTests(InputData inputData, OutputData output) {
		this.maxIterations = inputData.maxIterations;
		this.data = output;
		pooledService = new StubPooledStepService(inputData.poolType, inputData.gridSize, inputData.initialScore, inputData.weights);
		service.setRejectionFrequencyInitialEstimate(inputData.gridEstimate);
	}

	@Test
	public void testRepetitiveCallsToBalancedService() throws Exception {

		double tolerance = data.tolerance;

		for (int i = 0; i < maxIterations; i++) {

			try {
				service.handle(pooledService, new StepExecutionRequest());
			}
			catch (StepExecutionRequestRejectedException e) {
			}

		}

		logger.info("Rejected counts: " + Arrays.toString(pooledService.getRejectedCounts()));
		logger.info("Scores: " + Arrays.toString(pooledService.getScores()));

		pooledService.assertFairness(tolerance);

		assertTrue("Rejected count should be greater than zero: " + service, 0 < service.getRejectedCount());
		assertTrue("Rejected count should be smaller: " + service, data.rejectedCount * (1 + tolerance) >= service
				.getRejectedCount());

	}

	private static class StubPooledStepService extends StepServiceSupport {

		private int count = 0;

		private PoolType poolType;

		private StubStepService[] services;

		private final float[] weights;

		public StubPooledStepService(PoolType poolType, int gridSize, int initialScore, float[] weights) {
			super();
			this.poolType = poolType;
			this.weights = weights;
			services = new StubStepService[gridSize];
			for (int i = 0; i < services.length; i++) {
				float weight = 1;
				if (weights!=null && i<weights.length) {
					weight = weights[i];
				}
				services[i] = new StubStepService(initialScore, weight);
			}
		}

		public StepExecutionResponse execute(StepExecutionRequest wrapper) throws NoSuchStepExecutionException, NoSuchStepException {
			if (poolType == PoolType.ROUND_ROBIN) {
				count = (++count) % services.length;
			}
			else {
				count = (int) (Math.random() * services.length);
			}
			StepService service = services[count];
			return service.execute(wrapper);
		}

		public int[] getRejectedCounts() {
			int[] result = new int[services.length];
			for (int i = 0; i < result.length; i++) {
				result[i] = services[i].rejectedCount;
			}
			return result;
		}

		public int[] getScores() {
			int[] result = new int[services.length];
			for (int i = 0; i < result.length; i++) {
				result[i] = services[i].score;
			}
			return result;
		}

		public void assertFairness(double tolerance) {

			int[] scores = getScores();

			double max = 0;
			double min = Double.MAX_VALUE;
			double avg = 0;
			double sqs = 0;
			for (int i = 0; i < scores.length; i++) {
				float weight = 1;
				if (weights!=null && i<weights.length) {
					weight = weights[i];
				}
				float score = scores[i]/weight;
				max = Math.max(max, score);
				min = Math.min(min, score);
				avg += score;
				sqs += score*score;
			}
			avg /= scores.length;
			sqs /= scores.length;

			double sigma = Math.sqrt(sqs - avg*avg);
			assertTrue("Score out of tolerance: " + Arrays.toString(scores), sigma/avg <= tolerance);

		}

	}

	private static class StubStepService extends StepServiceSupport {

		private int score = 0;

		private int rejectedCount = 0;

		private final float weight;

		public StubStepService(int score, float weight) {
			super();
			this.score = score;
			this.weight = weight;
		}

		public StepExecutionResponse execute(StepExecutionRequest wrapper) {
			boolean rejected = (score + 1) > wrapper.getThreshold() * weight;
			if (!rejected) {
				score++;
			}
			else {
				rejectedCount++;
			}
			return new StepExecutionResponse(wrapper.getThreshold() * weight - score, rejected);
		}

	}

	private static class InputData {

		private final int gridEstimate;

		private final int gridSize;

		private final int maxIterations;

		private final PoolType poolType;

		private final int initialScore;

		private final float[] weights;

		public InputData(int gridSize, int gridEstimate, int maxIterations, PoolType poolType) {
			this(gridSize, gridEstimate, maxIterations, poolType, 0);
		}

		public InputData(int gridSize, int gridEstimate, int maxIterations, PoolType poolType, int initialScore) {
			this(gridSize, gridEstimate, maxIterations, poolType, initialScore, null);
		}

		public InputData(int gridSize, int gridEstimate, int maxIterations, PoolType poolType, float[] weights) {
			this(gridSize, gridEstimate, maxIterations, poolType, 0, weights);
		}
		
		public InputData(int gridSize, int gridEstimate, int maxIterations, PoolType poolType, int initialScore, float[] weights) {
			this.gridEstimate = gridEstimate;
			this.gridSize = gridSize;
			this.maxIterations = maxIterations;
			this.poolType = poolType;
			this.initialScore = initialScore;
			this.weights = weights;
		}

	}

	private static class OutputData {

		private final double tolerance;

		private final int rejectedCount;

		public OutputData(double tolerance, int rejectedCount) {
			this.tolerance = tolerance;
			this.rejectedCount = rejectedCount;
		}

	}

}
