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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.batch.admin.web.filter.SessionIdFilter;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


public class SessionIdFilterTests {

	private SessionIdFilter filter = new SessionIdFilter();

	@Test
	public void testDoFilterWithNoChanges() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("foo_bar", "");
		MockFilterChain filterChain = new MockFilterChain();
		filter.doFilter(request, new MockHttpServletResponse(), filterChain);
		assertEquals(request, filterChain.getRequest());
	}

	@Test
	public void testDoFilterWithRedirect() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/foo;jsessionid=1234");
		request.addParameter("foo", "bar");
		MockFilterChain filterChain = new MockFilterChain();
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, filterChain);
		assertNull(filterChain.getResponse());
		String url = response.getRedirectedUrl();
		assertFalse("Wrong: "+url, url.contains(";"));
		assertFalse("Wrong: "+url, url.contains("jsessionid"));
	}

	@Test
	public void testDoFilterWithWrappedResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("foo", "bar");
		MockFilterChain filterChain = new MockFilterChain();
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, filterChain);
		HttpServletResponse result = (HttpServletResponse) filterChain.getResponse();
		assertNotSame(response, result);
		assertEquals("spam", result.encodeRedirectURL("spam"));
		assertEquals("spam", result.encodeURL("spam"));
	}

}
