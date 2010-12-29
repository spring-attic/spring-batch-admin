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

package org.springframework.batch.admin.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;

/**
 * @author Dave Syer
 * 
 */
public class SimpleEhCacheInterceptorTests {

	private SimpleEhCacheInterceptor interceptor = new SimpleEhCacheInterceptor();

	@Before
	public void open() throws Exception {
		interceptor.destroy();
		interceptor.afterPropertiesSet();
	}

	@After
	public void close() throws Exception {
		interceptor.destroy();
	}

	@Test
	public void testNonCaching() throws Exception {
		TestService target = new TestService();
		Service service = target;
		assertEquals("foo.test", service.get("foo"));
		target.setSuffix(".bar");
		assertEquals("foo.bar", service.get("foo"));
	}

	@Test
	public void testCachingSimple() throws Exception {
		ProxyFactory factory = new ProxyFactory(Service.class, interceptor);
		TestService target = new TestService();
		factory.setTarget(target);
		Service service = (Service) factory.getProxy();
		assertEquals("foo.test", service.get("foo"));
		target.setSuffix(".bar");
		assertEquals("foo.test", service.get("foo"));
	}

	@Test
	public void testCachingSimpleExpired() throws Exception {
		interceptor.destroy();
		interceptor = new SimpleEhCacheInterceptor();
		interceptor.setTimeout(1);
		interceptor.afterPropertiesSet();
		ProxyFactory factory = new ProxyFactory(Service.class, interceptor);
		TestService target = new TestService();
		factory.setTarget(target);
		Service service = (Service) factory.getProxy();
		assertEquals("foo.test", service.get("foo"));
		// N.B. this will always take more than 1 second...
		Thread.sleep(1500L);
		target.setSuffix(".bar");
		assertEquals("foo.bar", service.get("foo"));
	}

	@Test
	public void testCachingCollection() throws Exception {
		ProxyFactory factory = new ProxyFactory(Service.class, interceptor);
		TestService target = new TestService();
		factory.setTarget(target);
		Service service = (Service) factory.getProxy();
		assertEquals("[foo.test]", service.getCollection("foo").toString());
		target.setSuffix(".bar");
		assertEquals("[foo.test]", service.getCollection("foo").toString());
	}

	@Test
	public void testCachingMap() throws Exception {
		ProxyFactory factory = new ProxyFactory(Service.class, interceptor);
		TestService target = new TestService();
		factory.setTarget(target);
		Service service = (Service) factory.getProxy();
		assertEquals("{foo=foo.test}", service.getMap("foo").toString());
		target.setSuffix(".bar");
		assertEquals("{foo=foo.test}", service.getMap("foo").toString());
	}

	@Test
	public void testCachingArray() throws Exception {
		ProxyFactory factory = new ProxyFactory(Service.class, interceptor);
		TestService target = new TestService();
		factory.setTarget(target);
		Service service = (Service) factory.getProxy();
		assertEquals("[foo.test]", Arrays.asList(service.getArray("foo")).toString());
		target.setSuffix(".bar");
		assertEquals("[foo.test]", Arrays.asList(service.getArray("foo")).toString());
	}

	@Test
	public void testCachingEmptySimple() throws Exception {
		ProxyFactory factory = new ProxyFactory(Service.class, interceptor);
		EmptyService target = new EmptyService();
		factory.setTarget(target);
		Service service = (Service) factory.getProxy();
		assertEquals(null, service.get("foo"));
		target.setSuffix(".bar");
		assertEquals("foo.bar", service.get("foo"));
	}

	@Test
	public void testCachingEmptyCollection() throws Exception {
		ProxyFactory factory = new ProxyFactory(Service.class, interceptor);
		EmptyService target = new EmptyService();
		factory.setTarget(target);
		Service service = (Service) factory.getProxy();
		assertEquals("[]", service.getCollection("foo").toString());
		target.setSuffix(".bar");
		assertEquals("[foo.bar]", service.getCollection("foo").toString());
	}

	@Test
	public void testCachingEmptyMap() throws Exception {
		ProxyFactory factory = new ProxyFactory(Service.class, interceptor);
		EmptyService target = new EmptyService();
		factory.setTarget(target);
		Service service = (Service) factory.getProxy();
		assertEquals("{}", service.getMap("foo").toString());
		target.setSuffix(".bar");
		assertEquals("{foo=foo.bar}", service.getMap("foo").toString());
	}

	@Test
	public void testCachingEmptyArray() throws Exception {
		ProxyFactory factory = new ProxyFactory(Service.class, interceptor);
		EmptyService target = new EmptyService();
		factory.setTarget(target);
		Service service = (Service) factory.getProxy();
		assertEquals("[]", Arrays.asList(service.getArray("foo")).toString());
		target.setSuffix(".bar");
		assertEquals("[foo.bar]", Arrays.asList(service.getArray("foo")).toString());
	}

	public static interface Service {
		String get(String input);

		String[] getArray(String input);

		Collection<String> getCollection(String input);

		Map<String, String> getMap(String input);
	}

	public static class TestService implements Service {
		private String suffix = ".test";

		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}

		public String get(String input) {
			return input + suffix;
		}

		public String[] getArray(String input) {
			return new String[] { input + suffix };
		}

		public Collection<String> getCollection(String input) {
			return Collections.singleton(input + suffix);
		}

		public Map<String, String> getMap(String input) {
			return Collections.singletonMap(input, input + suffix);
		}
	}

	public static class EmptyService implements Service {
		private String suffix = null;

		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}

		public String get(String input) {
			return suffix == null ? null : input + suffix;
		}

		public String[] getArray(String input) {
			return suffix == null ? new String[0] : new String[] { input + suffix };
		}

		public Collection<String> getCollection(String input) {
			return suffix == null ? Collections.<String> emptySet() : Collections.singleton(input + suffix);
		}

		public Map<String, String> getMap(String input) {
			return suffix == null ? Collections.<String, String> emptyMap() : Collections.singletonMap(input, input
					+ suffix);
		}
	}

}
