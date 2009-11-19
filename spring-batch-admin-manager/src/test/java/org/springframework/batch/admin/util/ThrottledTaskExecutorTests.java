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
package org.springframework.batch.admin.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ThrottledTaskExecutorTests {

	private CompletionService<String> service = new ExecutorCompletionService<String>(new ThrottledTaskExecutor());

	private volatile boolean ready = false;

	@Test
	public void testPoll() throws Exception {
		service.submit(new Callable<String>() {
			public String call() throws Exception {
				return "foo";
			}
		});
		assertEquals("foo", service.poll().get());
	}

	@Test
	public void testPollLongTimeUnit() throws Exception {
		service.submit(new Callable<String>() {
			public String call() throws Exception {
				return "foo";
			}
		});
		assertEquals("foo", service.poll(1, TimeUnit.SECONDS).get());
	}

	@Test
	public void testSubmitRunnable() throws Exception {
		Future<String> future = service.submit(new Runnable() {
			public void run() {
			}
		}, "foo");
		assertNotNull(future);
	}

	@Test(expected = NullPointerException.class)
	public void testSubmitNullRunnable() throws Exception {
		service.submit(null, null);
	}

	@Test(expected = NullPointerException.class)
	public void testSubmitNullCallable() throws Exception {
		service.submit(null);
	}

	@Test
	public void testAsyncTake() throws Exception {
		ThrottledTaskExecutor service = new ThrottledTaskExecutor(new SimpleAsyncTaskExecutor());
		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			public String call() throws Exception {
				return "foo";
			}
		});
		service.execute(task);
		assertEquals("foo", task.get());
	}

	@Test
	public void testBufferedExecute() throws Exception {

		ThrottledTaskExecutor executor = new ThrottledTaskExecutor(new SimpleAsyncTaskExecutor(), 1);
		service = new ExecutorCompletionService<String>(executor);

		service.submit(new Callable<String>() {
			public String call() throws Exception {
				while (!ready) {
					Thread.sleep(10);
				}
				return "foo";
			}
		});
		assertNull(service.poll());
		assertEquals(1, executor.size());

		new Thread(new Runnable() {
			public void run() {
				service.submit(new Callable<String>() {
					public String call() throws Exception {
						return "bar";
					}
				});
			}
		}).start();

		Thread.sleep(50);
		// The second submit blocks, so still only one...
		assertEquals(1, executor.size());
		ready = true;
		assertEquals("foo", service.take().get());

		// Wait for the second task to be submitted...
		Thread.sleep(50);
		assertEquals("bar", service.take().get());

	}

	@Test
	public void testBufferedExecuteRejected() throws Exception {

		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(1);
		taskExecutor.setMaxPoolSize(1);
		taskExecutor.setQueueCapacity(0);
		taskExecutor.afterPropertiesSet();

		ThrottledTaskExecutor executor = new ThrottledTaskExecutor(taskExecutor, 10);
		service = new ExecutorCompletionService<String>(executor);

		service.submit(new Callable<String>() {
			public String call() throws Exception {
				while (!ready) {
					Thread.sleep(10);
				}
				return "foo";
			}
		});
		assertNull(service.poll());
		assertEquals(1, executor.size());

		try {
			service.submit(new Callable<String>() {
				public String call() throws Exception {
					return "bar";
				}
			});
			fail("Expected TaskRejectedException");
		}
		catch (TaskRejectedException e) {
			// Expected
		}

		// The second submit fails, so still only one...
		assertEquals(1, executor.size());
		ready = true;
		assertEquals("foo", service.take().get());

	}

}
