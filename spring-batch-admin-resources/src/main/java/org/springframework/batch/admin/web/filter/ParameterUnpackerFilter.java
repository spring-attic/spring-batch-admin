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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class ParameterUnpackerFilter extends OncePerRequestFilter {

	private String prefix = "unpack_";

	private String delimiter = "|";

	private boolean putEmptyParamsInPath = false;

	/**
	 * Flag to say that empty parameter value signal their name should be used
	 * as a path parameter. E.g. <code>/context/path?unpack_foo</code> goes to
	 * <code>context/path/foo</code>.
	 * 
	 * @param putEmptyParamsInPath the flag value to set (defaults to false)
	 */
	public void setPutEmptyParamsInPath(boolean putEmptyParamsInPath) {
		this.putEmptyParamsInPath = putEmptyParamsInPath;
	}

	/**
	 * @param delimiter the delimiter used to separate parameter names from
	 * values
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * @param prefix the prefix for parameter names that need to be unpacked
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		Map<String, String[]> unpacked = new HashMap<String, String[]>();
		List<String> pathKeys = new ArrayList<String>();

		for (Object key : request.getParameterMap().keySet()) {

			String paramName = (String) key;

			if (paramName.startsWith(prefix)) {

				String embedded = paramName.substring(prefix.length());
				unpacked.putAll(unpack(embedded, delimiter));
				pathKeys.addAll(extractPathKeys(embedded, delimiter));

			}

		}

		if (unpacked.size() > 0) {
			filterChain.doFilter(new EnhancedRequestWrapper(unpacked, request, putEmptyParamsInPath ? pathKeys
					: Collections.<String> emptyList()), response);
		}
		else {
			filterChain.doFilter(request, response);
		}

	}

	protected Collection<? extends String> extractPathKeys(String paramName, String delimiter) {
		List<String> result = new ArrayList<String>();
		String[] values = StringUtils.delimitedListToStringArray(paramName, delimiter);
		for (int i = 0; i < (values.length + 1) / 2; i++) {
			int j = 2 * i + 1;
			String key = values[j - 1];
			String next = j < values.length ? values[j].trim() : "";
			if (next!=null && next.length()==0) {
				result.add(key);
			}
		}
		return result;
	}

	protected Map<String, String[]> unpack(String paramName, String delimiter) {
		Map<String, String[]> unpacked = new HashMap<String, String[]>();
		String[] values = StringUtils.delimitedListToStringArray(paramName, delimiter);
		for (int i = 0; i < (values.length + 1) / 2; i++) {
			int j = 2 * i + 1;
			String key = values[j - 1];
			String[] saved = unpacked.get(key);
			String next = j < values.length ? values[j] : "";
			if (saved == null) {
				saved = new String[] { next };
			}
			else {
				List<String> list = new ArrayList<String>(Arrays.asList(saved));
				list.add(next);
				saved = list.toArray(new String[list.size()]);
			}
			unpacked.put(key, saved);
		}
		return unpacked;
	}

	private static class EnhancedRequestWrapper extends HttpServletRequestWrapper {

		private final Map<String, String[]> params = new HashMap<String, String[]>();

		private final List<String> pathParams;

		public EnhancedRequestWrapper(Map<String, String[]> params, HttpServletRequest request, List<String> pathParams) {
			super(request);
			this.pathParams = pathParams;
			this.params.putAll(params);
		}

		@Override
		public String getContextPath() {
			String base = super.getContextPath();
			if (pathParams!=null && pathParams.isEmpty()) {
				return base;
			}
			StringBuffer buffer = new StringBuffer(base);
			for (String key : pathParams) {
				buffer.append("/" + key);
			}
			return buffer.toString();
		}

		@Override
		public Enumeration<String> getParameterNames() {
			Set<String> paramNames = new HashSet<String>();
			@SuppressWarnings("unchecked")
			Enumeration paramEnum = super.getParameterNames();
			while (paramEnum.hasMoreElements()) {
				paramNames.add((String) paramEnum.nextElement());
			}
			paramNames.addAll(params.keySet());
			return Collections.enumeration(paramNames);
		}

		@Override
		public String getParameter(String name) {
			String[] value = params.get(name);
			if (value != null) {
				return (value.length > 0 ? value[0] : null);
			}
			return super.getParameter(name);
		}

		@Override
		public String[] getParameterValues(String name) {
			String[] value = params.get(name);
			if (value != null) {
				return value;
			}
			return super.getParameterValues(name);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Map<String, String[]> getParameterMap() {
			Map<String, String[]> paramMap = new HashMap<String, String[]>();
			paramMap.putAll(super.getParameterMap());
			paramMap.putAll(params);
			return paramMap;
		}

	}

}
