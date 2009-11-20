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
package org.springframework.batch.admin.partition.remote.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class WebServiceJobExecutionTests {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job job;

	private static ConfigurableApplicationContext context;

	@BeforeClass
	public static void start() {
		// This has to start before the driver context that runs the job, so
		// that the services are already available and can be connected to...
		context = new ClassPathXmlApplicationContext("/service-context.xml");
	}

	@AfterClass
	public static void stop() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void testSimpleProperties() throws Exception {
		assertNotNull(jobLauncher);
	}

	@Test
	public void testLaunchJob() throws Exception {
		JobExecution jobExecution = jobLauncher.run(job,
				new JobParametersBuilder().addString("test.id",
						getClass().getSimpleName()).toJobParameters());
		assertNotNull(jobExecution);
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}

}
