/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.integration.partition.support;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.support.SimpleJobExplorer;
import org.springframework.batch.core.repository.dao.MapExecutionContextDao;
import org.springframework.batch.core.repository.dao.MapJobExecutionDao;
import org.springframework.batch.core.repository.dao.MapJobInstanceDao;
import org.springframework.batch.core.repository.dao.MapStepExecutionDao;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.batch.integration.partition.StepExecutionRequestAggregator;
import org.springframework.batch.integration.partition.StepExecutionResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * @author Sebastien Gerard
 */
public class GenericPartitionHandlerTests {

    protected static final long JOB_EXECUTION_ID = 1234L;
    protected static final String STEP_NAME = "my-step";
    protected static final JobExecution JOB_EXECUTION = new JobExecution(JOB_EXECUTION_ID);

    private long currentStepExecutionId = 0;

    @Test
    public void createWithoutJobExplorer() {
        final GenericPartitionHandler handler = new GenericPartitionHandler();
        handler.setStepName("my-step");
        try {
            handler.afterPropertiesSet();
            Assert.fail("Should have failed to build a handler without a job explorer if no result mapper is set.");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    @Test
    public void createWithJobExplorer() {
        final GenericPartitionHandler handler = new GenericPartitionHandler();
        handler.setStepName("my-step");

        handler.setJobExplorer(new SimpleJobExplorer(new MapJobInstanceDao(),
                new MapJobExecutionDao(), new MapStepExecutionDao(), new MapExecutionContextDao()));
        // Should create the mapper based on the job explorer
        handler.afterPropertiesSet();
    }

    @Test
    public void notStepExecution() throws Exception {
        Assert.assertEquals("Wrong number of step executions", 0, new TestDescription().launchTest().size());
    }

    @Test
    public void stepExecutions() throws Exception {
        List<StepExecution> stepExecutions = randomStepExecutionSet(2);

        final Set<StepExecution> results = new TestDescription().addInnerStepExecutions(stepExecutions).launchTest();

        Assert.assertEquals("Wrong number of step execution", stepExecutions.size(), results.size());
        for (StepExecution stepExecution : results) {
            Assert.assertEquals("The step execution must be completed", BatchStatus.COMPLETED, stepExecution.getStatus());
        }
    }

    @Test
    public void timeout() throws Exception {
        List<StepExecution> stepExecutions = randomStepExecutionSet(2);

        final TestDescription testDescription = new TestDescription();
        testDescription.partitionHandler.setRequestAggregator(new StepExecutionRequestAggregator() {
            public List<StepExecutionResult> aggregate(List<StepExecutionRequest> requests) throws TimeoutException {
                throw new TimeoutException("test");
            }
        });

        try {
            testDescription.addInnerStepExecutions(stepExecutions).launchTest();
            Assert.fail("An exception should have been thrown");
        } catch (TimeoutException e) {
        }
    }

    protected List<StepExecution> randomStepExecutionSet(int numberSteps) {
        final List<StepExecution> executionSet = new ArrayList<StepExecution>();

        for (int i = 0; i < numberSteps; i++) {
            executionSet.add(createTestStepExecution());
        }

        return executionSet;
    }

    protected StepExecution createTestStepExecution() {
        return new StepExecution(STEP_NAME, JOB_EXECUTION, currentStepExecutionId++);
    }

    protected static class TestDescription {
        public final GenericPartitionHandler partitionHandler = new GenericPartitionHandler();
        public final StepExecution masterStepExecution;
        private final Set<StepExecution> stepExecutions = new HashSet<StepExecution>();

        public TestDescription() {
            this.masterStepExecution = createMasterStepExecution();

            partitionHandler.setRequestAggregator(new DummyStepExecutionRequestAggregator());
            partitionHandler.setStepName(STEP_NAME);
            partitionHandler.setResultMapper(new TestStepExecutionResultMapper(stepExecutions));
        }

        public TestDescription addInnerStepExecutions(List<StepExecution> stepExecutions) {
            this.stepExecutions.addAll(stepExecutions);
            masterStepExecution.getJobExecution().addStepExecutions(stepExecutions);

            return this;
        }

        public Set<StepExecution> launchTest() throws Exception {
            return partitionHandler.doHandle(masterStepExecution, stepExecutions);
        }

        private StepExecution createMasterStepExecution() {
            return new StepExecution(STEP_NAME, JOB_EXECUTION, 12345L);
        }
    }

    private static class DummyStepExecutionRequestAggregator implements StepExecutionRequestAggregator {

        public List<StepExecutionResult> aggregate(List<StepExecutionRequest> requests) {
            final List<StepExecutionResult> results = new ArrayList<StepExecutionResult>();

            for (StepExecutionRequest request : requests) {
                results.add(new StepExecutionResult(request));
            }

            return results;
        }
    }

    private static class TestStepExecutionResultMapper implements StepExecutionResultMapper {
        private final Set<StepExecution> stepExecutions;

        private TestStepExecutionResultMapper(Set<StepExecution> stepExecutions) {
            this.stepExecutions = stepExecutions;
        }

        public StepExecution map(StepExecutionResult result, StepExecution masterStepExecution) {
            for (StepExecution stepExecution : stepExecutions) {
                if (stepExecution.getId().equals(result.getRequest().getStepExecutionId())) {
                    stepExecution.setStatus(BatchStatus.COMPLETED);

                    return stepExecution;
                }
            }

            throw new IllegalStateException("Cannot find step execution for [" + result + "]");
        }
    }

}