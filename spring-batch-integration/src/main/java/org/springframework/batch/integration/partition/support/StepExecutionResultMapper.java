package org.springframework.batch.integration.partition.support;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.integration.partition.StepExecutionResult;

/**
 * Maps a {@link StepExecutionResult} to an actual {@link StepExecution}.
 *
 * @author Sebastien Gerard
 */
public interface StepExecutionResultMapper {

    /**
     * Returns the {@link StepExecution} referenced in the specified <tt>result</tt>.
     *
     * @param result the given result
     * @param masterStepExecution the whole partition step execution
     * @return the corresponding execution
     * @throws NoSuchJobExecutionException if the job execution mentioned in the result does not exist
     * @throws NoSuchStepException if the step mentioned in the result does not exist in the
     * {@link org.springframework.batch.core.JobExecution}
     */
    StepExecution map(StepExecutionResult result, StepExecution masterStepExecution)
            throws NoSuchJobExecutionException, NoSuchStepException;

}