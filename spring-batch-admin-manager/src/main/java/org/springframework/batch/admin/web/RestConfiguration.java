/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.batch.admin.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Takes care of infrastructure setup for the web/rest layer.
 *
 * @author Eric Bottard
 * @author David Turanski
 * @author Andrew Eisenberg
 * @author Scott Andrews
 * @author Gunnar Hillert
 * @since 2.0
 */
@Configuration
@Import(RestControllerAdvice.class)
public class RestConfiguration {

	@Bean
	public BatchJobExecutionsController batchJobExecutionsController() {
		return new BatchJobExecutionsController();
	}

	@Bean
	public BatchJobInstancesController batchJobInstancesController() {
		return new BatchJobInstancesController();
	}

	@Bean
	public BatchJobsController batchJobsController() {
		return new BatchJobsController();
	}

	@Bean
	public BatchStepExecutionsController batchStepExecutionsController() {
		return new BatchStepExecutionsController();
	}
}
