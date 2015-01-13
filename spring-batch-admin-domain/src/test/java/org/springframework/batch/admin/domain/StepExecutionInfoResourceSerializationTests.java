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

import java.util.Date;
import java.util.TimeZone;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.test.util.JsonPathExpectationsHelper;

/**
 * @author Michael Minella
 */
public class StepExecutionInfoResourceSerializationTests extends AbstractSerializationTests<StepExecutionInfoResource> {
	@Override
	public void assertJson(String json) throws Exception {
		new JsonPathExpectationsHelper("$.jobExecutionId").assertValue(json, 5);
		new JsonPathExpectationsHelper("$.stepType").assertValue(json, "org.springframework.batch.core.step.tasklet.TaskletStep");
		new JsonPathExpectationsHelper("$.commitCount").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.endTime").assertValue(json, "1969-12-31T18:00:01.000-06:00");
		new JsonPathExpectationsHelper("$.exitStatus.exitCode").assertValue(json, "FINISHED");
		new JsonPathExpectationsHelper("$.exitStatus.exitDescription").assertValue(json, "All Done");
		new JsonPathExpectationsHelper("$.filterCount").assertValue(json, 2);
		new JsonPathExpectationsHelper("$.executionId").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.lastUpdated").assertValue(json, "1969-12-31T18:00:02.000-06:00");
		new JsonPathExpectationsHelper("$.processSkipCount").assertValue(json, 3);
		new JsonPathExpectationsHelper("$.readCount").assertValue(json, 4);
		new JsonPathExpectationsHelper("$.rollbackCount").assertValue(json, 6);
		new JsonPathExpectationsHelper("$.readSkipCount").assertValue(json, 5);
		new JsonPathExpectationsHelper("$.startTime").assertValue(json, "1969-12-31T18:00:00.001-06:00");
		new JsonPathExpectationsHelper("$.status").assertValue(json, BatchStatus.COMPLETED.toString());
		new JsonPathExpectationsHelper("$.stepName").assertValue(json, "step1");
		new JsonPathExpectationsHelper("$.terminateOnly").assertValue(json, false);
		new JsonPathExpectationsHelper("$.version").assertValue(json, 9);
		new JsonPathExpectationsHelper("$.writeCount").assertValue(json, 7);
		new JsonPathExpectationsHelper("$.writeSkipCount").assertValue(json, 8);
	}

	@Override
	public void assertObject(StepExecutionInfoResource stepExecutionInfoResource) throws Exception {
		assertEquals(5l, (long) stepExecutionInfoResource.getJobExecutionId());
		assertEquals("org.springframework.batch.core.step.tasklet.TaskletStep", stepExecutionInfoResource.getStepType());
		assertEquals("step1", stepExecutionInfoResource.getStepName());
		assertEquals(1, stepExecutionInfoResource.getCommitCount());
		assertEquals("1969-12-31T18:00:01.000-06:00", stepExecutionInfoResource.getEndTime());
		assertEquals(new ExitStatus("FINISHED", "All Done"), stepExecutionInfoResource.getExitStatus());
		assertEquals(2, stepExecutionInfoResource.getFilterCount());
		assertEquals("1969-12-31T18:00:02.000-06:00", stepExecutionInfoResource.getLastUpdated());
		assertEquals(3, stepExecutionInfoResource.getProcessSkipCount());
		assertEquals(4, stepExecutionInfoResource.getReadCount());
		assertEquals(5, stepExecutionInfoResource.getReadSkipCount());
		assertEquals(6, stepExecutionInfoResource.getRollbackCount());
		assertEquals(7, stepExecutionInfoResource.getWriteCount());
		assertEquals(8, stepExecutionInfoResource.getWriteSkipCount());
		assertEquals(9l, (long)stepExecutionInfoResource.getVersion());
		assertEquals(1, stepExecutionInfoResource.getExecutionContext().size());
		assertEquals("org.springframework.batch.core.step.tasklet.TaskletStep", stepExecutionInfoResource.getExecutionContext().get(Step.STEP_TYPE_KEY));
	}

	@Override
	public StepExecutionInfoResource getSerializationValue() {
		JobExecution jobExecution = new JobExecution(5l, new JobParametersBuilder().addString("foo", "bar").toJobParameters(), "config.xml");
		StepExecution stepExecution = new StepExecution("step1", jobExecution, 1l);
		stepExecution.setCommitCount(1);
		stepExecution.setEndTime(new Date(1000));
		stepExecution.setExitStatus(new ExitStatus("FINISHED", "All Done"));
		stepExecution.setFilterCount(2);
		stepExecution.setLastUpdated(new Date(2000));
		stepExecution.setProcessSkipCount(3);
		stepExecution.setReadCount(4);
		stepExecution.setReadSkipCount(5);
		stepExecution.setRollbackCount(6);
		stepExecution.setStartTime(new Date(1));
		stepExecution.setStatus(BatchStatus.COMPLETED);
		stepExecution.setWriteCount(7);
		stepExecution.setWriteSkipCount(8);
		stepExecution.setVersion(9);

		ExecutionContext context = new ExecutionContext();
		context.put(Step.STEP_TYPE_KEY, StepType.TASKLET_STEP.getClassName());

		stepExecution.setExecutionContext(context);

		return new StepExecutionInfoResource(stepExecution, TimeZone.getTimeZone("America/Chicago"));
	}
}
