/*
 * Copyright 2013-2015 the original author or authors.
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

package org.springframework.batch.admin.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import org.junit.Before;

import org.springframework.batch.admin.domain.JobExecutionInfoResource;
import org.springframework.batch.admin.domain.JobInstanceInfoResource;
import org.springframework.batch.admin.domain.StepExecutionHistory;
import org.springframework.batch.admin.domain.support.ExitStatusJacksonMixIn;
import org.springframework.batch.admin.domain.support.ISO8601DateFormatWithMilliSeconds;
import org.springframework.batch.admin.domain.support.JobParameterJacksonMixIn;
import org.springframework.batch.admin.domain.support.JobParametersJacksonMixIn;
import org.springframework.batch.admin.domain.support.StepExecutionHistoryJacksonMixIn;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Base class for Controller layer tests. Takes care of resetting the mocked (be them mockito mocks or <i>e.g.</i> in
 * memory) dependencies before each test.
 * 
 * @author Eric Bottard
 * @author Ilayaperumal Gopinathan
 */
@ContextConfiguration(classes = { AbstractControllerIntegrationTest.LegacyMvcConfiguration.class })
public class AbstractControllerIntegrationTest {

	protected MockMvc mockMvc;

	@Configuration
	@EnableWebMvc
	protected static class LegacyMvcConfiguration extends WebMvcConfigurerAdapter {

		@Override
		public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
			Map<String, Class> aliases = new HashMap<String, Class>();
			aliases.put("jobInstanceInfo", JobInstanceInfoResource.class);
			aliases.put("jobExecutionInfo", JobExecutionInfoResource.class);
			aliases.put("link", Link.class);
			aliases.put("jobExecution", JobExecution.class);
			aliases.put("jobParameter", JobParameter.class);
			aliases.put("stepExecution", StepExecution.class);

			Map<Class<?>, String> omittedFields = new HashMap<Class<?>, String>();

			omittedFields.put(JobExecutionInfoResource.class, "dateFormat");
			omittedFields.put(JobExecutionInfoResource.class, "durationFormat");
			omittedFields.put(JobExecutionInfoResource.class, "timeFormat");
			omittedFields.put(JobExecutionInfoResource.class, "converter");
			omittedFields.put(StepExecution.class, "jobExecution");
			omittedFields.put(Link.class, "template");

			XStreamMarshaller marshaller = new XStreamMarshaller();
			marshaller.setAliasesByType(aliases);
			marshaller.setOmittedFields(omittedFields);
			marshaller.setConverters(new SingleValueConverter() {
				@Override
				public String toString(Object obj) {
					return ((TimeZone) obj).getID();
				}

				@Override
				public Object fromString(String str) {
					return TimeZone.getTimeZone(str);
				}

				@Override
				public boolean canConvert(Class type) {
					return TimeZone.class.isAssignableFrom(type);
				}
			});

			marshaller.afterPropertiesSet();

			MarshallingHttpMessageConverter converter = new MarshallingHttpMessageConverter(marshaller);

			converters.add(converter);

			MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();

			ObjectMapper objectMapper = jsonConverter.getObjectMapper();
			objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			objectMapper.setDateFormat(new ISO8601DateFormatWithMilliSeconds());
			objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
			objectMapper.addMixInAnnotations(JobParameters.class, JobParametersJacksonMixIn.class);
			objectMapper.addMixInAnnotations(JobParameter.class, JobParameterJacksonMixIn.class);
			objectMapper.addMixInAnnotations(StepExecutionHistory.class, StepExecutionHistoryJacksonMixIn.class);
			objectMapper.addMixInAnnotations(ExitStatus.class, ExitStatusJacksonMixIn.class);

			converters.add(jsonConverter);
		}
	}

	@Autowired
	private WebApplicationContext wac;

	@Before
	public void setupMockMVC() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).defaultRequest(
				get("/").accept(MediaType.APPLICATION_JSON)).build();
	}

	// Instance repositories
	@Autowired
	protected JobRepository jobRepository;

	@Autowired
	protected JobService jobService;

}
