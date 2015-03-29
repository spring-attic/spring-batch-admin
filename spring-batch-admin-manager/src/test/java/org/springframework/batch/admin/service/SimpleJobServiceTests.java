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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.batch.api.chunk.AbstractItemReader;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.batch.operations.JobOperator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.explore.JobExplorer;
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
 * @author Michael Minella
 * 
 */
public class SimpleJobServiceTests {

	@Mock
	private SearchableJobInstanceDao jobInstanceDao;

	@Mock
	private SearchableJobExecutionDao jobExecutionDao;

	@Mock
	private SearchableStepExecutionDao stepExecutionDao;

	@Mock
	private JobRepository jobRepository;

	@Mock
	private JobLauncher jobLauncher;

	@Mock
	private ListableJobLocator jobLocator;

	@Mock
	private ExecutionContextDao executionContextDao;

	@Mock
	private JobExplorer jobExplorer;

	@Mock
	private JobOperator jsrJobOperator;

    @Mock
    private org.springframework.batch.core.launch.JobOperator jobOperator;

	private SimpleJobService service;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		service = new SimpleJobService(jobInstanceDao, jobExecutionDao, stepExecutionDao,
				jobRepository, jobLauncher, jobLocator, executionContextDao, jobOperator, jsrJobOperator);
	}

	@Test
	public void testJsrLaunch() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(5L, Arrays.asList(
				"step1"));
		JobExecution completeJobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(5L, Arrays.asList(
				"step1"));
		completeJobExecution.setStatus(BatchStatus.COMPLETED);
		completeJobExecution.setEndTime(new Date());
		JobParameters jobParameters = new JobParametersBuilder().addString("fail", String.valueOf(false), false).toJobParameters();
		when(jsrJobOperator.start("jsr352-job", jobParameters.toProperties())).thenReturn(5l);
		when(jobExecutionDao.getJobExecution(5l)).thenReturn(jobExecution, jobExecution, completeJobExecution);

		JobExecution result = service.launch("jsr352-job", jobParameters);

		while(true) {
			result = service.getJobExecution(result.getId());

			if(!result.isRunning()) {
				break;
			}
		}

		assertEquals(BatchStatus.COMPLETED, result.getStatus());
		assertEquals(1, result.getStepExecutions().size());
	}

	@Test
	public void testJsrRestartNoParams() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(124L);
		JobExecution completeJobExecution = MetaDataInstanceFactory.createJobExecution(124L);
		completeJobExecution.setStatus(BatchStatus.COMPLETED);
		completeJobExecution.setEndTime(new Date());
		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(
				MetaDataInstanceFactory.createJobInstance());
		when(jsrJobOperator.restart(123L, new Properties())).thenReturn(124L);
		when(jobExecutionDao.getJobExecution(124l)).thenReturn(jobExecution, jobExecution, completeJobExecution);

		JobExecution result = service.restart(123l);

		while(true) {
			result = service.getJobExecution(result.getId());

			if(!result.isRunning()) {
				break;
			}
		}

		assertEquals(BatchStatus.COMPLETED, result.getStatus());
	}

	@Test
	public void testJsrRestartWithParams() throws Exception {
		JobParameters params = new JobParametersBuilder().addString("foo", "bar").toJobParameters();
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(124L);
		JobExecution completeJobExecution = MetaDataInstanceFactory.createJobExecution(124L);
		completeJobExecution.setStatus(BatchStatus.COMPLETED);
		completeJobExecution.setEndTime(new Date());
		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(
				MetaDataInstanceFactory.createJobInstance());
		when(jsrJobOperator.restart(123L, params.toProperties())).thenReturn(124L);
		when(jobExecutionDao.getJobExecution(124l)).thenReturn(jobExecution, jobExecution, completeJobExecution);

		JobExecution result = service.restart(123l, params);

		while(true) {
			result = service.getJobExecution(result.getId());

			if(!result.isRunning()) {
				break;
			}
		}

		assertEquals(BatchStatus.COMPLETED, result.getStatus());
	}

	@Test
	public void testJsrStop() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		JobExecution stoppedJobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		stoppedJobExecution.setStatus(BatchStatus.STOPPED);
		stoppedJobExecution.setEndTime(new Date());

		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution, stoppedJobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(
				MetaDataInstanceFactory.createJobInstance("jsr352-job", 3L));

		JobExecution result = service.stop(123l);

		verify(jsrJobOperator).stop(123L);
		assertEquals(BatchStatus.STOPPED, result.getStatus());
	}

	@Test
	public void testJsrAbandon() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		jobExecution.setEndTime(new Date());
		jobExecution.setStatus(BatchStatus.FAILED);
		JobExecution stoppedJobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		stoppedJobExecution.setStatus(BatchStatus.ABANDONED);
		stoppedJobExecution.setEndTime(new Date());

		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution, stoppedJobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(
				MetaDataInstanceFactory.createJobInstance("jsr352-job", 3L));

		JobExecution result = service.abandon(123l);

		verify(jsrJobOperator).abandon(123L);
		assertEquals(BatchStatus.ABANDONED, result.getStatus());
	}

	/**
	 * Test method for {@link SimpleJobService#getStepExecutions(Long)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetStepExecutions() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(12L, Arrays.asList(
				"step1", "step2"));
		when(jobExecutionDao.getJobExecution(12L)).thenReturn(jobExecution);
		stepExecutionDao.addStepExecutions(jobExecution);

		when(stepExecutionDao.findStepNamesForJobExecution("job", "*:partition*")).thenReturn(
				Arrays.asList("step1", "step4"));
		List<StepExecution> stepExecutions = new ArrayList<StepExecution>(service.getStepExecutions(12L));
		assertEquals(3, stepExecutions.size());
		assertEquals("step4", stepExecutions.get(2).getStepName());
		assertEquals(BatchStatus.UNKNOWN, stepExecutions.get(2).getStatus());

		verify(stepExecutionDao, times(2)).addStepExecutions(jobExecution);
	}

	/**
	 * Test method for {@link SimpleJobService#isLaunchable(String)}.
	 */
	@Test
	public void testIsLaunchable() throws Exception {
		when(jobLocator.getJobNames()).thenReturn(Arrays.asList("foo", "bar"));

		assertTrue(service.isLaunchable("foo"));
		assertFalse(service.isLaunchable("job"));
	}

	/**
	 * Test method for {@link SimpleJobService#isIncrementable(String)}.
	 */
	@Test
	public void testIsIncementable() throws Exception {
		when(jobLocator.getJobNames()).thenReturn(Arrays.asList("foo", "bar"));
		when(jobLocator.getJob("foo")).thenReturn(new JobSupport("foo", new RunIdIncrementer()));

		assertTrue(service.isIncrementable("foo"));
		assertFalse(service.isIncrementable("job"));
	}

	/**
	 * Test method for {@link SimpleJobService#launch(String, JobParameters)}.
	 */
	@Test
	public void testLaunch() throws Exception {
		JobParameters jobParameters = new JobParameters();
		Job job = new JobSupport("job");
		when(jobLocator.getJobNames()).thenReturn(Arrays.asList("job", "job1"));
		when(jobLocator.getJob("job")).thenReturn(job);
		when(jobLauncher.run(job, jobParameters)).thenReturn(MetaDataInstanceFactory.createJobExecution());

		assertNotNull(service.launch("job", jobParameters));
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
		when(jobLocator.getJobNames()).thenReturn(Arrays.asList("job", "job1"));
		when(jobLocator.getJob("job")).thenReturn(job);
		when(jobLauncher.run(job, nextJobParameters)).thenReturn(MetaDataInstanceFactory.createJobExecution());

		assertNotNull(service.launch("job", jobParameters));
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
		when(jobLocator.getJobNames()).thenReturn(Arrays.asList("job", "job1"));
		when(jobLocator.getJob("job")).thenReturn(job);
		JobExecution failed = MetaDataInstanceFactory.createJobExecution();
		failed.setStatus(BatchStatus.FAILED);
		when(jobRepository.getLastJobExecution("job", jobParameters)).thenReturn(failed);
		when(jobLauncher.run(job, jobParameters)).thenReturn(MetaDataInstanceFactory.createJobExecution());

		assertNotNull(service.launch("job", jobParameters));
	}

	/**
	 * Test method for {@link SimpleJobService#launch(String, JobParameters)}.
	 */
	@Test
	public void testRestart() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(
				MetaDataInstanceFactory.createJobInstance());
		JobParameters jobParameters = new JobParameters();
		Job job = new JobSupport("job");
		when(jobLocator.getJobNames()).thenReturn(Arrays.asList("job", "job1"));
		when(jobLocator.getJob("job")).thenReturn(job);
		when(jobLauncher.run(job, jobParameters))
		.thenReturn(MetaDataInstanceFactory.createJobExecution(124L));

		assertNotNull(service.restart(123L));
	}

	/**
	 * Test method for {@link SimpleJobService#listJobExecutions(int, int)}.
	 */
	@Test
	public void testListJobExecutions() throws Exception {
		when(jobExecutionDao.getJobExecutions(0, 2)).thenReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobExecution(123L), MetaDataInstanceFactory
						.createJobExecution(1234L)));

		assertEquals(2, service.listJobExecutions(0, 2).size());
	}

	/**
	 * Test method for {@link SimpleJobService#countJobExecutions() throws Exception}.
	 */
	@Test
	public void testCountJobExecutions() throws Exception {
		when(jobExecutionDao.countJobExecutions()).thenReturn(3);

		assertEquals(3, service.countJobExecutions());
	}

	/**
	 * Test method for {@link SimpleJobService#listJobs(int, int)}.
	 */
	@Test
	public void testListJobs() throws Exception {
		when(jobLocator.getJobNames()).thenReturn(Arrays.asList("job1", "job2"));

		assertEquals(2, service.listJobs(0, 2).size());
	}

	/**
	 * Test method for {@link SimpleJobService#listJobs(int, int)}.
	 */
	@Test
	public void testListJobsNotLaunchable() throws Exception {
		when(jobLocator.getJobNames()).thenReturn(Arrays.asList("job1", "job2"));
		when(jobInstanceDao.getJobNames()).thenReturn(Arrays.asList("job3", "job2"));

		assertEquals(4, service.listJobs(0, 5).size());
	}

	/**
	 * Test method for {@link SimpleJobService#countJobs() throws Exception}.
	 */
	@Test
	public void testCountJobs() throws Exception {
		when(jobLocator.getJobNames()).thenReturn(Arrays.asList("job1", "job2"));
		when(jobInstanceDao.getJobNames()).thenReturn(Arrays.asList("job3", "job2"));

		assertEquals(3, service.countJobs());
	}

	/**
	 * Test method for {@link SimpleJobService#abandon(Long)}.
	 */
	@Test
	public void testAbort() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		jobExecution.setStatus(BatchStatus.STOPPING);
		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(jobExecution.getJobInstance());
		jobRepository.update(jobExecution);
		service.abandon(123L);

		assertEquals(BatchStatus.ABANDONED, jobExecution.getStatus());
		assertNotNull(jobExecution.getEndTime());
	}

	/**
	 * Test method for {@link SimpleJobService#stop(Long)}.
	 */
	@Test
	public void testStop() throws Exception {
		final JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(jobExecution.getJobInstance());
		jobRepository.update(jobExecution);
        when(jobOperator.stop(123L)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                jobExecution.setStatus(BatchStatus.STOPPING);
                return null;
            }
        });
		service.stop(123L);

		assertEquals(BatchStatus.STOPPING, jobExecution.getStatus());
	}

	/**
	 * Test method for {@link SimpleJobService#countJobExecutionsForJob(String)} .
	 */
	@Test
	public void testCountJobExecutionsForJob() throws Exception {
		String jobName = "job";
		when(jobLocator.getJobNames()).thenReturn(Collections.<String>emptyList());
		when(jobInstanceDao.countJobInstances(jobName)).thenReturn(1);
		when(jobExecutionDao.countJobExecutions(jobName)).thenReturn(2);

		assertEquals(2, service.countJobExecutionsForJob("job"));
	}

	/**
	 * Test method for {@link SimpleJobService#countJobInstances(String)}.
	 */
	@Test
	public void testCountJobInstances() throws Exception {
		when(jobInstanceDao.countJobInstances("job")).thenReturn(3);

		assertEquals(3, service.countJobInstances("job"));
	}

	/**
	 * Test method for {@link SimpleJobService#getJobExecution(Long)}.
	 */
	@Test
	public void testGetJobExecution() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		JobInstance jobInstance = jobExecution.getJobInstance();
		jobExecution.setJobInstance(null);
		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(jobInstance);
		when(executionContextDao.getExecutionContext(jobExecution)).thenReturn(new ExecutionContext());

		JobExecution result = service.getJobExecution(123L);
		assertNotNull(result);
		assertNotNull(result.getJobInstance());

		verify(stepExecutionDao).addStepExecutions(jobExecution);
	}

	@Test
	public void testGetStepNamesFromJobExecution() throws Exception {
		when(jobLocator.getJob("job")).thenReturn(null);
		when(jobLocator.getJobNames()).thenReturn(Collections.<String>emptyList());
		when(jobInstanceDao.countJobInstances("job")).thenReturn(1);
		when(jobExecutionDao.getJobExecutions("job", 0, 100)).thenReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobExecutionWithStepExecutions(123L, Arrays.asList("foo",
						"bar")), MetaDataInstanceFactory.createJobExecutionWithStepExecutions(124L, Arrays
						.asList("bar"))));
		stepExecutionDao.addStepExecutions(isA(JobExecution.class));
		stepExecutionDao.addStepExecutions(isA(JobExecution.class));

		Collection<String> result = service.getStepNamesForJob("job");
		assertNotNull(result);
		assertEquals("[foo, bar]", result.toString());
	}

	@Test
	public void testGetStepNamesFromStepLocator() throws Exception {
		SimpleJob job = new SimpleJob("job");
		job.addStep(new TaskletStep("foo"));
		when(jobLocator.getJob("job")).thenReturn(job);

		Collection<String> result = service.getStepNamesForJob("job");
		assertNotNull(result);
		assertEquals("[foo]", result.toString());
	}

	@Test
	public void testGetJobExecutionWithUnserializableExecutionContext() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution(123L);
		JobInstance jobInstance = jobExecution.getJobInstance();
		jobExecution.setJobInstance(null);
		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(jobInstance);
		when(executionContextDao.getExecutionContext(jobExecution)).thenThrow(
				new IllegalStateException("Planned"));

		JobExecution result = service.getJobExecution(123L);
		assertNotNull(result);
		assertNotNull(result.getJobInstance());
		assertNotNull(result.getExecutionContext());

		verify(stepExecutionDao).addStepExecutions(jobExecution);
	}

	/**
	 * Test method for {@link SimpleJobService#getJobExecutionsForJobInstance(String, Long)}.
	 */
	@Test
	public void testGetJobExecutionsForJobInstance() throws Exception {
		String jobName = "job";
		Long jobInstanceId = 12L;
		JobInstance jobInstance = MetaDataInstanceFactory.createJobInstance(jobName, jobInstanceId);
		when(jobLocator.getJobNames()).thenReturn(Collections.<String>emptyList());
		when(jobInstanceDao.countJobInstances(jobName)).thenReturn(1);
		when(jobInstanceDao.getJobInstance(jobInstanceId)).thenReturn(jobInstance);
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		when(jobExecutionDao.findJobExecutions(jobInstance)).thenReturn(Arrays.asList(jobExecution));

		assertEquals(1, service.getJobExecutionsForJobInstance("job", jobInstance.getId()).size());
		verify(stepExecutionDao).addStepExecutions(jobExecution);
	}

	/**
	 * Test method for {@link SimpleJobService#getStepExecution(Long, Long)}.
	 */
	@Test
	public void testGetStepExecution() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(123L, Arrays
				.asList("step1"));
		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(null);
		when(executionContextDao.getExecutionContext(jobExecution)).thenReturn(new ExecutionContext());
		stepExecutionDao.addStepExecutions(jobExecution);
		StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
		Long stepExecutionId = stepExecution.getId();
		when(stepExecutionDao.getStepExecution(jobExecution, stepExecutionId)).thenReturn(stepExecution);
		when(executionContextDao.getExecutionContext(stepExecution)).thenReturn(new ExecutionContext());

		assertNotNull(service.getStepExecution(123L, 1234L));
	}

	@Test
	public void testGetStepExecutionWithUnserializableExecutionContent() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(123L, Arrays
				.asList("step1"));
		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(null);
		when(executionContextDao.getExecutionContext(jobExecution)).thenReturn(new ExecutionContext());
		stepExecutionDao.addStepExecutions(jobExecution);
		StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
		Long stepExecutionId = stepExecution.getId();
		when(stepExecutionDao.getStepExecution(jobExecution, stepExecutionId)).thenReturn(stepExecution);
		when(executionContextDao.getExecutionContext(stepExecution)).thenThrow(
				new IllegalStateException("Expected"));

		StepExecution result = service.getStepExecution(123L, 1234L);
		assertNotNull(result);
		// If there is a problem extracting the execution context it will be empty
		assertNotNull(result.getExecutionContext());
	}

	@Test
	public void testCountStepExecutions() throws Exception {
		when(stepExecutionDao.countStepExecutions("job", "step")).thenReturn(2);
		assertEquals(2, service.countStepExecutionsForStep("job", "step"));
	}

	@Test
	public void testListStepExecutions() throws Exception {
		when(stepExecutionDao.countStepExecutions("job", "step")).thenReturn(2);
		ArrayList<StepExecution> results = new ArrayList<StepExecution>();
		when(stepExecutionDao.findStepExecutions("job", "step", 0, 20)).thenReturn(results);
		assertSame(results, service.listStepExecutionsForStep("job", "step", 0, 20));
	}

	/**
	 * Test method for {@link SimpleJobService#listJobExecutionsForJob(String, int, int)}.
	 */
	@Test
	public void testListJobExecutionsForJob() throws Exception {
		String jobName = "job";
		when(jobLocator.getJobNames()).thenReturn(Collections.<String> emptyList());
		when(jobInstanceDao.countJobInstances(jobName)).thenReturn(1);
		when(jobExecutionDao.getJobExecutions("job", 0, 4)).thenReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobExecution(123L), MetaDataInstanceFactory
						.createJobExecution(124L)));
		assertEquals(2, service.listJobExecutionsForJob("job", 0, 4).size());
	}

	/**
	 * Test method for {@link SimpleJobService#listJobInstances(String, int, int)}.
	 */
	@Test
	public void testListJobInstances() throws Exception {
		String jobName = "job";
		when(jobLocator.getJobNames()).thenReturn(Collections.<String> emptyList());
		when(jobInstanceDao.countJobInstances(jobName)).thenReturn(1);
		when(jobInstanceDao.getJobInstances("job", 0, 4)).thenReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobInstance("job", 123L), MetaDataInstanceFactory
						.createJobInstance("job", 124L)));

			assertEquals(2, service.listJobInstances("job", 0, 4).size());
	}

	@Test
	public void testLastJobParameters() throws Exception {
		assertEquals(0, service.getLastJobParameters("job").getParameters().size());
	}

	@Test
	public void testRemoveInactives() throws Exception {

		testLaunch();

		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(null);
		when(executionContextDao.getExecutionContext(jobExecution)).thenReturn(new ExecutionContext());

		service.removeInactiveExecutions();

		verify(stepExecutionDao).addStepExecutions(jobExecution);
	}

	@Test
	public void testDestroy() throws Exception {

		service.destroy();

		verifyNoMoreInteractions(jobExecutionDao, stepExecutionDao, executionContextDao, jobInstanceDao);
	}

	@Test
	public void testDestroyWithActives() throws Exception {

		testLaunch();

		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.setEndTime(new Date());
		assertFalse(jobExecution.isRunning());

		when(jobExecutionDao.getJobExecution(123L)).thenReturn(jobExecution);
		when(jobInstanceDao.getJobInstance(jobExecution)).thenReturn(null);
		when(executionContextDao.getExecutionContext(jobExecution)).thenReturn(new ExecutionContext());

		service.destroy();

		verify(stepExecutionDao, times(2)).addStepExecutions(jobExecution);
	}

	public static class JsrItemReader extends AbstractItemReader {

		private Iterator<String> items = Arrays.asList("foo", "bar", "baz", "qux").iterator();

		@Override
		public Object readItem() throws Exception {
			if(items.hasNext()) {
				return items.next();
			}
			else {
				return null;
			}
		}
	}

	public static class JsrItemWriter extends AbstractItemWriter {

		public List<Object> writtenItems = new ArrayList<Object>();

		@Override
		public void writeItems(List<Object> items) throws Exception {
			writtenItems.addAll(items);
		}
	}
}
