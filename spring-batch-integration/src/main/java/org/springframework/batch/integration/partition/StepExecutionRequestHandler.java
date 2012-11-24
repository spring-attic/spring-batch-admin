package org.springframework.batch.integration.partition;

/**
 * Component responsible of executing a {@link StepExecutionRequest}. Typically
 * implemented by remote workers. Returns a {@link StepExecutionResult} containing
 * only the actual exception that occurred, if any since the actual step execution
 * is available from the data store.
 *
 * @author Sebastien Gerard
 */
public interface StepExecutionRequestHandler {

    /**
     * Executes the given request.
     * <p/>
     * Note that if an exception occurs during the handling of the request, it
     * must be placed in the exception list contained in {@link StepExecutionResult}.
     * <p/>
     * If an exception is thrown here, it is considered as an unexpected issue
     * that should not happen.
     *
     * @param request the request to execute
     * @return the handling result
     */
    StepExecutionResult handle(StepExecutionRequest request);

}