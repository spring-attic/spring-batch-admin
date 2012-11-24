package org.springframework.batch.integration.partition;

import org.junit.Test;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Sebastien Gerard
 */
@SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "ThrowableInstanceNeverThrown"})
public class StepExecutionResultTest {

    @Test
    public void roundTripNoException() {
        launchTest(createResult("my-step", 45456466L, 45L, null));
    }

    @Test
    public void roundTripMultipleException() {
        launchTest(createResult("my-step", 45456466L, 45L,
                createThrowableList(
                        new IllegalStateException("You cannot do that"),
                        new NoSuchMethodError("Method XYZ not found, bam!"))
        ));
    }

    @Test
    public void roundTripEmptyList() {
        launchTest(createResult("my-step-0", 4545646645L, 999L, createThrowableList()));
    }

    @Test
    public void emptyStepName() {
        try {
            createResult(null, 45456466L, 45L, null);
            fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    protected List<Throwable> createThrowableList(final Throwable... throwableInstances) {
        for (int i = 0; i < throwableInstances.length; i++) {
            throwableInstances[i] = initializeException(throwableInstances[i]);
        }

        return Arrays.asList(throwableInstances);
    }

    protected Throwable initializeException(Throwable throwable) {
        if (throwable == null) {
            throwable = new RuntimeException("Simulated exception");
        }
        return throwable.fillInStackTrace();
    }

    protected void launchTest(final StepExecutionResult result) {
        final StepExecutionResult deSerializedResult =
                (StepExecutionResult) SerializationUtils.deserialize(SerializationUtils.serialize(result));

        assertEquals("The step name is not the expected one",
                result.getRequest().getStepName(), deSerializedResult.getRequest().getStepName());
        assertEquals("The job execution id is not the expected one",
                result.getRequest().getJobExecutionId(), deSerializedResult.getRequest().getJobExecutionId());
        assertEquals("The step execution id is not the expected one",
                result.getRequest().getStepExecutionId(), deSerializedResult.getRequest().getStepExecutionId());
        assertExceptions(result, deSerializedResult);
    }

    protected StepExecutionResult createResult(final String stepName, final Long jobExecutionId, final Long stepExecutionId,
                                               final List<Throwable> failureExceptions) {
        final StepExecutionRequest request = new StepExecutionRequest(stepName, jobExecutionId, stepExecutionId);
        return (failureExceptions != null) ?
                new StepExecutionResult(request, failureExceptions) :
                new StepExecutionResult(request);
    }

    protected void assertExceptions(StepExecutionResult result, StepExecutionResult deSerializedResult) {
        assertNotNull("Failure exception list cannot be null (original object)", result.getFailureExceptions());
        assertNotNull("Failure exception list cannot be null (de-serialized object)",
                deSerializedResult.getFailureExceptions());
        assertEquals("The number of exceptions is not the expected one",
                result.getFailureExceptions().size(), deSerializedResult.getFailureExceptions().size());

        for (int i = 0; i < result.getFailureExceptions().size(); i++) {
            assertEquals("The exception message is not the expected one",
                    result.getFailureExceptions().get(i).getMessage(),
                    deSerializedResult.getFailureExceptions().get(i).getMessage());
            assertEquals("The number of exceptions is not the expected one",
                    result.getFailureExceptions().get(i).getStackTrace().length,
                    deSerializedResult.getFailureExceptions().get(i).getStackTrace().length);
        }
    }

}
