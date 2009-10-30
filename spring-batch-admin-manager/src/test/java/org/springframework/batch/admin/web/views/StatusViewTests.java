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

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.admin.partition.remote.StepExecutionResponse;
import org.springframework.batch.admin.partition.remote.StepServiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.WebApplicationContextLoader;
import org.springframework.web.servlet.View;


@ContextConfiguration(loader = WebApplicationContextLoader.class, locations={"AbstractManagerViewTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class StatusViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();
	
	@Autowired
	@Qualifier("services")
	private View home;
	
	@Autowired
	@Qualifier("services/local")
	private View local;
	
	@Autowired
	@Qualifier("services/remote")
	private View remote;
	
	@Test
	public void testServicesView() throws Exception {
		home.render(model, request, response);
		String content = response.getContentAsString();
		assertTrue(content.contains("Local service status"));
		assertTrue(content.contains("Remote service status"));
	}

	@Test
	public void testLocalView() throws Exception {
		model.put("serviceStatus", new StepServiceStatus(10));
		local.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("Press to get current service status."));
		assertTrue(content.contains("Status: available=true, load=10"));
	}

	@Test
	public void testRemoteView() throws Exception {
		model.put("response", new StepExecutionResponse(10, true));
		remote.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("remote service execution"));
		assertTrue(content.contains("Ping test passed OK"));
		assertTrue(content.contains("capacity=10"));
	}

}
