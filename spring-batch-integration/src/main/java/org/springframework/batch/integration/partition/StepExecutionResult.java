package org.springframework.batch.integration.partition;

import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of a worker once it has finished to process a particular step.
 * <p/>
 * If the worker failed to process the specified step, the exception(s) that occurred
 * are made available through the {@link #getFailureExceptions()} method. The final
 * status of the execution can be found by retrieving the
 * {@link org.springframework.batch.core.StepExecution} using its identifier.
 *
 * @author Sebastien Gerard
 * @see StepExecutionRequest
 */
public class StepExecutionResult implements Serializable {

    private static final long serialVersionUID = -7853764123860032790L;

    private final StepExecutionRequest request;
    private final List<Throwable> failureExceptions;

    /**
     * Creates a new instance with the list of {@link Throwable} that occurred
     * during the step execution.
     *
     * @param request           the original request
     * @param failureExceptions the exceptions that occurred during the step execution
     */
    public StepExecutionResult(StepExecutionRequest request, final List<Throwable> failureExceptions) {
        Assert.notNull(request, "The request could not be null.");
        this.request = request;

        this.failureExceptions = Collections.unmodifiableList(failureExceptions);
    }

    /**
     * Creates a new instance with no exception.
     *
     * @param request the original request
     */
    public StepExecutionResult(StepExecutionRequest request) {
        this(request, new ArrayList<Throwable>());
    }

    /**
     * @return the initial request
     */
    public StepExecutionRequest getRequest() {
        return request;
    }

    /**
     * @return all the exceptions occurred during the step execution
     */
    public List<Throwable> getFailureExceptions() {
        return failureExceptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepExecutionResult that = (StepExecutionResult) o;

        if (!failureExceptions.equals(that.failureExceptions)) return false;
        if (!request.equals(that.request)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = request.hashCode();
        result = 31 * result + failureExceptions.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("StepExecutionResult: [stepName='%s', jobExecutionId=%d, stepExecutionId=%d, numberExceptions=%d]",
                getRequest().getStepName(), getRequest().getJobExecutionId(), getRequest().getStepExecutionId(),
                getFailureExceptions().size());
    }

}
