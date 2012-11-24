package org.springframework.batch.integration.partition;

import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.util.SerializationUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Stephane Nicoll
 */
public class StepExecutionRequestTest {

    @Test
    public void emptyStepName() {
        try {
            new StepExecutionRequest(null, 45646788L, 4988748949849898L);
            fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void roundTrip() {
        launchTest(new StepExecutionRequest("my-step", 45456466L, 48418949849L));
    }

    @Test
    public void roundTripMinValues() {
        launchTest(new StepExecutionRequest("my-step", Long.MIN_VALUE, Long.MIN_VALUE));
    }

    @Test
    public void roundTripMaxValues() {
        launchTest(new StepExecutionRequest("my-step", Long.MAX_VALUE, Long.MAX_VALUE));
    }

    @Test
    public void secondConstructor() {
        final String stepName = "myStep";
        final Long jobExecutionId = 1234L;
        final Long stepExecutionId = 12345L;

        final StepExecutionRequest request =
                new StepExecutionRequest(stepName, new StepExecution("myStep2", new JobExecution(jobExecutionId), stepExecutionId));

        assertEquals("The step name is not the expected one", stepName, request.getStepName());
        assertEquals("The job execution id is not the expected one", jobExecutionId, request.getJobExecutionId());
        assertEquals("The step execution id is not the expected one", stepExecutionId, request.getStepExecutionId());
    }

    protected void launchTest(final StepExecutionRequest request) {
        final StepExecutionRequest deSerializedResult =
                (StepExecutionRequest) SerializationUtils.deserialize(SerializationUtils.serialize(request));

        assertEquals("The job execution id is not the expected one",
                request.getJobExecutionId(), deSerializedResult.getJobExecutionId());
        assertEquals("The step execution id is not the expected one",
                request.getStepExecutionId(), deSerializedResult.getStepExecutionId());
        assertEquals("The step name is not the expected one", request.getStepName(), deSerializedResult.getStepName());
    }
}
