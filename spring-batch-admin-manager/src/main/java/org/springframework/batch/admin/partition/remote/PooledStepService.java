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

import java.util.List;

import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PooledStepService implements StepService {

	private volatile int current;

	private Object lock = new Object();

	private List<StepService> stepServices;

	@Autowired
	public void setStepServices(List<StepService> stepServices) {
		this.stepServices = stepServices;
	}
	
	public StepServiceStatus getStatus() {
		float score = 0;
		for (StepService stepService : stepServices) {
			score += stepService.getStatus().getLoad();
		}
		return new StepServiceStatus(score);
	}

	public StepExecutionResponse execute(StepExecutionRequest wrapper) throws NoSuchStepExecutionException, NoSuchStepException {

		StepService stepService = stepServices.get(current);

		StepExecutionResponse result = stepService.execute(wrapper);
		synchronized (lock) {
			current++;
			if (current >= stepServices.size()) {
				current = 0;
			}
		}

		return result;

	}

}
