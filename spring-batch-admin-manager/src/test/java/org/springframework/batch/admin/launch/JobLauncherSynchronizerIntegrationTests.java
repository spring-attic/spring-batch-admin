/*
 * Copyright 2009-2013 the original author or authors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.poller.DirectPoller;
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

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobExplorer jobExplorer;

	@Test
	public void testLaunch() throws Exception {
		jobRepositoryTestUtils.removeJobExecutions();
		jobLauncher.run(job, new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis())
				.toJobParameters());
	}

	@Test
	public void testFakeRestart() throws Exception {

		// Test if we can fake a restart by creating a job execution and failing
		// it without running the job...
		jobRepositoryTestUtils.removeJobExecutions();
		List<JobExecution> list = new ArrayList<JobExecution>(jobRepositoryTestUtils.createJobExecutions("test-job",
				new String[0], 1));
		JobExecution jobExecution = list.get(0);
		jobExecution.setStatus(BatchStatus.FAILED);
		jobExecution.setEndTime(new Date());
		jobRepository.update(jobExecution);

		final JobExecution newExecution = jobLauncher.run(job, jobExecution.getJobParameters());

		assertEquals(jobExecution.getJobId(), newExecution.getJobId());
		Future<BatchStatus> poll = new DirectPoller<BatchStatus>(100L).poll(new Callable<BatchStatus>() {
			public BatchStatus call() throws Exception {
				JobExecution jobExecution = jobExplorer.getJobExecution(newExecution.getId());
				BatchStatus status = jobExecution.getStatus();
				return jobExecution.isRunning() ? null : status;
			}
		});
		assertEquals(BatchStatus.COMPLETED, poll.get(1000, TimeUnit.MILLISECONDS));

		list.add(newExecution);
		jobRepositoryTestUtils.removeJobExecutions();

	}

	@Test
	public void testLaunchWithJobRunning() throws Exception {
		JobExecution jobExecution;
		jobExecution = jobRepositoryTestUtils.createJobExecutions("test-job", new String[0], 1).get(0);

		try {
			jobLauncher.run(job, new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis())
					.toJobParameters());
			fail("Expected: JobExecutionAlreadyRunningException");
		}
		catch (JobExecutionAlreadyRunningException e) {
			// expected
		}
		finally {
			try {
				assertEquals(1, jobExplorer.getJobExecutions(jobExecution.getJobInstance()).size());
			}
			finally {
				jobRepositoryTestUtils.removeJobExecutions();
			}
		}
	}

	@Test
	public void testLaunchWithJobRunningButFails() throws Exception {

		jobRepositoryTestUtils.removeJobExecutions();

		List<JobExecution> list = new ArrayList<JobExecution>(jobRepositoryTestUtils.createJobExecutions("test-job",
				new String[0], 1));

		try {
			jobLauncher.run(job, new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis())
					.toJobParameters());
			fail("Expected: JobExecutionAlreadyRunningException");
		}
		catch (JobExecutionAlreadyRunningException e) {
			// expected
		}
		finally {

			try {
				// Now fail the job (after the parallel start failed)
				JobExecution jobExecution = list.get(0);
				jobExecution.setStatus(BatchStatus.FAILED);
				jobExecution.setEndTime(new Date());
				jobRepository.update(jobExecution);

				// And restart it...
				final JobExecution newExecution = jobLauncher
						.run(job, jobExecution.getJobParameters());

				assertEquals(jobExecution.getJobId(), newExecution.getJobId());
				Future<BatchStatus> poll = new DirectPoller<BatchStatus>(100L).poll(new Callable<BatchStatus>() {
					public BatchStatus call() throws Exception {
						JobExecution jobExecution = jobExplorer.getJobExecution(newExecution.getId());
						BatchStatus status = jobExecution.getStatus();
						return jobExecution.isRunning() ? null : status;
					}
				});
				assertEquals(BatchStatus.COMPLETED, poll.get(1000, TimeUnit.MILLISECONDS));

				List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobExecution.getJobInstance());
				assertEquals(2, jobExecutions.size());

			}
			finally {
				jobRepositoryTestUtils.removeJobExecutions();
			}

		}
	}

	@Test
	public void testAbandonedWhenCheckJobDuringLaunchFails() throws Exception {

		jobRepositoryTestUtils.removeJobExecutions();

		List<JobExecution> list = new ArrayList<JobExecution>(jobRepositoryTestUtils.createJobExecutions("test-job",
				new String[0], 1));

		JobParameters jobParameters = new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis())
				.toJobParameters();
		try {
			// Simulate a job starting without using jobLauncher
			jobRepository.createJobExecution("test-job", jobParameters);
			fail("Expected: JobExecutionAlreadyRunningException");
		}
		catch (JobExecutionAlreadyRunningException e) {
			// expected
		}
		finally {

			try {
				// Now fail the job (after the parallel "start" failed)
				JobExecution jobExecution = list.get(0);
				jobExecution.setStatus(BatchStatus.FAILED);
				jobExecution.setEndTime(new Date());
				jobRepository.update(jobExecution);

				// And restart it...
				final JobExecution newExecution = jobLauncher
						.run(job, jobExecution.getJobParameters());

				assertEquals(jobExecution.getJobId(), newExecution.getJobId());
				Future<BatchStatus> poll = new DirectPoller<BatchStatus>(100L).poll(new Callable<BatchStatus>() {
					public BatchStatus call() throws Exception {
						JobExecution jobExecution = jobExplorer.getJobExecution(newExecution.getId());
						BatchStatus status = jobExecution.getStatus();
						return jobExecution.isRunning() ? null : status;
					}
				});
				assertEquals(BatchStatus.COMPLETED, poll.get(1000, TimeUnit.MILLISECONDS));

				assertEquals(2, jobExplorer.getJobExecutions(jobExecution.getJobInstance()).size());
				JobExecution lastExecution = jobRepository.getLastJobExecution("test-job", jobParameters);
				assertEquals(BatchStatus.ABANDONED, lastExecution.getStatus());
				assertEquals(1, jobExplorer.getJobExecutions(lastExecution.getJobInstance()).size());

			}
			finally {
				jobRepositoryTestUtils.removeJobExecutions();
			}

		}
	}

}
