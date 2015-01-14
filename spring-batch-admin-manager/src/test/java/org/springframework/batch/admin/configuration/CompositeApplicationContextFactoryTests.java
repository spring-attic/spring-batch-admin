/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.batch.admin.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.ClasspathXmlApplicationContextsFactoryBean;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Michael Minella
 */
public class CompositeApplicationContextFactoryTests {

	private CompositeApplicationContextFactory factory;

	@Before
	public void setUp() {
		factory = new CompositeApplicationContextFactory();
	}

	@Test
	public void testAfterPropertiesSet() throws Exception {
		try {
			factory.afterPropertiesSet();
		} catch (Exception expected) {
			assertEquals(expected.getMessage(), "A factory or factoryBean is required");
		}

		factory.setFactories(Arrays.<ApplicationContextFactory>asList(new GenericApplicationContextFactory(null)));

		factory.afterPropertiesSet();

		factory = new CompositeApplicationContextFactory();
		factory.setFactoryBeans(Arrays.<FactoryBean<ApplicationContextFactory[]>>asList(new ClasspathXmlApplicationContextsFactoryBean()));
		factory.afterPropertiesSet();
	}

	@Test
	public void testIsSingleton() {
		assertTrue(factory.isSingleton());
	}

	@Test
	public void testGetObjectType() {
		assertTrue(factory.getObjectType().equals(ApplicationContextFactory[].class));
	}

	@Test
	public void testGetObjectFactorBeansOnly() throws Exception {
		ClasspathXmlApplicationContextsFactoryBean classpathXmlApplicationContextsFactoryBean =
				new ClasspathXmlApplicationContextsFactoryBean();

		Resource resource1 = new ClassPathResource("classpath:dummy-context.xml");
		Resource resource2 = new ClassPathResource("classpath:dummy-context.xml");

		classpathXmlApplicationContextsFactoryBean.setResources(new Resource[] {resource1, resource2});

		factory.setFactoryBeans(Arrays.<FactoryBean<ApplicationContextFactory[]>>asList(
				classpathXmlApplicationContextsFactoryBean));

		factory.afterPropertiesSet();

		assertEquals(2, factory.getObject().length);
	}

	@Test
	public void testGetObjectFactoriesOnly() throws Exception {
		Resource resource1 = new ClassPathResource("classpath:dummy-context.xml");
		Resource resource2 = new ClassPathResource("classpath:dummy-context.xml");

		factory.setFactories(Arrays.<ApplicationContextFactory>asList(new GenericApplicationContextFactory(resource1),
				new GenericApplicationContextFactory(resource2)));
		factory.afterPropertiesSet();

		assertEquals(2, factory.getObject().length);
	}

	@Test
	public void testGetObjectBothOptionsProvided() throws Exception {
		ClasspathXmlApplicationContextsFactoryBean classpathXmlApplicationContextsFactoryBean =
				new ClasspathXmlApplicationContextsFactoryBean();

		Resource resource1 = new ClassPathResource("classpath:dummy-context.xml");
		Resource resource2 = new ClassPathResource("classpath:dummy-context.xml");

		classpathXmlApplicationContextsFactoryBean.setResources(new Resource[] {resource1, resource2});

		factory.setFactoryBeans(Arrays.<FactoryBean<ApplicationContextFactory[]>>asList(
				classpathXmlApplicationContextsFactoryBean));

		Resource resource3 = new ClassPathResource("classpath:dummy-context.xml");
		Resource resource4 = new ClassPathResource("classpath:dummy-context.xml");

		factory.setFactories(Arrays.<ApplicationContextFactory>asList(new GenericApplicationContextFactory(resource3),
				new GenericApplicationContextFactory(resource4)));
		factory.afterPropertiesSet();

		assertEquals(4, factory.getObject().length);
	}
}
