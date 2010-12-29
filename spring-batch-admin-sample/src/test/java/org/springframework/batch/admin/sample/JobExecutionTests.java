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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class JobExecutionTests {

	@Autowired
	private JobLauncher jobLauncher;

	private JobParameters jobParameters = new JobParametersBuilder().addString("fail", "false").toJobParameters();

	@Autowired
	@Qualifier("job1")
	private Job job1;

	private SimpleJdbcTemplate jdbcTemplate;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	@Test
	public void testSimpleProperties() throws Exception {
		assertNotNull(jobLauncher);
	}

	@Test
	public void testLaunchJob() throws Exception {
		int before = SimpleJdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		JobExecution jobExecution = jobLauncher.run(job1, jobParameters);
		assertNotNull(jobExecution);
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		int after = SimpleJdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		assertEquals(before + 1, after);
	}

	@Test
	public void testFailedJob() throws Exception {
		int before = SimpleJdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		jobParameters = new JobParametersBuilder().addString("fail", "true").toJobParameters();
		JobExecution jobExecution = jobLauncher.run(job1, jobParameters);
		assertNotNull(jobExecution);
		assertEquals(BatchStatus.FAILED, jobExecution.getStatus());
		int after = SimpleJdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		assertEquals(before + 1, after);
	}

	@Test
	public void testLaunchTwoJobs() throws Exception {
		int before = SimpleJdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		long count = 0;
		JobExecution jobExecution1 = jobLauncher.run(job1, new JobParametersBuilder(jobParameters).addLong("run.id", count++)
				.toJobParameters());
		JobExecution jobExecution2 = jobLauncher.run(job1, new JobParametersBuilder(jobParameters).addLong("run.id", count++)
				.toJobParameters());
		assertEquals(BatchStatus.COMPLETED, jobExecution1.getStatus());
		assertEquals(BatchStatus.COMPLETED, jobExecution2.getStatus());
		int after = SimpleJdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		assertEquals(before + 2, after);
	}

}
