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
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.SubscribableChannel;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageHandler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={"JobIntegrationTests-context.xml", "WireTapJobIntegrationTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class WireTapJobIntegrationTests {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier("staging")
	private Job job;

	@Autowired
	@Qualifier("job-operator")
	private SubscribableChannel replies;

	private BlockingQueue<JobExecution> receiver = new ArrayBlockingQueue<JobExecution>(2);

	private MessageHandler handler = new MessageHandler() {
		public void handleMessage(Message<?> message) {
			receiver.add((JobExecution) message.getPayload());
		}
	};

	@Before
	public void start() throws Exception {
		JobExecution result = receiver.poll(100L, TimeUnit.MILLISECONDS);
		while (result != null) {
			result = receiver.poll(100L, TimeUnit.MILLISECONDS);
		}
		replies.subscribe(handler);
	}

	@After
	public void cleanup() {
		replies.unsubscribe(handler);
	}

	@Test
	@DirtiesContext
	public void testLaunch() throws Exception {

		assertEquals(0, receiver.size());

		JobParameters jobParameters = new JobParametersBuilder().addString("input.file", "classpath:data/test.txt")
				.addLong("timestamp", System.currentTimeMillis()).addString("run.id", getClass().getSimpleName())
				.toJobParameters();
		jobLauncher.run(job, jobParameters);

		// Expect 1 message: one from JobLauncher and the duplicate from Job is
		// filtered out (if it is quick enough)
		JobExecution result = receiver.poll(10000L, TimeUnit.MILLISECONDS);

		assertNotNull("Timed out waiting for result", result);
		assertEquals(BatchStatus.COMPLETED, result.getStatus());

		// Sometimes two messages get through, in which case it will be a
		// STARTED followed by a COMPLETED (which comes out first, so the
		// assertion above works).
		assertTrue("There were too many messages left in the operator channel", receiver.size() <= 1);

	}

}
