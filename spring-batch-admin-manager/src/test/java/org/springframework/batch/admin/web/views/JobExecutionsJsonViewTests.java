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
package org.springframework.batch.admin.web.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.admin.web.JobExecutionInfo;
import org.springframework.batch.admin.web.JobInfo;
import org.springframework.batch.admin.web.JobInstanceInfo;
import org.springframework.batch.admin.web.JsonWrapper;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.WebApplicationContextLoader;
import org.springframework.web.servlet.View;

@ContextConfiguration(loader = WebApplicationContextLoader.class, inheritLocations = false, locations = "AbstractManagerViewTests-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class JobExecutionsJsonViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	@Qualifier("jobs/executions.json")
	private View view;

	@Test
	public void testJobExecutionsForInstance() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		model.put("jobExecutions", Arrays.asList(new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT"))));
		model.put("baseUrl", "http://localhost:8080/springsource");
		model.put("jobInfo", new JobInfo("foo", 1, 123L, false, false));
		model.put("jobInstanceInfo", new JobInstanceInfo(MetaDataInstanceFactory.createJobInstance("job", 112L,
				"foo=bar,spam=foo"), Arrays.asList(jobExecution)));
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("jobInstance"));
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(3, wrapper.get("jobInstance", Map.class).size());
		assertEquals(2, wrapper.get("jobInstance.jobParameters", Map.class).size());
	}

	@Test
	public void testViewWithJobExecutionInfo() throws Exception {
		JobExecution jobExecution1 = MetaDataInstanceFactory.createJobExecution();
		JobExecution jobExecution2 = MetaDataInstanceFactory.createJobExecution(13L);
		jobExecution2.setEndTime(new Date());
		model.put("baseUrl", "http://localhost:8080/springsource");
		model.put("jobExecutions", Arrays.asList(new JobExecutionInfo(jobExecution1, TimeZone.getTimeZone("GMT")),
				new JobExecutionInfo(jobExecution2, TimeZone.getTimeZone("GMT"))));
		model.put("currentTime", new Date());
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(2, wrapper.get("jobExecutions", Map.class).size());
	}

	@Test
	public void testViewWithPagination() throws Exception {
		JobExecution jobExecution1 = MetaDataInstanceFactory.createJobExecution();
		JobExecution jobExecution2 = MetaDataInstanceFactory.createJobExecution(13L);
		jobExecution2.setEndTime(new Date());
		model.put("baseUrl", "http://localhost:8080/springsource");
		model.put("jobExecutions", Arrays.asList(new JobExecutionInfo(jobExecution1, TimeZone.getTimeZone("GMT")),
				new JobExecutionInfo(jobExecution2, TimeZone.getTimeZone("GMT"))));
		model.put("currentTime", new Date());
		model.put("startJobExecution", 11);
		model.put("endJobExecution", 30);
		model.put("totalJobExecutions", 100);
		model.put("nextJobExecution", 31);
		model.put("previousJobExecution", 21);
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(2, wrapper.get("jobExecutions", Map.class).size());
		assertEquals(5, wrapper.get("page", Map.class).size());
	}

	@Test
	public void testViewWithNoPagination() throws Exception {
		JobExecution jobExecution1 = MetaDataInstanceFactory.createJobExecution();
		JobExecution jobExecution2 = MetaDataInstanceFactory.createJobExecution(13L);
		jobExecution2.setEndTime(new Date());
		model.put("baseUrl", "http://localhost:8080/springsource");
		model.put("jobExecutions", Arrays.asList(new JobExecutionInfo(jobExecution1, TimeZone.getTimeZone("GMT")),
				new JobExecutionInfo(jobExecution2, TimeZone.getTimeZone("GMT"))));
		model.put("currentTime", new Date());
		model.put("startJobExecution", 1);
		model.put("endJobExecution", 30);
		model.put("totalJobExecutions", 30);
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(2, wrapper.get("jobExecutions", Map.class).size());
		assertEquals(1, wrapper.getMap().size()); // no pages if they are all there
	}

}
