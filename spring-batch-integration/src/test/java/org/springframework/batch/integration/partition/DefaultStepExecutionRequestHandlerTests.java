package org.springframework.batch.integration.partition;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.integration.partition.support.StepExecutionRequestMapperTester;

/**
 * @author Stephane Nicoll
 */
public class DefaultStepExecutionRequestHandlerTests {

    @Test
    public void errorMapping() {
        final DefaultStepExecutionRequestHandler handler = createHandler();

        final StepExecutionResult result = handler.handle(new StepExecutionRequest("myStep", 1234L, 123545L));
        Assert.assertEquals("The number of exceptions is not the expected one.", 1, result.getFailureExceptions().size());
        Assert.assertEquals("The exception is not the expected one.", NoSuchStepException.class,
                result.getFailureExceptions().get(0).getClass());
    }


    protected DefaultStepExecutionRequestHandler createHandler(StepExecution... stepExecutions) {
        final DefaultStepExecutionRequestHandler handler = new DefaultStepExecutionRequestHandler();

        handler.setStepExecutionRequestMapper(new StepExecutionRequestMapperTester(stepExecutions));

        return handler;
    }
}
