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

import org.springframework.batch.core.StepExecution;

/**
 * Manager for a thread local {@link StepExecution} instance. This is a key
 * enabler for monitoring and intervention in step executions. The methods
 * {@link #register(StepExecution)} and {@link #clear()} should be used in
 * conjunction (with clear in a finally block), so that other participants in
 * the step execution can use {@link #getStepExecution()} to find the current
 * instance.
 * 
 * @author Dave Syer
 * 
 */
public class StepExecutionSynchronizationManager {

	private static ThreadLocal<StepExecution> local = new ThreadLocal<StepExecution>();

	public static void register(StepExecution stepExecution) {
		local.set(stepExecution);
	}

	public static StepExecution getStepExecution() {
		return local.get();
	}

	public static void clear() {
		local.set(null);
	}

}
