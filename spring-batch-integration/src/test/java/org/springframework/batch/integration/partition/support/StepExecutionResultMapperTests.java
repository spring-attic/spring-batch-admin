package org.springframework.batch.integration.partition.support;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.batch.integration.partition.StepExecutionResult;

import java.util.Collections;

/**
 * @author Sebastien Gerard
 */
public class StepExecutionResultMapperTests {

    protected static final long JOB_EXECUTION_ID = 1234L;
    protected static final long STEP_EXECUTION_ID = 1234L;
    protected static final String STEP_NAME = "my-step";
    protected static final JobExecution JOB_EXECUTION = new JobExecution(JOB_EXECUTION_ID);
    protected static final StepExecution STEP_EXECUTION = new StepExecution(STEP_NAME, JOB_EXECUTION, STEP_EXECUTION_ID);
    protected static final StepExecutionRequest REQUEST = new StepExecutionRequest(STEP_NAME, STEP_EXECUTION);

    @Test
    public void nullStepExecutionResult() throws NoSuchJobExecutionException {
        try {
            createInstance().map(null, STEP_EXECUTION);
            Assert.fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void nullStepExecution() throws NoSuchJobExecutionException {
        try {
            createInstance().map(new StepExecutionResult(REQUEST), null);
            Assert.fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void normalUseCase() throws NoSuchJobExecutionException {
        Assert.assertEquals("The step execution is not the expected one",
                STEP_EXECUTION, createInstance().map(new StepExecutionResult(REQUEST), createMasterStepExecution()));
    }

    protected StepExecution createMasterStepExecution() {
        final StepExecution stepExecution = new StepExecution(STEP_NAME, JOB_EXECUTION, (STEP_EXECUTION_ID + 45L));

        stepExecution.getJobExecution().addStepExecutions(Collections.singletonList(STEP_EXECUTION));

        return stepExecution;
    }

    protected StepExecutionResultMapper createInstance() {
        final DefaultStepExecutionResultMapper mapper = new DefaultStepExecutionResultMapper();

        mapper.setRequestMapper(new StepExecutionRequestMapper() {
            public StepExecution map(StepExecutionRequest request) throws NoSuchJobExecutionException, NoSuchStepException {
                if ((request.getJobExecutionId() != JOB_EXECUTION_ID) || (request.getStepExecutionId() != STEP_EXECUTION_ID)) {
                    throw new NoSuchStepException("Cannot find [" + request + "]");
                }

                return STEP_EXECUTION;
            }
        });

        return mapper;
    }

}
