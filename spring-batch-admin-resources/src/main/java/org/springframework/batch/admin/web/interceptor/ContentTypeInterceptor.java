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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

/**
 * Interceptor that looks for an extension on the request path and adds it to
 * the view name if it matches a list provided. This can be used to do simple
 * content negotiation based on request path extensions, as is usual with
 * browsers (the view that is finally resolved could have a different content
 * type than the original request).
 * 
 * @author Dave Syer
 * 
 */
public class ContentTypeInterceptor extends HandlerInterceptorAdapter {

	private Collection<String> extensions = new HashSet<String>();

	/**
	 * A collection of extensions to append to view names.
	 * 
	 * @param extensions
	 *            the extensions (e.g. [rss, xml, atom])
	 */
	public void setExtensions(Collection<String> extensions) {
		this.extensions = new LinkedHashSet<String>(extensions);
	}

	/**
	 * Compare the extension of the request path (if there is one) with the set
	 * provided, and if it matches then add the same extension to the view name,
	 * if it is not already present.
	 * 
	 * @see HandlerInterceptorAdapter#postHandle(HttpServletRequest,
	 *      HttpServletResponse, Object, ModelAndView)
	 */
	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		if (modelAndView == null) {
			return;
		}

		String path = WebUtils.extractFullFilenameFromUrlPath(request
				.getPathInfo());
		if (!path.contains(".")) {
			return;
		}
		String extension = path.substring(path.indexOf(".") + 1);

		if (extensions.contains(extension)) {

			if (modelAndView.isReference()) {

				String viewName = modelAndView.getViewName();
				if (viewName.contains(".")) {
					viewName = viewName.substring(0, path.indexOf("."));
				}

				modelAndView.setViewName(viewName + "." + extension);

			}

		}

		String scheme = request.getScheme();
		StringBuffer url = new StringBuffer(scheme + "://");
		url.append(request.getServerName());
		int port = request.getServerPort();
		if ((scheme.equals("http") && port != 80)
				|| (scheme.equals("https") && port != 443)) {
			url.append(":" + port);
		}

		modelAndView.addObject("baseUrl", url.toString());
		modelAndView.addObject("currentTime", new Date());

	}
}
