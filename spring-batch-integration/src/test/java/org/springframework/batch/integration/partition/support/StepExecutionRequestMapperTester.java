package org.springframework.batch.integration.partition.support;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.integration.partition.StepExecutionRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stephane Nicoll
 */
public class StepExecutionRequestMapperTester implements StepExecutionRequestMapper {

    private final Map<Long, StepExecution> stepExecutionsMap = new HashMap<Long, StepExecution>();

    public StepExecutionRequestMapperTester(StepExecution... stepExecutions) {
        for (StepExecution stepExecution : stepExecutions) {
            stepExecutionsMap.put(stepExecution.getId(), stepExecution);
        }
    }

    public StepExecution map(StepExecutionRequest request) {
        final StepExecution stepExecution = stepExecutionsMap.get(request.getStepExecutionId());
        if (stepExecution != null) {
            return stepExecution;
        } else {
            throw new NoSuchStepException("Cannot find the step execution with the id [" + request.getStepExecutionId() + "]");
        }
    }
}
