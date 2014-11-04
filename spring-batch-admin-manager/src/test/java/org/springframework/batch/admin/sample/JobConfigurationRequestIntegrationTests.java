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
package org.springframework.batch.admin.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.admin.integration.JobConfigurationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={"JobIntegrationTests-context.xml", "JobConfigurationRequestIntegrationTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class JobConfigurationRequestIntegrationTests {

	@Autowired
	@Qualifier("job-configuration-requests")
	private MessageChannel requests;

	@Autowired
	@Qualifier("job-registrations")
	private SubscribableChannel replies;

	private List<String> result;

	private MessageHandler handler = new MessageHandler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message<?> message) throws MessagingException {
			result = (List<String>) message.getPayload();
		}
	};
	
	@Before
	public void init() {
		replies.subscribe(handler);
	}

	@After
	public void close() {
		replies.unsubscribe(handler);
	}

	@Test
	@DirtiesContext
	public void testRegisterFromSimpleRequestString() throws Exception {

		MessagingTemplate gateway = new MessagingTemplate();
		gateway.setReceiveTimeout(500L);

		JobConfigurationRequest request = new JobConfigurationRequest();
		request.setXml(IOUtils.toString(new ClassPathResource("/staging-context.xml").getInputStream()));
		gateway.convertAndSend(requests, request);
		assertNotNull("Time out waiting for reply", result);
 		assertEquals("[staging]", result.toString());
 		
	}

}
