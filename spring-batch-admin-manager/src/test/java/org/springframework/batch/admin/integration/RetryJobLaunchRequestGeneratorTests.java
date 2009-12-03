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
package org.springframework.batch.admin.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.batch.test.MetaDataInstanceFactory;

public class RetryJobLaunchRequestGeneratorTests {

	private RetryJobLaunchRequestGenerator generator = new RetryJobLaunchRequestGenerator();

	private MapJobRegistry jobLocator = new MapJobRegistry();

	private List<JobExecution> jobExecutions = new ArrayList<JobExecution>();

	private JobExecution jobExecution;

	private Job job;

	private StepExecution stepExecution;

	@Before
	public void setUp() throws Exception {

		jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(1L, Arrays.asList("step"));
		stepExecution = jobExecution.getStepExecutions().iterator().next();
		
		stepExecution.setStatus(BatchStatus.FAILED);
		stepExecution.setExitStatus(ExitStatus.FAILED.addExitDescription("SuspectedTimeoutException"));
		
		job = EasyMock.createMock(Job.class);
		String jobName = jobExecution.getJobInstance().getJobName();

		EasyMock.expect(job.getName()).andReturn(jobName).anyTimes();

		JobExplorer jobExplorer = EasyMock.createMock(JobExplorer.class);
		EasyMock.expect(jobExplorer.getJobExecutions(jobExecution.getJobInstance())).andReturn(jobExecutions)
				.anyTimes();
		EasyMock.replay(job, jobExplorer);

		jobLocator.register(new ReferenceJobFactory(job));
		generator.setJobLocator(jobLocator);
		generator.setJobExplorer(jobExplorer);

	}

	@Test
	public void testRetry() throws Exception {

		jobExecution.setStatus(BatchStatus.FAILED);
		JobLaunchRequest result = generator.retry(jobExecution);

		assertNotNull(result);
		assertEquals(job, result.getJob());

	}

	@Test
	public void testNotRetryable() throws Exception {

		stepExecution.setExitStatus(ExitStatus.FAILED.addExitDescription("Foo"));
		
		jobExecution.setStatus(BatchStatus.FAILED);
		JobLaunchRequest result = generator.retry(jobExecution);

		assertNull(result);

	}

	@Test
	public void testNoRetryForAlreadyProcessed() throws Exception {

		jobExecution.setStatus(BatchStatus.FAILED);

		generator.retry(jobExecution);
		JobLaunchRequest result = generator.retry(jobExecution);

		assertNull(result);

	}

	@Test
	public void testNoRetryForCompleted() throws Exception {

		jobExecution.setStatus(BatchStatus.COMPLETED);
		JobLaunchRequest result = generator.retry(jobExecution);

		assertNull(result);

	}

	@Test
	public void testNoRetryForAlreadyRestarted() throws Exception {

		jobExecution.setStatus(BatchStatus.FAILED);
		jobExecutions.add(jobExecution);
		jobExecutions.add(jobExecution);

		JobLaunchRequest result = generator.retry(jobExecution);

		assertNull(result);

	}

}
