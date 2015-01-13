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
import static org.junit.Assert.assertFalse;

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
 * @since 2.0
 */
public class DetailedJobInfoResourceSerializationTests extends AbstractSerializationTests<DetailedJobInfoResource> {

	@Override
	public void assertJson(String json) throws Exception {
		new JsonPathExpectationsHelper("$.executionCount").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.exitStatus.exitCode").assertValue(json, "COMPLETE");
		new JsonPathExpectationsHelper("$.exitStatus.exitDescription").assertValue(json, "Exit Description");
		new JsonPathExpectationsHelper("$.exitStatus.running").assertValue(json, false);
		new JsonPathExpectationsHelper("$.incrementable").assertValue(json, false);
		new JsonPathExpectationsHelper("$.jobInstanceId").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.jobParameters.parameters['foo'].identifying").assertValue(json, true);
		new JsonPathExpectationsHelper("$.jobParameters.parameters['foo'].type").assertValue(json, "STRING");
		new JsonPathExpectationsHelper("$.jobParameters.parameters['foo'].value").assertValue(json, "bar");
		new JsonPathExpectationsHelper("$.launchable").assertValue(json, true);
		new JsonPathExpectationsHelper("$.name").assertValue(json, "job1");
		new JsonPathExpectationsHelper("$.startTime").assertValue(json, "1970-01-01T00:00:01.000Z");
		new JsonPathExpectationsHelper("$.stepExecutionCount").assertValue(json, 1);
	}

	@Override
	public void assertObject(DetailedJobInfoResource detailedJobInfoResource) throws Exception {
		assertEquals("bar", detailedJobInfoResource.getJobParameters().getString("foo"));
		assertEquals(true, detailedJobInfoResource.getJobParameters().getParameters().get("foo").isIdentifying());
		assertEquals("1970-01-01T00:00:01.000Z", detailedJobInfoResource.getStartTime());
		assertEquals("job1", detailedJobInfoResource.getName());
		assertEquals(new ExitStatus("COMPLETE", "Exit Description"), detailedJobInfoResource.getExitStatus());
		assertFalse(detailedJobInfoResource.getExitStatus().isRunning());
		assertEquals(1, detailedJobInfoResource.getStepExecutionCount());
		assertEquals(1, detailedJobInfoResource.getExecutionCount());
		assertEquals(1l, (long) detailedJobInfoResource.getJobInstanceId());
		assertEquals(0, detailedJobInfoResource.getLinks().size());
	}

	@Override
	public DetailedJobInfoResource getSerializationValue() {
		JobInstance jobInstance = new JobInstance(1l, "job1");
		JobExecution jobExecution = new JobExecution(jobInstance, 2l, new JobParametersBuilder().addString("foo", "bar").toJobParameters(), "configName.xml");
		jobExecution.setExitStatus(new ExitStatus("COMPLETE", "Exit Description"));
		jobExecution.setCreateTime(new Date(0));
		jobExecution.setStartTime(new Date(1000));
		jobExecution.setEndTime(new Date(2000));
		jobExecution.setLastUpdated(new Date(3000));
		jobExecution.setStatus(BatchStatus.COMPLETED);
		StepExecution stepExecution = new StepExecution("step1", jobExecution, 3l);
		stepExecution.setLastUpdated(new Date(4000));
		jobExecution.addStepExecutions(Arrays.asList(stepExecution));
		JobExecutionInfoResource jobExecutionInfoResource = new JobExecutionInfoResource(jobExecution, TimeZone.getTimeZone("CDT"));

		return new DetailedJobInfoResource("job1", 1, true, false, jobExecutionInfoResource);
	}
}
