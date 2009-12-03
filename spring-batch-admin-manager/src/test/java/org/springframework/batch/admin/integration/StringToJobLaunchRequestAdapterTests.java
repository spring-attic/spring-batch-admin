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
package org.springframework.batch.admin.integration;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.integration.launch.JobLaunchRequest;

public class StringToJobLaunchRequestAdapterTests {
	
	private StringToJobLaunchRequestAdapter adapter = new StringToJobLaunchRequestAdapter();
	private MapJobRegistry jobRegistry = new MapJobRegistry();
	
	@Before
	public void init() throws Exception {
		jobRegistry.register(new ReferenceJobFactory(new SimpleJob("foo")));		
		jobRegistry.register(new ReferenceJobFactory(new SimpleJob("foo-bar")));		
		adapter.setJobLocator(jobRegistry);
	}
	
	@Test
	public void testSimpleJob() throws Exception {
		JobLaunchRequest request = adapter.adapt("foo");
		assertEquals("foo", request.getJob().getName());
	}

	@Test
	public void testSimpleJobWithHyphen() throws Exception {
		JobLaunchRequest request = adapter.adapt("foo-bar");
		assertEquals("foo-bar", request.getJob().getName());
	}

	@Test
	public void testJobParameter() throws Exception {
		JobLaunchRequest request = adapter.adapt("foo[bar=spam]");
		assertEquals("foo", request.getJob().getName());
		assertEquals(1, request.getJobParameters().getParameters().size());
		assertEquals("spam", request.getJobParameters().getString("bar"));
	}

	@Test
	public void testJobParameters() throws Exception {
		JobLaunchRequest request = adapter.adapt("foo[bar=spam,count(long)=123]");
		assertEquals("foo", request.getJob().getName());
		assertEquals(2, request.getJobParameters().getParameters().size());
		assertEquals("spam", request.getJobParameters().getString("bar"));
		assertEquals(123, request.getJobParameters().getLong("count"));
	}

}
