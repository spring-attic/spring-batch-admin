package org.springframework.batch.integration.partition;

import org.springframework.batch.core.StepExecution;

/**
 * Callback for {@link StepExecutionRequestHandlerProcessor}.
 * <p/>
 * Note that listeners are free to modify the {@link StepExecution} without
 * applying changes to the database (it will be done automatically). This listener is
 * not expected to throw any exception.
 *
 * @author Sebastien Gerard
 * @author Stephane Nicoll
 * @see StepExecutionRequestHandlerProcessor
 */
public interface StepExecutionRequestHandlerListener {

    /**
     * Callback before checking whether the step execution can be executed.
     *
     * @param stepExecution the current step execution
     */
    void beforeHandle(StepExecution stepExecution);

    /**
     * Callback when the step execution cannot be executed. It means
     * that {@link org.springframework.batch.core.Step#execute(org.springframework.batch.core.StepExecution)}
     * is not called at all.
     *
     * @param stepExecution the current step execution
     */
    void onDeniedException(StepExecution stepExecution);

    /**
     * Callback when the step execution is finished. In other words,
     * {@link org.springframework.batch.core.Step#execute(org.springframework.batch.core.StepExecution)}
     * has been called.
     *
     * @param stepExecution the current step execution
     */
    void afterHandle(StepExecution stepExecution);

}

