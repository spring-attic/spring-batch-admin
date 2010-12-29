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
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.admin.web.JobExecutionInfo;
import org.springframework.batch.admin.web.JsonWrapper;
import org.springframework.batch.admin.web.StepExecutionInfo;
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
public class JobExecutionJsonViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	@Qualifier("jobs/execution.json")
	private View view;

	@Test
	public void testLaunchViewWithJobExecutionInfo() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(123L, Arrays.asList(
				"foo", "bar"));
		model.put("jobExecutionInfo", new JobExecutionInfo(jobExecution, TimeZone.getDefault()));
		model.put("baseUrl", "http://localhost:8080/springsource");
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("\"duration\" : \"\""));
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals("STARTING", wrapper.get("jobExecution.status"));
		assertEquals(2, wrapper.get("jobExecution.stepExecutions", Map.class).size());
		assertEquals("http://localhost:8080/springsource/jobs/executions/123/steps/1235.json", wrapper
				.get("jobExecution.stepExecutions.bar.resource"));
	}

	@Test
	public void testLaunchViewWithJobAndStepExecutionInfo() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(123L, Arrays.asList(
				"foo", "bar"));
		model.put("jobExecutionInfo", new JobExecutionInfo(jobExecution, TimeZone.getDefault()));
		model.put("stepExecutionInfos", Arrays.asList(new StepExecutionInfo(jobExecution.getStepExecutions().iterator()
				.next(), TimeZone.getTimeZone("GMT")), new StepExecutionInfo("job", 123L, "bar", TimeZone.getTimeZone("GMT"))));
		model.put("baseUrl", "http://localhost:8080/springsource");
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("\"duration\" : \"\""));
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals("STARTING", wrapper.get("jobExecution.status"));
		assertEquals(2, wrapper.get("jobExecution.stepExecutions", Map.class).size());
		assertEquals("http://localhost:8080/springsource/jobs/executions/123/steps/1234.json", wrapper
				.get("jobExecution.stepExecutions.foo.resource"));
	}

}
