package org.springframework.batch.integration.partition.support;

import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.integration.partition.StepExecutionRequestHandlerTester;
import org.springframework.batch.integration.partition.StepExecutionRequestHandlerListener;

import java.util.Arrays;

/**
 * @author Stephane Nicoll
 */
public class CompositeStepExecutionRequestHandlerListenerTests {

    protected static final StepExecution DEFAULT_STEP_EXECUTION = new StepExecution("STEP",
            new JobExecution(1234L), 1234L);

    private final CompositeStepExecutionRequestHandlerListener listener = new CompositeStepExecutionRequestHandlerListener();

    @Test
    public void beforeHandleEmptyList() {
        listener.beforeHandle(DEFAULT_STEP_EXECUTION);
    }

    @Test
    public void onDeniedExceptionEmptyList() {
        listener.onDeniedException(DEFAULT_STEP_EXECUTION);
    }

    @Test
    public void afterHandleEmptyList() {
        listener.afterHandle(DEFAULT_STEP_EXECUTION);
    }

    @Test
    public void beforeHandle() {
        final StepExecutionRequestHandlerTester tester = new StepExecutionRequestHandlerTester();
        final StepExecutionRequestHandlerTester tester2 = new StepExecutionRequestHandlerTester();

        createListener(tester, tester2).beforeHandle(DEFAULT_STEP_EXECUTION);

        tester.assertBeforeCalls(DEFAULT_STEP_EXECUTION);
        tester2.assertBeforeCalls(DEFAULT_STEP_EXECUTION);
    }

    @Test
    public void afterHandle() {
        final StepExecutionRequestHandlerTester tester = new StepExecutionRequestHandlerTester();
        final StepExecutionRequestHandlerTester tester2 = new StepExecutionRequestHandlerTester();

        createListener(tester, tester2).afterHandle(DEFAULT_STEP_EXECUTION);

        tester.assertAfterCalls(DEFAULT_STEP_EXECUTION);
        tester2.assertAfterCalls(DEFAULT_STEP_EXECUTION);
    }

    @Test
    public void deniedException() {
        final StepExecutionRequestHandlerTester tester = new StepExecutionRequestHandlerTester();
        final StepExecutionRequestHandlerTester tester2 = new StepExecutionRequestHandlerTester();

        createListener(tester, tester2).onDeniedException(DEFAULT_STEP_EXECUTION);

        tester.assertOnDeniedExceptionCalls(DEFAULT_STEP_EXECUTION);
        tester2.assertOnDeniedExceptionCalls(DEFAULT_STEP_EXECUTION);
    }

    protected CompositeStepExecutionRequestHandlerListener createListener(StepExecutionRequestHandlerListener... listeners) {
        return new CompositeStepExecutionRequestHandlerListener(Arrays.asList(listeners));
    }

}
