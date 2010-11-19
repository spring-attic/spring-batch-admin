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
public class JdbcSearchableStepExecutionDaoTests {

	private JdbcSearchableStepExecutionDao dao;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryUtils;

	private JdbcSearchableJobExecutionDao jobExecutionDao;

	private List<JobExecution> list;

	@Autowired
	public void setDataSource(DataSource dataSource) throws Exception {
		dao = new JdbcSearchableStepExecutionDao();
		dao.setDataSource(dataSource);
		dao.afterPropertiesSet();
		jobExecutionDao = new JdbcSearchableJobExecutionDao();
		jobExecutionDao.setDataSource(dataSource);
		jobExecutionDao.afterPropertiesSet();
	}

	@BeforeTransaction
	public void prepareExecutions() throws Exception {
		jobRepositoryUtils.removeJobExecutions(jobExecutionDao.getJobExecutions(0, 1000));
		list = jobRepositoryUtils.createJobExecutions(3);
	}

	@AfterTransaction
	public void removeExecutions() throws Exception {
		jobRepositoryUtils.removeJobExecutions(list);
	}

	@Test
	@Transactional
	public void testFindStepNames() {
		assertEquals("[step]", dao.findStepNamesForJobExecution("job", "-").toString());
	}

	@Test
	@Transactional
	public void testFindStepNamesWithMoreJobs() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("other", new String[] {"step"}, 1));
		assertEquals("[step]", dao.findStepNamesForJobExecution("job", "-").toString());
	}

	@Test
	@Transactional
	public void testFindStepNamesWithMatch() {
		assertEquals("[]", dao.findStepNamesForJobExecution("job", "*").toString());
	}

	@Test
	@Transactional
	public void testFindStepExecutionsByName() {
		assertEquals(1, dao.findStepExecutions("job", "step", 2, 2).size());
	}

	@Test
	@Transactional
	public void testFindStepExecutionsByNameWithMoreJobs() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("other", new String[] {"step"}, 2));
		assertEquals(1, dao.findStepExecutions("job", "step", 2, 2).size());
	}

	@Test
	@Transactional
	public void testFindStepExecutionsByPattern() {
		assertEquals(1, dao.findStepExecutions("job", "s*", 2, 2).size());
	}

	@Test
	@Transactional
	public void testFindStepExecutionsPastEnd() {
		assertEquals(0, dao.findStepExecutions("job", "step", 100, 100).size());
	}

	@Test
	@Transactional
	public void testCountStepExecutionsByName() {
		assertEquals(3, dao.countStepExecutions("job", "step"));
	}

	@Test
	@Transactional
	public void testCountStepExecutionsByNameWithMoreJobs() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("other", new String[] {"step"}, 2));
		assertEquals(3, dao.countStepExecutions("job", "step"));
	}

	@Test
	@Transactional
	public void testCountStepExecutionsByPattern() {
		assertEquals(3, dao.countStepExecutions("job", "s*"));
	}

	@Test
	@Transactional
	public void testCountStepExecutionsByPatternWithMoreJobs() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("other", new String[] {"step"}, 2));
		assertEquals(3, dao.countStepExecutions("job", "s*"));
	}

}
