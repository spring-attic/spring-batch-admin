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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.admin.service.SearchableStepExecutionDao;
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

	@Autowired
	private SearchableStepExecutionDao dao;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryUtils;

	@Autowired
	private SearchableJobExecutionDao jobExecutionDao;

	private List<JobExecution> list;

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
	public void testFindStepNamesWithMatch() {
		assertEquals("[]", dao.findStepNamesForJobExecution("job", "*").toString());
	}

	@Test
	@Transactional
	public void testFindStepExecutionsByName() {
		assertEquals(1, dao.findStepExecutions("step", 2, 2).size());
	}

	@Test
	@Transactional
	public void testFindStepExecutionsByPattern() {
		assertEquals(1, dao.findStepExecutions("s*", 2, 2).size());
	}

	@Test
	@Transactional
	public void testCountStepExecutionsByName() {
		assertEquals(3, dao.countStepExecutions("step"));
	}

	@Test
	@Transactional
	public void testCountStepExecutionsByPattern() {
		assertEquals(3, dao.countStepExecutions("s*"));
	}

}
