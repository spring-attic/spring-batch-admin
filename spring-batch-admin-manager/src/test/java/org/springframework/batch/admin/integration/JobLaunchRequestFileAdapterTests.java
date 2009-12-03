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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

public class JobLaunchRequestFileAdapterTests {

	private FileToJobLaunchRequestAdapter adapter = new FileToJobLaunchRequestAdapter();

	@Before
	public void init() throws Exception {
		adapter.setJob(new SimpleJob("foo"));
	}

	@Test
	public void testSimpleJob() throws Exception {
		JobLaunchRequest request = adapter.adapt(new File("src/test/resources/data/test.txt"));
		assertEquals("foo", request.getJob().getName());
		String fileName = request.getJobParameters().getString("input.file");
		Resource resource = new DefaultResourceLoader().getResource(fileName);
		assertTrue("File does not exist: " + fileName, resource.exists());
		assertNotNull("File is empty", IOUtils.toString(resource.getInputStream()));
	}

}
