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
package org.springframework.batch.admin.web.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.batch.admin.web.filter.ParameterUnpackerFilter;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


public class ParameterUnpackerFilterTests {
	
	private ParameterUnpackerFilter filter = new ParameterUnpackerFilter();

	@Test
	public void testDoFilterWithNoChanges() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("foo_bar", "");
		MockFilterChain filterChain = new MockFilterChain();
		filter.doFilter(request, new MockHttpServletResponse(), filterChain);
		assertEquals(request, filterChain.getRequest());
	}

	@Test
	public void testDoFilterWithParameterChanges() throws Exception {
		filter.setPrefix("foo_");
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("foo_bar|spam", "");
		MockFilterChain filterChain = new MockFilterChain();
		filter.doFilter(request, new MockHttpServletResponse(), filterChain);
		assertNotSame(request, filterChain.getRequest());
		assertEquals("spam", filterChain.getRequest().getParameter("bar"));
	}

	@Test
	public void testDoFilterWithPotentialPathChanges() throws Exception {
		filter.setPrefix("foo_");
		filter.setPutEmptyParamsInPath(true);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("foo_bar|spam", "");
		MockFilterChain filterChain = new MockFilterChain();
		filter.doFilter(request, new MockHttpServletResponse(), filterChain);
		HttpServletRequest result = (HttpServletRequest) filterChain.getRequest();
		assertNotSame(request, result);
		assertEquals("spam", result.getParameter("bar"));
		assertEquals("", result.getContextPath());
	}

	@Test
	public void testDoFilterWithActualPathChanges() throws Exception {
		filter.setPrefix("foo_");
		filter.setPutEmptyParamsInPath(true);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("foo_bar", "");
		MockFilterChain filterChain = new MockFilterChain();
		filter.doFilter(request, new MockHttpServletResponse(), filterChain);
		HttpServletRequest result = (HttpServletRequest) filterChain.getRequest();
		assertNotSame(request, result);
		assertEquals("", result.getParameter("bar"));
		assertEquals("/bar", result.getContextPath());
	}

	@Test
	public void testDoFilterWithMultiplePathChanges() throws Exception {
		filter.setPrefix("foo_");
		filter.setPutEmptyParamsInPath(true);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("foo_bar||123||spam", "");
		MockFilterChain filterChain = new MockFilterChain();
		filter.doFilter(request, new MockHttpServletResponse(), filterChain);
		HttpServletRequest result = (HttpServletRequest) filterChain.getRequest();
		assertNotSame(request, result);
		assertEquals("/bar/123/spam", result.getContextPath());
	}

	@Test
	public void testDoFilterWithDuplicatePathChanges() throws Exception {
		filter.setPrefix("foo_");
		filter.setPutEmptyParamsInPath(true);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("foo_bar||123||spam||123", "");
		MockFilterChain filterChain = new MockFilterChain();
		filter.doFilter(request, new MockHttpServletResponse(), filterChain);
		HttpServletRequest result = (HttpServletRequest) filterChain.getRequest();
		assertNotSame(request, result);
		assertEquals("/bar/123/spam/123", result.getContextPath());
	}

	@Test
	public void testUnpackEvenUnique() {
		Map<String, String[]> unpack = filter.unpack("a|b|c|d", "|");
		assertEquals("b", unpack.get("a")[0]);
		assertEquals("d", unpack.get("c")[0]);
	}

	@Test
	public void testUnpackOddUnique() {
		Map<String, String[]> unpack = filter.unpack("a|b|c", "|");
		assertEquals("b", unpack.get("a")[0]);
		assertEquals("", unpack.get("c")[0]);
	}

	@Test
	public void testUnpackEvenDups() {
		Map<String, String[]> unpack = filter.unpack("a|b|c|d|a|e", "|");
		assertEquals("e", unpack.get("a")[1]);
		assertEquals("d", unpack.get("c")[0]);
	}

}
