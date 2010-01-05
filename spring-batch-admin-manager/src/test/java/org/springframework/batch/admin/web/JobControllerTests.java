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
import java.util.Date;
import java.util.TimeZone;

import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.web.JobController;
import org.springframework.batch.admin.web.LaunchRequest;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindException;


public class JobControllerTests {

	private JobService jobService = EasyMock.createMock(JobService.class);

	private JobController controller = new JobController(jobService);

	@Test
	public void testTimeFormat() throws Exception {

		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		assertEquals("01:00:01", timeFormat.format(new Date(3601000)).substring(0, 8));

	}

	@Test
	public void testLaunchSunnyDay() throws Exception {

		LaunchRequest request = new LaunchRequest();

		jobService.launch("foo", new JobParameters());
		EasyMock.expectLastCall().andReturn(
				MetaDataInstanceFactory.createJobExecution());
		jobService.listJobInstances("foo", 0, 20);
		EasyMock.expectLastCall().andReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobInstance("foo", 11L)));
		jobService.getJobExecutionsForJobInstance("foo", 11L);
		EasyMock.expectLastCall().andReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobExecution("foo", 11L, 123L)));
		jobService.countJobInstances("foo");
		EasyMock.expectLastCall().andReturn(100);
		jobService.countJobExecutionsForJob("foo");
		EasyMock.expectLastCall().andReturn(1);
		jobService.isLaunchable("foo");
		EasyMock.expectLastCall().andReturn(true);
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		controller.launch(model, "foo", request, new BindException(request,
				"request"), "job");
		assertEquals("foo", request.getJobName());
		// Job, JobInstances, jobParameters, JobExecution, total, next, start, end, launchable
		assertEquals(9, model.size());
		
		assertTrue(model.containsKey("jobExecutionInfo"));
		assertTrue(model.containsKey("job"));

		EasyMock.verify(jobService);

	}

	@Test
	public void testListSunnyDay() throws Exception {

		jobService.listJobs(10, 20);
		EasyMock.expectLastCall().andReturn(Arrays.asList("job"));
		jobService.countJobExecutionsForJob("job");
		EasyMock.expectLastCall().andReturn(12);
		jobService.countJobs();
		EasyMock.expectLastCall().andReturn(100);
		jobService.isLaunchable("job");
		EasyMock.expectLastCall().andReturn(true);
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		controller.jobs(model, 10, 20);
		// Jobs, total, next, previous, start, end
		assertEquals(6, model.size());

		EasyMock.verify(jobService);

	}

	@Test
	public void testJobSunnyDay() throws Exception {

		jobService.listJobInstances("job", 10, 20);
		EasyMock.expectLastCall().andReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobInstance()));
		jobService.getJobExecutionsForJobInstance("job", 12L);
		EasyMock.expectLastCall().andReturn(
				Arrays.asList(MetaDataInstanceFactory.createJobExecution("job", 12L, 123L)));
		jobService.countJobExecutionsForJob("job");
		EasyMock.expectLastCall().andReturn(12);
		jobService.countJobInstances("job");
		EasyMock.expectLastCall().andReturn(100);
		jobService.isLaunchable("job");
		EasyMock.expectLastCall().andReturn(true);
		EasyMock.replay(jobService);

		ExtendedModelMap model = new ExtendedModelMap();
		controller.details(model, "job", null, null, 10, 20);
		// Job, JobInstances, jobParameters, total, next, previous, start, end, launchable
		assertEquals(9, model.size());

		EasyMock.verify(jobService);

	}

}
