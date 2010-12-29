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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.admin.web.JobExecutionInfo;
import org.springframework.batch.admin.web.JobInfo;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.WebApplicationContextLoader;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.servlet.View;

@ContextConfiguration(loader = WebApplicationContextLoader.class, inheritLocations = false, locations = "AbstractManagerViewTests-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class JobExecutionsViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	@Qualifier("jobs/executions")
	private View view;

	@Test
	public void testLaunchedJobExecutions() throws Exception {
		model.put("jobExecutions", Arrays.asList(new JobExecutionInfo(MetaDataInstanceFactory.createJobExecution(),
				TimeZone.getTimeZone("GMT"))));
		model.put(BindingResult.MODEL_KEY_PREFIX + "stopRequest", new MapBindingResult(model, "stopRequest"));
		model.put("startJobExecution", 3);
		model.put("endJobExecution", 4);
		model.put("nextJobExecution", 4);
		model.put("previousJobExecution", 0);
		model.put("totalJobExecutions", 100);
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("Recent and Current Job Executions"));
		assertTrue(content.contains("<a href=\"/jobs/executions/123\">"));
		assertTrue(content.contains("title=\"RSS Feed\" href=\"/jobs/executions.rss\">"));
	}

	@Test
	public void testJobExecutionsForInstance() throws Exception {
		model.put("jobExecutions", Arrays.asList(new JobExecutionInfo(MetaDataInstanceFactory.createJobExecution(),
				TimeZone.getTimeZone("GMT"))));
		model.put("jobInfo", new JobInfo("foo", 1));
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("Recent and Current Job Executions"));
		assertTrue(content.contains("<a href=\"/jobs/executions/123\">"));
	}

	@Test
	public void testJobExecutionsForLaunchableInstance() throws Exception {
		model.put("jobExecutions", Arrays.asList(new JobExecutionInfo(MetaDataInstanceFactory.createJobExecution(),
				TimeZone.getTimeZone("GMT"))));
		model.put("jobInfo", new JobInfo("foo", 1, true));
		model.put("jobParameters", "foo=bar");
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("<form id=\"launchForm\""));
		assertTrue(content.contains("foo=bar</textarea>"));
		assertTrue(content.contains("Recent and Current Job Executions"));
		assertTrue(content.contains("<a href=\"/jobs/executions/123\">"));
	}

	@Test
	public void testStoppedJobExecutions() throws Exception {
		model.put("stoppedCount", 2);
		model.put("jobExecutions", Arrays.asList(new JobExecutionInfo(MetaDataInstanceFactory.createJobExecution(),
				TimeZone.getTimeZone("GMT"))));
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("You may need to wait"));
	}

}
