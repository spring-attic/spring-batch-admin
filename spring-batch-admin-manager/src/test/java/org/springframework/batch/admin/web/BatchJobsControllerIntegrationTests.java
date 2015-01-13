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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.admin.domain.JobExecutionInfo;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Tests REST compliance of {@link BatchJobsController} endpoints.
 *
 * @author Ilayaperumal Gopinathan
 * @author Gunnar Hillert
 * @author Michael Minella
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDependencies.class, RestConfiguration.class})
@WebAppConfiguration
public class BatchJobsControllerIntegrationTests extends AbstractControllerIntegrationTest {

	private JobExecution execution;

	private TimeZone timeZone = TimeZone.getTimeZone("UTC");

	@Before
	public void before() throws Exception {
		Date startTime = new Date(1000);
		Date endTime = new Date(2000);
		execution = new JobExecution(0L,
				new JobParametersBuilder().addString("foo", "bar").addLong("foo2", 0L).toJobParameters());
		execution.setExitStatus(ExitStatus.COMPLETED);
		execution.setStartTime(startTime);
		execution.setEndTime(endTime);
		execution.setLastUpdated(new Date());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetBatchJobs() throws Exception {
		when(jobService.countJobs()).thenReturn(2);
		when(jobService.listJobs(0, 20)).thenReturn(Arrays.asList("job1", "job2"));
		when(jobService.isLaunchable("job1")).thenReturn(false);
		when(jobService.isLaunchable("job2")).thenReturn(true);
		when(jobService.countJobExecutionsForJob("job1")).thenReturn(2);
		when(jobService.countJobExecutionsForJob("job2")).thenReturn(1);
		when(jobService.isIncrementable("job1")).thenReturn(false);
		when(jobService.isIncrementable("job2")).thenReturn(true);
		when(jobService.listJobExecutionsForJob("job1", 0, 1)).thenReturn(Arrays.asList(execution));

		JobExecutionInfo info = new JobExecutionInfo(execution, timeZone);
		mockMvc.perform(
				get("/batch/configurations").accept(
						MediaType.APPLICATION_JSON)).andDo(print()).andExpect(
				status().isOk()).andExpect(jsonPath("$.content[1]", Matchers.hasSize(2))).andExpect(
				jsonPath("$.content[1][*].executionCount", contains(2, 1))).andExpect(
				jsonPath("$.content[1][*].launchable", contains(false, true))).andExpect(
				jsonPath("$.content[1][*].incrementable", contains(false, true))).andExpect(
				jsonPath("$.content[1][*].jobInstanceId", contains(nullValue(), nullValue()))).andExpect(
				jsonPath("$.content[1][*].startTime", contains("1970-01-01T00:00:01.000Z", null))).andExpect(
				jsonPath("$.content[1][*].endTime", contains("1970-01-01T00:00:02.000Z", null))).andExpect(
				jsonPath("$.content[1][*].stepExecutionCount", contains(info.getStepExecutionCount(), 0))).andExpect(
				jsonPath("$.content[1][*].jobParameters.parameters['foo'].value", contains("bar")))

				// should contain the display name (ie- without the .job suffix)
				.andExpect(jsonPath("$.content[1][0].name", equalTo("job1"))).andExpect(
						jsonPath("$.content[1][0].jobInstanceId", nullValue()))

				.andExpect(jsonPath("$.content[1][1].name", equalTo("job2"))).andExpect(
						jsonPath("$.content[1][1].jobInstanceId", nullValue()))

				// exit status is non null for job 0 and null for job 1
				.andExpect(
						jsonPath("$.content[1][0].exitStatus.exitDescription",
								equalTo(execution.getExitStatus().getExitDescription()))).andExpect(
						jsonPath("$.content[1][0].exitStatus.exitCode", equalTo(execution.getExitStatus().getExitCode()))).andExpect(
						jsonPath("$.content[1][0].exitStatus.running", equalTo(false))).andExpect(
						jsonPath("$.content[1][1].exitStatus", nullValue()));
	}

	@Test
	public void testGetPagedBatchJobs() throws Exception {
		when(jobService.countJobs()).thenReturn(2);
		when(jobService.listJobs(0, 1)).thenReturn(Arrays.asList("job1"));
		when(jobService.isLaunchable("job1")).thenReturn(false);
		when(jobService.countJobExecutionsForJob("job1")).thenReturn(2);
		when(jobService.isIncrementable("job1")).thenReturn(false);
		when(jobService.listJobExecutionsForJob("job1", 0, 1)).thenReturn(Arrays.asList(execution));

		JobExecutionInfo info = new JobExecutionInfo(execution, timeZone);
		mockMvc.perform(
				get("/batch/configurations").param("page", "0").param("size", "1").accept(
						MediaType.APPLICATION_JSON)).andDo(print()).andExpect(
				status().isOk()).andExpect(jsonPath("$.content[1]", Matchers.hasSize(1))).andExpect(
				jsonPath("$.content[1][*].executionCount", contains(2))).andExpect(
				jsonPath("$.content[1][*].launchable", contains(false))).andExpect(
				jsonPath("$.content[1][*].incrementable", contains(false))).andExpect(
				jsonPath("$.content[1][*].jobInstanceId", contains(nullValue()))).andExpect(
				jsonPath("$.content[1][*].startTime", contains("1970-01-01T00:00:01.000Z"))).andExpect(
				jsonPath("$.content[1][*].endTime", contains("1970-01-01T00:00:02.000Z"))).andExpect(
				jsonPath("$.content[1][*].stepExecutionCount", contains(info.getStepExecutionCount()))).andExpect(
				jsonPath("$.content[1][*].jobParameters.parameters['foo'].value", contains("bar"))).andExpect(
				jsonPath("$.content[1][*].jobParameters.parameters['foo2'].value[1]", contains(0)))

				// should contain the display name (ie- without the .job suffix)
				.andExpect(jsonPath("$.content[1][0].name", equalTo("job1"))).andExpect(
						jsonPath("$.content[1][0].jobInstanceId", nullValue()))

				.andExpect(
						jsonPath("$.content[1][0].exitStatus.exitDescription",
								equalTo(execution.getExitStatus().getExitDescription()))).andExpect(
						jsonPath("$.content[1][0].exitStatus.exitCode", equalTo(execution.getExitStatus().getExitCode()))).andExpect(
						jsonPath("$.content[1][0].exitStatus.running", equalTo(false)));
	}

	@Test
	public void testGetJobInfoByJobName() throws Exception {
		when(jobService.isLaunchable("job1")).thenReturn(false);
		when(jobService.countJobExecutionsForJob("job1")).thenReturn(2);
		when(jobService.isIncrementable("job1")).thenReturn(false);
		JobExecution jobExecution = new JobExecution(5l);
		jobExecution.setLastUpdated(new Date());
		when(jobService.listJobExecutionsForJob("job1", 0, 1)).thenReturn(Arrays.asList(jobExecution));

		mockMvc.perform(
				get("/batch/configurations/job1")
						.param("startJobInstance", "0")
						.param("pageSize", "20")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(print())
				.andExpect(jsonPath("$.executionCount").value(2))
				.andExpect(jsonPath("$.launchable").value(false))
				.andExpect(jsonPath("$.incrementable").value(false))
				.andExpect(jsonPath("$.jobInstanceId", nullValue()));
	}

}
