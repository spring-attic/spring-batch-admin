/*
 * Copyright 2014-2015 the original author or authors.
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
package org.springframework.batch.admin.domain;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.admin.domain.support.ExitStatusJacksonMixIn;
import org.springframework.batch.admin.domain.support.ISO8601DateFormatWithMilliSeconds;
import org.springframework.batch.admin.domain.support.JobParameterJacksonMixIn;
import org.springframework.batch.admin.domain.support.JobParametersJacksonMixIn;
import org.springframework.batch.admin.domain.support.StepExecutionHistoryJacksonMixIn;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Michael Minella
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AbstractSerializationTests.SerializationConfiguration.class})
public abstract class AbstractSerializationTests<T> {

	@Autowired
	protected ObjectMapper mapper;

	public abstract void assertJson(String json) throws Exception;

	public abstract void assertObject(T object) throws Exception;

	public abstract T getSerializationValue();

	@Test
	@SuppressWarnings("unchecked")
	public void testJsonSerialization() throws Exception {
		T serializationValue = getSerializationValue();
		String json = mapper.writeValueAsString(serializationValue);
		assertJson(json);
		T roundTrip = (T) mapper.readValue(json, serializationValue.getClass());
		assertObject(roundTrip);
	}

	@Configuration
	public static class SerializationConfiguration {

		@Bean
		public ObjectMapper mapper() {
			Map<Class<?>, Class<?>> mixins = new HashMap<Class<?>, Class<?>>();
			mixins.put(JobParameters.class, JobParametersJacksonMixIn.class);
			mixins.put(JobParameter.class, JobParameterJacksonMixIn.class);
			mixins.put(StepExecutionHistory.class, StepExecutionHistoryJacksonMixIn.class);
			mixins.put(ExitStatus.class, ExitStatusJacksonMixIn.class);

			ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
					.featuresToDisable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
					.dateFormat(new ISO8601DateFormatWithMilliSeconds())
					.mixIns(mixins)
					.build();

			objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

			return objectMapper;
		}
	}
}