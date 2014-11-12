/*
 * Copyright 2009-2013 the original author or authors.
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
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.batch.admin.domain.StepExecutionInfo;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindException;


public class JobExecutionControllerTests {

	@Mock
	private JobService jobService;

	private JobExecutionController controller;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		controller = new JobExecutionController(jobService);
	}

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

		when(jobService.stop(123L)).thenReturn(MetaDataInstanceFactory.createJobExecution());

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.stop(model, request, new BindException(request, "request"), 123L);
		// JobExecution
		assertEquals(1, model.size());
		assertEquals("jobs/execution", result);

		assertTrue(model.containsKey("jobExecutionInfo"));
	}

	@Test
	public void testDetailSunnyDay() throws Exception {

		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		MetaDataInstanceFactory.createStepExecution(jobExecution,"foo", 111L);
		MetaDataInstanceFactory.createStepExecution(jobExecution, "bar", 222L);
		when(jobService.getJobExecution(123L)).thenReturn(jobExecution);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.detail(model, 123L, null, null);
		// JobExecution, StepExecutionInfos
		assertEquals(2, model.size());
		assertEquals("jobs/execution", result);

		assertTrue(model.containsKey("jobExecutionInfo"));
		assertTrue(model.containsKey("stepExecutionInfos"));
		assertTrue(((List<StepExecutionInfo>) model.get("stepExecutionInfos")).get(0).getName().equals("foo"));
		assertTrue(((List<StepExecutionInfo>) model.get("stepExecutionInfos")).get(1).getName().equals("bar"));
	}

	@Test
	public void testListForJobSunnyDay() throws Exception {

		when(jobService.countJobExecutionsForJob("foo")).thenReturn(100).thenReturn(10);
		when(jobService.listJobExecutionsForJob("foo", 10, 20)).thenReturn(Arrays.asList(MetaDataInstanceFactory.createJobExecution()));
		when(jobService.isLaunchable("foo")).thenReturn(true);
		when(jobService.isIncrementable("foo")).thenReturn(true);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.listForJob(model, "foo", null, null, 10, 20);
		// JobExecutions, Job, total, next, previous, start, end
		for (Map.Entry curEntry : model.entrySet()) {
			System.out.println(curEntry.getKey() + " : " + curEntry.getValue());
		}
		assertEquals(7, model.size());
		assertEquals("jobs/executions", result);

		assertTrue(model.containsKey("jobInfo"));
	}

	@Test
	public void testListForJobInstanceSunnyDay() throws Exception {

		when(jobService.getJobInstance(11L)).thenReturn(MetaDataInstanceFactory.createJobInstance("foo", 11L));
		when(jobService.getJobExecutionsForJobInstance("foo", 11L)).thenReturn(Arrays.asList(MetaDataInstanceFactory.createJobExecution()));
		when(jobService.isLaunchable("foo")).thenReturn(true);
		when(jobService.isIncrementable("foo")).thenReturn(true);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.listForInstance(model, "foo", 11L, null, null);
		// JobExecutions, JobInfo, JobInstance, jobParameters
		assertEquals(2, model.size());
		assertEquals("jobs/executions", result);

		assertTrue(model.containsKey("jobInfo"));
		assertTrue(model.containsKey("jobExecutions"));
	}

	@Test
	public void testListForJobInstanceNoSuchJobInstance() throws Exception {

		when(jobService.getJobInstance(11L)).thenThrow(new NoSuchJobInstanceException("Foo"));

		ExtendedModelMap model = new ExtendedModelMap();
		BindException errors = new BindException("target", "target");
		String result = controller.listForInstance(model, "foo", 11L, null, errors);
		assertEquals(1, errors.getAllErrors().size());
		assertEquals("jobs/executions", result);
	}

	@Test
	public void testListForJobInstanceWrongJobName() throws Exception {

		when(jobService.getJobInstance(11L)).thenReturn(MetaDataInstanceFactory.createJobInstance("bar", 11L));

		ExtendedModelMap model = new ExtendedModelMap();
		BindException errors = new BindException("target", "target");
		String result = controller.listForInstance(model, "foo", 11L, null, errors);
		assertEquals(1, errors.getAllErrors().size());
		assertEquals("jobs/executions", result);
	}

	@Test
	public void testRestartSunnyDay() throws Exception {

		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.setStatus(BatchStatus.FAILED);
		when(jobService.getJobExecutionsForJobInstance("foo", 11L)).thenReturn(Arrays.asList(jobExecution));
		when(jobService.restart(123L)).thenReturn(jobExecution);

		ExtendedModelMap model = new ExtendedModelMap();
		String result = controller.restart(model, "foo", 11L, null, null);
		// JobExecution, Job
		assertEquals(2, model.size());
		assertEquals("jobs/execution", result);

		assertTrue(model.containsKey("jobInfo"));
	}

	@Test
	public void testListSunnyDay() throws Exception {

		when(jobService.countJobExecutions()).thenReturn(100);
		when(jobService.listJobExecutions(10, 20)).thenReturn(new ArrayList<JobExecution>());

		ExtendedModelMap model = new ExtendedModelMap();
		controller.list(model, 10, 20);
	}

}
