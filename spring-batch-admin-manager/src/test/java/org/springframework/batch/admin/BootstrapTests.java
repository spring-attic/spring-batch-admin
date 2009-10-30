/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.batch.admin.partition.remote.StepService;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author Dave Syer
 * 
 */
public class BootstrapTests {

	@Test
	public void testBootstrapConfiguration() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:/org/springframework/batch/admin/bootstrap/*.xml");
		assertTrue(context.containsBean("jobRepository"));
	}

	@Test
	public void testWebappRootConfiguration() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath*:/org/springframework/batch/admin/web/resources/webapp-config.xml");
		assertTrue(context.containsBean("jobRepository"));
	}

	@Test
	public void testServletConfiguration() throws Exception {
		ApplicationContext parent = new ClassPathXmlApplicationContext(
				"classpath*:/org/springframework/batch/admin/web/resources/webapp-config.xml");
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] {
						"classpath*:/org/springframework/batch/admin/web/**/*-servlet.xml",
						// This is not used in a web app, but step1 is needed
						// while we are using a SimpleStepLocator
						"classpath:/test-job-context.xml" }, parent);

		assertTrue(context.containsBean("jobRepository"));
		String[] beanNames = BeanFactoryUtils
				.beanNamesForTypeIncludingAncestors(context.getBeanFactory(),
						StepService.class);
		assertEquals(2, beanNames.length);

		context.refresh();
		assertNotNull(context.getBean("stepService", StepService.class));
	}

}
