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
package org.springframework.batch.poller.scheduling;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.rules.ExpectedException;
import org.springframework.batch.poller.scheduling.TaskSchedulerPoller;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.TaskUtils;

/**
 * @author Dave Syer
 * 
 */
public class TaskSchedulerPollerTests {

	private Set<String> repository = new HashSet<String>();
	
	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Test
	public void testSunnyDay() throws Exception {

		Callable<String> callback = new Callable<String>() {

			public String call() throws Exception {
				Set<String> executions = new HashSet<String>(repository);
				if (executions.isEmpty()) {
					return null;
				}
				return executions.iterator().next();
			}

		};

		TaskSchedulerPoller<String> poller = new TaskSchedulerPoller<String>();

		sleepAndCreateStringInBackground(500L);

		String value = poller.poll(callback).get(1000L, TimeUnit.MILLISECONDS);
		assertEquals("foo", value);

	}

	@Test
	public void testWithError() throws Exception {

		Callable<String> callback = new Callable<String>() {

			public String call() throws Exception {
				Set<String> executions = new HashSet<String>(repository);
				if (executions.isEmpty()) {
					return null;
				}
				throw new RuntimeException("Expected");
			}

		};

		TaskSchedulerPoller<String> poller = new TaskSchedulerPoller<String>();

		sleepAndCreateStringInBackground(500L);

		expected.expect(ExecutionException.class);
		expected.expect(Cause.expectMessage("Expected"));
		String value = poller.poll(callback).get(1000L, TimeUnit.MILLISECONDS);
		assertEquals(null, value);

	}

	@Test
	public void testWithErrorAndLoggingErrorHandler() throws Exception {

		Callable<String> callback = new Callable<String>() {

			public String call() throws Exception {
				Set<String> executions = new HashSet<String>(repository);
				if (executions.isEmpty()) {
					return null;
				}
				throw new RuntimeException("Expected");
			}

		};

		TaskSchedulerPoller<String> poller = new TaskSchedulerPoller<String>();
		ConcurrentTaskScheduler taskScheduler = new ConcurrentTaskScheduler();
		taskScheduler.setErrorHandler(TaskUtils.LOG_AND_SUPPRESS_ERROR_HANDLER);
		poller.setTaskScheduler(taskScheduler);

		sleepAndCreateStringInBackground(500L);

		expected.expect(ExecutionException.class);
		expected.expect(Cause.expectMessage("Expected"));
		String value = poller.poll(callback).get(1000L, TimeUnit.MILLISECONDS);
		assertEquals(null, value);

	}

	private void sleepAndCreateStringInBackground(final long duration) {
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(duration);
					repository.add("foo");
				}
				catch (Exception e) {
					throw new IllegalStateException("Unexpected");
				}
			}
		}).start();
	}

	private static class Cause extends TypeSafeMatcher<Throwable> {

		private String message;

		private Cause(String message) {
			super();
			this.message = message;
		}

		public void describeTo(Description description) {
			description.appendText("exception with message '" + message + "'");
		}

		@Override
		public boolean matchesSafely(Throwable item) {
			return item.getCause().getMessage().equals(message);
		}

		public static Cause expectMessage(String message) {
			return new Cause(message);
		}
	}

}
