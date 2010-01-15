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
package org.springframework.batch.admin.integration;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Stack;

import org.springframework.integration.core.Message;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.message.MessageBuilder;

public class MessagesHolderHandler extends AbstractMessageHandler implements MessagesHolder {

	private Stack<Message<?>> messages = new Stack<Message<?>>();

	private static int MAX_SIZE = 20;

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		messages.push(getMessage(message));
		if (messages.size() > MAX_SIZE) {
			messages.removeElementAt(MAX_SIZE);
		}
	}
	
	/**
	 * The message stack for external consumption.
	 * @return the messages
	 */
	public Collection<Message<?>> getMessages() {
		return new LinkedHashSet<Message<?>>(messages);
	}

	private Message<?> getMessage(Message<?> message) {
		if (!(message.getPayload() instanceof Collection<?>)) {
			return message;
		}
		return MessageBuilder.withPayload(message.getPayload().toString()).copyHeaders(message.getHeaders()).build();
	}

}
