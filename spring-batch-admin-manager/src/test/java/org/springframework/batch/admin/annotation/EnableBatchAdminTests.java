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
package org.springframework.batch.admin.annotation;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.net.URLClassLoader;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Michael Minella
 */
public class EnableBatchAdminTests {

	private AnnotationConfigApplicationContext context;

	@Before
	public void setUp() {
		this.context = new AnnotationConfigApplicationContext(BatchAdminConfiguration.class);
	}

	@After
	public void tearDown() {
		this.context.close();
	}

	@Test
	public void testContext() {

		ClassLoader cl = ClassLoader.getSystemClassLoader();

		URL[] urls = ((URLClassLoader)cl).getURLs();

		System.out.println("****************************************");
		for(URL url: urls){
			System.out.println(url.getFile());
		}

		String[] beanDefinitionNames = this.context.getBeanDefinitionNames();

		System.out.println("****************************************");
		for (String beanDefinitionName : beanDefinitionNames) {
			System.out.println(beanDefinitionName);
		}
		System.out.println("****************************************");

		assertEquals(1, this.context.getBeanNamesForType(JobBuilderFactory.class).length);
		assertEquals(1, this.context.getBeanNamesForType(StepBuilderFactory.class).length);
		assertEquals(1, this.context.getBeanNamesForType(JobRepository.class).length);
		assertEquals(1, this.context.getBeanNamesForType(JobExplorer.class).length);
		assertEquals(1, this.context.getBeanNamesForType(JobLauncher.class).length);
		assertEquals(10, this.context.getBeanNamesForAnnotation(Controller.class).length);
		assertEquals(1, this.context.getBeanNamesForType(DataSource.class).length);
		assertEquals(1, this.context.getBeanNamesForType(PlatformTransactionManager.class).length);
		assertEquals(1, this.context.getBeanNamesForType(JobService.class).length);
	}

	@Configuration
	@EnableBatchAdmin
	public static class BatchAdminConfiguration {
	}
}
