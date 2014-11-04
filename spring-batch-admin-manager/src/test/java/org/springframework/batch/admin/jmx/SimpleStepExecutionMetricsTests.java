package org.springframework.batch.admin.jmx;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

public class SimpleStepExecutionMetricsTests {

	private SimpleStepExecutionMetrics metrics;

	@Mock
	private JobService jobService;

	private StepExecution stepExecution;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		stepExecution = MetaDataInstanceFactory.createStepExecution("step", 123L);
		stepExecution.setStatus(BatchStatus.COMPLETED);
		stepExecution.setExitStatus(ExitStatus.COMPLETED);
		stepExecution.setStartTime(new Date());
		stepExecution.setEndTime(new Date(stepExecution.getStartTime().getTime() + 100));
		metrics = new SimpleStepExecutionMetrics(jobService, "job", "step");
	}
	
	private void prepareServiceWithSingleStepExecution() throws Exception {
		when(jobService.listStepExecutionsForStep("job", "step", 0, 4)).thenReturn(Arrays.asList(stepExecution));
	}

	private void prepareServiceWithMultipleStepExecutions() throws Exception {
		when(jobService.listStepExecutionsForStep("job", "step", 0, 100)).thenReturn(Arrays.asList(stepExecution));
		when(jobService.listStepExecutionsForStep("job", "step", 100, 100)).thenReturn(new ArrayList<StepExecution>());
	}

	private void prepareServiceWithMultipleStepExecutions(int total) throws Exception {
		when(jobService.listStepExecutionsForStep("job", "step", 0, total)).thenReturn(Arrays.asList(stepExecution));
	}

	@Test
	public void testGetStepExecutionCount() throws Exception {
		when(jobService.countStepExecutionsForStep("job", "step")).thenReturn(10);
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
