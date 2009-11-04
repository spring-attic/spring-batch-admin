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
package org.springframework.batch.admin.sample;

import org.springframework.batch.item.ItemReader;

/**
 * {@link ItemReader} with hard-coded input data.
 */
public class ExampleItemReader implements ItemReader<String> {

	private static final int MAX_OUTER = 0;

	private String[] input = { "Hello", "world!", "Wow", "that's", "cool!" };

	private int index = 0;

	private int outer = 0;

	/**
	 * Reads next record from input
	 */
	public synchronized String read() throws Exception {
		if (index >= input.length) {
			outer++;
			if (outer > MAX_OUTER) {
				return null;
			} else {
				index = 0;
			}
		}
		return input[index++];
	}

}
