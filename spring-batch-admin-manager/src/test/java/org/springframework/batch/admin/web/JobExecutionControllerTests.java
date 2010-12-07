/*
 * Copyright 2009-2010 the original author or authors.
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
package org.springframework.batch.admin.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindException;


public class JobExecutionControllerTests {

	private JobService jobService = EasyMock.createMock(JobService.class);

	private JobExecutionController controller = new JobExecutionController(jobService);

	@Test
	public void testTimeFormat() throws Exception {

		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		assertEquals("01:00:01", timeFormat.format(new Date(3601000)).substring(0, 8));

	}

	@Test
	public void testStopSunnyDay() throws Exception {

		JobExecutionController.StopRequest request = new JobExecutionController.StopRequest();
		request.setJobExecutionId(123L);

		jobService.stop(123L);
		EasyMock.expectLastCall().andReturn(MetaDataInstanceFactory.createJobExecution());
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.stop(model, request, new BindException(request, "request"), 123L);
		// JobExecution
		assertEquals(1, model.size());
		assertEquals("jobs/execution", result);

		assertTrue(model.containsKey("jobExecutionInfo"));

		EasyMock.verify(jobService);

	}

	@Test
	public void testDetailSunnyDay() throws Exception {

		jobService.getJobExecution(123L);
		EasyMock.expectLastCall().andReturn(MetaDataInstanceFactory.createJobExecution());
		jobService.getStepNamesForJob("job");
		EasyMock.expectLastCall().andReturn(Arrays.asList("foo", "bar"));
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.detail(model, 123L, null, null);
		// JobExecution, StepExecutionInfos
		assertEquals(2, model.size());
		assertEquals("jobs/execution", result);

		assertTrue(model.containsKey("jobExecutionInfo"));
		assertTrue(model.containsKey("stepExecutionInfos"));

		EasyMock.verify(jobService);

	}

	@Test
	public void testListForJobSunnyDay() throws Exception {

		jobService.countJobExecutionsForJob("foo");
		EasyMock.expectLastCall().andReturn(100);
		jobService.listJobExecutionsForJob("foo", 10, 20);
		EasyMock.expectLastCall().andReturn(Arrays.asList(MetaDataInstanceFactory.createJobExecution()));
		jobService.countJobExecutionsForJob("foo");
		EasyMock.expectLastCall().andReturn(10);
		jobService.isLaunchable("foo");
		EasyMock.expectLastCall().andReturn(true);
		jobService.isIncrementable("foo");
		EasyMock.expectLastCall().andReturn(true);
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.listForJob(model, "foo", null, null, 10, 20);
		// JobExecutions, Job, total, next, previous, start, end
		assertEquals(7, model.size());
		assertEquals("jobs/executions", result);

		assertTrue(model.containsKey("jobInfo"));

		EasyMock.verify(jobService);

	}

	@Test
	public void testListForJobInstanceSunnyDay() throws Exception {

		jobService.getJobInstance(11L);
		EasyMock.expectLastCall().andReturn(MetaDataInstanceFactory.createJobInstance("foo", 11L));
		jobService.getJobExecutionsForJobInstance("foo", 11L);
		EasyMock.expectLastCall().andReturn(Arrays.asList(MetaDataInstanceFactory.createJobExecution()));
		jobService.isLaunchable("foo");
		EasyMock.expectLastCall().andReturn(true);
		jobService.isIncrementable("foo");
		EasyMock.expectLastCall().andReturn(true);
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.listForInstance(model, "foo", 11L, null, null);
		// JobExecutions, JobInfo, JobInstance, jobParameters
		assertEquals(4, model.size());
		assertEquals("jobs/executions", result);

		assertTrue(model.containsKey("jobInfo"));
		assertTrue(model.containsKey("jobInstanceInfo"));

		EasyMock.verify(jobService);

	}

	@Test
	public void testListForJobInstanceNoSuchJobInstance() throws Exception {

		jobService.getJobInstance(11L);
		EasyMock.expectLastCall().andThrow(new NoSuchJobInstanceException("Foo"));
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		BindException errors = new BindException("target", "target");
		String result = controller.listForInstance(model, "foo", 11L, null, errors);
		assertEquals(1, errors.getAllErrors().size());
		assertEquals("jobs/executions", result);
		
		EasyMock.verify(jobService);

	}

	@Test
	public void testListForJobInstanceWrongJobName() throws Exception {

		jobService.getJobInstance(11L);
		EasyMock.expectLastCall().andReturn(MetaDataInstanceFactory.createJobInstance("bar", 11L));
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		BindException errors = new BindException("target", "target");
		String result = controller.listForInstance(model, "foo", 11L, null, errors);
		assertEquals(1, errors.getAllErrors().size());
		assertEquals("jobs/executions", result);
		
		EasyMock.verify(jobService);

	}

	@Test
	public void testRestartSunnyDay() throws Exception {

		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.setStatus(BatchStatus.FAILED);
		jobService.getJobExecutionsForJobInstance("foo", 11L);
		EasyMock.expectLastCall().andReturn(Arrays.asList(jobExecution));
		jobService.restart(123L);
		EasyMock.expectLastCall().andReturn(jobExecution);
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.restart(model, "foo", 11L, null, null);
		// JobExecution, Job
		assertEquals(2, model.size());
		assertEquals("jobs/execution", result);

		assertTrue(model.containsKey("jobInfo"));

		EasyMock.verify(jobService);

	}

	@Test
	public void testListSunnyDay() throws Exception {

		jobService.countJobExecutions();
		EasyMock.expectLastCall().andReturn(100);
		jobService.listJobExecutions(10, 20);
		EasyMock.expectLastCall().andReturn(Collections.emptyList());
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		controller.list(model, 10, 20);

		EasyMock.verify(jobService);

	}

}
