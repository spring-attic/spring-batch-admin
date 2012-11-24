package org.springframework.batch.integration.partition.support;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.support.SimpleJobExplorer;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.dao.*;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.integration.partition.StepExecutionRequest;

/**
 * @author Sebastien Gerard
 */
public class StepExecutionRequestMapperTests {

    protected static final String EXCEPTION_NOT_THROWN_MSG = "An exception should have been thrown";

    @Test
    public void mapNullRequest() throws NoSuchJobExecutionException {
        final TestContext context = new TestContext();

        try {
            context.requestMapper.map(null);
            Assert.fail(EXCEPTION_NOT_THROWN_MSG);
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void jobExecutionNotFound() {
        final TestContext context = new TestContext();

        try {
            context.requestMapper.map(new StepExecutionRequest("toto", 1234L, 987654321L));
            Assert.fail(EXCEPTION_NOT_THROWN_MSG);
        } catch (NoSuchJobExecutionException e) {
        }
    }

    @Test
    public void stepExecutionNotFound() throws NoSuchJobExecutionException {
        final TestContext context = new TestContext();
        final JobExecution jobExecution = createDefaultJobExecution(context);

        try {
            context.requestMapper.map(new StepExecutionRequest("toto", jobExecution.getId(), 1234L));
            Assert.fail(EXCEPTION_NOT_THROWN_MSG);
        } catch (NoSuchStepException e) {
        }
    }

    @Test
    public void stepExecutionFoundMapped() throws NoSuchJobExecutionException {
        final TestContext context = new TestContext();
        final StepExecution defaultStepExecution = createDefaultStepExecution(context);

        Assert.assertEquals("The step execution is not the expected one",
                defaultStepExecution, context.requestMapper.map(new StepExecutionRequest("step name", defaultStepExecution)));
    }

    protected JobExecution createDefaultJobExecution(TestContext context) {
        final JobExecution jobExecution = new JobExecution(new JobInstance(1234L, new JobParameters(), "myJob"));

        context.jobExecutionDao.saveJobExecution(jobExecution);

        return jobExecution;
    }

    protected StepExecution createDefaultStepExecution(TestContext context) {
        final StepExecution stepExecution = new StepExecution("myStep", createDefaultJobExecution(context));

        context.stepExecutionDao.saveStepExecution(stepExecution);

        return stepExecution;
    }

    protected static class TestContext {
        public final DefaultStepExecutionRequestMapper requestMapper;
        public final JobExecutionDao jobExecutionDao;
        public final StepExecutionDao stepExecutionDao;

        public TestContext() {
            requestMapper = new DefaultStepExecutionRequestMapper();
            jobExecutionDao = new MapJobExecutionDao();
            stepExecutionDao = new MapStepExecutionDao();
            requestMapper.setJobExplorer(new SimpleJobExplorer(new MapJobInstanceDao(), jobExecutionDao,
                    stepExecutionDao, new MapExecutionContextDao()));
        }
    }
}
