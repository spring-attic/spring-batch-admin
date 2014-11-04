/*
 * Copyright 2006-2010 the original author or authors.
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

package org.springframework.batch.admin.sample;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.integration.MessageRejectedException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;

/**
 * @author Dave Syer
 * 
 */
public class TestMessagingGateway {

	private AtomicReference<Object> reference = new AtomicReference<Object>();

	private MessageChannel requestChannel;

	private SubscribableChannel replyChannel;

	private MessageHandler handler = new MessageHandler() {
		@Override
		public void handleMessage(Message<?> message) throws MessageRejectedException, MessageHandlingException,
				MessageDeliveryException {
			reference.set(message.getPayload());
		}
	};

	/**
	 * @param requestChannel
	 * @param replyChannel
	 */
	public TestMessagingGateway(MessageChannel requestChannel, SubscribableChannel replyChannel) {
		this.requestChannel = requestChannel;
		this.replyChannel = replyChannel;
	}

	/**
	 * @param object a payload to send
	 * @return the returned messages payload
	 */
	public Object sendAndReceive(Object object) {
		replyChannel.subscribe(handler);
		requestChannel.send(new GenericMessage<Object>(object));
		try {
			return reference.get();
		}
		finally {
			replyChannel.unsubscribe(handler);
		}
	}

}
