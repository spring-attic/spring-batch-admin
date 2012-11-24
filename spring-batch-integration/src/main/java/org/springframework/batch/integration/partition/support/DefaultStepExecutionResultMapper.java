package org.springframework.batch.integration.partition.support;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.partition.support.DefaultStepExecutionAggregator;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.integration.partition.StepExecutionResult;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;

/**
 * Default implementation of {@link StepExecutionResultMapper}.
 *
 * @author Sebastien Gerard
 */
public class DefaultStepExecutionResultMapper implements StepExecutionResultMapper {

    private StepExecutionRequestMapper requestMapper;
    private final DefaultStepExecutionAggregator defaultStepExecutionAggregator;

    public DefaultStepExecutionResultMapper() {
        this.defaultStepExecutionAggregator  = new DefaultStepExecutionAggregator();
    }

    public DefaultStepExecutionResultMapper(StepExecutionRequestMapper requestMapper) {
        this();
        this.requestMapper = requestMapper;
    }

    public StepExecution map(StepExecutionResult result, StepExecution masterStepExecution)
            throws NoSuchJobExecutionException, NoSuchStepException {
        Assert.notNull(result, "The step execution result cannot be null");
        Assert.notNull(masterStepExecution, "The master step execution cannot be null");

        final StepExecution updatedStepExecution = requestMapper.map(result.getRequest());

        // NICE: there should be an easier way to locate a step execution and update the contribution from another
        // instance
        final StepExecution existingStepExecution = locateExistingStepExecution(
                masterStepExecution, updatedStepExecution);
        return updateStepExecution(existingStepExecution, result, updatedStepExecution);
    }

    /**
     * Updates the step execution with the metadata contained from the specified {@link StepExecutionResult} and
     * the contributions from the updated {@link StepExecution}.
     *
     * @param stepExecution the existing step execution to update
     * @param result the result of the step execution
     * @param updatedStepExecution the step execution holding updated metadata
     * @return the merge between the <tt>stepExecution</tt> and the information held in the result
     */
    protected StepExecution updateStepExecution(StepExecution stepExecution, StepExecutionResult result,
                                                StepExecution updatedStepExecution) {

        defaultStepExecutionAggregator.aggregate(stepExecution, Collections.singletonList(updatedStepExecution));

        stepExecution.setVersion(updatedStepExecution.getVersion());
        stepExecution.setExecutionContext(updatedStepExecution.getExecutionContext());
        stepExecution.getFailureExceptions().clear();
        stepExecution.getFailureExceptions().addAll(result.getFailureExceptions());
        stepExecution.setLastUpdated(updatedStepExecution.getLastUpdated());
        stepExecution.setStatus(updatedStepExecution.getStatus());
        stepExecution.setEndTime(updatedStepExecution.getEndTime());

        if (updatedStepExecution.isTerminateOnly()) {
            stepExecution.setTerminateOnly();
        }

        return stepExecution;
    }

    private StepExecution locateExistingStepExecution(StepExecution masterStepExecution,
                                                      StepExecution updatedStepExecution) {
        final Collection<StepExecution> allStepExecutions = masterStepExecution.getJobExecution().getStepExecutions();

        for (StepExecution stepExecution : allStepExecutions) {
            if (stepExecution.equals(updatedStepExecution)) {
                return stepExecution;
            }
        }
        throw new IllegalStateException("Cannot find [" + updatedStepExecution + "] in " + allStepExecutions);

    }

    /**
     * Sets the mapper mapping {@link org.springframework.batch.integration.partition.StepExecutionRequest}
     * to {@link StepExecution}.
     *
     * @param requestMapper the mapper to use
     */
    @Required
    public void setRequestMapper(StepExecutionRequestMapper requestMapper) {
        this.requestMapper = requestMapper;
    }

}
