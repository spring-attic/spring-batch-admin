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
package org.springframework.batch.admin.integration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.batch.test.MetaDataInstanceFactory;

public class JobRestartRequestStringAdapterTests {

	private JobNameToJobRestartRequestAdapter adapter = new JobNameToJobRestartRequestAdapter();

	private MapJobRegistry jobRegistry = new MapJobRegistry();

	private JobExplorer jobExplorer = EasyMock.createNiceMock(JobExplorer.class);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void init() throws Exception {
		jobRegistry.register(new ReferenceJobFactory(new SimpleJob("foo")));
		adapter.setJobLocator(jobRegistry);
		adapter.setJobExplorer(jobExplorer);
	}

	@Test
	public void testSimpleJob() throws Exception {

		JobInstance jobInstance = MetaDataInstanceFactory.createJobInstance("foo", 11L, "bar=spam");
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution("foo", 11L, 123L);
		jobExecution.setEndTime(new Date());
		jobExecution.setStatus(BatchStatus.FAILED);

		jobExplorer.getJobInstances("foo", 0, 100);
		EasyMock.expectLastCall().andReturn(Arrays.asList(jobInstance));
		jobExplorer.getJobExecutions(jobInstance);
		EasyMock.expectLastCall().andReturn(Arrays.asList(jobExecution));
		EasyMock.replay(jobExplorer);

		JobLaunchRequest request = adapter.adapt("foo");
		assertEquals("foo", request.getJob().getName());
		assertEquals(1, request.getJobParameters().getParameters().size());
		assertEquals("spam", request.getJobParameters().getString("bar"));

		EasyMock.verify(jobExplorer);

	}

	@Test
	public void testSimpleJobNotFailed() throws Exception {
		
		thrown.expect(JobParametersNotFoundException.class);
		thrown.expectMessage("No failed or stopped execution");

		JobInstance jobInstance = MetaDataInstanceFactory.createJobInstance("foo", 11L, "bar=spam");
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution("foo", 11L, 123L);
		jobExecution.setEndTime(new Date());
		jobExecution.setStatus(BatchStatus.COMPLETED);

		jobExplorer.getJobInstances("foo", 0, 100);
		EasyMock.expectLastCall().andReturn(Arrays.asList(jobInstance));
		jobExplorer.getJobExecutions(jobInstance);
		EasyMock.expectLastCall().andReturn(Arrays.asList(jobExecution));
		jobExplorer.getJobInstances("foo", 100, 100);
		EasyMock.expectLastCall().andReturn(new ArrayList<JobInstance>());
		EasyMock.replay(jobExplorer);

		adapter.adapt("foo");

		EasyMock.verify(jobExplorer);

	}
}
