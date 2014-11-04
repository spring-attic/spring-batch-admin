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

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={"JobIntegrationTests-context.xml", "StringPayloadJobIntegrationTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class StringPayloadJobIntegrationTests {

	@Autowired
	@Qualifier("job-launches")
	private MessageChannel requests;

	@Autowired
	@Qualifier("job-operator")
	private SubscribableChannel replies;

	@Test
	@DirtiesContext
	public void testLaunchFromSimpleRequestString() throws Exception {

		TestMessagingGateway gateway = new TestMessagingGateway(requests, replies);

		JobExecution result = (JobExecution) gateway.sendAndReceive("staging[input.file=classpath:data/test.txt,foo=bar]");
 		assertEquals(BatchStatus.COMPLETED, result.getStatus());

	}

}
