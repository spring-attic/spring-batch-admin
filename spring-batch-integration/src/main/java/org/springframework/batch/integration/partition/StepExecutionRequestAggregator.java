package org.springframework.batch.integration.partition;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * A {@link StepExecutionRequest} aggregator is responsible of executing
 * all the {@link StepExecutionRequest} and waiting for their results.
 *
 * @author Sebastien Gerard
 */
public interface StepExecutionRequestAggregator {

    /**
     * Executes the specified <tt>requests</tt> and then aggregates the results.
     * <p/>
     * If the execution takes too much time, a {@link TimeoutException} can be thrown.
     *
     * @param requests the step execution request(s) to send
     * @return the aggregated results
     * @throws TimeoutException if the execution takes too much time
     */
    List<StepExecutionResult> aggregate(List<StepExecutionRequest> requests) throws TimeoutException;

}
