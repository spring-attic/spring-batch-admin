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

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "JobIntegrationTests-context.xml", "FileDropJobIntegrationTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class FileDropJobIntegrationTests {

	@Autowired
	@Qualifier("job-operator")
	private SubscribableChannel replies;

	private BlockingQueue<JobExecution> receiver = new ArrayBlockingQueue<JobExecution>(1);

	private MessageHandler handler = new MessageHandler() {
		public void handleMessage(Message<?> message) {
			receiver.add((JobExecution) message.getPayload());
		}
	};

	@Before
	public void start() {
		replies.subscribe(handler);
	}
	
	@BeforeClass
	@AfterClass
	public static void deleteOldFiles() throws Exception {
		FileUtils.deleteDirectory(new File("target/data"));
		new File("target/data").mkdirs();	
	}
	
	@After
	public void cleanup() {
		replies.unsubscribe(handler);
	}

	@Test
	@DirtiesContext
	public void testLaunchFromFileDrop() throws Exception {

		assertEquals(0, receiver.size());

		FileUtils.copyFile(new File(this.getClass().getResource("/data/test.txt").toURI()), new File("target/data/drop.txt"));

		JobExecution result = receiver.poll(2000L, TimeUnit.MILLISECONDS);

		assertNotNull("Timed out waiting for result", result);
		assertEquals(BatchStatus.COMPLETED, result.getStatus());
	}

}
