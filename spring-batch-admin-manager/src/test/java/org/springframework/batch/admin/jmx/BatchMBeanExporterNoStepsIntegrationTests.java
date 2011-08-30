/*
 * Copyright 2006-2011 the original author or authors.
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

package org.springframework.batch.admin.jmx;

import static org.junit.Assert.assertEquals;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Dave Syer
 *
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class BatchMBeanExporterNoStepsIntegrationTests {
	
	@Autowired
	private MBeanServer server;
	
	@Autowired
	private BatchMBeanExporter exporter;
	
	@Autowired
	private JobRepository jobRepository;
	
	@Before
	public void init() throws Exception {
		exporter.getJobCount();
		exporter.getStepCount();
	}
	
	@Test
	public void testMBeanCreation() throws Exception {
		JobExecution jobExecution = jobRepository.createJobExecution("foo", new JobParameters());
		jobRepository.add(jobExecution.createStepExecution("step"));
		init();
		assertEquals(1, server.queryNames(new ObjectName("*:type=JobExecution,name=foo,*"), null).size());
		assertEquals(0, server.queryNames(new ObjectName("*:type=JobExecution,name=foo,step=step,*"), null).size());
	}

}
