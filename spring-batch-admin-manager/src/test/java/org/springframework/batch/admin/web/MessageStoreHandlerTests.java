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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;

public class MessageStoreHandlerTests {

	private MessageStoreHandler handler = new MessageStoreHandler();

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	private TextView defaultView = new TextView();

	@Before
	public void setUp() {
		request = new MockHttpServletRequest();
		request.setRequestURI("/springsource-batch");
		response = new MockHttpServletResponse();
		GenericWebApplicationContext context = new GenericWebApplicationContext();
		request.getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
				context);
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
		handler.setDefaultView(defaultView);
		handler.setFeedView(defaultView);
	}

	@Test
	public void testHandleRequest() throws Exception {
		handler.handleRequest(request, response);
		assertEquals("{feedPath=messages.rss, messages=[]}", response.getContentAsString());

	}

	@Test
	public void testHandleMessageTimestamp() throws Exception {
		handler.handleMessageInternal(MessageBuilder.withPayload("foo").setHeader("bar", "spam").build());
		handler.handleRequest(request, response);
		assertEquals(2, defaultView.getModel().size());
		@SuppressWarnings("unchecked")
		Collection<Message<?>> messages = (Collection<Message<?>>) defaultView.getModel().get("messages");
		assertEquals(1, messages.size());
		Message<?> message = messages.iterator().next();
		assertEquals(new Date(message.getHeaders().getTimestamp()), message.getHeaders().get("timestamp"));
	}

	@Test
	public void testHandleMessageInternalMessage() throws Exception {
		handler.handleMessageInternal(MessageBuilder.withPayload("foo").setHeader("bar", "spam").build());
		handler.handleRequest(request, response);
		assertEquals(2, defaultView.getModel().size());
		@SuppressWarnings("unchecked")
		Collection<Message<?>> messages = (Collection<Message<?>>) defaultView.getModel().get("messages");
		assertEquals(1, messages.size());
	}

	@Test
	public void testHandleMessageInternalCollectionMessage() throws Exception {
		handler.handleMessageInternal(MessageBuilder.withPayload(Arrays.asList("foo")).setHeader("bar", "spam").build());
		handler.handleRequest(request, response);
		@SuppressWarnings("unchecked")
		Collection<Message<?>> messages = (Collection<Message<?>>) defaultView.getModel().get("messages");
		assertEquals(1, messages.size());
		assertEquals("[foo]", messages.iterator().next().getPayload());
	}

	private class TextView implements View {

		private Map<String, ?> model;

		public String getContentType() {
			return "text/plain";
		}

		public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
				throws Exception {
			this.model = model;
			response.getWriter().write(model.toString());
		}

		public Map<String, ?> getModel() {
			return model;
		}

	}

}
