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

import org.springframework.batch.core.JobExecution;
import org.springframework.integration.channel.SubscribableChannel;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.gateway.SimpleMessagingGateway;
import org.springframework.integration.message.MessageDeliveryException;
import org.springframework.integration.message.MessageHandler;
import org.springframework.integration.message.MessageHandlingException;
import org.springframework.integration.message.MessageRejectedException;

/**
 * @author Dave Syer
 *
 */
public class TestMessagingGateway {
	
	private SimpleMessagingGateway gateway = new SimpleMessagingGateway();
	private AtomicReference<Object> reference = new AtomicReference<Object>();

	/**
	 * 
	 * @see org.springframework.integration.endpoint.AbstractEndpoint#afterPropertiesSet()
	 */
	public final void afterPropertiesSet() {
		gateway.afterPropertiesSet();
	}

	/**
	 * @param object
	 * @return
	 * @see org.springframework.integration.gateway.AbstractMessagingGateway#sendAndReceive(java.lang.Object)
	 */
	public Object sendAndReceive(Object object) {
		gateway.send(object);
		return reference.get();
	}

	/**
	 * @param replyChannel
	 * @see org.springframework.integration.gateway.AbstractMessagingGateway#setReplyChannel(org.springframework.integration.core.MessageChannel)
	 */
	public void setReplyChannel(SubscribableChannel replyChannel) {
		replyChannel.subscribe(new MessageHandler() {
			public void handleMessage(Message<?> message) throws MessageRejectedException, MessageHandlingException,
					MessageDeliveryException {
				reference.set((JobExecution) message.getPayload());
			}
		});	
	}

	/**
	 * @param requestChannel
	 * @see org.springframework.integration.gateway.AbstractMessagingGateway#setRequestChannel(org.springframework.integration.core.MessageChannel)
	 */
	public void setRequestChannel(MessageChannel requestChannel) {
		gateway.setRequestChannel(requestChannel);
	}

	/**
	 * 
	 * @see org.springframework.integration.endpoint.AbstractEndpoint#stop()
	 */
	public final void stop() {
		gateway.stop();
	}

}
