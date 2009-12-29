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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.admin.web.util.ResourceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.WebApplicationContextLoader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.View;


@ContextConfiguration(loader = WebApplicationContextLoader.class, inheritLocations=false, locations={"AbstractResourceViewTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class HomeViewTests extends AbstractResourceViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();
	
	@Autowired
	@Qualifier("home")
	private View home;
	
	@Autowired
	@Qualifier("secondary")
	private View secondary;
	
	@Test
	public void testLocalViewWithBody() throws Exception {
		List<ResourceInfo> resources = new ArrayList<ResourceInfo>();
		resources.add(new ResourceInfo("/local", RequestMethod.GET));
		resources.add(new ResourceInfo("/jobs/{jobName}", RequestMethod.GET));
		model.put("resources", resources);
		model.put("servletPath", "/batch");
		home.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("<td><a href=\"/batch/local\">/local</a></td>"));
		assertTrue(content.contains("<td>/jobs/{jobName}</td>"));
		assertTrue(content.contains("<title>Test Title</title>"));
	}

	@Test
	public void testLocalViewWithSideNav() throws Exception {
		List<ResourceInfo> resources = new ArrayList<ResourceInfo>();
		resources.add(new ResourceInfo("/local", RequestMethod.GET));
		resources.add(new ResourceInfo("/jobs/{jobName}", RequestMethod.GET));
		model.put("resources", resources);
		model.put("servletPath", "/batch");
		secondary.render(model, request, response);
		String content = response.getContentAsString();
		// System.err.println(content);
		assertTrue(content.contains("<div id=\"content\" >"));
		assertTrue(content.contains("<div id=\"secondary-navigation\">"));
	}

}
