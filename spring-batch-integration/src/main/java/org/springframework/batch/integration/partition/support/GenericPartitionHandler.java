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
package org.springframework.batch.integration.partition.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.partition.support.AbstractPartitionHandler;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.batch.integration.partition.StepExecutionRequestAggregator;
import org.springframework.batch.integration.partition.StepExecutionResult;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A generic {@link org.springframework.batch.core.partition.PartitionHandler}
 * implementation that delegates the processing of each partition to a
 * {@link StepExecutionRequestAggregator}, based on a standard
 * request/response model
 *
 * @author Sebastien Gerard
 * @author Stephane Nicoll
 * @see StepExecutionRequestAggregator
 * @see StepExecutionRequest
 * @see StepExecutionResult
 */
public class GenericPartitionHandler extends AbstractPartitionHandler implements InitializingBean {

    private static final Log log = LogFactory.getLog(GenericPartitionHandler.class);

    private StepExecutionRequestAggregator requestAggregator;
    private JobExplorer jobExplorer;
    private StepExecutionResultMapper resultMapper;
    private String stepName;

    @Override
    protected Set<StepExecution> doHandle(StepExecution masterStepExecution,
                                          Set<StepExecution> partitionStepExecutions) throws Exception {
        final List<StepExecutionRequest> stepExecutionRequests =
                createRequests(masterStepExecution, partitionStepExecutions);

        final List<StepExecutionResult> results = requestAggregator.aggregate(stepExecutionRequests);

        return mapStepExecutionResults(masterStepExecution, results);
    }

    /**
     * Initializes requests that will be sent to remote workers.
     *
     * @param masterStepExecution the whole partition step execution
     * @param partitionStepExecutions the partition step executions from which requests are created
     * @return requests based on the given set of partition step executions
     */
    protected List<StepExecutionRequest> createRequests(StepExecution masterStepExecution,
                                                        Set<StepExecution> partitionStepExecutions) {
        final List<StepExecutionRequest> requests = new ArrayList<StepExecutionRequest>();

        for (StepExecution stepExecution : partitionStepExecutions) {
            requests.add(new StepExecutionRequest(stepName, stepExecution));
        }

        return requests;
    }

    /**
     * Returns step executions referenced in the specified <tt>results</tt>.
     *
     * @param masterStepExecution the whole partition step execution
     * @param results step execution results
     * @return the corresponding executions
     * @throws NoSuchJobExecutionException if the job execution mentioned in the request does not exist
     * @throws NoSuchStepException if the step mentioned in the request does not exist in the
     * {@link org.springframework.batch.core.JobExecution}
     */
    protected Set<StepExecution> mapStepExecutionResults(StepExecution masterStepExecution,
                                                         List<StepExecutionResult> results)
            throws NoSuchJobExecutionException, NoSuchStepException {

        final Set<StepExecution> stepExecutions = new HashSet<StepExecution>();

        for (StepExecutionResult result : results) {
            stepExecutions.add(resultMapper.map(result, masterStepExecution));
        }

        return stepExecutions;
    }

    /**
     * Sets the aggregator responsible of sending requests and waiting for results.
     *
     * @param requestAggregator the aggregator to use
     */
    @Required
    public void setRequestAggregator(StepExecutionRequestAggregator requestAggregator) {
        this.requestAggregator = requestAggregator;
    }

    /**
     * Sets the job explorer to use
     *
     * @param jobExplorer the job explorer to use
     */
    @Required
    public void setJobExplorer(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    void setResultMapper(StepExecutionResultMapper resultMapper) {
        this.resultMapper = resultMapper;
    }

    /**
     * Sets the actual name of the partition (not the internal one).
     *
     * @param stepName the actual partition step name
     */
    @Required
    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public void afterPropertiesSet() {
        if (resultMapper == null) {
            Assert.notNull(jobExplorer, "JobExplorer must be set if not result mapper is set.");
            log.debug("Result mapper not set, using default implementation based on the specified jobExplorer");
            final StepExecutionRequestMapper requestMapper = new DefaultStepExecutionRequestMapper(jobExplorer);
            this.resultMapper = new DefaultStepExecutionResultMapper(requestMapper);
        }
    }

}
