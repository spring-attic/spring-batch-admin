/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.admin.web;

import static org.mockito.Mockito.mock;

import org.springframework.batch.admin.service.FileService;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
* @author mminella
*/
@Configuration
@EnableSpringDataWebSupport
public class TestDependencies {

	@Bean
	public FileService fileService() {
		return mock(FileService.class);
	}

	@Bean
	public ListableJobLocator jobLocator () {
		return mock(ListableJobLocator.class);
	}

	@Bean
	public JobService jobService() {
		return mock(JobService.class);
	}

	@Bean
	public JobRepository jobRepository() {
		return mock(JobRepository.class);
	}
}
