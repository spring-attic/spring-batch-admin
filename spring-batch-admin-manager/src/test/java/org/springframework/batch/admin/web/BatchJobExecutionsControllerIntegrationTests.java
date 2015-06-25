/*
 * Copyright 2013-2015 the original author or authors.
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

package org.springframework.batch.admin.web;

import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Date;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.admin.service.JobSupport;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Tests REST compliance of {@link BatchJobExecutionsController} endpoints.
 *
 * @author Ilayaperumal Gopinathan
 * @author Gunnar Hillert
 * @author Michael Minella
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDependencies.class, RestConfiguration.class})
@WebAppConfiguration
public class BatchJobExecutionsControllerIntegrationTests extends AbstractControllerIntegrationTest {

	@Autowired
	private JobLocator jobLocator;

	private JobExecution execution1;
	private JobExecution execution2;

	@SuppressWarnings("unchecked")
	@Before
	public void before() throws Exception {
		JobInstance jobInstance1 = new JobInstance(2l, "job1");
		JobInstance jobInstance2 = new JobInstance(0l, "job1");

		execution1 = new JobExecution(jobInstance1, 3l, new JobParametersBuilder().addString("param1", "test").addLong("param2", 123l, false).toJobParameters(), null);
		execution1.setLastUpdated(new Date());
		execution2 = new JobExecution(jobInstance2, 0l, new JobParametersBuilder().addString("param1", "test").addLong("param2", 123l, false).toJobParameters(), null);
		execution2.setLastUpdated(new Date());

		StepExecution step1 = new StepExecution("step1", execution2);
		step1.setLastUpdated(new Date());
		step1.setId(1l);
		StepExecution step2 = new StepExecution("step2", execution2);
		step2.setLastUpdated(new Date());
		step2.setId(4l);

		execution2.addStepExecutions(Arrays.asList(step1, step2));
	}

	@Test
	public void testGetJobExecutionsByName() throws Exception {
		when(jobService.listJobExecutionsForJob("job1", 0, 20)).thenReturn(Arrays.asList(execution1));
		when(jobLocator.getJob("job1")).thenReturn(new JobSupport("job1"));
		when(jobService.countJobExecutions()).thenReturn(1);

		mockMvc.perform(
				get("/batch/executions").param("jobname", "job1").param("startJobInstance", "0").param("pageSize", "20").accept(
						MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.pagedResources.content", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.pagedResources.content[0].executionId").value(3))
				.andExpect(jsonPath("$.pagedResources.content[0].jobId").value(2))
				.andExpect(
						jsonPath("$.pagedResources.content[0].jobParameters.parameters.param1.value").value("test"))
				.andExpect(
						jsonPath("$.pagedResources.content[0].jobParameters.parameters.param1.type").value("STRING"))
				.andExpect(jsonPath("$.pagedResources.content[0].jobParameters.parameters.param1.identifying").value(
						true))
				.andExpect(
						jsonPath("$.pagedResources.content[0].jobParameters.parameters.param2.value").value(123))
				.andExpect(
						jsonPath("$.pagedResources.content[0].jobParameters.parameters.param2.type").value("LONG"))
				.andExpect(jsonPath("$.pagedResources.content[0].jobParameters.parameters.param2.identifying").value(
						false));
	}

	@Test
	public void testGetBatchJobExecutions() throws Exception {
		when(jobService.listJobExecutions(0, 20)).thenReturn(Arrays.asList(execution2, execution1));
		when(jobLocator.getJob("job1")).thenReturn(new JobSupport("job1"));
		when(jobService.countJobExecutions()).thenReturn(1);

		mockMvc.perform(
				get("/batch/executions").accept(
						MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.pagedResources.content", Matchers.hasSize(2)))
				.andExpect(jsonPath("$.pagedResources.content[*].executionId", contains(0, 3)))
				.andExpect(jsonPath("$.pagedResources.content[*].stepExecutions", Matchers.hasSize(2)))
				.andExpect(jsonPath("$.pagedResources.content[*].jobId", contains(0, 2)))
				.andExpect(jsonPath("$.pagedResources.content[*].jobParameters.parameters.param1.value", contains("test", "test")))
				.andExpect(jsonPath("$.pagedResources.content[*].jobParameters.parameters.param1.type", contains("STRING", "STRING")))
				.andExpect(jsonPath("$.pagedResources.content[*].jobParameters.parameters.param1.identifying", contains(true, true)))
				.andExpect(jsonPath("$.pagedResources.content[*].jobParameters.parameters.param2.value", contains(123, 123)))
				.andExpect(jsonPath("$.pagedResources.content[*].jobParameters.parameters.param2.type", contains("LONG", "LONG")))
				.andExpect(jsonPath("$.pagedResources.content[*].jobParameters.parameters.param2.identifying", contains(false, false)));
	}

	@Test
	public void testGetBatchJobExecutionsPaginated() throws Exception {
		when(jobService.listJobExecutions(5, 5)).thenReturn(Arrays.asList(execution2, execution1));
		when(jobLocator.getJob("job1")).thenReturn(new JobSupport("job1"));
		when(jobService.countJobExecutions()).thenReturn(1);

		mockMvc.perform(
				get("/batch/executions").param("page", "1").param("size", "5").accept(
						MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(
				jsonPath("$.pagedResources.content[*]", Matchers.hasSize(2)));
	}

	@Test
	public void testGetSingleBatchJobExecution() throws Exception {
		when(jobService.getJobExecution(0l)).thenReturn(execution2);
		when(jobLocator.getJob("job1")).thenReturn(new JobSupport("job1"));

		mockMvc.perform(
				get("/batch/executions/0").accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.jobExecutionInfoResource.executionId", Matchers.is(0)))
				.andExpect(jsonPath("$.jobExecutionInfoResource.jobParameters.parameters.param1.type", Matchers.is("STRING")))
				.andExpect(jsonPath("$.jobExecutionInfoResource.jobParameters.parameters.param1.identifying", Matchers.is(true)))
				.andExpect(jsonPath("$.jobExecutionInfoResource.jobParameters.parameters.param1.value", Matchers.is("test")))
				.andExpect(jsonPath("$.jobExecutionInfoResource.jobParameters.parameters.param2.type", Matchers.is("LONG")))
				.andExpect(jsonPath("$.jobExecutionInfoResource.jobParameters.parameters.param2.identifying", Matchers.is(false)))
				.andExpect(jsonPath("$.jobExecutionInfoResource.jobParameters.parameters.param2.value", Matchers.is(123)))
				.andExpect(jsonPath("$.jobExecutionInfoResource.stepExecutions", Matchers.hasSize(2)))
				.andExpect(jsonPath("$.jobExecutionInfoResource.stepExecutionCount", Matchers.is(2)))
				.andExpect(jsonPath("$.jobExecutionInfoResource.name", Matchers.is("job1")));
	}

	@Test
	public void testGetNonExistingBatchJobExecution() throws Exception {
		when(jobService.getJobExecution(99999l)).thenThrow(new NoSuchJobExecutionException("Could not find jobExecution with id 99999"));

		mockMvc.perform(get("/batch/executions/99999").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound()).andDo(print())
				.andExpect(jsonPath("$[1][0].message", Matchers.is("Could not find jobExecution with id 99999")));
	}

	@Test
	public void testStopAllJobExecutions() throws Exception {
		mockMvc.perform(put("/batch/executions?stop=true")).andExpect(status().isOk());

		verify(jobService).stopAll();
	}

	@Test
	public void testStopJobExecution() throws Exception {
		mockMvc.perform(put("/batch/executions/{executionId}?stop=true", "0")).andExpect(status().isOk());

		verify(jobService).stop(0l);
	}

	@Test
	public void testRestartNonExistingJobExecution() throws Exception {
		when(jobService.getJobExecution(1234l)).thenThrow(new NoSuchJobExecutionException(""));

		mockMvc.perform(put("/batch/executions/{executionId}?restart=true", "1234")).andExpect(status().isNotFound()).andExpect(
				jsonPath("$[1][0].message", Matchers.is("Could not find jobExecution with id 1234")));
	}

	@Test
	public void testRestartAlreadyRunningJobExecution() throws Exception {
		JobInstance instance = new JobInstance(4l, "job4running");
		JobExecution execution = new JobExecution(instance, 4l, new JobParameters(), null);
		when(jobService.getJobExecution(4l)).thenReturn(execution);

		mockMvc.perform(put("/batch/executions/{executionId}?restart=true", "4")).andExpect(status().isBadRequest()).andExpect(
				jsonPath(
						"$[1][0].message",
						Matchers.is("Job Execution for this job is already running: JobInstance: id=4, version=null, Job=[job4running]")));
	}

	@Test
	public void testRestartAlreadyCompleteJobExecution() throws Exception {
		JobInstance instance = new JobInstance(33l, "job4running");
		JobExecution execution = new JobExecution(instance, 33l, new JobParameters(), null);
		execution.setEndTime(new Date());
		execution.upgradeStatus(BatchStatus.COMPLETED);

		when(jobService.getJobExecution(33l)).thenReturn(execution);
		when(jobLocator.getJob("job4running")).thenReturn(new JobSupport("job4running"));

		mockMvc.perform(put("/batch/executions/{executionId}?restart=true", "33")).andExpect(status().isBadRequest()).andExpect(
				jsonPath("$[1][0].message", Matchers.is("Job Execution 33 is already complete.")));
	}

	@Test
	public void testRestartJobExecutionWithJobNotAvailable() throws Exception {
		when(jobService.getJobExecution(3333l)).thenThrow(new NoSuchJobExecutionException(""));

		mockMvc.perform(put("/batch/executions/{executionId}?restart=true", "3333")).andExpect(status().isNotFound()).andExpect(
				jsonPath("$[1][0].message", Matchers.is("Could not find jobExecution with id 3333")));
	}

	@Test
	public void testRestartJobExecutionWithInvalidJobParameters() throws Exception {
		JobInstance instance = new JobInstance(5l, "job4running");
		JobExecution execution = new JobExecution(instance, 5l, new JobParameters(), null);
		execution.setEndTime(new Date());

		SimpleJob job4running = new SimpleJob("job4running");
		DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
		validator.setRequiredKeys(new String [] {"missing-key"});
		job4running.setJobParametersValidator(validator);

		when(jobService.getJobExecution(5l)).thenReturn(execution);
		when(jobLocator.getJob("job4running")).thenReturn(job4running);

		mockMvc.perform(put("/batch/executions/{executionId}?restart=true", "5")).andExpect(status().isBadRequest()).andExpect(
				jsonPath("$[1][0].message", Matchers.is("The Job Parameters for Job Execution 5 are invalid.")));
	}

	@Test
	public void testRestartNonRestartableJob() throws Exception {
		JobInstance instance = new JobInstance(2l, "job2");
		JobExecution execution = new JobExecution(instance, 2l, new JobParameters(), null);
		execution.setEndTime(new Date());

		SimpleJob job4running = new SimpleJob("job2");
		job4running.setRestartable(false);

		when(jobService.getJobExecution(2l)).thenReturn(execution);
		when(jobLocator.getJob("job2")).thenReturn(job4running);

		mockMvc.perform(put("/batch/executions/{executionId}?restart=true", "2")).andExpect(status().isBadRequest()).andExpect(
				jsonPath("$[1][0].message", Matchers.is("The job 'job2' is not restartable.")));
	}

	@Test
	public void testStopJobExecutionNotRunning() throws Exception {
		when(jobService.stop(3l)).thenThrow(new JobExecutionNotRunningException(""));

		mockMvc.perform(put("/batch/executions/{executionId}?stop=true", "3")).andExpect(status().isNotFound()).andDo(print()).andExpect(
				jsonPath("$[1][0].message", Matchers.is("Job execution with executionId 3 is not running.")));
	}

	@Test
	public void testStopJobExecutionNotExists() throws Exception {
		when(jobService.stop(5l)).thenThrow(new NoSuchJobExecutionException(""));

		mockMvc.perform(put("/batch/executions/{executionId}?stop=true", "5")).andExpect(status().isNotFound()).andExpect(
				jsonPath("$[1][0].message",
						Matchers.is("Could not find jobExecution with id 5")));
	}
}
