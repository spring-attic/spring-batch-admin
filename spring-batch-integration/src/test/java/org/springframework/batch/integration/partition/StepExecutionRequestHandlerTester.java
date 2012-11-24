package org.springframework.batch.integration.partition;

import org.junit.Assert;
import org.springframework.batch.core.StepExecution;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastien Gerard
 */
public class StepExecutionRequestHandlerTester implements StepExecutionRequestHandlerListener {

    protected static final String WRONG_NUMBER_OF_STEP_EXECUTIONS = "Wrong number of step executions";

    private final List<StepExecution> before = new ArrayList<StepExecution>();
    private final List<StepExecution> after = new ArrayList<StepExecution>();
    private final List<StepExecution> error = new ArrayList<StepExecution>();

    public void beforeHandle(StepExecution stepExecution) {
        before.add(stepExecution);
    }

    public void onDeniedException(StepExecution stepExecution) {
        error.add(stepExecution);
    }

    public void afterHandle(StepExecution stepExecution) {
        after.add(stepExecution);
    }

    public void assertBeforeCalls(StepExecution... stepExecutions) {
        Assert.assertEquals(WRONG_NUMBER_OF_STEP_EXECUTIONS, before.size(), stepExecutions.length);
        for (StepExecution stepExecution : stepExecutions) {
            Assert.assertTrue("The step execution [" + stepExecution + "] has not been provided in the before method",
                    before.contains(stepExecution));
        }
    }

    public void assertAfterCalls(StepExecution... stepExecutions) {
        Assert.assertEquals(WRONG_NUMBER_OF_STEP_EXECUTIONS, after.size(), stepExecutions.length);
        for (StepExecution stepExecution : stepExecutions) {
            Assert.assertTrue("The step execution [" + stepExecution + "] has not been provided in the after method",
                    after.contains(stepExecution));
        }
    }

    public void assertOnDeniedExceptionCalls(StepExecution... stepExecutions) {
        Assert.assertEquals(WRONG_NUMBER_OF_STEP_EXECUTIONS, error.size(), stepExecutions.length);
        for (StepExecution stepExecution : stepExecutions) {
            Assert.assertTrue("The step execution [" + stepExecution + "] has not been provided in the on denied method",
                    error.contains(stepExecution));
        }
    }

}