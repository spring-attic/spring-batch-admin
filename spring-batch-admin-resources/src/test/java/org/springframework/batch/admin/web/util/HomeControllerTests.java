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
package org.springframework.batch.admin.web.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


public class HomeControllerTests {

	private HomeController metaData = new HomeController();

	private StaticApplicationContext context = new StaticApplicationContext();

	@Before
	public void init() {
		metaData.setApplicationContext(context);
	}
	
	@Test
	public void testVanillaMapping() throws Exception {
		context.registerSingleton("controller", VanillaController.class);
		metaData.afterPropertiesSet();
		assertTrue(metaData.getUrlPatterns().contains("/list/{id}"));
		assertEquals(1, metaData.getUrlPatterns().size());
	}

	@Test
	public void testInjectedMapping() throws Exception {
		Properties props= new Properties();
		props.setProperty("GET/list/{id}", "");
		metaData.setDefaultResources(props);
		metaData.afterPropertiesSet();
		assertTrue(metaData.getUrlPatterns().contains("/list/{id}"));
		assertEquals(1, metaData.getUrlPatterns().size());
	}

	@Test
	public void testInjectedJsonMapping() throws Exception {
		Properties props = new Properties();
		props.setProperty("GET/list/{id}", "");
		metaData.setDefaultResources(props);
		Properties json = new Properties();
		json.setProperty("GET/list/{id}.json", "");
		metaData.setJsonResources(json);
		metaData.afterPropertiesSet();
		// System.err.println(metaData.getUrlPatterns());
		assertTrue(metaData.getUrlPatterns().contains("/list/{id}"));
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap model = new ModelMap();
		metaData.getResources(request, model);
		@SuppressWarnings("unchecked")
		List<ResourceInfo> resources = (List<ResourceInfo>) model.get("resources");
		assertEquals(2, resources.size());
	}

	@Test
	public void testJsonURI() throws Exception {
		Properties props = new Properties();
		props.setProperty("GET/list/{id}", "foo");
		metaData.setDefaultResources(props);
		Properties json = new Properties();
		json.setProperty("GET/list/{id}.json", "");
		metaData.setJsonResources(json);
		metaData.afterPropertiesSet();
		assertTrue(metaData.getUrlPatterns().contains("/list/{id}"));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "foo.json");
		ModelMap model = new ModelMap();
		metaData.getResources(request, model);
		@SuppressWarnings("unchecked")
		List<ResourceInfo> resources = (List<ResourceInfo>) model.get("resources");
		// System.err.println(resources);
		assertEquals(1, resources.size());
		assertEquals("", resources.get(0).getDescription());
	}

	@Test(expected=IllegalStateException.class)
	public void testDuplicateMapping() throws Exception {
		context.registerSingleton("vanilla", VanillaController.class);
		context.registerSingleton("form", FormController.class);
		metaData.afterPropertiesSet();
	}

	@Test
	public void testSuffixMapping() throws Exception {
		context.registerSingleton("controller", SuffixController.class);
		metaData.afterPropertiesSet();
		assertTrue(metaData.getUrlPatterns().contains("/list.html"));
		assertEquals(1, metaData.getUrlPatterns().size());
	}

	@Test
	public void testNestedResourceMapping() throws Exception {
		context.registerSingleton("controller", NestedResourceController.class);
		metaData.afterPropertiesSet();
		assertTrue(metaData.getUrlPatterns().contains("/list/{id}/foo/{name}"));
		assertEquals(1, metaData.getUrlPatterns().size());
	}

	@Test
	public void testTypeMapping() throws Exception {
		context.registerSingleton("controller", TypeMappingController.class);
		metaData.afterPropertiesSet();
		assertTrue(metaData.getUrlPatterns().contains("/list/{id}/foo/{name}"));
		assertEquals(2, metaData.getUrlPatterns().size());
	}

	@Test
	public void testTypeMappingWithDefault() throws Exception {
		context.registerSingleton("controller", TypeMappingWithDefaultController.class);
		metaData.afterPropertiesSet();
		assertTrue(metaData.getUrlPatterns().contains("/list/{id}"));
		assertEquals(1, metaData.getUrlPatterns().size());
	}

	@Test
	public void testAdapter() throws Exception {
		context.registerSingleton("controller", VanillaController.class);
		metaData.afterPropertiesSet();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap model = new ModelMap();
		metaData.getResources(request, model);
		@SuppressWarnings("unchecked")
		List<ResourceInfo> resources = (List<ResourceInfo>) model.get("resources");
		assertEquals(1, resources.size());
		assertEquals(RequestMethod.GET, resources.iterator().next().getMethod());
		assertEquals("", model.get("servletPath"));
	}

	@Test
	public void testAdapterWithMultipleMethods() throws Exception {
		context.registerSingleton("controller", FormController.class);
		metaData.afterPropertiesSet();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap model = new ModelMap();
		metaData.getResources(request, model);
		@SuppressWarnings("unchecked")
		List<ResourceInfo> resources = (List<ResourceInfo>) model.get("resources");
		Collections.sort(resources);
		assertEquals(2, resources.size());
		Iterator<ResourceInfo> iterator = resources.iterator();
		assertEquals(RequestMethod.GET, iterator.next().getMethod());
		assertEquals(RequestMethod.DELETE, iterator.next().getMethod());
	}

	@Test
	public void testAdapterWithMultipleMethodsAndDifferentMapping() throws Exception {
		context.registerSingleton("controller", MultiMethodController.class);
		metaData.afterPropertiesSet();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap model = new ModelMap();
		metaData.getResources(request, model);
		@SuppressWarnings("unchecked")
		List<ResourceInfo> resources = (List<ResourceInfo>) model.get("resources");
		Collections.sort(resources);
		assertEquals(3, resources.size());
		Iterator<ResourceInfo> iterator = resources.iterator();
		assertEquals(RequestMethod.GET, iterator.next().getMethod());
		assertEquals(RequestMethod.GET, iterator.next().getMethod());
		assertEquals(RequestMethod.PUT, iterator.next().getMethod());
	}

	@Controller
	public static class VanillaController {
		@RequestMapping("/list/{id}")
		public String list(@PathVariable Long id) {
			return "foo";
		}
	}

	@Controller
	public static class MultiMethodController {
		@RequestMapping(value="/list/{id}", method=RequestMethod.GET)
		public String get(@PathVariable Long id) {
			return "foo";
		}
		@RequestMapping(value="/list/{id}", method=RequestMethod.PUT)
		public String update(@PathVariable Long id) {
			return "foo";
		}
		@RequestMapping(value="/list", method=RequestMethod.GET)
		public String list() {
			return "foo";
		}
	}

	@Controller
	public static class SuffixController {
		@RequestMapping("/list.html")
		public String list() {
			return "foo";
		}
	}

	@Controller
	@RequestMapping("/list/{id}")
	public static class FormController {
		@RequestMapping(method=RequestMethod.GET)
		public String list(@PathVariable Long id) {
			return "foo";
		}
		@RequestMapping(method=RequestMethod.DELETE)
		public String remove(@PathVariable Long id) {
			return "foo";
		}
	}

	@Controller
	public static class NestedResourceController {
		@RequestMapping("/list/{id}/foo/{name}")
		public String list(@PathVariable Long id, @PathVariable String name) {
			return "foo";
		}
	}

	@Controller
	@RequestMapping("/list")
	public static class TypeMappingController {
		@RequestMapping("/{id}")
		public String list(@PathVariable Long id) {
			return "foo";
		}

		@RequestMapping("/{id}/foo/{name}")
		public String list(@PathVariable Long id, @PathVariable String name) {
			return "foo";
		}
	}

	@Controller
	@RequestMapping("/list")
	public static class TypeMappingWithDefaultController {
		@RequestMapping("/{id}")
		public String list(@PathVariable Long id) {
			return "foo";
		}

		public String list() {
			return "foo";
		}
	}
}
