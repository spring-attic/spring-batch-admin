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

import java.io.Serializable;

public class StepExecutionResponse implements Serializable {

	private boolean rejected;

	private float capacity;

	public StepExecutionResponse(float capacity, boolean rejected) {
		super();
		this.rejected = rejected;
		this.capacity = capacity;
	}

	public StepExecutionResponse(float capacity) {
		this(capacity, false);
	}

	public boolean isRejected() {
		return rejected;
	}

	public void setRejected(boolean rejected) {
		this.rejected = rejected;
	}

	public float getCapacity() {
		return capacity;
	}
	
	public void setCapacity(float capacity) {
		this.capacity = capacity;
	}
	
	@Override
	public String toString() {
		return String.format("%s: rejected=%b, capacity=%f", getClass().getSimpleName(), rejected, capacity);
	}

}
