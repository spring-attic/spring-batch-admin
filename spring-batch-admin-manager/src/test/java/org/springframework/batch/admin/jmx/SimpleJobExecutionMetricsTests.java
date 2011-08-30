/*
 * Copyright 2006-2010 the original author or authors.
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

package org.springframework.batch.admin.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

/**
 * @author Dave Syer
 * 
 */
public class SimpleJobExecutionMetricsTests {

	private SimpleJobExecutionMetrics metrics;

	private JobService jobService = EasyMock.createMock(JobService.class);

	private JobExecution jobExecution;

	private JobExecution earlierExecution;

	@Before
	public void init() throws Exception {

		earlierExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(122L, Arrays.asList("step"));
		earlierExecution.setStatus(BatchStatus.FAILED);
		earlierExecution.setExitStatus(ExitStatus.FAILED);
		earlierExecution.setStartTime(new Date());
		earlierExecution.setEndTime(new Date(earlierExecution.getStartTime().getTime() + 100));
		assertFalse(earlierExecution.isRunning());

		jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(123L, Arrays.asList("first","step"));
		jobExecution.setStatus(BatchStatus.COMPLETED);
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		jobExecution.setStartTime(new Date());
		jobExecution.setEndTime(new Date(earlierExecution.getEndTime().getTime() + 100));
		assertFalse(jobExecution.isRunning());

		Iterator<StepExecution> iterator = jobExecution.getStepExecutions().iterator();
		iterator.next();
		StepExecution stepExecution = iterator.next();
		stepExecution.setStatus(BatchStatus.COMPLETED);
		stepExecution.setExitStatus(ExitStatus.COMPLETED.addExitDescription("Foo"));

		metrics = new SimpleJobExecutionMetrics(jobService, "job");

	}
	
	@After
	public void verify() {
		EasyMock.verify(jobService);
	}

	private void prepareServiceWithSingleJobExecution() throws Exception {
		jobService.listJobExecutionsForJob("job", 0, 4);
		EasyMock.expectLastCall().andReturn(Arrays.asList(jobExecution));
		EasyMock.replay(jobService);
	}

	private void prepareServiceWithMultipleJobExecutions(int total) throws Exception {
		jobService.listJobExecutionsForJob("job", 0, total);
		EasyMock.expectLastCall().andReturn(Arrays.asList(earlierExecution, jobExecution));
		EasyMock.replay(jobService);
	}

	private void prepareServiceWithMultipleJobExecutions() throws Exception {
		jobService.listJobExecutionsForJob("job", 0, 100);
		EasyMock.expectLastCall().andReturn(Arrays.asList(jobExecution, earlierExecution));
		jobService.listJobExecutionsForJob("job", 100, 100);
		EasyMock.expectLastCall().andReturn(Arrays.asList());
		EasyMock.replay(jobService);
	}

	@Test
	public void testGetJobExecutionCount() throws Exception {
		jobService.countJobExecutionsForJob("job");
		EasyMock.expectLastCall().andReturn(10);
		EasyMock.replay(jobService);
		assertEquals(10, metrics.getExecutionCount());
	}

	@Test
	public void testGetJobExecutionFailureCount() throws Exception {
		prepareServiceWithMultipleJobExecutions();
		assertEquals(1, metrics.getFailureCount());
	}

	@Test
	public void testGetLatestJobExecutionDuration() throws Exception {
		prepareServiceWithMultipleJobExecutions(10);
		assertEquals(jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime(),
				metrics.getMaxDuration(), .001);
	}

	@Test
	public void testGetMeanJobExecutionDuration() throws Exception {
		prepareServiceWithMultipleJobExecutions(10);
		assertEquals(jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime(),
				metrics.getMaxDuration(), .001);
	}

	@Test
	public void testGetMaxJobExecutionDuration() throws Exception {
		prepareServiceWithMultipleJobExecutions(10);
		assertEquals(jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime(),
				metrics.getMaxDuration(), .001);
	}

	@Test
	public void testGetLatestJobExecutionStartTime() throws Exception {
		prepareServiceWithSingleJobExecution();
		assertEquals(jobExecution.getStartTime(), metrics.getLatestStartTime());
	}

	@Test
	public void testGetLatestJobExecutionEndTime() throws Exception {
		prepareServiceWithSingleJobExecution();
		assertEquals(jobExecution.getEndTime(), metrics.getLatestEndTime());
	}

	@Test
	public void testGetLatestJobExecutionExitCode() throws Exception {
		prepareServiceWithSingleJobExecution();
		assertEquals("COMPLETED", metrics.getLatestExitCode());
	}

	@Test
	public void testGetLatestJobExecutionStatus() throws Exception {
		prepareServiceWithSingleJobExecution();
		assertEquals("COMPLETED", metrics.getLatestStatus());
	}

	@Test
	public void testGetLatestJobExecutionLastStepExitDescription() throws Exception {
		prepareServiceWithSingleJobExecution();
		assertEquals("Foo", metrics.getLatestStepExitDescription());
	}

	@Test
	public void testGetLatestJobExecutionWhenTied() throws Exception {
		prepareServiceWithMultipleJobExecutions(4);
		earlierExecution.setCreateTime(new Date(jobExecution.getCreateTime().getTime()));
		assertEquals("COMPLETED", metrics.getLatestStatus());
	}

	@Test
	public void testGetLatestJobExecutionLastStepName() throws Exception {
		prepareServiceWithSingleJobExecution();
		assertEquals("step", metrics.getLatestStepName());
	}

	@Test
	public void testIsJobExecutionCurrentlyRunning() throws Exception {
		prepareServiceWithSingleJobExecution();
		assertEquals(false, metrics.isJobRunning());
	}

}
