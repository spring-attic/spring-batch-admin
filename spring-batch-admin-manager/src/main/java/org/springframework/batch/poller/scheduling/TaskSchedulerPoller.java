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

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.batch.poller.Poller;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.util.ErrorHandler;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link Poller} implementation that uses a {@link TaskScheduler} to poll in
 * a background thread.
 * 
 * @author Dave Syer
 * 
 */
public class TaskSchedulerPoller<T> implements Poller<T>, BeanFactoryAware, InitializingBean {

	private static final String TASK_SCHEDULER_BEAN_NAME = "taskScheduler";

	private volatile Trigger trigger;

	private volatile boolean initialized;

	private final Object initializationMonitor = new Object();

	private TaskScheduler taskScheduler;

	private BeanFactory beanFactory;

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void afterPropertiesSet() throws Exception {
		initialize();
	}

	private void initialize() {
		synchronized (this.initializationMonitor) {
			if (this.initialized) {
				return;
			}
			if (this.trigger == null) {
				this.trigger = new PeriodicTrigger(100L);
			}
			if (taskScheduler == null && beanFactory != null) {
				taskScheduler = beanFactory.getBean(TASK_SCHEDULER_BEAN_NAME, TaskScheduler.class);
			}
		}
	}

	private ScheduledFuture<?> getSchedule(final Callable<T> callable, final Queue<T> queue, final AtomicReference<Throwable> throwable) {

		TaskScheduler scheduler = taskScheduler;
		if (scheduler == null) {
			ConcurrentTaskScheduler concurrentTaskScheduler = new ConcurrentTaskScheduler();
			concurrentTaskScheduler.setErrorHandler(new PropagatingErrorHandler());
			scheduler = concurrentTaskScheduler;
		}

		Runnable task = new Runnable() {

			public void run() {
				if (!queue.isEmpty() || throwable.get() != null) {
					return;
				}
				T result;
				try {
					result = callable.call();
				}
				catch (RuntimeException e) {
					throwable.set(e);
					throw e;
				}
				catch (Exception e) {
					throwable.set(e);
					throw new IllegalStateException("Could not obtain result", e);
				}
				if (result != null) {
					queue.add(result);
				}
			}
		};

		ScheduledFuture<?> schedule = scheduler.schedule(task, trigger);

		return schedule;

	}

	/**
	 * @param callback a {@link Callable} to use to retrieve a result
	 * @return the result, or null if the operation times out
	 * 
	 * @see Poller#poll(Callable)
	 */
	public Future<T> poll(Callable<T> callback) throws Exception {

		if (!initialized) {
			initialize();
		}

		final BlockingQueue<T> queue = new LinkedBlockingQueue<T>(1);

		final AtomicReference<Throwable> throwable = new AtomicReference<Throwable>();
		final ScheduledFuture<?> schedule = getSchedule(callback, queue, throwable);

		return new Future<T>() {

			public boolean cancel(boolean mayInterruptIfRunning) {
				return schedule.cancel(mayInterruptIfRunning);
			}

			public T get() throws InterruptedException, ExecutionException {
				try {
					T result = queue.take();
					if (throwable.get()!=null) {
						throw new ExecutionException(throwable.get());
					}
					return result;
				}
				finally {
					cancelAndMaybeRethrow(schedule);
				}
			}

			public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				try {
					T result = queue.poll(timeout, unit);
					if (throwable.get()!=null) {
						throw new ExecutionException(throwable.get());
					}
					return result;
				}
				finally {
					cancelAndMaybeRethrow(schedule);
				}
			}

			public boolean isCancelled() {
				return schedule.isCancelled();
			}

			public boolean isDone() {
				return schedule.isDone() || !queue.isEmpty();
			}

			private void cancelAndMaybeRethrow(final ScheduledFuture<?> schedule) throws InterruptedException, ExecutionException {
				try {
					// Just returns null if the task was successful.
					schedule.get();
				}
				catch (ExecutionException e) {
					throw e;
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw e;
				}
				schedule.cancel(true);
			}

		};

	}

	/**
	 * An {@link ErrorHandler} implementation that propagates the throwable as a
	 * runtime exception.
	 */
	static class PropagatingErrorHandler implements ErrorHandler {

		public void handleError(Throwable t) {
			ReflectionUtils.rethrowRuntimeException(t);
		}

	}

}
