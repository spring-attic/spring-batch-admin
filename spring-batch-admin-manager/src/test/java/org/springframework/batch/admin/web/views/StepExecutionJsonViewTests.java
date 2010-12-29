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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.admin.web.JsonWrapper;
import org.springframework.batch.admin.web.StepExecutionInfo;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.WebApplicationContextLoader;
import org.springframework.web.servlet.View;

@ContextConfiguration(loader = WebApplicationContextLoader.class, inheritLocations = false, locations = "AbstractManagerViewTests-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class StepExecutionJsonViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	@Qualifier("jobs/executions/step.json")
	private View view;

	@Test
	public void testLaunchViewWithJobExecutionInfo() throws Exception {
		model.put("stepExecutionInfo", new StepExecutionInfo(MetaDataInstanceFactory.createStepExecution(), TimeZone
				.getTimeZone("GMT")));
		model.put("baseUrl", "http://localhost:8080/springsource");
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("\"duration\" : \""));
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(0, wrapper.get("stepExecution.commitCount"));
		assertEquals("http://localhost:8080/springsource/jobs/executions/123.json", wrapper.get("jobExecution.resource"));
	}

	@Test
	public void testLaunchViewWithStackTrace() throws Exception {
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		StringWriter description = new StringWriter();
		new RuntimeException("planned").printStackTrace(new PrintWriter(description));
		stepExecution.setExitStatus(ExitStatus.FAILED.addExitDescription(description.toString()));
		model.put("stepExecutionInfo", new StepExecutionInfo(stepExecution, TimeZone
				.getTimeZone("GMT")));
		model.put("baseUrl", "http://localhost:8080/springsource");
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("\"duration\" : \""));
		JsonWrapper wrapper = new JsonWrapper(content);
		assertEquals(0, wrapper.get("stepExecution.commitCount"));
		assertEquals("http://localhost:8080/springsource/jobs/executions/123.json", wrapper.get("jobExecution.resource"));
		assertEquals(stepExecution.getId(), wrapper.get("stepExecution.id", Long.class));
		assertEquals(stepExecution.getJobExecution().getStatus().toString(), wrapper.get("jobExecution.status"));
		assertEquals(stepExecution.getJobExecutionId(), wrapper.get("jobExecution.id", Long.class));
	}

}
