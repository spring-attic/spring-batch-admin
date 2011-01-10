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
package org.springframework.batch.admin.web.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;


public class ContentTypeInterceptorTests {

	private ContentTypeInterceptor interceptor = new ContentTypeInterceptor();

	protected MockHttpServletRequest request;

	protected MockHttpServletResponse response;

	@Before
	public void setUp() {
		request = new MockHttpServletRequest("GET", "/foo.rss");
		request.setPathInfo("/foo.rss");
		response = new MockHttpServletResponse();
	}

	@Test
	public void testPreHandleVanilla() throws Exception {
		interceptor.setExtensions(Collections.singleton("rss"));
		interceptor.preHandle(request, response, null);
		assertNotNull(request.getAttribute("currentTime"));
		assertNotNull(request.getAttribute("baseUrl"));
	}

	@Test
	public void testPostHandleVanilla() throws Exception {
		interceptor.setExtensions(Collections.singleton("rss"));
		ModelAndView modelAndView = new ModelAndView("foo");
		interceptor.postHandle(request, response, null, modelAndView);
		assertEquals("foo.rss", modelAndView.getViewName());
	}

	@Test
	public void testPostHandleNullPath() throws Exception {
		interceptor.setExtensions(Collections.singleton("rss"));
		ModelAndView modelAndView = new ModelAndView("foo");
		request.setPathInfo(null);
		interceptor.postHandle(request, response, null, modelAndView);
		assertEquals("foo", modelAndView.getViewName());
	}

	@Test
	public void testPostHandleWithExtension() throws Exception {
		interceptor.setExtensions(Collections.singleton("rss"));
		ModelAndView modelAndView = new ModelAndView("foo.rss");
		interceptor.postHandle(request, response, null, modelAndView);
		assertEquals("foo.rss", modelAndView.getViewName());
	}

	@Test
	public void testPostHandleWithDoubleExtension() throws Exception {
		interceptor.setExtensions(Collections.singleton("rss"));
		request.setPathInfo("/bar/foo.1.rss");
		ModelAndView modelAndView = new ModelAndView("foo.1");
		interceptor.postHandle(request, response, null, modelAndView);
		assertEquals("foo.1.rss", modelAndView.getViewName());
	}

}
