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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.integration.core.Message;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;

public class MessageStoreHandler extends AbstractMessageHandler implements HttpRequestHandler {

	private Stack<Message<?>> messages = new Stack<Message<?>>();

	private View defaultView;

	private View feedView;

	private static int MAX_SIZE = 20;

	public void setDefaultView(View defaultView) {
		this.defaultView = defaultView;
	}

	public void setFeedView(View feedView) {
		this.feedView = feedView;
	}

	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {

		Map<String, Object> model = new HashMap<String, Object>();
		List<Message<?>> messages = new ArrayList<Message<?>>();
		for (Message<?> message : this.messages) {
			message = MessageBuilder.fromMessage(message).setHeaderIfAbsent("timestamp",
					new Date(message.getHeaders().getTimestamp())).build();
			messages.add(message);
		}
		Collections.sort(messages, new MessageTimestampComparator());
		model.put("messages", messages);

		View view;
		if (request.getRequestURL().toString().endsWith("rss")) {
			String scheme = request.getScheme();
			StringBuffer url = new StringBuffer(scheme + "://");
			url.append(request.getServerName());
			int port = request.getServerPort();
			if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
				url.append(":" + port);
			}
			model.put("baseUrl", url.toString());
			model.put("currentTime", new Date());
			view = feedView;
		}
		else {
			model.put("feedPath", new RequestContext(request, response, null, model).getContextUrl("messages.rss"));
			view = defaultView;
		}

		try {
			view.render(model, request, response);
		}
		catch (Exception e) {
			throw new ServletException("Could not render view", e);
		}

	}

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		messages.push(getMessage(message));
		if (messages.size() > MAX_SIZE) {
			messages.removeElementAt(MAX_SIZE);
		}
	}

	private Message<?> getMessage(Message<?> message) {
		if (!(message.getPayload() instanceof Collection<?>)) {
			return message;
		}
		return MessageBuilder.withPayload(message.getPayload().toString()).copyHeaders(message.getHeaders()).build();
	}

	private static class MessageTimestampComparator implements Comparator<Message<?>> {
		public int compare(Message<?> message1, Message<?> message2) {
			Long s1 = message1.getHeaders().getTimestamp();
			Long s2 = message2.getHeaders().getTimestamp();
			if (s1 == null) {
				s1 = 0L;
			}
			if (s2 == null) {
				s2 = 0L;
			}
			return s2.compareTo(s1);
		}
	}

}
