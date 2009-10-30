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
package org.springframework.batch.admin.launch;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class JobLauncherSynchronizerIntegrationTests {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job job;
	
	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;

	@Test
	public void testLaunch() throws Exception {
		jobRepositoryTestUtils.removeJobExecutions();
		jobLauncher.run(job, new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis())
				.toJobParameters());
	}

	@Test(expected=JobExecutionAlreadyRunningException.class)
	public void testLaunchWithJobRunning() throws Exception {
		List<JobExecution> list = jobRepositoryTestUtils.createJobExecutions("test-job", new String[0], 1);
		try {
			jobLauncher.run(job, new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis())
				.toJobParameters());
		} catch (JobExecutionAlreadyRunningException e) {
			// expected
			throw e;
		} finally {
			jobRepositoryTestUtils.removeJobExecutions(list);
		}
	}

}
