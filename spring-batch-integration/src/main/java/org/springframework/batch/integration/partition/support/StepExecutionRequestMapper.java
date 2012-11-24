package org.springframework.batch.integration.partition.support;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.integration.partition.StepExecutionRequest;

/**
 * Maps a {@link StepExecutionRequest} to an actual {@link StepExecution}.
 *
 * @author Sebastien Gerard
 */
public interface StepExecutionRequestMapper {

    /**
     * Returns the {@link StepExecution} referenced in the specified <tt>request</tt>.
     *
     * @param request the given request
     * @return the corresponding execution
     * @throws NoSuchJobExecutionException if the job execution mentioned in the request does not exist
     * @throws NoSuchStepException if the step mentioned in the request does not exist in the
     * {@link org.springframework.batch.core.JobExecution}
     */
    StepExecution map(StepExecutionRequest request) throws NoSuchJobExecutionException, NoSuchStepException;

}
