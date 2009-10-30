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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

/**
 * <p>
 * A {@link TaskExecutor} with a throttle limit which works by delegating to an
 * existing task executor and limiting the number of tasks submitted.
 * </p>
 * <p>
 * A throttle limit is provided to limit the number of pending requests over and
 * above the features provided by the other task executors. The submit method
 * blocks until there are results available to retrieve. This limit is different
 * (and orthogonal) to any queue size imposed by the delegate
 * {@link TaskExecutor}: such queues normally do not throttle, in the sense that
 * they always accept more work, until they fill up, at which point they reject.
 * The point of a throttle is to not reject any work, but to still limit the
 * number of concurrent tasks.
 * </p>
 * @author Dave Syer
 * 
 * @param <V> the type of object returned by the service
 */
public class ThrottledTaskExecutor implements TaskExecutor {

	private final BlockingQueue<Runnable> completionQueue;

	private final Semaphore semaphore;

	private volatile int count = 0;

	private TaskExecutor taskExecutor = new SyncTaskExecutor();

	/**
	 * Create a {@link TaskExecutorCompletionService} with infinite
	 * (Integer.MAX_VALUE) throttle limit. A task can always be submitted.
	 */
	public ThrottledTaskExecutor() {
		this(null, Integer.MAX_VALUE);
	}

	/**
	 * Create a {@link TaskExecutorCompletionService} with infinite
	 * (Integer.MAX_VALUE) throttle limit. A task can always be submitted.
	 * 
	 * @param taskExecutor the {@link TaskExecutor} to use
	 */
	public ThrottledTaskExecutor(TaskExecutor taskExecutor) {
		this(taskExecutor, Integer.MAX_VALUE);
	}

	/**
	 * Create a {@link TaskExecutorCompletionService} with finite throttle
	 * limit. The submit method will block when this limit is reached until one
	 * of the tasks has finished.
	 * 
	 * @param taskExecutor the {@link TaskExecutor} to use
	 * @param throttleLimit the throttle limit
	 */
	public ThrottledTaskExecutor(TaskExecutor taskExecutor, int throttleLimit) {
		super();
		if (taskExecutor != null) {
			this.taskExecutor = taskExecutor;
		}
		this.completionQueue = new LinkedBlockingQueue<Runnable>(throttleLimit);
		this.semaphore = new Semaphore(throttleLimit);
	}

	/**
	 * Public setter for the {@link TaskExecutor} to be used to execute the
	 * tasks submitted. The default is synchronous, executing tasks on the
	 * calling thread. In this case the throttle limit is irrelevant as there
	 * will always be at most one task pending.
	 * 
	 * @param taskExecutor
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * Submit a task for execution by the delegate task executor, blocking if
	 * the throttleLimit is exceeded.
	 * 
	 * @see TaskExecutor#execute(Runnable)
	 */
	public void execute(Runnable task) {
		if (task == null) {
			throw new NullPointerException("Task is null in ThrottledTaskExecutor.");
		}
		doSubmit(task);
	}

	/**
	 * Get an estimate of the number of pending requests.
	 * 
	 * @return the estimate
	 */
	public int size() {
		return count;
	}

	private Runnable doSubmit(final Runnable task) {

		try {
			semaphore.acquire();
			count++;
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new TaskRejectedException("Task could not be submitted because of a thread interruption.");
		}

		taskExecutor.execute(new FutureTask<Object>(task, null) {
			@Override
			protected void done() {
				try {
					completionQueue.put(task);
					semaphore.release();
					count--;
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});

		return task;
	}

}
