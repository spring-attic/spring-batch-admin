/*
 * Copyright 2006-2007 the original author or authors.
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
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration("/test-config.xml")
public class JdbcSearchableJobExecutionDaoTests {

	private JdbcSearchableJobExecutionDao dao;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryUtils;

	private List<JobExecution> list;

	@Autowired
	public void setDataSource(DataSource dataSource) throws Exception {
		dao = new JdbcSearchableJobExecutionDao();
		dao.setDataSource(dataSource);
		dao.afterPropertiesSet();
	}

	@BeforeTransaction
	public void prepareExecutions() throws Exception {
		jobRepositoryUtils.removeJobExecutions();
		list = jobRepositoryUtils.createJobExecutions(3);
	}

	@AfterTransaction
	public void removeExecutions() throws Exception {
		jobRepositoryUtils.removeJobExecutions(list);
	}

	@Test
	@Transactional
	public void testCountJobExecutions() {
		assertEquals(3, dao.countJobExecutions());
	}

	@Test
	@Transactional
	public void testGetJobExecutions() {
		List<JobExecution> jobExecutions = dao.getJobExecutions(0, 10);
		assertEquals(3, jobExecutions.size());
		assertNotNull(jobExecutions.get(0).getJobInstance());
	}

	@Test
	@Transactional
	public void testGetJobExecutionsPaged() {
		List<JobExecution> jobExecutions = dao.getJobExecutions(2, 2);
		assertEquals(1, jobExecutions.size());
		assertEquals(list.get(0), jobExecutions.get(0));
	}

	@Test
	@Transactional
	public void testGetJobExecutionsLatest() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("other", new String[] {"step"}, 1));
		List<JobExecution> jobExecutions = dao.getJobExecutions(0, 10);
		assertEquals(4, jobExecutions.size());
		assertEquals(list.get(list.size()-1), jobExecutions.get(0));
	}

	@Test
	@Transactional
	public void testGetJobExecutionsLatestForJob() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("job", new String[] {"step"}, 1));
		List<JobExecution> jobExecutions = dao.getJobExecutions("job", 0, 1);
		assertEquals(1, jobExecutions.size());
		assertEquals(list.get(list.size()-1), jobExecutions.get(0));
	}

	@Test
	@Transactional
	public void testGetJobExecutionsByName() {
		assertEquals(3, dao.getJobExecutions("job", 0, 10).size());
	}

	@Test
	@Transactional
	public void testCountJobExecutionsByName() {
		assertEquals(3, dao.countJobExecutions("job"));
	}

	@Test
	@Transactional
	public void testGetJobExecutionsByNamePaged() {
		List<JobExecution> jobExecutions = dao.getJobExecutions("job", 2, 2);
		assertEquals(1, jobExecutions.size());
		assertEquals(list.get(0), jobExecutions.get(0));
	}

	@Test
	@Transactional
	public void testGetJobExecutionsPastEnd() {
		List<JobExecution> jobExecutions = dao.getJobExecutions("job", 100, 100);
		assertEquals(0, jobExecutions.size());
	}

	@Test
	@Transactional
	public void testGetJobExecutionsByNamePastEnd() {
		List<JobExecution> jobExecutions = dao.getJobExecutions(100, 100);
		assertEquals(0, jobExecutions.size());
	}

}
