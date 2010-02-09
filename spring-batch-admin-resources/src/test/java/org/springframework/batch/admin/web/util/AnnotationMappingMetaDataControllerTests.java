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

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.admin.web.util.AnnotationMappingMetaDataController;
import org.springframework.batch.admin.web.util.ResourceInfo;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


public class AnnotationMappingMetaDataControllerTests {

	private AnnotationMappingMetaDataController metaData = new AnnotationMappingMetaDataController();

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
		assertTrue(metaData.getUrlPatterns().contains("/list"));
		assertEquals(3, metaData.getUrlPatterns().size());
	}

	@Test
	public void testTypeMappingWithDefault() throws Exception {
		context.registerSingleton("controller", TypeMappingWithDefaultController.class);
		metaData.afterPropertiesSet();
		assertTrue(metaData.getUrlPatterns().contains("/list/{id}"));
		assertTrue(metaData.getUrlPatterns().contains("/list"));
		assertEquals(2, metaData.getUrlPatterns().size());
	}

	@Test
	public void testAdapter() throws Exception {
		context.registerSingleton("controller", VanillaController.class);
		metaData.afterPropertiesSet();
		MockHttpServletRequest request = new MockHttpServletRequest();
		List<ResourceInfo> resources = metaData.getResources(request);
		assertEquals(1, resources.size());
		assertEquals(RequestMethod.GET, resources.iterator().next().getMethod());
		assertEquals("", request.getAttribute("servletPath"));
	}

	@Test
	public void testAdapterWithMultipleMethods() throws Exception {
		context.registerSingleton("controller", FormController.class);
		metaData.afterPropertiesSet();
		MockHttpServletRequest request = new MockHttpServletRequest();
		List<ResourceInfo> resources = metaData.getResources(request);
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
		List<ResourceInfo> resources = metaData.getResources(request);
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
