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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Date;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.admin.service.NoSuchStepExecutionException;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Tests REST compliance of {@link BatchStepExecutionsController} endpoints.
 *
 * @author Gunnar Hillert
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDependencies.class, RestConfiguration.class})
@WebAppConfiguration
public class BatchStepExecutionsControllerIntegrationTests extends AbstractControllerIntegrationTest {

	@Test
	public void testGetBatchStepExecutions() throws Exception {
		JobExecution jobExecution = new JobExecution(2l);
		jobExecution.setLastUpdated(new Date());
		StepExecution execution1 = new StepExecution("step1", jobExecution, 1l);
		execution1.setLastUpdated(new Date());
		StepExecution execution2 = new StepExecution("step2", jobExecution, 2l);
		execution2.setLastUpdated(new Date());
		StepExecution execution3 = new StepExecution("step3", jobExecution, 3l);
		execution3.setLastUpdated(new Date());

		when(jobService.getStepExecutions(2l)).thenReturn(Arrays.asList(execution1, execution2, execution3));

		mockMvc.perform(
				get("/batch/executions/2/steps").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(print())
				.andExpect(jsonPath("$.stepExecutionInfoResourceList", Matchers.hasSize(3)))
				.andExpect(jsonPath("$.stepExecutionInfoResourceList[*].executionId", contains(1, 2, 3)))
				.andExpect(jsonPath("$.stepExecutionInfoResourceList[*].jobExecutionId", contains(2, 2, 2)))
				.andExpect(jsonPath("$.stepExecutionInfoResourceList[*].stepName", contains("step1", "step2", "step3")))
				.andExpect(jsonPath("$.stepExecutionInfoResourceList[*].links[*].href", contains(
						"http://localhost/batch/executions/2/steps/1",
						"http://localhost/batch/executions/2/steps/2",
						"http://localhost/batch/executions/2/steps/3")));
	}

	@Test
	public void testGetBatchStepExecutionsNotExists() throws Exception {
		when(jobService.getStepExecutions(5555l)).thenThrow(new NoSuchJobExecutionException(""));

		mockMvc.perform(get("/batch/executions/{executionId}/steps", "5555")).andDo(print()).andExpect(status().isNotFound()).andExpect(
				jsonPath("$[1][0].message",
						Matchers.is("Could not find jobExecution with id 5555")));

	}

	@Test
	public void testGetSingleBatchStepExecution() throws Exception {
		JobExecution jobExecution = new JobExecution(2l, new JobParametersBuilder().addString("param1", "test").addLong("param2", 123l).toJobParameters());
		jobExecution.setLastUpdated(new Date());
		StepExecution execution = new StepExecution("step1", jobExecution, 1l);
		execution.setLastUpdated(new Date());
		execution.getExecutionContext().put("contextTestKey", "someValue");

		when(jobService.getStepExecution(2l, 1l)).thenReturn(execution);

		mockMvc.perform(
				get("/batch/executions/2/steps/1").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(print())
				.andExpect(jsonPath("$.stepExecutionInfoResource.executionId", Matchers.is(1)))
				.andExpect(jsonPath("$.stepExecutionInfoResource.jobExecutionId", Matchers.is(2)))
				.andExpect(jsonPath("$.stepExecutionInfoResource.stepName", Matchers.is("step1")))
				.andExpect(jsonPath("$.stepExecutionInfoResource.executionContext", Matchers.not(Matchers.empty())))
				.andExpect(jsonPath("$.stepExecutionInfoResource.executionContext['contextTestKey']", Matchers.is("someValue")));
	}

	@Test
	public void testGetSingleBatchStepExecutionForNonExistingJobExecution() throws Exception {
		when(jobService.getStepExecution(5555l, 1l)).thenThrow(new NoSuchJobExecutionException(""));

		mockMvc.perform(get("/batch/executions/{jobExecutionId}/steps/{stepExecutionId}", "5555", "1")).andExpect(
				status().isNotFound()).andExpect(
				jsonPath("$[1][0].message",
						Matchers.is("Could not find jobExecution with id 5555")));
	}

	@Test
	public void testGetSingleBatchStepExecutionThatDoesNotExist() throws Exception {
		when(jobService.getStepExecution(2l, 5555l)).thenThrow(new NoSuchStepExecutionException(""));

		mockMvc.perform(get("/batch/executions/{jobExecutionId}/steps/{stepExecutionId}", "2", "5555")).andExpect(
				status().isNotFound()).andExpect(
				jsonPath("$[1][0].message",
						Matchers.is("Could not find step execution with id 5555")));
	}

	@Test
	public void testGetBatchStepExecutionProgress() throws Exception {
		JobInstance jobInstance = new JobInstance(1l, "job1");
		JobExecution jobExecution = new JobExecution(jobInstance, 2l, new JobParametersBuilder().addString("param1", "test").addLong("param2", 123l).toJobParameters(), null);
		jobExecution.setLastUpdated(new Date());
		StepExecution execution = new StepExecution("step1", jobExecution, 1l);
		execution.setLastUpdated(new Date());

		when(jobService.getStepExecution(2l, 1l)).thenReturn(execution);
		when(jobService.countStepExecutionsForStep("job", "step1")).thenReturn(1);
		when(jobService.listStepExecutionsForStep("job1", "step1", 0, 1000)).thenReturn(Arrays.asList(new StepExecution("step1", new JobExecution(5l))));

		mockMvc.perform(
				get("/batch/executions/2/steps/1/progress").accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stepExecutionProgressInfoResource.executionId", Matchers.is(1)))
				.andExpect(jsonPath("$.stepExecutionProgressInfoResource.percentageComplete", Matchers.is(0.5)));
	}

	@Test
	public void testGetProgressForJobExecutionNotExists() throws Exception {
		when(jobService.getStepExecution(5555l, 2l)).thenThrow(new NoSuchJobExecutionException(""));

		mockMvc.perform(get("/batch/executions/{jobExecutionId}/steps/{stepExecutionId}/progress", "5555", "2")).andExpect(
				status().isNotFound()).andExpect(
				jsonPath("$[1][0].message",
						Matchers.is("Could not find jobExecution with id 5555")));
	}

	@Test
	public void testGetProgressForStepExecutionNotExists() throws Exception {
		when(jobService.getStepExecution(3l, 5555l)).thenThrow(new NoSuchStepExecutionException(""));

		mockMvc.perform(get("/batch/executions/{jobExecutionId}/steps/{stepExecutionId}/progress", "3", "5555")).andExpect(
				status().isNotFound()).andExpect(
				jsonPath("$[1][0].message",
						Matchers.is("Could not find step execution with id 5555")));
	}
}
