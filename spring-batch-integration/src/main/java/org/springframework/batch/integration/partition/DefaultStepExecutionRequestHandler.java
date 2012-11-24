/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.integration.partition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.StepRegistry;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.partition.support.CompositeStepExecutionRequestHandlerListener;
import org.springframework.batch.integration.partition.support.StepExecutionRequestMapper;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link StepExecutionRequestHandlerProcessor}.
 *
 * @author Sebastien Gerard
 */
public class DefaultStepExecutionRequestHandler implements StepExecutionRequestHandler {

    private static final Log logger = LogFactory.getLog(DefaultStepExecutionRequestHandler.class);

    private StepRegistry stepRegistry;
    private StepExecutionRequestMapper stepExecutionRequestMapper;
    private JobRepository jobRepository;
    private StepExecutionRequestHandlerListener handlerListener = new CompositeStepExecutionRequestHandlerListener();

    public StepExecutionResult handle(StepExecutionRequest request) {
        final StepExecution stepExecution;

        try {
            stepExecution = stepExecutionRequestMapper.map(request);
        } catch (Throwable e) {
            logger.error("Failed to retrieve step execution from " + request, e);
            return new StepExecutionResult(request, Collections.singletonList(e));
        }

        doHandle(request.getStepName(), stepExecution);

        return new StepExecutionResult(request, stepExecution.getFailureExceptions());
    }

    /**
     * Handles the processing of the given step name.
     *
     * @param stepName the given step name
     * @param stepExecution the step execution to use
     */
    protected void doHandle(String stepName, StepExecution stepExecution) {
        try {
            beforeHandling(stepExecution);
            checkStepExecution(stepExecution);

            final String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();

            final Step step = stepRegistry.getStep(jobName, stepName);

            step.execute(stepExecution);
        } catch (JobInterruptedException e) {
            logger.warn("Job has been interrupted, stopping execution of ["+stepExecution.getStepName()+"]");
            stepExecution.setStatus(BatchStatus.STOPPED);
            stepExecution.setExitStatus(ExitStatus.STOPPED);
        } catch (Throwable e) {
            logger.error("Failed to execute step [" + stepExecution.getStepName() + "]", e);
            stepExecution.addFailureException(e);
            stepExecution.setStatus(BatchStatus.FAILED);
            stepExecution.setExitStatus(ExitStatus.FAILED);
        } finally {
            afterHandling(stepExecution);
            updateStepExecution(stepExecution);
        }
    }

    /**
     * Checks that the given step execution has not been already started.
     *
     * @param stepExecution the step execution to check
     * @throws JobExecutionAlreadyRunningException if the step execution has been already started
     */
    protected void checkStepExecution(StepExecution stepExecution) throws JobExecutionAlreadyRunningException {
        try {
            final boolean canProcess = (stepExecution.getStatus() == BatchStatus.STARTING);

            if (!canProcess) {
                throw new JobExecutionAlreadyRunningException("The step  [" + stepExecution +
                        "] has been already started in previous execution");
            }

            final JobExecution newestJobExecution =
                    jobRepository.getLastJobExecution(stepExecution.getJobExecution().getJobInstance().getJobName(),
                            stepExecution.getJobExecution().getJobInstance().getJobParameters());

            if ((newestJobExecution == null) || !newestJobExecution.getId().equals(stepExecution.getJobExecutionId())) {
                throw new IllegalStateException("Cannot execute the step with the execution [" + stepExecution +
                        "] because the job has been restarted in the meantime. The Newest step execution is [" +
                        newestJobExecution + "] while the current is [" + stepExecution + "]");
            }
        } catch (Exception e) {
            onDeliveryException(stepExecution);
        }
    }

    /**
     * Persists the given step execution.
     *
     * @param stepExecution the given step execution
     */
    protected void updateStepExecution(StepExecution stepExecution) {
        try {
            jobRepository.update(stepExecution);
            jobRepository.updateExecutionContext(stepExecution);
        } catch (Exception e) {
            stepExecution.setStatus(BatchStatus.UNKNOWN);
            stepExecution.setExitStatus(ExitStatus.UNKNOWN);
            stepExecution.addFailureException(e);
            logger.error("Encountered an error saving batch meta data. "
                    + "This job is now in an unknown state and should not be restarted.", e);
        }
    }

    /**
     * Calls back the listener notifying that the given step may be executed.
     *
     * @param stepExecution the current step execution
     */
    protected void beforeHandling(StepExecution stepExecution) {
        handlerListener.beforeHandle(stepExecution);
    }

    /**
     * Calls back the listener notifying that the given step has been executed.
     *
     * @param stepExecution the current step execution
     */
    protected void afterHandling(StepExecution stepExecution) {
        handlerListener.afterHandle(stepExecution);
    }

    /**
     * Calls back the listener notifying a delivery exception.
     *
     * @param stepExecution the current step execution
     */
    protected void onDeliveryException(StepExecution stepExecution) {
        handlerListener.onDeniedException(stepExecution);
    }

    /**
     * Sets the registry locating steps to call.
     *
     * @param stepRegistry the step registry to use
     */
    @Required
    public void setStepRegistry(StepRegistry stepRegistry) {
        this.stepRegistry = stepRegistry;
    }

    /**
     * Sets the mapper retrieving a step execution from a request.
     *
     * @param stepExecutionRequestMapper the mapper to use
     */
    @Required
    public void setStepExecutionRequestMapper(StepExecutionRequestMapper stepExecutionRequestMapper) {
        this.stepExecutionRequestMapper = stepExecutionRequestMapper;
    }

    /**
     * Sets the repository containing job information.
     *
     * @param jobRepository the repository to use
     */
    @Required
    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Sets the set of listeners to call back.
     *
     * @param handlerListener the listener to use
     */
    public void setHandlingListener(List<StepExecutionRequestHandlerListener> handlerListener) {
        this.handlerListener = new CompositeStepExecutionRequestHandlerListener(handlerListener);
    }

}
