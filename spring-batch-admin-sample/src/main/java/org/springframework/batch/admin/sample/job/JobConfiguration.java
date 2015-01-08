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
package org.springframework.batch.admin.sample.job;

import org.springframework.batch.admin.sample.ExampleItemReader;
import org.springframework.batch.admin.sample.ExampleItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Michael Minella
 */
@Configuration
public class JobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	@JobScope
	public ExampleItemReader itemReader() {
		return new ExampleItemReader();
	}

	@Bean
	@StepScope
	public ExampleItemWriter itemWriter(@Value("#{jobParameters[fail]}") Boolean fail) {
		ExampleItemWriter itemWriter = new ExampleItemWriter();
		itemWriter.setFail(fail);
		return itemWriter;
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1")
				.<String, Object>chunk(5)
				.reader(itemReader())
				.writer(itemWriter(null))
				.build();
	}

	@Bean
	public Job javaJob() {
		return jobBuilderFactory.get("javaJob")
				.start(step1())
				.build();
	}
}