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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.configuration.support.DefaultJobLoader;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ByteArrayResource;

public class JobConfigurationRequestLoaderTests {

	private JobConfigurationResourceLoader loader = new JobConfigurationResourceLoader();

	@Before
	public void setUp() {
		loader.setJobLoader(new DefaultJobLoader(new MapJobRegistry()));
	}

	@Test
	public void testLoadJobs() throws Exception {
		String jobNames = loader.loadJobs(new ByteArrayResource(JOB_XML.getBytes(),
				"http://localhost/jobs/configurations"));
		assertEquals("Registered jobs: [job]", jobNames.toString());
	}

	@Test
	public void testParentApplicationContext() throws Exception {
		StaticApplicationContext parent = new StaticApplicationContext();
		parent.refresh();
		loader.setApplicationContext(parent);
		String jobNames = loader.loadJobs(new ByteArrayResource(JOB_XML.getBytes(),
				"http://localhost/jobs/configurations"));
		assertEquals("Registered jobs: [job]", jobNames.toString());
	}

	private static final String JOB_XML = String
			.format(
					"<beans xmlns='http://www.springframework.org/schema/beans' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
							+ "xsi:schemaLocation='http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd'><bean class='%s$StubJob'/></beans>",
					JobConfigurationRequestLoaderTests.class.getName());

	public static class StubJob implements Job {

		public void execute(JobExecution execution) {
		}

		public JobParametersIncrementer getJobParametersIncrementer() {
			return null;
		}

		public String getName() {
			return "job";
		}

		public boolean isRestartable() {
			return false;
		}

	}

}
