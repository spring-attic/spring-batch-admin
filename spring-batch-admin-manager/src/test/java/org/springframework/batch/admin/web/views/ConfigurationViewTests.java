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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.WebApplicationContextLoader;
import org.springframework.web.servlet.View;

@ContextConfiguration(loader = WebApplicationContextLoader.class, inheritLocations = false, locations = "AbstractIntegrationViewTests-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ConfigurationViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	@Qualifier("configuration")
	private View view;

	@Test
	public void testConfiguration() throws Exception {
		view.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("<div id=\"secondary-navigation\">"));
		assertTrue(content.contains("Register XML File"));
	}

}
