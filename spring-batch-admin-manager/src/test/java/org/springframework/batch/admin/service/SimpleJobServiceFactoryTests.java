/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.batch.admin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.support.MapJobExplorerFactoryBean;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

/**
 * @author Michael Minella
 */
public class SimpleJobServiceFactoryTests {

	private SimpleJobServiceFactoryBean factoryBean;

	@Test
	public void testAfterPropertiesSet() throws Exception {
		factoryBean = new SimpleJobServiceFactoryBean();

		try {
			factoryBean.afterPropertiesSet();
			fail();
		} catch (IllegalArgumentException expected) {
			assertEquals("DataSource must not be null.", expected.getMessage());
		}

		factoryBean.setDataSource(new EmbeddedDatabaseBuilder().build());

		try {
			factoryBean.afterPropertiesSet();
			fail();
		} catch (IllegalArgumentException expected) {
			assertEquals("JobRepository must not be null.", expected.getMessage());
		}

		factoryBean.setJobRepository((JobRepository) new MapJobRepositoryFactoryBean(new ResourcelessTransactionManager()).getObject());

		try {
			factoryBean.afterPropertiesSet();
			fail();
		} catch (IllegalArgumentException expected) {
			assertEquals("JobLocator must not be null.", expected.getMessage());
		}

		factoryBean.setJobLocator(new MapJobRegistry());

		try {
			factoryBean.afterPropertiesSet();
			fail();
		} catch (IllegalArgumentException expected) {
			assertEquals("JobLauncher must not be null.", expected.getMessage());
		}

		factoryBean.setJobLauncher(new SimpleJobLauncher());

		try {
			factoryBean.afterPropertiesSet();
			fail();
		} catch (IllegalArgumentException expected) {
			assertEquals("JobExplorer must not be null.", expected.getMessage());
		}

		factoryBean.setJobExplorer(new MapJobExplorerFactoryBean(new MapJobRepositoryFactoryBean(new ResourcelessTransactionManager())).getObject());

        try {
            factoryBean.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("JobRegistry must not be null.", expected.getMessage());
        }

        factoryBean.setJobRegistry(new MapJobRegistry());


        factoryBean.afterPropertiesSet();
	}

}