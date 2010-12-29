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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.admin.web.JobInfo;
import org.springframework.batch.admin.web.JobInstanceInfo;
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
public class JobViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	private View jobs;

	@Autowired
	@Qualifier("jobs/job")
	private View job;
	
	@Test
	public void testRegex() throws Exception {
		String pattern = "([\\w\\.-_\\)\\(]+=.*[,\\n])*([\\w\\.-_\\)\\(]+=.*)";
		assertTrue("foo=bar".matches(pattern));
		assertTrue("foo=bar,spam=bucket".matches(pattern));
		assertTrue("foo(long)=bar".matches(pattern));
		assertTrue("foo=bar\nbar=spam".matches(pattern));
	}

	@Test
	public void testListViewWithJobs() throws Exception {
		model.put("jobs", Arrays.asList(new JobInfo("foo", 1, true), new JobInfo("bar", 2)));
		model.put("startJob", 3);
		model.put("endJob", 4);
		model.put("nextJob", 4);
		model.put("previousJob", 0);
		model.put("totalJobs", 100);
		jobs.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("<th>Name</th>"));
		assertTrue(content.contains("<td><a href=\"/jobs/foo\">foo</a></td>"));
		assertTrue(content.contains("<td>1</td>"));
	}

	@Test
	public void testJobView() throws Exception {
		model.put("jobInfo", new JobInfo("foo", 1, true));
		model.put("jobInstances", Arrays.asList(new JobInstanceInfo(MetaDataInstanceFactory.createJobInstance("foo",
				1L, "bar=spam"), new ArrayList<JobExecution>())));
		model.put("jobParameters", "foo=bar");
		model.put("startJobInstance", 3);
		model.put("endJobInstance", 4);
		model.put("nextJobInstance", 4);
		model.put("previousJobInstance", 0);
		model.put("totalJobInstances", 100);
		job.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("Job name=foo"));
		assertTrue(content.contains("foo=bar"));
		assertTrue(content.contains("<form id=\"launchForm\" action=\"/jobs/foo\" method=\"POST\">"));
		assertFalse(content.contains("<input type=\"hidden\" name=\"_method\""));
		assertTrue(content.contains("<th>ID</th>"));
	}

	@Test
	public void testJobViewNotLaunchable() throws Exception {
		model.put("job", new JobInfo("foo", 1));
		model.put("jobInstances", Arrays.asList(new JobInstanceInfo(MetaDataInstanceFactory.createJobInstance("foo",
				1L, "bar=spam"), new ArrayList<JobExecution>())));
		model.put("launchable", false);
		model.put("startJobInstance", 3);
		model.put("endJobInstance", 4);
		model.put("totalJobInstances", 100);
		job.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertFalse(content.contains("<form id=\"launchForm\" action=\"/jobs/foo\" method=\"POST\">"));
	}
	
}
