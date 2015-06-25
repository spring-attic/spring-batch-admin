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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Arrays;
import java.util.Date;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Tests REST compliance of {@link BatchJobInstancesController} endpoints.
 * 
 * @author Ilayaperumal Gopinathan
 * @author Michael Minella
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDependencies.class, RestConfiguration.class})
@WebAppConfiguration
public class BatchJobInstancesControllerIntegrationTests extends AbstractControllerIntegrationTest {

	private JobExecution execution;

	private JobInstance jobInstance;

	@Before
	public void before() throws Exception {
		jobInstance = new JobInstance(0l, "job1");

		Date startTime = new Date();
		Date endTime = new Date();
		execution = new JobExecution(3L,
				new JobParametersBuilder().addString("foo", "bar").addLong("foo2", 0L).toJobParameters());
		execution.setExitStatus(ExitStatus.COMPLETED);
		execution.setStartTime(startTime);
		execution.setEndTime(endTime);
		execution.setLastUpdated(new Date());

		StepExecution stepExecution = new StepExecution("s1", execution);
		stepExecution.setLastUpdated(new Date());
		stepExecution.setId(1l);
		execution.addStepExecutions(Arrays.asList(stepExecution));
	}

	@Test
	public void testGetJobInstanceByInstanceId() throws Exception {
		when(jobService.getJobInstance(0)).thenReturn(jobInstance);
		when(jobService.getJobExecutionsForJobInstance(jobInstance.getJobName(), jobInstance.getId())).thenReturn(Arrays.asList(execution));

		mockMvc.perform(
				get("/batch/instances/0").accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.jobInstanceInfoResource.instanceId").value(0))
				.andExpect(jsonPath("$.jobInstanceInfoResource.jobName").value("job1"))
				.andExpect(jsonPath("$.jobInstanceInfoResource.jobExecutions", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.jobInstanceInfoResource.jobExecutions[0].executionId").value(3))
				.andExpect(jsonPath("$.jobInstanceInfoResource.jobExecutions[0].stepExecutions", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.jobInstanceInfoResource.jobExecutions[0].stepExecutions[0].stepName").value("s1"));
	}

	@Test
	public void testGetJobInstanceByJobName() throws Exception {
		when(jobService.listJobInstances("job1", 0, 20)).thenReturn(Arrays.asList(jobInstance, new JobInstance(3l, "job1")));
		when(jobService.getJobExecutionsForJobInstance(jobInstance.getJobName(), jobInstance.getId())).thenReturn(Arrays.asList(execution));

		mockMvc.perform(
				get("/batch/instances").param("jobname", "job1").param("startJobInstance", "0").param("pageSize", "20").accept(
						MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.pagedResources.content", Matchers.hasSize(2)))
				.andExpect(jsonPath("$.pagedResources.content[*].instanceId", contains(0, 3)))
				.andExpect(jsonPath("$.pagedResources.content[*].jobName", contains("job1", "job1")));
	}

	@Test
	public void testGetJobInstanceByInvalidInstanceId() throws Exception {
		when(jobService.getJobInstance(100l)).thenThrow(new NoSuchJobInstanceException("Batch Job instance with the id 100 doesn't exist"));

		mockMvc.perform(
				get("/batch/instances/100").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$[1][0].message", Matchers.is("Batch Job instance with the id 100 doesn't exist")));
	}
}
