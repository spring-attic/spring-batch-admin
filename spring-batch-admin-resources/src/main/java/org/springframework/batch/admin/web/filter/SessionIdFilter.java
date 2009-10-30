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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionIdFilter extends OncePerRequestFilter {

	private static final String JSESSIONID_PATTERN = ";[jJ][sS][eE][sS][sS][iI][oO][nN][iI][dD]=[A-Za-z0-9]+";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (!bounceRequestWithSessionIdInUrl(request, response)) {
			filterChain.doFilter(request, new EnhancedResponseWrapper(response));
		}

	}

	private boolean bounceRequestWithSessionIdInUrl(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		String url = request.getRequestURL().toString();

		if (url.matches("(.*)" + JSESSIONID_PATTERN + "(.*)")) {

			String queryString = request.getQueryString();

			url = url.replaceAll(JSESSIONID_PATTERN, "") + (StringUtils.hasText(queryString) ? "?" + queryString : "");

			response.sendRedirect(url);

			return true;

		}

		return false;

	}

	private static class EnhancedResponseWrapper extends HttpServletResponseWrapper {

		public EnhancedResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		@Override
		public String encodeRedirectUrl(String url) {
			return encodeRedirectURL(url);
		}

		@Override
		public String encodeRedirectURL(String url) {
			String encoded = super.encodeRedirectURL(url);
			return stripSessionId(encoded);
		}

		@Override
		public String encodeUrl(String url) {
			return encodeURL(url);
		}

		@Override
		public String encodeURL(String url) {
			String encoded = super.encodeURL(url);
			return stripSessionId(encoded);
		}

		private String stripSessionId(String encoded) {
			return encoded.replaceAll(JSESSIONID_PATTERN, "");
		}

	}

}
