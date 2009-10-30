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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.NoSuchStepException;

/**
 * {@link StepService} wrapper that adjusts the threshold in the
 * {@link StepExecutionRequest} to dynamically account for information returned
 * about the load on the service. Usually the delegate service is a proxy for a
 * remote clustered service which itself is statically balanced (e.g. a round
 * robin).<br/>
 * <br/>
 * 
 * Heterogeneous clusters should balance naturally, as long as the
 * {@link StepExecutionResponse} contains accurate information about the
 * remaining capacity on the node that processes the request.<br/>
 * <br/>
 * 
 * Create a new {@link BalancedStepServiceHandler} per request, or use it in
 * step scope, to prevent the score from accumulating without limit.
 * 
 * @author Dave Syer
 * 
 */
public class BalancedStepServiceHandler {

	private Log logger = LogFactory.getLog(getClass());

	private float score = 0;

	private float rejectionFrequency = 1;

	private int requestCount = 0;

	private int currentSuccessful = 0;

	private int currentRejected = 0;

	private int rejectedCount;

	private int threshold = 1;

	/**
	 * Set the initial value for the estimated rejection frequency statistic
	 * (defaults to 1, but is more accurate if set to the actual grid size in
	 * the round-robin case).
	 * 
	 * @param rejectionFrequency
	 */
	public void setRejectionFrequencyInitialEstimate(int rejectionFrequency) {
		this.rejectionFrequency = rejectionFrequency;
	}

	/**
	 * Execute the request using the delegate service. Adjusts the estimates of
	 * the current state of the cluster dynamically and throws an exception if
	 * the request is rejected. Clients can retry the
	 * {@link StepExecutionRequestRejectedException} automatically, assuming
	 * that a retry will eventually send the request to another node in the
	 * cluster which isn't as busy. After two rejections, the threshold for new
	 * requests is increased taking into account information from the failed and
	 * successful requests. In making the adjustment only includes results
	 * collected since the threshold was last updated.
	 * 
	 * @see StepService#execute(StepExecutionRequest)
	 * 
	 * @throws StepExecutionRequestRejectedException if the request is rejected
	 * @throws NoSuchStepExecutionException if the delegate service does
	 * @throws NoSuchStepException if the {@link Step} specified by name in the
	 * request cannot be located
	 */
	public void handle(StepService stepService, StepExecutionRequest request) throws NoSuchStepExecutionException,
			NoSuchStepException {

		initializeWrapper(request);

		logger.debug(String.format("Requesting: [%s]", request));

		// The service sends back a result even if it is overloaded
		StepExecutionResponse result = stepService.execute(request);
		logger.debug(String.format("Request responded (%s): [%s] from [%s]", this, result, request));

		// ...but if it hasn't done any processing we should force a retry
		if (result.isRejected()) {

			registerRejection(result);
			logger.debug(String.format("New threshold: " + this));
			throw new StepExecutionRequestRejectedException("StepService was busy.  Please try again later.");

		}

		registerSuccess(result);

	}

	/**
	 * @return the current value of the threshold for new requests
	 */
	public float getThreshold() {
		return calculateThreshold();
	}

	/**
	 * @return the cumulative total number of rejected requests
	 */
	public int getRejectedCount() {
		return rejectedCount;
	}

	/**
	 * @return the cumulative total number of requests
	 */
	public int getRequestCount() {
		return requestCount;
	}

	/**
	 * @return the current estimate of the rejection frequency
	 */
	public float getRejectionFrequency() {
		return rejectionFrequency;
	}

	/**
	 * @return the cumulative total score used to calculate the threshold
	 */
	public float getScore() {
		return score;
	}

	/**
	 * Update estimates of current state of grid given that a request was
	 * accepted successfully.
	 * 
	 * @param result
	 */
	private void registerSuccess(StepExecutionResponse result) {
		updateScores(-result.getCapacity());
		requestCount++;
		currentSuccessful++;
	}

	/**
	 * Update estimates of current state of grid, given that there was a
	 * rejection.
	 */
	private void registerRejection(StepExecutionResponse result) {
		updateScores(1 - result.getCapacity());
		rejectionFrequency = (rejectionFrequency * rejectedCount + currentSuccessful) / (rejectedCount + 1);
		if (rejectionFrequency < 1) {
			rejectionFrequency = 1;
		}
		currentRejected++;
		rejectedCount++;
		requestCount++;
		currentSuccessful = 0;
	}

	/**
	 * Set up the threshold for the request and update estimates of grid size if
	 * necessary.
	 * 
	 * @param wrapper the current request to be enhanced
	 * @return the threshold that was used in the wrapper
	 */
	private float initializeWrapper(StepExecutionRequest wrapper) {
		float threshold = calculateThreshold();
		wrapper.setThreshold(threshold);
		return threshold;
	}

	private void updateScores(float score) {
		this.score += score;
		if (this.score < 0) {
			this.score = 0;
		}
	}

	private float calculateThreshold() {

		/*
		 * Waiting for at least the second rejection gives some smoothing in the
		 * case that the load is balanced randomly, and when the grid is
		 * randomly loaded when the step starts. In the (opposite) round robin
		 * with equal loads case it results in an expected rejection frequency
		 * of 2/(g+2) where g is the real grid size (not the estimate).
		 */
		if (currentRejected >= 2) {

			int delta = (int) Math.round(score / currentRejected);
			if (delta >= 1) {
				score = 0;
				threshold += delta;
			}

			currentRejected = 0;

		}

		return threshold;

	}

	@Override
	public String toString() {
		return String.format("%s: threshold=%f, rejectionFrequency=%f, requestCount=%d, rejectedCount=%d", getClass()
				.getSimpleName(), getThreshold(), rejectionFrequency, requestCount, rejectedCount);
	}

}
