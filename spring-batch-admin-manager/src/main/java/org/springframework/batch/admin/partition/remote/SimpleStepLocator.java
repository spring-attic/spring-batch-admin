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

import java.util.Arrays;
import java.util.Collection;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.core.step.StepLocator;

public class SimpleStepLocator implements StepLocator {

	private Step step;

	public SimpleStepLocator() {
	}

	public SimpleStepLocator(Step step) {
		this.step = step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	/**
	 * Fetch the step by name. There is only one step in this implementation, so
	 * it either returns successfully with that step instance, or throws an
	 * exception.
	 * 
	 * @throws NoSuchStepException if the step name given is wrong
	 */
	public Step getStep(String name) throws NoSuchStepException {
		if (name == null || !name.equals(step.getName())) {
			throw new NoSuchStepException(String.format(
					"No step with that name was registered [%s].  Did you mean [%s]?", name, step.getName()));
		}
		return step;
	}

	/**
	 * The name of the step wrapped in a list.
	 * 
	 * @return the name of the step
	 */
	public Collection<String> getStepNames() {
		return Arrays.asList(step.getName());
	}

}
