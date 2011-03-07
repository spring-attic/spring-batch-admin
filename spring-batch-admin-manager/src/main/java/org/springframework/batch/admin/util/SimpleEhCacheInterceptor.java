/*
 * Copyright 2006-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.batch.admin.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * @author Dave Syer
 * 
 */
@ManagedResource
public class SimpleEhCacheInterceptor implements MethodInterceptor, InitializingBean, DisposableBean, Lifecycle {

	private Cache cache;

	private CacheManager manager;

	private volatile boolean caching = true;

	private long timeout = 60;

	private String name = "simple";

	/**
	 * The expiry timeout of cache entries (a.k.a. time to live) in seconds.
	 * Default 60.
	 * @param timeout in seconds
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * The name of the cache used internally by EhCache. Defaults to
	 * <code>simple</code>.
	 * 
	 * @param name the cache name to set
	 */
	public void setCacheName(String name) {
		this.name = name;
	}

	public void afterPropertiesSet() throws Exception {
		Configuration config  = ConfigurationFactory.parseConfiguration();
		config.setUpdateCheck(false);
		manager = CacheManager.create(config );
		cache = new Cache(name, 0, true, false, timeout, 0);
		manager.addCache(cache);
	}

	public void destroy() throws Exception {
		if (manager != null) {
			manager.removalAll();
			manager.shutdown();
		}
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Serializable key = getKey(invocation);
		Element element = cache.get(key);
		Object value = null;
		if (caching && (element == null || element.isExpired())) {
			cache.remove(key);
			Object old = element == null ? null : element.getValue();
			value = invocation.proceed();
			if (cacheable(value, old)) {
				cache.putIfAbsent(new Element(key, value));
			}
		}
		else {
			value = element.getObjectValue();
		}
		return value;
	}

	@SuppressWarnings("rawtypes")
	private boolean cacheable(Object value, Object old) {
		if (value == null) {
			return false;
		}
		if (old != null) {
			return true;
		}
		if (value instanceof Collection) {
			if (((Collection) value).isEmpty()) {
				return false;
			}

		}
		if (value instanceof Map) {
			if (((Map) value).isEmpty()) {
				return false;
			}
		}
		if (value.getClass().isArray()) {
			if (((Object[]) value).length == 0) {
				return false;
			}
		}
		return true;
	}

	private Serializable getKey(MethodInvocation invocation) {
		return invocation.getMethod().getName() + Arrays.asList(invocation.getArguments());
	}

	@ManagedOperation
	public void start() {
		caching = true;
	}

	@ManagedOperation
	public void stop() {
		caching = false;
	}

	@ManagedAttribute
	public boolean isRunning() {
		return caching;
	}

}
