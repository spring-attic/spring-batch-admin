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

import java.util.List;
import java.util.Map;

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
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={"JobIntegrationTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class StagingJobTests {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job job;

	private SimpleJdbcTemplate jdbcTemplate;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	@Test
	@DirtiesContext
	public void testSmallLaunch() throws Exception {

		int before = jdbcTemplate.queryForInt("SELECT COUNT(1) from LEAD_INPUTS");

		JobParameters jobParameters = new JobParametersBuilder().addString("input.file", "classpath:data/test.txt")
				.addLong("timestamp", System.currentTimeMillis()).toJobParameters();
		JobExecution jobExecution = jobLauncher.run(job, jobParameters);

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

		List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT * from LEAD_INPUTS");
		assertEquals(7, result.size() - before);
		// System.err.println(result);

	}
}
