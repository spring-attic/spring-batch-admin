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

import java.util.Arrays;
import java.util.HashMap;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.admin.web.JobExecutionInfo;
import org.springframework.batch.admin.web.StepExecutionInfo;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
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
public class JobExecutionViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	@Qualifier("jobs/execution")
	private View view;

	@Test
	public void testLaunchViewWithJobExecution() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(123L, Arrays.asList(
				"foo", "bar"));
		model.put("jobExecutionInfo", new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT")));
		model.put("stepExecutionInfos", Arrays.asList(new StepExecutionInfo(jobExecution.getStepExecutions().iterator()
				.next(), TimeZone.getTimeZone("GMT")), new StepExecutionInfo("job", 123L, "bar", TimeZone.getTimeZone("GMT"))));
		model.put(BindingResult.MODEL_KEY_PREFIX + "stopRequest", new MapBindingResult(model, "stopRequest"));
		view.render(model, request, response);
		String content = response.getContentAsString();
		System.err.println(content);
		assertTrue(content.contains("Details for Job Execution"));
		assertTrue(content.contains("<input type=\"hidden\" name=\"_method\" value=\"DELETE\"/>"));
		assertTrue(content.contains("<a href=\"/jobs/executions/123/steps\"/>"));
		assertTrue(content.contains("<a href=\"/jobs/executions/123/steps/1234/progress\"/>"));
		assertTrue(content.contains("<td>ID</td>"));
	}

	@Test
	public void testLaunchViewWithNotRestartable() throws Exception {
		JobExecution execution = MetaDataInstanceFactory.createJobExecution("job", 12L, 123L, "foo=bar");
		execution.setStatus(BatchStatus.STARTED);
		model.put("jobExecutionInfo", new JobExecutionInfo(execution, TimeZone.getTimeZone("GMT")));
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertFalse(content.contains("restartForm"));
		assertTrue(content.contains("<input id=\"stop\" type=\"submit\" value=\"Stop\" name=\"stop\" />"));
	}

	@Test
	public void testLaunchViewWithStopped() throws Exception {
		JobExecution execution = MetaDataInstanceFactory.createJobExecution("job", 12345L, 1233456L, "foo=bar");
		execution.setStatus(BatchStatus.STOPPED);
		model.put("jobExecutionInfo", new JobExecutionInfo(execution, TimeZone.getTimeZone("GMT")));
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("restartForm"));
		assertTrue(content.contains("/jobs/job/12345/executions"));
		assertTrue(content.contains("/jobs/executions/1233456/steps"));
		assertTrue(content.contains("<input id=\"stop\" type=\"submit\" value=\"Abandon\" name=\"abandon\" />"));
	}

	@Test
	public void testLaunchViewWithAbandonable() throws Exception {
		JobExecution execution = MetaDataInstanceFactory.createJobExecution("job", 12L, 123L, "foo=bar");
		execution.setStatus(BatchStatus.STOPPING);
		model.put("jobExecutionInfo", new JobExecutionInfo(execution, TimeZone.getTimeZone("GMT")));
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertFalse(content.contains("restartForm"));
		assertTrue(content.contains("<input id=\"stop\" type=\"submit\" value=\"Abandon\" name=\"abandon\" />"));
	}

}
