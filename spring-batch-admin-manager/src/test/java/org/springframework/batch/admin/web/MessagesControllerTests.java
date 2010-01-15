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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.admin.integration.MessagesHolder;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

public class MessagesControllerTests {

	private MessagesController handler;

	protected Collection<Message<?>> messages = new ArrayList<Message<?>>();

	@Before
	public void setUp() {
		MessagesHolder messagesHolder = new MessagesHolder() {
			public Collection<Message<?>> getMessages() {
				return messages;
			}
		};
		handler = new MessagesController(messagesHolder);
	}

	@Test
	public void testHandleRequest() throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		handler.handle(model);
		assertEquals("{messages=[]}", model.toString());

	}

	@Test
	public void testHandleMessageTimestamp() throws Exception {
		messages.add(MessageBuilder.withPayload("foo").setHeader("bar", "spam").build());
		Map<String, Object> model = new HashMap<String, Object>();
		handler.handle(model);
		assertEquals(1, model.size());
		@SuppressWarnings("unchecked")
		Collection<Message<?>> messages = (Collection<Message<?>>) model.get("messages");
		assertEquals(1, messages.size());
		Message<?> message = messages.iterator().next();
		assertEquals(new Date(message.getHeaders().getTimestamp()), message.getHeaders().get("timestamp"));
	}

	@Test
	public void testHandleMessageInternalMessage() throws Exception {
		messages.add(MessageBuilder.withPayload("foo").setHeader("bar", "spam").build());
		Map<String, Object> model = new HashMap<String, Object>();
		handler.handle(model);
		assertEquals(1, model.size());
		@SuppressWarnings("unchecked")
		Collection<Message<?>> messages = (Collection<Message<?>>) model.get("messages");
		assertEquals(1, messages.size());
	}

	@Test
	public void testHandleMessageInternalCollectionMessage() throws Exception {
		messages.add(MessageBuilder.withPayload(Arrays.asList("foo")).setHeader("bar", "spam").build());
		Map<String, Object> model = new HashMap<String, Object>();
		handler.handle(model);
		@SuppressWarnings("unchecked")
		Collection<Message<?>> messages = (Collection<Message<?>>) model.get("messages");
		assertEquals(1, messages.size());
		assertEquals("[foo]", messages.iterator().next().getPayload());
	}

}
