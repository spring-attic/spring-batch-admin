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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.WebApplicationContextLoader;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.servlet.View;


@ContextConfiguration(loader = WebApplicationContextLoader.class, locations = { "AbstractManagerViewTests-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class RemoteViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	@Qualifier("services/remote")
	private View remote;

	@Test
	public void testRemoteViewWithResponse() throws Exception {
		model.put("response", new StepExecutionResponse(0, false));
		model.put(BindingResult.MODEL_KEY_PREFIX + "response", new MapBindingResult(model, "response"));
		remote.render(model, request, response);
		String content = response.getContentAsString();
		assertTrue(content.contains("capacity=0"));
		assertTrue(content.contains("rejected=false"));
	}

}
