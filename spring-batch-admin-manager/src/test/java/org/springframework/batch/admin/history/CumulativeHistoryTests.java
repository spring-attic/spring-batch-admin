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
package org.springframework.batch.admin.history;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.springframework.batch.admin.domain.CumulativeHistory;

public class CumulativeHistoryTests {

	private CumulativeHistory history = new CumulativeHistory();

	@Test
	public void testUndefinedMean() {
		assertEquals(0, history.getMean(), 0.001);
		assertEquals(0, history.getStandardDeviation(), 0.001);
	}

	@Test
	public void testString() {
		history.append(1);
		history.append(2);
		history.append(3);

		String output = String.format("[N=%d, min=%f, max=%f, mean=%f, sigma=%f]", 3, 1.0f, 3.0f, 2.0f, 0.816497);

		assertEquals(output, history.toString());
	}

	@Test
	public void testCount() {
		assertEquals(0, history.getCount());
		history.append(1);
		history.append(2);
		history.append(3);
		assertEquals(3, history.getCount());
	}

	@Test
	public void testMean() {
		history.append(1);
		history.append(2);
		history.append(3);
		assertEquals(2, history.getMean(), 0.001);
	}

	@Test
	public void testMin() {
		history.append(1);
		history.append(2);
		history.append(3);
		assertEquals(1, history.getMin(), 0.001);
	}

	@Test
	public void testMax() {
		history.append(1);
		history.append(2);
		history.append(3);
		assertEquals(3, history.getMax(), 0.001);
	}

	@Test
	public void testStandardDeviation() {
		history.append(1);
		history.append(2);
		history.append(3);
		assertEquals(0.8165, history.getStandardDeviation(), 0.001);
	}

	@Test
	public void testTrivialStandardDeviation() {
		history.append(1);
		history.append(1);
		history.append(1);
		assertEquals(0, history.getStandardDeviation(), 0.001);
	}
}
