package org.springframework.batch.admin.jmx;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

public class SimpleStepExecutionMetricsTests {

	private SimpleStepExecutionMetrics metrics;

	private JobService jobService = EasyMock.createMock(JobService.class);

	private StepExecution stepExecution;

	@Before
	public void init() throws Exception {
		stepExecution = MetaDataInstanceFactory.createStepExecution("step", 123L);
		stepExecution.setStatus(BatchStatus.COMPLETED);
		stepExecution.setExitStatus(ExitStatus.COMPLETED);
		stepExecution.setStartTime(new Date());
		stepExecution.setEndTime(new Date(stepExecution.getStartTime().getTime() + 100));
		metrics = new SimpleStepExecutionMetrics(jobService, "job", "step");
	}
	
	@After
	public void verify() {
		EasyMock.verify(jobService);
	}

	private void prepareServiceWithSingleStepExecution() throws Exception {
		jobService.listStepExecutionsForStep("job", "step", 0, 4);
		EasyMock.expectLastCall().andReturn(Arrays.asList(stepExecution));
		EasyMock.replay(jobService);
	}

	private void prepareServiceWithMultipleStepExecutions() throws Exception {
		jobService.listStepExecutionsForStep("job", "step", 0, 100);
		EasyMock.expectLastCall().andReturn(Arrays.asList(stepExecution));
		jobService.listStepExecutionsForStep("job", "step", 100, 100);
		EasyMock.expectLastCall().andReturn(Arrays.asList());
		EasyMock.replay(jobService);
	}

	private void prepareServiceWithMultipleStepExecutions(int total) throws Exception {
		jobService.listStepExecutionsForStep("job", "step", 0, total);
		EasyMock.expectLastCall().andReturn(Arrays.asList(stepExecution));
		EasyMock.replay(jobService);
	}

	@Test
	public void testGetStepExecutionCount() throws Exception {
		jobService.countStepExecutionsForStep("job", "step");
		EasyMock.expectLastCall().andReturn(10);
		EasyMock.replay(jobService);
		assertEquals(10, metrics.getExecutionCount());
	}

	@Test
	public void testGetStepExecutionFailureCount() throws Exception {
		prepareServiceWithMultipleStepExecutions();
		assertEquals(0, metrics.getFailureCount());		
	}

	@Test
	public void testGetMeanStepExecutionDuration() throws Exception {
		prepareServiceWithMultipleStepExecutions(10);
		assertEquals(stepExecution.getEndTime().getTime() - stepExecution.getStartTime().getTime(), metrics.getMeanDuration(), .001);		
	}

	@Test
	public void testGetMaxStepExecutionDuration() throws Exception {
		prepareServiceWithMultipleStepExecutions(10);
		assertEquals(stepExecution.getEndTime().getTime() - stepExecution.getStartTime().getTime(), metrics.getMaxDuration(), .001);				
	}

	@Test
	public void testGetLatestStepExecutionDuration() throws Exception {
		prepareServiceWithSingleStepExecution();
		assertEquals(stepExecution.getEndTime().getTime() - stepExecution.getStartTime().getTime(), metrics.getLatestDuration(), .001);
	}

	@Test
	public void testGetLatestStepExecutionDurationIncomplete() throws Exception {
		stepExecution.setEndTime(null);
		prepareServiceWithSingleStepExecution();
		assertEquals(System.currentTimeMillis() - stepExecution.getStartTime().getTime(), metrics.getLatestDuration(), 10);
	}

	@Test
	public void testGetLatestStepExecutionReadCount() throws Exception {
		prepareServiceWithSingleStepExecution();
		assertEquals(0, metrics.getLatestReadCount());		
	}

	@Test
	public void testGetLatestStepExecutionWriteCount() throws Exception {
		prepareServiceWithSingleStepExecution();
		assertEquals(0, metrics.getLatestWriteCount());
	}

	@Test
	public void testGetLatestStepExecutionFilterCount() throws Exception {
		prepareServiceWithSingleStepExecution();
		assertEquals(0, metrics.getLatestFilterCount());		
	}

	@Test
	public void testGetLatestStepExecutionSkipCount() throws Exception {
		prepareServiceWithSingleStepExecution();
		assertEquals(0, metrics.getLatestSkipCount());		
	}

	@Test
	public void testGetLatestStepExecutionCommitCount() throws Exception {
		prepareServiceWithSingleStepExecution();
		assertEquals(0, metrics.getLatestCommitCount());		
	}

	@Test
	public void testGetLatestStepExecutionRollbackCount() throws Exception {
		prepareServiceWithSingleStepExecution();
		assertEquals(0, metrics.getLatestRollbackCount());		
	}

}
