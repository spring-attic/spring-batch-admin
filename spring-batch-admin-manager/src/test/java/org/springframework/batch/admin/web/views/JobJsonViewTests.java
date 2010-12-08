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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.View;

@ContextConfiguration(loader = WebApplicationContextLoader.class, inheritLocations = false, locations = "AbstractManagerViewTests-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class JobJsonViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	@Qualifier("jobs.json")
	private View jobs;

	@Autowired
	@Qualifier("jobs/job.json")
	private View job;

	private Errors errors = new BindException(new Object(), "launchRequest");

	@Test
	public void testViewWithJob() throws Exception {
		model.put(BindingResult.MODEL_KEY_PREFIX + "launchRequest", errors);
		model.put("jobInfo", new JobInfo("foo", 1));
		model.put("jobInstances", Arrays.asList(new JobInstanceInfo(MetaDataInstanceFactory.createJobInstance("foo",
				1L, "bar=spam"), new ArrayList<JobExecution>())));
		model.put("baseUrl", "http://localhost:8080/springsource");
		model.put("startJobInstance", 11);
		model.put("endJobInstance", 30);
		model.put("totalJobInstances", 100);
		model.put("nextJobInstance", 31);
		model.put("previousJobInstance", 21);
		job.render(model, request, response);
		String content = response.getContentAsString();
		System.err.println(content);
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(1, wrapper.get("job.jobInstances", Map.class).size());
		assertEquals(4, wrapper.get("job", Map.class).size());
		assertEquals(5, wrapper.get("job.page", Map.class).size());
	}

	@Test
	public void testViewWithJobAndNoPagination() throws Exception {
		model.put(BindingResult.MODEL_KEY_PREFIX + "launchRequest", errors);
		model.put("jobInfo", new JobInfo("foo", 1));
		model.put("jobInstances", Arrays.asList(new JobInstanceInfo(MetaDataInstanceFactory.createJobInstance("foo",
				1L, "bar=spam"), new ArrayList<JobExecution>())));
		model.put("baseUrl", "http://localhost:8080/springsource");
		model.put("startJobInstance", 11);
		model.put("endJobInstance", 30);
		model.put("totalJobInstances", 100);
		job.render(model, request, response);
		String content = response.getContentAsString();
		System.err.println(content);
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(1, wrapper.get("job.jobInstances", Map.class).size());
		assertEquals(3, wrapper.get("job", Map.class).size());
	}

	@Test
	public void testViewWithNoSuchJob() throws Exception {
		BindException errors = new BindException(new Object(), "launchRequest");
		model.put("errors", errors);
		errors.reject("no.such.job", "No such Job");
		model.put("baseUrl", "http://localhost:8080/springsource");
		job.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(1, wrapper.get("errors", Map.class).size());
		assertEquals("No such Job", wrapper.get("errors['no.such.job']"));
	}

	@Test
	public void testViewWithLaunchErrors() throws Exception {
		model.put("job", new JobInfo("foo", 1));
		BindException errors = new BindException(new Object(), "launchRequest");
		model.put("errors", errors);
		errors.reject("job.already.complete", "Already complete");
		model.put("baseUrl", "http://localhost:8080/springsource");
		job.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(1, wrapper.get("errors", Map.class).size());
		assertEquals("Already complete", wrapper.get("errors['job.already.complete']"));
	}

	@Test
	public void testViewWithJobAndExecutions() throws Exception {
		model.put(BindingResult.MODEL_KEY_PREFIX + "launchRequest", errors);
		model.put("jobInfo", new JobInfo("foo", 1));
		model.put("jobInstances", Arrays.asList(new JobInstanceInfo(MetaDataInstanceFactory.createJobInstance("foo",
				123456789L, "bar=spam"), Arrays.asList(MetaDataInstanceFactory.createJobExecution()))));
		model.put("baseUrl", "http://localhost:8080/springsource");
		job.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals("STARTING", wrapper.get("job.jobInstances[123456789].lastJobExecutionStatus", String.class));
	}

	@Test
	public void testListViewWithJobs() throws Exception {
		model.put("jobs", Arrays.asList(new JobInfo("foo", 1, true), new JobInfo("bar", 2)));
		model.put("baseUrl", "http://localhost:8080/springsource");
		model.put("startJob", 11);
		model.put("endJob", 30);
		model.put("totalJobs", 100);
		model.put("nextJob", 31);
		model.put("previousJob", 21);
		jobs.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(2, wrapper.get("jobs.registrations", Map.class).size());
		assertEquals(5, wrapper.get("page", Map.class).size());
	}

	@Test
	public void testListViewWithJobsNoPages() throws Exception {
		model.put("jobs", Arrays.asList(new JobInfo("foo", 1, true), new JobInfo("bar", 2)));
		model.put("baseUrl", "http://localhost:8080/springsource");
		model.put("startJob", 11);
		model.put("endJob", 30);
		model.put("totalJobs", 100);
		jobs.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(2, wrapper.get("jobs.registrations", Map.class).size());
		assertEquals(1, wrapper.getMap().size()); // no pages
	}

}
