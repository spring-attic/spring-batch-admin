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
package org.springframework.batch.admin.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.View;

/**
 * Trivial HTTP request handler that just renders a view. Useful as an entry
 * point to present a form for sending data to an HTTP inbound endpoint.
 * 
 * @author Dave Syer
 * 
 */
public class ViewHandler implements HttpRequestHandler {

	private View view;

	private Map<String, ?> model = new HashMap<String, Object>();

	public void setView(View view) {
		this.view = view;
	}

	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		try {
			view.render(model, request, response);
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
