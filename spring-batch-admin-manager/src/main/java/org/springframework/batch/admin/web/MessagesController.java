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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.batch.admin.integration.MessagesHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MessagesController {

	private final MessagesHolder messagesHolder;

	/**
	 * Create a new instance wit the message sholder provided.
	 * 
	 * @param messagesHolder the {@link MessagesHolder}
	 */
	@Autowired
	public MessagesController(MessagesHolder messagesHolder) {
		super();
		this.messagesHolder = messagesHolder;
	}

	@RequestMapping(value="/messages", method=RequestMethod.GET)
	public String handle(Map<String, Object> model) {

		List<Message<?>> messages = new ArrayList<Message<?>>();
		for (Message<?> message : messagesHolder.getMessages()) {
			message = MessageBuilder.withPayload(message.getPayload().toString()).copyHeaders(message.getHeaders())
					.setHeaderIfAbsent("timestamp", new Date(message.getHeaders().getTimestamp())).build();
			messages.add(message);
		}
		Collections.sort(messages, new MessageTimestampComparator());
		model.put("messages", messages);

		return "messages";

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
