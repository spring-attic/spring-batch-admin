/*
 * Copyright 2006-2013 the original author or authors.
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
package org.springframework.batch.admin.service;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Dave Syer
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-config.xml")
public class JdbcSearchableJobInstanceDaoTests {

	private JdbcSearchableJobInstanceDao dao;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryUtils;

	private List<JobExecution> list;

	@Autowired
	public void setDataSource(DataSource dataSource) throws Exception {
		dao = new JdbcSearchableJobInstanceDao();
		dao.setJdbcTemplate(new JdbcTemplate(dataSource));
		dao.afterPropertiesSet();
	}

	// Need to use @BeforeTransaction because the job repository defaults to
	// propagation=REQUIRES_NEW for createJobExecution()
	@BeforeTransaction
	public void prepareExecutions() throws Exception {
		list = jobRepositoryUtils.createJobExecutions(3);
	}

	@AfterTransaction
	public void removeExecutions() throws Exception {
		jobRepositoryUtils.removeJobExecutions(list);
	}

	@Test
	@Transactional
	public void testGetJobInstancesByName() {
		assertEquals(3, dao.getJobInstances("job", 0, 10).size());
	}

	@Test
	@Transactional
	public void testCountJobInstancesByName() {
		assertEquals(3, dao.countJobInstances("job"));
	}

	@Test
	@Transactional
	public void testGetJobInstancesByNamePaged() {
		List<JobInstance> jobInstances = dao.getJobInstances("job", 2, 2);
		assertEquals(1, jobInstances.size());
		assertEquals(list.get(0), jobInstances.get(0));
	}

}
