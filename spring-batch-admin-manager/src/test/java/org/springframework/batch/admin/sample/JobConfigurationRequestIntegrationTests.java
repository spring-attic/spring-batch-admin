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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.admin.integration.JobConfigurationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.channel.SubscribableChannel;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.gateway.SimpleMessagingGateway;
import org.springframework.integration.message.MessageHandler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={"JobIntegrationTests-context.xml", "JobConfigurationRequestIntegrationTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class JobConfigurationRequestIntegrationTests {

	@Autowired
	@Qualifier("job-configuration-requests")
	private MessageChannel requests;

	@Autowired
	@Qualifier("job-registrations")
	private SubscribableChannel replies;

	private BlockingQueue<String> receiver = new ArrayBlockingQueue<String>(1);

	private MessageHandler handler = new MessageHandler() {
		public void handleMessage(Message<?> message) {
			receiver.add((String) message.getPayload());
		}
	};

	@Before
	public void start() {
		replies.subscribe(handler);
	}
	
	@Test
	@DirtiesContext
	public void testRegisterFromSimpleRequestString() throws Exception {

		SimpleMessagingGateway gateway = new SimpleMessagingGateway();
		gateway.setRequestChannel(requests);
		gateway.setReplyTimeout(500L);
		gateway.afterPropertiesSet();

		JobConfigurationRequest request = new JobConfigurationRequest();
		request.setXml(IOUtils.toString(new ClassPathResource("/META-INF/batch/staging-context.xml").getInputStream()));
		gateway.send(request);
		String result = receiver.poll(500L, TimeUnit.MILLISECONDS);
		assertNotNull("Time out waiting for reply", result);
 		assertEquals("Registered jobs: [staging]", result.toString());
 		
 		gateway.stop();

	}
}
