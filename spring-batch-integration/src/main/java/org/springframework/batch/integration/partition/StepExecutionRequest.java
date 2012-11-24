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

import org.springframework.batch.core.StepExecution;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Materializes a request to execute a particular
 * {@link org.springframework.batch.core.Step} with a given
 * {@link org.springframework.batch.core.StepExecution}. Used to distribute
 * partition executions on multiple workers.
 *
 * @author Dave Syer
 */
public class StepExecutionRequest implements Serializable {

    private static final long serialVersionUID = -3730313177540237305L;

    private final String stepName;

    private final Long jobExecutionId;

	private final Long stepExecutionId;

    /**
     * Creates a new instance.
     *
     * @param stepName the name of the step to execute
     * @param jobExecutionId the id of the current job execution
     * @param stepExecutionId the id of the partition execution to be started
     */
	public StepExecutionRequest(String stepName, Long jobExecutionId, Long stepExecutionId) {
        Assert.notNull(stepName, "The step name cannot be null");
        this.stepName = stepName;
		this.jobExecutionId = jobExecutionId;
		this.stepExecutionId = stepExecutionId;
	}

    /**
     * Creates a request mentioning the given {@link org.springframework.batch.core.StepExecution}.
     *
     * @param actualStepName the actual name of the step to execute
     * @param stepExecution the given {@link org.springframework.batch.core.StepExecution}.
     */
    public StepExecutionRequest(final String actualStepName, final StepExecution stepExecution) {
        this(actualStepName, stepExecution.getJobExecutionId(), stepExecution.getId());
    }

    /**
     * @return the step to which the partition belongs
     */
    public String getStepName() {
		return stepName;
	}

    /**
     * @return the id of the current job execution
     */
	public Long getJobExecutionId() {
		return jobExecutionId;
	}

    /**
     * @return the id of the partition execution to be started
     */
	public Long getStepExecutionId() {
		return stepExecutionId;
	}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepExecutionRequest that = (StepExecutionRequest) o;

        if (!jobExecutionId.equals(that.jobExecutionId)) return false;
        if (!stepExecutionId.equals(that.stepExecutionId)) return false;
        if (!stepName.equals(that.stepName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = stepName.hashCode();
        result = 31 * result + jobExecutionId.hashCode();
        result = 31 * result + stepExecutionId.hashCode();
        return result;
    }

    @Override
	public String toString() {
		return String.format("StepExecutionRequest: [jobExecutionId=%d, stepExecutionId=%d, stepName=%s]",
				jobExecutionId, stepExecutionId, stepName);
	}

}
