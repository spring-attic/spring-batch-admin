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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.MetaDataInstanceFactory;

/**
 * @author Dave Syer
 * 
 */
public class SimpleJobServiceTests {

	private SearchableJobInstanceDao jobInstanceDao = EasyMock.createMock(SearchableJobInstanceDao.class);

	private SearchableJobExecutionDao jobExecutionDao = EasyMock.createMock(SearchableJobExecutionDao.class);

	private SearchableStepExecutionDao stepExecutionDao = EasyMock.createMock(SearchableStepExecutionDao.class);

	private JobRepository jobRepository = EasyMock.createMock(JobRepository.class);

	private JobLauncher jobLauncher = EasyMock.createMock(JobLauncher.class);

	private ListableJobLocator jobLocator = EasyMock.createMock(ListableJobLocator.class);

	private ExecutionContextDao executionContextDao = EasyMock.createMock(ExecutionContextDao.class);

	private SimpleJobService service = new SimpleJobService(jobInstanceDao, jobExecutionDao, stepExecutionDao,
			jobRepository, jobLauncher, jobLocator, executionContextDao);

	/**
	 * Test method for {@link SimpleJobService#getStepExecutions(Long)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetStepExecutions() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(12L, Arrays.asList(
				"step1", "step2"));
		EasyMock.expect(jobExecutionDao.getJobExecution(12L)).andReturn(jobExecution);
		stepExecutionDao.addStepExecutions(jobExecution);
		EasyMock.expectLastCall();
		EasyMock.expect(stepExecutionDao.findStepNamesForJobExecution("job", "*:partition*")).andReturn(
				Arrays.asList("step1", "step4"));
		EasyMock.replay(jobExecutionDao, stepExecutionDao);
		List<StepExecution> stepExecutions = new ArrayList<StepExecution>(service.getStepExecutions(12L));
		assertEquals(3, stepExecutions.size());
		assertEquals("step4", stepExecutions.get(2).getStepName());
		assertEquals(BatchStatus.UNKNOWN, stepExecutions.get(2).getStatus());
		EasyMock.verify(jobExecutionDao, stepExecutionDao);
	}

	/**
	 * Test method for {@link SimpleJobService#isLaunchable(String)}.
	 */
	@Test
	public void testIsLaunchable() throws Exception {
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Arrays.asList("foo", "bar")).anyTimes();
		EasyMock.replay(jobLauncher, jobLocator);
		assertTrue(service.isLaunchable("foo"));
		assertFalse(service.isLaunchable("job"));
		EasyMock.verify(jobLauncher, jobLocator);
	}

	/**
	 * Test method for {@link SimpleJobService#isIncrementable(String)}.
	 */
	@Test
	public void testIsIncementable() throws Exception {
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Arrays.asList("foo", "bar")).anyTimes();
		EasyMock.expect(jobLocator.getJob("foo")).andReturn(new JobSupport("foo", new RunIdIncrementer())).anyTimes();
		EasyMock.replay(jobLauncher, jobLocator);
		assertTrue(service.isIncrementable("foo"));
		assertFalse(service.isIncrementable("job"));
		EasyMock.verify(jobLauncher, jobLocator);
	}

	/**
	 * Test method for {@link SimpleJobService#launch(String, JobParameters)}.
	 */
	@Test
	public void testLaunch() throws Exception {
		JobParameters jobParameters = new JobParameters();
		Job job = new JobSupport("job");
		EasyMock.expect(jobLocator.getJob("job")).andReturn(job);
		EasyMock.expect(jobLauncher.run(job, jobParameters)).andReturn(MetaDataInstanceFactory.createJobExecution());
		EasyMock.replay(jobLauncher, jobLocator);
		assertNotNull(service.launch("job", jobParameters));
		EasyMock.verify(jobLauncher, jobLocator);
	}

	/**
	 * Test method for {@link SimpleJobService#launch(String, JobParameters)}.
	 */
	@Test
	public void testLaunchWithIncrementer() throws Exception {
		JobParameters jobParameters = new JobParameters();
		JobParameters nextJobParameters = new RunIdIncrementer().getNext(jobParameters);
		Job job = new JobSupport("job") {
			@Override
			public JobParametersIncrementer getJobParametersIncrementer() {
				return new RunIdIncrementer();
			}
		};
		EasyMock.expect(jobLocator.getJob("job")).andReturn(job);
		EasyMock.expect(jobLauncher.run(job, nextJobParameters)).andReturn(MetaDataInstanceFactory.createJobExecution());
		EasyMock.replay(jobLauncher, jobLocator);
		assertNotNull(service.launch("job", jobParameters));
		EasyMock.verify(jobLauncher, jobLocator);
	}

	/**
	 * Test method for {@link SimpleJobService#launch(String, JobParameters)}.
	 */
	@Test
	public void testLaunchFailedExecution() throws Exception {
		JobParameters jobParameters = new JobParameters();
		Job job = new JobSupport("job") {
			@Override
			public JobParametersIncrementer getJobParametersIncrementer() {
				return new RunIdIncrementer();
			}
		};
		EasyMock.expect(jobLocator.getJob("job")).andReturn(job);
		JobExecution failed = MetaDataInstanceFactory.createJobExecution();
		failed.setStatus(BatchStatus.FAILED);
		EasyMock.expect(jobRepository.getLastJobExecution("job", jobParameters)).andReturn(failed);
		EasyMock.expect(jobLauncher.run(job, jobParameters)).andReturn(MetaDataInstanceFactory.createJobExecution());
		EasyMock.replay(jobLauncher, jobLocator, jobRepository);
		assertNotNull(service.launch("job", jobParameters));
		EasyMock.verify(jobLauncher, jobLocator, jobRepository);
	}

	/**
	 * Test method for {@link SimpleJobService#launch(String, JobParameters)}.
	 */
	@Test
	public void testRestart() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		EasyMock.expect(jobExecutionDao.getJobExecution(123L)).andReturn(jobExecution);
		EasyMock.expect(jobInstanceDao.getJobInstance(jobExecution)).andReturn(
				MetaDataInstanceFactory.createJobInstance());
		JobParameters jobParameters = new JobParameters();
		Job job = new JobSupport("job");
		EasyMock.expect(jobLocator.getJob("job")).andReturn(job);
		EasyMock.expect(jobLauncher.run(job, jobParameters))
				.andReturn(MetaDataInstanceFactory.createJobExecution(124L));
		EasyMock.replay(jobInstanceDao, jobExecutionDao, jobLauncher, jobLocator);
		assertNotNull(service.restart(123L));
		EasyMock.verify(jobInstanceDao, jobExecutionDao, jobLauncher, jobLocator);
	}

	/**
	 * Test method for {@link SimpleJobService#listJobExecutions(int, int)}.
	 */
	@Test
	public void testListJobExecutions() throws Exception {
		EasyMock.expect(jobExecutionDao.getJobExecutions(0, 2)).andReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobExecution(123L), MetaDataInstanceFactory
						.createJobExecution(1234L)));
		EasyMock.replay(jobExecutionDao);
		assertEquals(2, service.listJobExecutions(0, 2).size());
		EasyMock.verify(jobExecutionDao);
	}

	/**
	 * Test method for {@link SimpleJobService#countJobExecutions() throws Exception}.
	 */
	@Test
	public void testCountJobExecutions() throws Exception {
		EasyMock.expect(jobExecutionDao.countJobExecutions()).andReturn(3);
		EasyMock.replay(jobExecutionDao);
		assertEquals(3, service.countJobExecutions());
		EasyMock.verify(jobExecutionDao);
	}

	/**
	 * Test method for {@link SimpleJobService#listJobs(int, int)}.
	 */
	@Test
	public void testListJobs() throws Exception {
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Arrays.asList("job1", "job2"));
		EasyMock.replay(jobLocator, jobInstanceDao);
		assertEquals(2, service.listJobs(0, 2).size());
		EasyMock.verify(jobLocator, jobInstanceDao);
	}

	/**
	 * Test method for {@link SimpleJobService#listJobs(int, int)}.
	 */
	@Test
	public void testListJobsNotLaunchable() throws Exception {
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Arrays.asList("job1", "job2"));
		EasyMock.expect(jobInstanceDao.getJobNames()).andReturn(Arrays.asList("job3", "job2"));
		EasyMock.replay(jobLocator, jobInstanceDao);
		assertEquals(3, service.listJobs(0, 4).size());
		EasyMock.verify(jobLocator, jobInstanceDao);
	}

	/**
	 * Test method for {@link SimpleJobService#countJobs() throws Exception}.
	 */
	@Test
	public void testCountJobs() throws Exception {
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Arrays.asList("job1", "job2"));
		EasyMock.expect(jobInstanceDao.getJobNames()).andReturn(Arrays.asList("job3", "job2"));
		EasyMock.replay(jobLocator, jobInstanceDao);
		assertEquals(3, service.countJobs());
		EasyMock.verify(jobLocator, jobInstanceDao);
	}

	/**
	 * Test method for {@link SimpleJobService#abandon(Long)}.
	 */
	@Test
	public void testAbort() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		jobExecution.setStatus(BatchStatus.STOPPING);
		EasyMock.expect(jobExecutionDao.getJobExecution(123L)).andReturn(jobExecution);
		jobRepository.update(jobExecution);
		EasyMock.replay(jobExecutionDao, jobRepository);
		service.abandon(123L);
		assertEquals(BatchStatus.ABANDONED, jobExecution.getStatus());
		EasyMock.verify(jobExecutionDao, jobRepository);
	}

	/**
	 * Test method for {@link SimpleJobService#stop(Long)}.
	 */
	@Test
	public void testStop() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		EasyMock.expect(jobExecutionDao.getJobExecution(123L)).andReturn(jobExecution);
		jobRepository.update(jobExecution);
		EasyMock.replay(jobExecutionDao, jobRepository);
		service.stop(123L);
		assertEquals(BatchStatus.STOPPING, jobExecution.getStatus());
		EasyMock.verify(jobExecutionDao, jobRepository);
	}

	/**
	 * Test method for {@link SimpleJobService#countJobExecutionsForJob(String)} .
	 */
	@Test
	public void testCountJobExecutionsForJob() throws Exception {
		String jobName = "job";
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Collections.<String> emptyList());
		EasyMock.expect(jobInstanceDao.countJobInstances(jobName)).andReturn(1);
		EasyMock.expect(jobExecutionDao.countJobExecutions(jobName)).andReturn(2);
		EasyMock.replay(stepExecutionDao, jobLocator, jobInstanceDao, jobExecutionDao);
		assertEquals(2, service.countJobExecutionsForJob("job"));
		EasyMock.verify(stepExecutionDao, jobLocator, jobInstanceDao, jobExecutionDao);
	}

	/**
	 * Test method for {@link SimpleJobService#countJobInstances(String)}.
	 */
	@Test
	public void testCountJobInstances() throws Exception {
		EasyMock.expect(jobInstanceDao.countJobInstances("job")).andReturn(3);
		EasyMock.replay(jobInstanceDao);
		assertEquals(3, service.countJobInstances("job"));
		EasyMock.verify(jobInstanceDao);
	}

	/**
	 * Test method for {@link SimpleJobService#getJobExecution(Long)}.
	 */
	@Test
	public void testGetJobExecution() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		JobInstance jobInstance = jobExecution.getJobInstance();
		jobExecution.setJobInstance(null);
		EasyMock.expect(jobExecutionDao.getJobExecution(123L)).andReturn(jobExecution);
		EasyMock.expect(jobInstanceDao.getJobInstance(jobExecution)).andReturn(jobInstance);
		EasyMock.expect(executionContextDao.getExecutionContext(jobExecution)).andReturn(new ExecutionContext());
		stepExecutionDao.addStepExecutions(jobExecution);
		EasyMock.expectLastCall();
		EasyMock.replay(stepExecutionDao, jobExecutionDao, jobInstanceDao, executionContextDao);
		JobExecution result = service.getJobExecution(123L);
		assertNotNull(result);
		assertNotNull(result.getJobInstance());
		EasyMock.verify(stepExecutionDao, jobExecutionDao, jobInstanceDao, executionContextDao);
	}

	@Test
	public void testGetStepNamesFromJobExecution() throws Exception {
		EasyMock.expect(jobLocator.getJob("job")).andReturn(null);
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Collections.<String> emptyList());
		EasyMock.expect(jobInstanceDao.countJobInstances("job")).andReturn(1);
		EasyMock.expect(jobExecutionDao.getJobExecutions("job", 0, 100)).andReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobExecutionWithStepExecutions(123L, Arrays.asList("foo",
						"bar")), MetaDataInstanceFactory.createJobExecutionWithStepExecutions(124L, Arrays
						.asList("bar"))));
		stepExecutionDao.addStepExecutions(EasyMock.isA(JobExecution.class));
		stepExecutionDao.addStepExecutions(EasyMock.isA(JobExecution.class));
		EasyMock.replay(jobLocator, stepExecutionDao, jobExecutionDao, jobInstanceDao, executionContextDao);
		Collection<String> result = service.getStepNamesForJob("job");
		assertNotNull(result);
		assertEquals("[foo, bar]", result.toString());
		EasyMock.verify(jobLocator, stepExecutionDao, jobExecutionDao, jobInstanceDao, executionContextDao);
	}

	@Test
	public void testGetStepNamesFromStepLocator() throws Exception {
		SimpleJob job = new SimpleJob("job");
		job.addStep(new TaskletStep("foo"));
		EasyMock.expect(jobLocator.getJob("job")).andReturn(job);
		EasyMock.replay(jobLocator);
		Collection<String> result = service.getStepNamesForJob("job");
		assertNotNull(result);
		assertEquals("[foo]", result.toString());
		EasyMock.verify(jobLocator);
	}

	@Test
	public void testGetJobExecutionWithUnserializableExecutionContext() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		JobInstance jobInstance = jobExecution.getJobInstance();
		jobExecution.setJobInstance(null);
		EasyMock.expect(jobExecutionDao.getJobExecution(123L)).andReturn(jobExecution);
		EasyMock.expect(jobInstanceDao.getJobInstance(jobExecution)).andReturn(jobInstance);
		EasyMock.expect(executionContextDao.getExecutionContext(jobExecution)).andThrow(
				new IllegalStateException("Planned"));
		stepExecutionDao.addStepExecutions(jobExecution);
		EasyMock.expectLastCall();
		EasyMock.replay(stepExecutionDao, jobExecutionDao, jobInstanceDao, executionContextDao);
		JobExecution result = service.getJobExecution(123L);
		assertNotNull(result);
		assertNotNull(result.getJobInstance());
		assertNotNull(result.getExecutionContext());
		EasyMock.verify(stepExecutionDao, jobExecutionDao, jobInstanceDao, executionContextDao);
	}

	/**
	 * Test method for {@link SimpleJobService#getJobExecutionsForJobInstance(String, Long)}.
	 */
	@Test
	public void testGetJobExecutionsForJobInstance() throws Exception {
		String jobName = "job";
		Long jobInstanceId = 12L;
		JobInstance jobInstance = MetaDataInstanceFactory.createJobInstance(jobName, jobInstanceId);
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Collections.<String> emptyList());
		EasyMock.expect(jobInstanceDao.countJobInstances(jobName)).andReturn(1);
		EasyMock.expect(jobInstanceDao.getJobInstance(jobInstanceId)).andReturn(jobInstance);
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		EasyMock.expect(jobExecutionDao.findJobExecutions(jobInstance)).andReturn(Arrays.asList(jobExecution));
		stepExecutionDao.addStepExecutions(jobExecution);
		EasyMock.expectLastCall();
		EasyMock.replay(jobLocator, jobInstanceDao, stepExecutionDao, jobExecutionDao);
		assertEquals(1, service.getJobExecutionsForJobInstance("job", jobInstance.getId()).size());
		EasyMock.verify(jobLocator, jobInstanceDao, stepExecutionDao, jobExecutionDao);
	}

	/**
	 * Test method for {@link SimpleJobService#getStepExecution(Long, Long)}.
	 */
	@Test
	public void testGetStepExecution() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(123L, Arrays
				.asList("step1"));
		EasyMock.expect(jobExecutionDao.getJobExecution(123L)).andReturn(jobExecution);
		EasyMock.expect(jobInstanceDao.getJobInstance(jobExecution)).andReturn(null);
		EasyMock.expect(executionContextDao.getExecutionContext(jobExecution)).andReturn(new ExecutionContext());
		stepExecutionDao.addStepExecutions(jobExecution);
		StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
		Long stepExecutionId = stepExecution.getId();
		EasyMock.expect(stepExecutionDao.getStepExecution(jobExecution, stepExecutionId)).andReturn(stepExecution);
		EasyMock.expect(executionContextDao.getExecutionContext(stepExecution)).andReturn(new ExecutionContext());
		EasyMock.replay(jobExecutionDao, stepExecutionDao, executionContextDao, jobInstanceDao);
		assertNotNull(service.getStepExecution(123L, 1234L));
		EasyMock.verify(jobExecutionDao, stepExecutionDao, executionContextDao, jobInstanceDao);
	}

	@Test
	public void testGetStepExecutionWithUnserializableExecutionContent() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(123L, Arrays
				.asList("step1"));
		EasyMock.expect(jobExecutionDao.getJobExecution(123L)).andReturn(jobExecution);
		EasyMock.expect(jobInstanceDao.getJobInstance(jobExecution)).andReturn(null);
		EasyMock.expect(executionContextDao.getExecutionContext(jobExecution)).andReturn(new ExecutionContext());
		stepExecutionDao.addStepExecutions(jobExecution);
		StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
		Long stepExecutionId = stepExecution.getId();
		EasyMock.expect(stepExecutionDao.getStepExecution(jobExecution, stepExecutionId)).andReturn(stepExecution);
		EasyMock.expect(executionContextDao.getExecutionContext(stepExecution)).andThrow(
				new IllegalStateException("Expected"));
		EasyMock.replay(jobExecutionDao, stepExecutionDao, executionContextDao, jobInstanceDao);
		StepExecution result = service.getStepExecution(123L, 1234L);
		assertNotNull(result);
		// If there is a problem extracting the execution context it will be empty
		assertNotNull(result.getExecutionContext());
		EasyMock.verify(jobExecutionDao, stepExecutionDao, executionContextDao, jobInstanceDao);
	}

	@Test
	public void testCountStepExecutions() throws Exception {
		stepExecutionDao.countStepExecutions("job", "step");
		EasyMock.expectLastCall().andReturn(2);
		EasyMock.replay(jobExecutionDao, stepExecutionDao);
		service.countStepExecutionsForStep("job", "step");
		EasyMock.verify(jobExecutionDao, stepExecutionDao);
	}

	@Test
	public void testListStepExecutions() throws Exception {
		stepExecutionDao.countStepExecutions("job", "step");
		EasyMock.expectLastCall().andReturn(2);
		stepExecutionDao.findStepExecutions("job", "step", 0, 20);
		EasyMock.expectLastCall().andReturn(new ArrayList<StepExecution>());
		EasyMock.replay(jobExecutionDao, stepExecutionDao);
		service.listStepExecutionsForStep("job", "step", 0, 20);
		EasyMock.verify(jobExecutionDao, stepExecutionDao);
	}

	/**
	 * Test method for {@link SimpleJobService#listJobExecutionsForJob(String, int, int)}.
	 */
	@Test
	public void testListJobExecutionsForJob() throws Exception {
		String jobName = "job";
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Collections.<String> emptyList());
		EasyMock.expect(jobInstanceDao.countJobInstances(jobName)).andReturn(1);
		EasyMock.expect(jobExecutionDao.getJobExecutions("job", 0, 4)).andReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobExecution(123L), MetaDataInstanceFactory
						.createJobExecution(124L)));
		EasyMock.replay(jobLocator, jobInstanceDao, jobExecutionDao);
		assertEquals(2, service.listJobExecutionsForJob("job", 0, 4).size());
		EasyMock.verify(jobLocator, jobInstanceDao, jobExecutionDao);
	}

	/**
	 * Test method for {@link SimpleJobService#listJobInstances(String, int, int)}.
	 */
	@Test
	public void testListJobInstances() throws Exception {
		String jobName = "job";
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Collections.<String> emptyList());
		EasyMock.expect(jobInstanceDao.countJobInstances(jobName)).andReturn(1);
		EasyMock.expect(jobInstanceDao.getJobInstances("job", 0, 4)).andReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobInstance("job", 123L), MetaDataInstanceFactory
						.createJobInstance("job", 124L)));
		EasyMock.replay(jobLocator, jobInstanceDao);
		assertEquals(2, service.listJobInstances("job", 0, 4).size());
		EasyMock.verify(jobLocator, jobInstanceDao);
	}

	@Test
	public void testLastJobParameters() throws Exception {
		String jobName = "job";
		EasyMock.expect(jobLocator.getJobNames()).andReturn(Collections.<String> emptyList());
		EasyMock.expect(jobInstanceDao.countJobInstances(jobName)).andReturn(1);
		EasyMock.expect(jobInstanceDao.getJobInstances("job", 0, 1)).andReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobInstance("job", 123L)));
		EasyMock.replay(jobLocator, jobInstanceDao);
		assertEquals(0, service.getLastJobParameters("job").getParameters().size());
		EasyMock.verify(jobLocator, jobInstanceDao);
	}

	@Test
	public void testRemoveInactives() throws Exception {

		testLaunch();

		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		EasyMock.expect(jobExecutionDao.getJobExecution(123L)).andReturn(jobExecution);
		EasyMock.expect(jobInstanceDao.getJobInstance(jobExecution)).andReturn(null);
		EasyMock.expect(executionContextDao.getExecutionContext(jobExecution)).andReturn(new ExecutionContext());
		stepExecutionDao.addStepExecutions(jobExecution);

		EasyMock.replay(jobExecutionDao, stepExecutionDao, executionContextDao, jobInstanceDao);

		service.removeInactiveExecutions();

		EasyMock.verify(jobExecutionDao, stepExecutionDao, executionContextDao, jobInstanceDao);
	}

	@Test
	public void testDestroy() throws Exception {

		EasyMock.replay(jobExecutionDao, stepExecutionDao, executionContextDao, jobInstanceDao);

		service.destroy();

		EasyMock.verify(jobExecutionDao, stepExecutionDao, executionContextDao, jobInstanceDao);

	}

	@Test
	public void testDestroyWithActives() throws Exception {

		testLaunch();

		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.setEndTime(new Date());
		assertFalse(jobExecution.isRunning());

		EasyMock.expect(jobExecutionDao.getJobExecution(123L)).andReturn(jobExecution).anyTimes();
		EasyMock.expect(jobInstanceDao.getJobInstance(jobExecution)).andReturn(null).anyTimes();
		EasyMock.expect(executionContextDao.getExecutionContext(jobExecution)).andReturn(new ExecutionContext())
				.anyTimes();
		stepExecutionDao.addStepExecutions(jobExecution);
		EasyMock.expectLastCall().anyTimes();

		EasyMock.replay(jobExecutionDao, stepExecutionDao, executionContextDao, jobInstanceDao);

		service.destroy();

		EasyMock.verify(jobExecutionDao, stepExecutionDao, executionContextDao, jobInstanceDao);

	}

}
