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

import static org.junit.Assert.*;

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
public class JobInstanceInfoResourceSerializationTests extends AbstractSerializationTests<JobInstanceInfoResource> {
	@Override
	public void assertJson(String json) throws Exception {
		new JsonPathExpectationsHelper("$.instanceId").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.jobName").assertValue(json, "job1");
		new JsonPathExpectationsHelper("$.jobExecutions[0].duration").assertValue(json, "00:00:00");
		new JsonPathExpectationsHelper("$.jobExecutions[0].abandonable").assertValue(json, false);
		new JsonPathExpectationsHelper("$.jobExecutions[0].executionId").assertValue(json, 2);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobId").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobParameters['foo']").assertValue(json, "bar");
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobParametersString").assertValue(json, "foo=bar");
		new JsonPathExpectationsHelper("$.jobExecutions[0].name").assertValue(json, "job1");
		new JsonPathExpectationsHelper("$.jobExecutions[0].restartable").assertValue(json, false);

		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.exitStatus.exitCode").assertValue(json, "COMPLETE");
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.exitStatus.exitDescription").assertValue(json, "Exit Description");
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.exitStatus.running").assertValue(json, false);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.id").assertValue(json, 2);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.jobConfigurationName").assertValue(json, "configName.xml");
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.jobInstance.id").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.jobInstance.jobName").assertValue(json, "job1");
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.status").assertValue(json, "COMPLETED");

		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.stepExecutions[0].commitCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.stepExecutions[0].filterCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.stepExecutions[0].id").assertValue(json, 3);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.stepExecutions[0].processSkipCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.stepExecutions[0].readCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.stepExecutions[0].readSkipCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.stepExecutions[0].rollbackCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.stepExecutions[0].stepName").assertValue(json, "step1");
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.stepExecutions[0].terminateOnly").assertValue(json, false);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.stepExecutions[0].writeCount").assertValue(json, 0);
		new JsonPathExpectationsHelper("$.jobExecutions[0].jobExecution.stepExecutions[0].writeSkipCount").assertValue(json, 0);
	}

	@Override
	public void assertObject(JobInstanceInfoResource jobInstanceInfoResource) throws Exception {
		assertEquals(1l, jobInstanceInfoResource.getInstanceId());
		assertEquals("job1", jobInstanceInfoResource.getJobName());
		assertEquals(1, jobInstanceInfoResource.getJobExecutions().size());
		assertEquals("foo=bar", jobInstanceInfoResource.getJobExecutions().get(0).getJobParametersString());
		assertEquals("job1", jobInstanceInfoResource.getJobExecutions().get(0).getName());
		assertEquals("1970-01-01", jobInstanceInfoResource.getJobExecutions().get(0).getStartDate());
		assertEquals("00:00:00", jobInstanceInfoResource.getJobExecutions().get(0).getStartTime());
		assertEquals(2l, (long) jobInstanceInfoResource.getJobExecutions().get(0).getExecutionId());
		assertEquals(1, jobInstanceInfoResource.getJobExecutions().get(0).getStepExecutionCount());
		assertEquals(new ExitStatus("COMPLETE", "Exit Description"), jobInstanceInfoResource.getJobExecutions().get(0).getJobExecution().getExitStatus());
	}

	@Override
	public JobInstanceInfoResource getSerializationValue() {
		JobInstance jobInstance = new JobInstance(1l, "job1");
		JobExecution jobExecution = new JobExecution(jobInstance, 2l, new JobParametersBuilder().addString("foo", "bar").toJobParameters(), "configName.xml");
		jobExecution.setExitStatus(new ExitStatus("COMPLETE", "Exit Description"));
		jobExecution.setStartTime(new Date(1));
		jobExecution.setEndTime(new Date(100));
		jobExecution.setStatus(BatchStatus.COMPLETED);
		StepExecution stepExecution = new StepExecution("step1", jobExecution, 3l);
		jobExecution.addStepExecutions(Arrays.asList(stepExecution));
		JobExecutionInfoResource jobExecutionInfoResource = new JobExecutionInfoResource(jobExecution, TimeZone.getTimeZone("CDT"));

		return new JobInstanceInfoResource(jobInstance, Arrays.asList(jobExecutionInfoResource));
	}
}
