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
package org.springframework.batch.admin.domain.support;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;

/**
 * Extension of the {@link org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean} to
 * configure the resulting {@link com.fasterxml.jackson.databind.ObjectMapper} to provide the types in
 * the output.  This is needed for use cases involving Spring Batch's
 * {@link org.springframework.batch.item.ExecutionContext} which can contain a mixture of value types.
 *
 * @author Michael Minella
 * @since 2.0
 */
public class VariableTypeJackson2ObjectMapperFactoryBean extends Jackson2ObjectMapperFactoryBean {
	@Override
	public ObjectMapper getObject() {
		ObjectMapper objectMapper = super.getObject();
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
		return objectMapper;
	}
}
