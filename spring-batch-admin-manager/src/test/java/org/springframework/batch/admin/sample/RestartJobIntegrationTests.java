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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.sql.DataSource;

import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "JobIntegrationTests-context.xml", "RestartJobIntegrationTests-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class RestartJobIntegrationTests {

	@Autowired
	@Qualifier("job-launches")
	private MessageChannel requests;

	@Autowired
	@Qualifier("job-restarts")
	private MessageChannel restarts;

	@Autowired
	@Qualifier("job-operator")
	private SubscribableChannel replies;

	private JobRepositoryTestUtils jobRepositoryTestUtils;

	@Autowired
	public void initializeTestUtils(JobRepository jobRepository, DataSource dataSource) {
		jobRepositoryTestUtils = new JobRepositoryTestUtils(jobRepository, dataSource);
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	@DirtiesContext
	public void testLaunchAndRestart() throws Exception {

		TestMessagingGateway launch = new TestMessagingGateway(requests, replies);

		TestMessagingGateway restart = new TestMessagingGateway(restarts, replies);

		JobExecution result = (JobExecution) launch
				.sendAndReceive("staging[input.file=classpath:data/bad.txt,foo=bar]");
		assertEquals(BatchStatus.FAILED, result.getStatus());
		result = (JobExecution) restart.sendAndReceive("staging");
		assertEquals(BatchStatus.FAILED, result.getStatus());

	}

	@Test
	@DirtiesContext
	public void testFailedRestart() throws Exception {

		thrown.expect(MessageHandlingException.class);

		thrown.expect(new TypeSafeMatcher<Exception>() {
			@Override
			public boolean matchesSafely(Exception item) {
				StringWriter writer = new StringWriter();
				item.printStackTrace(new PrintWriter(writer));
				return writer.toString().matches("(?s).*JobParametersNotFoundException.*");
				// item.printStackTrace();
				// return item.getCause() instanceof
				// JobParametersNotFoundException;
			}

			public void describeTo(Description description) {
				description.appendText("exception has cause of JobParametersNotFoundException");
			}
		});

		jobRepositoryTestUtils.removeJobExecutions();

		TestMessagingGateway restart = new TestMessagingGateway(restarts, replies);

		JobExecution result = (JobExecution) restart.sendAndReceive("staging");
		assertEquals(BatchStatus.FAILED, result.getStatus());

	}

}
