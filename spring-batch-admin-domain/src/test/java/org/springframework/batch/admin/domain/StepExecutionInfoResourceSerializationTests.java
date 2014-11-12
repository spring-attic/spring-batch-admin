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

import java.util.Date;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.test.util.JsonPathExpectationsHelper;

/**
 * @author Michael Minella
 */
public class StepExecutionInfoResourceSerializationTests extends AbstractSerializationTests<StepExecutionInfoResource> {
	@Override
	public void assertJson(String json) throws Exception {
		new JsonPathExpectationsHelper("$.jobExecutionId").assertValue(json, 5);
		new JsonPathExpectationsHelper("$.stepType").assertValue(json, StepType.TASKLET_STEP.getDisplayName());
		new JsonPathExpectationsHelper("$.stepExecution.commitCount").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.stepExecution.endTime").assertValue(json, "1970-01-01T00:00:00.100Z");
		new JsonPathExpectationsHelper("$.stepExecution.exitStatus.exitCode").assertValue(json, "FINISHED");
		new JsonPathExpectationsHelper("$.stepExecution.exitStatus.exitDescription").assertValue(json, "All Done");
		new JsonPathExpectationsHelper("$.stepExecution.filterCount").assertValue(json, 2);
		new JsonPathExpectationsHelper("$.stepExecution.id").assertValue(json, 1);
		new JsonPathExpectationsHelper("$.stepExecution.lastUpdated").assertValue(json, "1970-01-01T00:00:00.101Z");
		new JsonPathExpectationsHelper("$.stepExecution.processSkipCount").assertValue(json, 3);
		new JsonPathExpectationsHelper("$.stepExecution.readCount").assertValue(json, 4);
		new JsonPathExpectationsHelper("$.stepExecution.rollbackCount").assertValue(json, 6);
		new JsonPathExpectationsHelper("$.stepExecution.readSkipCount").assertValue(json, 5);
		new JsonPathExpectationsHelper("$.stepExecution.startTime").assertValue(json, "1970-01-01T00:00:00.001Z");
		new JsonPathExpectationsHelper("$.stepExecution.status").assertValue(json, BatchStatus.COMPLETED.toString());
		new JsonPathExpectationsHelper("$.stepExecution.stepName").assertValue(json, "step1");
		new JsonPathExpectationsHelper("$.stepExecution.terminateOnly").assertValue(json, false);
		new JsonPathExpectationsHelper("$.stepExecution.version").assertValue(json, 9);
		new JsonPathExpectationsHelper("$.stepExecution.writeCount").assertValue(json, 7);
		new JsonPathExpectationsHelper("$.stepExecution.writeSkipCount").assertValue(json, 8);
	}

	@Override
	public void assertObject(StepExecutionInfoResource stepExecutionInfoResource) throws Exception {
		assertEquals(5l, (long) stepExecutionInfoResource.getJobExecutionId());
		assertEquals(StepType.TASKLET_STEP.getDisplayName(), stepExecutionInfoResource.getStepType());
		assertEquals("step1", stepExecutionInfoResource.getStepExecution().getStepName());
		assertEquals(1, stepExecutionInfoResource.getStepExecution().getCommitCount());
		assertEquals(new Date(100), stepExecutionInfoResource.getStepExecution().getEndTime());
		assertEquals(new ExitStatus("FINISHED", "All Done"), stepExecutionInfoResource.getStepExecution().getExitStatus());
		assertEquals(2, stepExecutionInfoResource.getStepExecution().getFilterCount());
		assertEquals(new Date(101), stepExecutionInfoResource.getStepExecution().getLastUpdated());
		assertEquals(3, stepExecutionInfoResource.getStepExecution().getProcessSkipCount());
		assertEquals(4, stepExecutionInfoResource.getStepExecution().getReadCount());
		assertEquals(5, stepExecutionInfoResource.getStepExecution().getReadSkipCount());
		assertEquals(6, stepExecutionInfoResource.getStepExecution().getRollbackCount());
		assertEquals(16, stepExecutionInfoResource.getStepExecution().getSkipCount());
		assertEquals(7, stepExecutionInfoResource.getStepExecution().getWriteCount());
		assertEquals(8, stepExecutionInfoResource.getStepExecution().getWriteSkipCount());
		assertEquals(9l, (long)stepExecutionInfoResource.getStepExecution().getVersion());
	}

	@Override
	public StepExecutionInfoResource getSerializationValue() {
		JobExecution jobExecution = new JobExecution(5l, new JobParametersBuilder().addString("foo", "bar").toJobParameters(), "config.xml");
		StepExecution stepExecution = new StepExecution("step1", jobExecution, 1l);
		stepExecution.setCommitCount(1);
		stepExecution.setEndTime(new Date(100));
		stepExecution.setExitStatus(new ExitStatus("FINISHED", "All Done"));
		stepExecution.setFilterCount(2);
		stepExecution.setLastUpdated(new Date(101));
		stepExecution.setProcessSkipCount(3);
		stepExecution.setReadCount(4);
		stepExecution.setReadSkipCount(5);
		stepExecution.setRollbackCount(6);
		stepExecution.setStartTime(new Date(1));
		stepExecution.setStatus(BatchStatus.COMPLETED);
		stepExecution.setWriteCount(7);
		stepExecution.setWriteSkipCount(8);
		stepExecution.setVersion(9);

		return new StepExecutionInfoResource(5l, stepExecution, StepType.TASKLET_STEP.getDisplayName());
	}
}
