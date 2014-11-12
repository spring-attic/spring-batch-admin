/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.batch.admin.domain;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.test.util.JsonPathExpectationsHelper;

/**
 * @author Michael Minella
 */
public class JobExecutionInfoResourceSerializationTests extends AbstractSerializationTests<JobExecutionInfoResource> {

	@Override
	public void assertJson(String json) throws Exception {
		new JsonPathExpectationsHelper("$.abandonable").assertValue(json, false);
		new JsonPathExpectationsHelper("$.duration").assertValue(json, "00:00:00");
		new JsonPathExpectationsHelper("$.executionId").assertValue(json, 2);
		new JsonPathExpectationsHelper("$.jobId").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.jobParametersString").assertValue(json, "foo=bar");
		new JsonPathExpectationsHelper("$.name").assertValue(json, "job1");
		new JsonPathExpectationsHelper("$.restartable").assertValue(json, false);
		new JsonPathExpectationsHelper("$.startDate").assertValue(json, "1970-01-01");
		new JsonPathExpectationsHelper("$.startTime").assertValue(json, "00:00:00");
		new JsonPathExpectationsHelper("$.stepExecutionCount").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.stoppable").assertValue(json, false);

		new JsonPathExpectationsHelper("$.jobExecution.createTime").assertValue(json, "1970-01-01T00:00:00.000Z");
		new JsonPathExpectationsHelper("$.jobExecution.endTime").assertValue(json, "1970-01-01T00:00:00.100Z");
		new JsonPathExpectationsHelper("$.jobExecution.exitStatus.exitCode").assertValue(json, "COMPLETE");
		new JsonPathExpectationsHelper("$.jobExecution.exitStatus.exitDescription").assertValue(json, "Exit Description");
		new JsonPathExpectationsHelper("$.jobExecution.exitStatus.running").assertValue(json, false);
		new JsonPathExpectationsHelper("$.jobExecution.id").assertValue(json, 2);
		new JsonPathExpectationsHelper("$.jobExecution.jobConfigurationName").assertValue(json, "configName.xml");
		new JsonPathExpectationsHelper("$.jobExecution.jobInstance.id").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.jobExecution.jobInstance.jobName").assertValue(json, "job1");
		new JsonPathExpectationsHelper("$.jobExecution.jobParameters.parameters['foo'].value").assertValue(json, "bar");
		new JsonPathExpectationsHelper("$.jobExecution.lastUpdated").assertValue(json, "1970-01-01T00:00:00.101Z");
		new JsonPathExpectationsHelper("$.jobExecution.startTime").assertValue(json, "1970-01-01T00:00:00.001Z");
		new JsonPathExpectationsHelper("$.jobExecution.status").assertValue(json, "COMPLETED");
		new JsonPathExpectationsHelper("$.jobExecution.version").assertValue(json, 1);

		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions").assertValueIsArray(json);
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].commitCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].endTime").assertValue(json, "1970-01-01T00:00:00.099Z");
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].exitStatus.exitCode").assertValue(json, "ALL DONE");
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].exitStatus.exitDescription").assertValue(json, "Step Exit Description");
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].exitStatus.running").assertValue(json, false);
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].filterCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].id").assertValue(json, 3);
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].processSkipCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].readCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].readSkipCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].rollbackCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].startTime").assertValue(json, "1970-01-01T00:00:00.002Z");
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].status").assertValue(json, "COMPLETED");
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].stepName").assertValue(json, "step1");
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].terminateOnly").assertValue(json, false);
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].writeCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecution.stepExecutions[0].writeSkipCount").assertValue(json, 0);
	}

	@Override
	public void assertObject(JobExecutionInfoResource jobExecutionInfoResource) throws Exception {
		assertEquals("00:00:00", jobExecutionInfoResource.getDuration());
		assertEquals("foo=bar", jobExecutionInfoResource.getJobParametersString());
		assertEquals(1, jobExecutionInfoResource.getJobParameters().size());
		assertEquals("bar", jobExecutionInfoResource.getJobParameters().getProperty("foo"));
		assertEquals("job1", jobExecutionInfoResource.getName());
		assertEquals("1970-01-01", jobExecutionInfoResource.getStartDate());
		assertEquals("00:00:00", jobExecutionInfoResource.getStartTime());
		assertEquals(2l, (long) jobExecutionInfoResource.getExecutionId());
		assertEquals(1, jobExecutionInfoResource.getStepExecutionCount());
		assertEquals(TimeZone.getTimeZone("CDT"), jobExecutionInfoResource.getTimeZone());
		assertEquals(1, (int) jobExecutionInfoResource.getJobExecution().getVersion());
		assertEquals(new Date(0), jobExecutionInfoResource.getJobExecution().getCreateTime());
		assertEquals(new Date(100), jobExecutionInfoResource.getJobExecution().getEndTime());
		assertEquals(0, jobExecutionInfoResource.getJobExecution().getExecutionContext().size());
		assertEquals(new ExitStatus("COMPLETE", "Exit Description"), jobExecutionInfoResource.getJobExecution().getExitStatus());
		assertEquals(0, jobExecutionInfoResource.getJobExecution().getFailureExceptions().size());
		assertEquals("configName.xml", jobExecutionInfoResource.getJobExecution().getJobConfigurationName());
		assertEquals(1l, (long) jobExecutionInfoResource.getJobExecution().getJobId());
		assertEquals("job1", jobExecutionInfoResource.getJobExecution().getJobInstance().getJobName());
		assertEquals(1l, jobExecutionInfoResource.getJobExecution().getJobInstance().getInstanceId());
		assertEquals(1, jobExecutionInfoResource.getJobExecution().getJobParameters().toProperties().size());
		assertEquals(new Date(101), jobExecutionInfoResource.getJobExecution().getLastUpdated());
		assertEquals(new Date(1), jobExecutionInfoResource.getJobExecution().getStartTime());
		assertEquals(BatchStatus.COMPLETED, jobExecutionInfoResource.getJobExecution().getStatus());
		assertEquals(1, jobExecutionInfoResource.getJobExecution().getStepExecutions().size());
		assertEquals("step1", jobExecutionInfoResource.getJobExecution().getStepExecutions().iterator().next().getStepName());
		assertEquals(2l, (long) jobExecutionInfoResource.getJobExecution().getId());
	}

	@Override
	public JobExecutionInfoResource getSerializationValue() {
		JobInstance jobInstance = new JobInstance(1l, "job1");
		JobExecution jobExecution = new JobExecution(jobInstance, 2l, new JobParametersBuilder().addString("foo", "bar").toJobParameters(), "configName.xml");
		jobExecution.setVersion(1);
		jobExecution.setExitStatus(new ExitStatus("COMPLETE", "Exit Description"));
		jobExecution.setCreateTime(new Date(0));
		jobExecution.setStartTime(new Date(1));
		jobExecution.setEndTime(new Date(100));
		jobExecution.setLastUpdated(new Date(101));
		jobExecution.setStatus(BatchStatus.COMPLETED);
		StepExecution stepExecution = new StepExecution("step1", jobExecution, 3l);
		stepExecution.setExitStatus(new ExitStatus("ALL DONE", "Step Exit Description"));
		stepExecution.setStatus(BatchStatus.COMPLETED);
		stepExecution.setStartTime(new Date(2));
		stepExecution.setEndTime(new Date(99));
		jobExecution.addStepExecutions(Arrays.asList(stepExecution));
		return new JobExecutionInfoResource(jobExecution, TimeZone.getTimeZone("CDT"));
	}
}
