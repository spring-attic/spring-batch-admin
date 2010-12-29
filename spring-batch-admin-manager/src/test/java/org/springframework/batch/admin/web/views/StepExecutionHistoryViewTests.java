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

import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.admin.history.StepExecutionHistory;
import org.springframework.batch.admin.web.StepExecutionInfo;
import org.springframework.batch.admin.web.StepExecutionProgress;
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
public class StepExecutionHistoryViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	@Qualifier("jobs/executions/step/history")
	private View history;

	@Autowired
	@Qualifier("jobs/executions/step/progress")
	private View progress;

	@Test
	public void testHistoryView() throws Exception {
		StepExecution oldStepExecution = MetaDataInstanceFactory
				.createStepExecution();
		oldStepExecution.setEndTime(new Date());
		StepExecutionHistory stepExecutionHistory = new StepExecutionHistory(
				"step");
		stepExecutionHistory.append(oldStepExecution);
		model.put("stepExecutionHistory", stepExecutionHistory);
		history.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("History of Step Execution for Step=step"));
		assertTrue(content.contains("<th>Mean</th>"));
		assertTrue(content.contains("<td>Duration</td>"));
	}

	@Test
	public void testProgressView() throws Exception {
		StepExecution oldStepExecution = MetaDataInstanceFactory
				.createStepExecution();
		oldStepExecution.setEndTime(new Date());
		StepExecution stepExecution = MetaDataInstanceFactory
				.createStepExecution();
		model.put("stepExecutionInfo", new StepExecutionInfo(stepExecution,
				TimeZone.getTimeZone("GMT")));
		StepExecutionHistory stepExecutionHistory = new StepExecutionHistory(
				"step");
		stepExecutionHistory.append(oldStepExecution);
		model.put("stepExecutionHistory", stepExecutionHistory);
		model.put("stepExecutionProgress", new StepExecutionProgress(
				stepExecution, stepExecutionHistory));
		progress.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue("Wrong content: "+content, content.contains("This execution is estimated to be 50% complete"));
		assertTrue("Wrong content: "+content, content.contains("Details for Step Execution"));
		assertTrue("Wrong content: "+content, content
				.contains("<a href=\"/jobs/executions/123\">123</a>"));
	}

}
