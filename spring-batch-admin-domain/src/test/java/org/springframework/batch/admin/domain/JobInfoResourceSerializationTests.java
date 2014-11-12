/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.batch.admin.domain;

import static org.junit.Assert.*;

import org.springframework.test.util.JsonPathExpectationsHelper;

/**
 * @author Michael Minella
 */
public class JobInfoResourceSerializationTests extends AbstractSerializationTests<JobInfoResource> {
	@Override
	public void assertJson(String json) throws Exception {
		new JsonPathExpectationsHelper("$.name").assertValue(json, "job1");
		new JsonPathExpectationsHelper("$.executionCount").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.jobInstanceId").assertValue(json, 2);
		new JsonPathExpectationsHelper("$.launchable").assertValue(json, true);
		new JsonPathExpectationsHelper("$.incrementable").assertValue(json, false);
	}

	@Override
	public void assertObject(JobInfoResource jobInfoResource) throws Exception {
		assertEquals("job1", jobInfoResource.getName());
		assertEquals(1, jobInfoResource.getExecutionCount());
		assertEquals(2l, (long) jobInfoResource.getJobInstanceId());
		assertTrue(jobInfoResource.isLaunchable());
		assertFalse(jobInfoResource.isIncrementable());
	}

	@Override
	public JobInfoResource getSerializationValue() {
		JobInfoResource resource = new JobInfoResource("job1", 1, 2l, true, false);
		return resource;
	}
}
