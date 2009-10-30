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

/**
 * Response from a {@link StepService} to indicate  
 * 
 * @author Dave Syer
 *
 */
public class StepServiceStatus implements Serializable {
	
	private final boolean authoritative;
	private final float load;
	private final boolean available;

	public StepServiceStatus(float load, boolean authoritative) {
		this(true, load, authoritative);
	}
	
	public StepServiceStatus(float load) {
		this(true, load, true);
	}
	
	/**
	 * @param available true if the service is available
	 * @param load an estimate of the load on the service (number of executing tasks)
	 * @param authoritative false if the load figure is a complete guess
	 */
	public StepServiceStatus(boolean available, float load, boolean authoritative) {
		this.available = available;
		this.load = load;
		this.authoritative = authoritative;
	}
	
	public float getLoad() {
		return load;
	}
	
	public boolean isAuthoritative() {
		return authoritative;
	}
	
	public boolean isAvailable() {
		return available;
	}

}
