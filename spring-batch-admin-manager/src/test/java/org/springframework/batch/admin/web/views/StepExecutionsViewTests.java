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
import org.springframework.batch.admin.web.StepExecutionInfo;
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
public class StepExecutionsViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	@Qualifier("jobs/executions/steps")
	private View view;

	@Test
	public void testLaunchViewWithStepExecutions() throws Exception {
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		model.put("stepExecutions", Arrays.asList(new StepExecutionInfo(stepExecution, TimeZone.getTimeZone("GMT"))));
		model.put("jobExecutionInfo",
				new JobExecutionInfo(stepExecution.getJobExecution(), TimeZone.getTimeZone("GMT")));
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("Step Executions"));
		assertTrue(content.contains("for Job = job"));
		assertTrue(content.contains("<a href=\"/jobs/executions/123/steps/1234/progress\">detail</a>"));
		assertTrue(content.contains("<th>ID</th>"));
	}

}
