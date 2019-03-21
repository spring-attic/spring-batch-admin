/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.admin.domain.support;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.batch.core.JobParameter;

/**
 * Jackson Deserializer for {@link org.springframework.batch.core.JobParameter} de-serialization.
 *
 * @author Gunnar Hillert
 * @since 1.0
 */
public class JobParameterJacksonDeserializer extends JsonDeserializer<JobParameter> {

	private static Log logger = LogFactory.getLog(JobParameterJacksonDeserializer.class);

	@Override
	public JobParameter deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException{
		SimpleDateFormat formatter = new SimpleDateFormat();
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		final String value = node.get("value").asText();
		final boolean identifying = node.get("identifying").asBoolean();
		final String type = node.get("type").asText();

		final JobParameter jobParameter;

		if (!type.isEmpty() && !type.equalsIgnoreCase("STRING"))  {
			if ("DATE".equalsIgnoreCase(type)) {
				try {
					jobParameter = new JobParameter(formatter.parse(value), identifying);
				}
				catch (ParseException e) {
					throw new IOException(e);
				}
			}
			else if ("DOUBLE".equalsIgnoreCase(type)) {
				jobParameter = new JobParameter(Double.valueOf(value), identifying);
			}
			else if ("LONG".equalsIgnoreCase(type)) {
				jobParameter = new JobParameter(Long.valueOf(value), identifying);
			}
			else {
				throw new IllegalStateException("Unsupported JobParameter type: " + type);
			}
		}
		else {
			jobParameter = new JobParameter(value, identifying);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("jobParameter - value: %s (type: %s, isIdentifying: %s)",
					jobParameter.getValue(), jobParameter.getType().name(), jobParameter.isIdentifying()));
		}

		return jobParameter;
	}
}
