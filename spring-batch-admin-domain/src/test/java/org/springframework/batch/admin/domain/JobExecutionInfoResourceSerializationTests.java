/*
 * Copyright 2014-2015 the original author or authors.
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
		System.out.println(json);
		new JsonPathExpectationsHelper("$.abandonable").assertValue(json, false);
		new JsonPathExpectationsHelper("$.createDate").assertValue(json, "1969-12-31T18:00:00.000-06:00");
		new JsonPathExpectationsHelper("$.endTime").assertValue(json, "1969-12-31T18:00:02.000-06:00");
		new JsonPathExpectationsHelper("$.executionId").assertValue(json, 2);
		new JsonPathExpectationsHelper("$.exitStatus.exitCode").assertValue(json, "COMPLETE");
		new JsonPathExpectationsHelper("$.exitStatus.exitDescription").assertValue(json, "Exit Description");
		new JsonPathExpectationsHelper("$.exitStatus.running").assertValue(json, false);
		new JsonPathExpectationsHelper("$.jobConfigurationName").assertValue(json, "configName.xml");
		new JsonPathExpectationsHelper("$.jobId").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.jobParameters.parameters['foo'].identifying").assertValue(json, true);
		new JsonPathExpectationsHelper("$.jobParameters.parameters['foo'].type").assertValue(json, "STRING");
		new JsonPathExpectationsHelper("$.jobParameters.parameters['foo'].value").assertValue(json, "bar");
		new JsonPathExpectationsHelper("$.jobParameters.parameters['baz'].identifying").assertValue(json, false);
		new JsonPathExpectationsHelper("$.jobParameters.parameters['baz'].type").assertValue(json, "DOUBLE");
		new JsonPathExpectationsHelper("$.jobParameters.parameters['baz'].value").assertValue(json, 3.0);
		new JsonPathExpectationsHelper("$.lastUpdated").assertValue(json, "1969-12-31T18:00:03.000-06:00");
		new JsonPathExpectationsHelper("$.name").assertValue(json, "job1");
		new JsonPathExpectationsHelper("$.restartable").assertValue(json, false);
		new JsonPathExpectationsHelper("$.startTime").assertValue(json, "1969-12-31T18:00:01.000-06:00");
		new JsonPathExpectationsHelper("$.stepExecutionCount").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.stoppable").assertValue(json, false);
		new JsonPathExpectationsHelper("$.timeZone[1]").assertValue(json, "America/Chicago");
		new JsonPathExpectationsHelper("$.version").assertValue(json, 1);

		new JsonPathExpectationsHelper("$.stepExecutions").assertValueIsArray(json);
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].commitCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].endTime").assertValue(json, "1969-12-31T18:00:02.000-06:00");
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].exitStatus.exitCode").assertValue(json, "ALL DONE");
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].exitStatus.exitDescription").assertValue(json, "Step Exit Description");
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].exitStatus.running").assertValue(json, false);
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].filterCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].executionId").assertValue(json, 3);
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].processSkipCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].readCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].readSkipCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].rollbackCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].startTime").assertValue(json, "1969-12-31T18:00:01.000-06:00");
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].status").assertValue(json, "COMPLETED");
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].stepName").assertValue(json, "step1");
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].terminateOnly").assertValue(json, false);
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].writeCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.stepExecutions[1].[0].writeSkipCount").assertValue(json, 0);
	}

	@Override
	public void assertObject(JobExecutionInfoResource jobExecutionInfoResource) throws Exception {
		assertEquals("{foo=bar, baz=3.0}", jobExecutionInfoResource.getJobParameters().toString());
		assertEquals(2, jobExecutionInfoResource.getJobParameters().toProperties().size());
		assertEquals("bar", jobExecutionInfoResource.getJobParameters().getString("foo"));
		assertEquals(true, jobExecutionInfoResource.getJobParameters().getParameters().get("foo").isIdentifying());
		assertEquals("3.0", jobExecutionInfoResource.getJobParameters().getString("baz"));
		assertEquals(false, jobExecutionInfoResource.getJobParameters().getParameters().get("baz").isIdentifying());
		assertEquals("job1", jobExecutionInfoResource.getName());
		assertEquals("1969-12-31T18:00:01.000-06:00", jobExecutionInfoResource.getStartTime());
		assertEquals(2l, (long) jobExecutionInfoResource.getExecutionId());
		assertEquals(1, jobExecutionInfoResource.getStepExecutionCount());
		assertEquals(TimeZone.getTimeZone("America/Chicago"), jobExecutionInfoResource.getTimeZone());
		assertEquals(1, (int) jobExecutionInfoResource.getVersion());
		assertEquals("1969-12-31T18:00:00.000-06:00", jobExecutionInfoResource.getCreateDate());
		assertEquals(0, jobExecutionInfoResource.getExecutionContext().size());
		assertEquals(new ExitStatus("COMPLETE", "Exit Description"), jobExecutionInfoResource.getExitStatus());
		assertEquals(0, jobExecutionInfoResource.getFailureExceptions().size());
		assertEquals("configName.xml", jobExecutionInfoResource.getJobConfigurationName());
		assertEquals(1l, (long) jobExecutionInfoResource.getJobId());
		assertEquals("job1", jobExecutionInfoResource.getName());
		assertEquals("1969-12-31T18:00:03.000-06:00", jobExecutionInfoResource.getLastUpdated());
		assertEquals("1969-12-31T18:00:01.000-06:00", jobExecutionInfoResource.getStartTime());
		assertEquals(BatchStatus.COMPLETED, jobExecutionInfoResource.getStatus());
		assertEquals(1, jobExecutionInfoResource.getStepExecutions().size());
		assertEquals("step1", jobExecutionInfoResource.getStepExecutions().iterator().next().getStepName());
		assertEquals(2l, (long) jobExecutionInfoResource.getExecutionId());
	}

	@Override
	public JobExecutionInfoResource getSerializationValue() {
		JobInstance jobInstance = new JobInstance(1l, "job1");
		JobExecution jobExecution = new JobExecution(jobInstance, 2l, new JobParametersBuilder().addString("foo", "bar").addDouble("baz", 3.0, false).toJobParameters(), "configName.xml");
		jobExecution.setVersion(1);
		jobExecution.setExitStatus(new ExitStatus("COMPLETE", "Exit Description"));
		jobExecution.setCreateTime(new Date(0));
		jobExecution.setStartTime(new Date(1000));
		jobExecution.setEndTime(new Date(2000));
		jobExecution.setLastUpdated(new Date(3000));
		jobExecution.setStatus(BatchStatus.COMPLETED);
		StepExecution stepExecution = new StepExecution("step1", jobExecution, 3l);
		stepExecution.setExitStatus(new ExitStatus("ALL DONE", "Step Exit Description"));
		stepExecution.setStatus(BatchStatus.COMPLETED);
		stepExecution.setStartTime(new Date(1000));
		stepExecution.setEndTime(new Date(2000));
		stepExecution.setLastUpdated(new Date(3000));
		jobExecution.addStepExecutions(Arrays.asList(stepExecution));
		JobExecutionInfoResource jobExecutionInfoResource = new JobExecutionInfoResource(jobExecution, TimeZone.getTimeZone("America/Chicago"));
		jobExecutionInfoResource.setStepExecutions(Arrays.asList(new StepExecutionInfoResource(stepExecution, TimeZone.getTimeZone("America/Chicago"))));
		return jobExecutionInfoResource;
	}
}
