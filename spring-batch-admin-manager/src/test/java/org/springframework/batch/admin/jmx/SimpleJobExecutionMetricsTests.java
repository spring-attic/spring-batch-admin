/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

	@Mock
	private JobService jobService;

	private JobExecution jobExecution;

	private JobExecution earlierExecution;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

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

	private void prepareServiceWithSingleJobExecution() throws Exception {
		when(jobService.listJobExecutionsForJob("job", 0, 4)).thenReturn(Arrays.asList(jobExecution));
	}

	private void prepareServiceWithMultipleJobExecutions(int total) throws Exception {
		when(jobService.listJobExecutionsForJob("job", 0, total)).thenReturn(Arrays.asList(earlierExecution, jobExecution));
	}

	private void prepareServiceWithMultipleJobExecutions() throws Exception {
		when(jobService.listJobExecutionsForJob("job", 0, 100)).thenReturn(Arrays.asList(jobExecution, earlierExecution));
		when(jobService.listJobExecutionsForJob("job", 100, 100)).thenReturn(new ArrayList<JobExecution>());
	}

	@Test
	public void testGetJobExecutionCount() throws Exception {
		when(jobService.countJobExecutionsForJob("job")).thenReturn(10);
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
