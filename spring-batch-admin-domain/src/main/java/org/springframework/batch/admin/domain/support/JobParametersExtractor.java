/*
 * Copyright 2006-2014 the original author or authors.
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

package org.springframework.batch.admin.domain.support;

import java.util.Properties;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.support.PropertiesConverter;

/**
 * Helper class for extracting a String representation of {@link JobParameters}
 * for rendering.
 * 
 * @author Dave Syer
 * 
 */
public class JobParametersExtractor {

	private JobParametersConverter converter = new DefaultJobParametersConverter();

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/**
	 * @param oldParameters the latest job parameters
	 * @return a String representation for rendering the job parameters from the
	 * last instance
	 */
	public String fromJobParameters(JobParameters oldParameters) {

		String properties = PropertiesConverter.propertiesToString(converter.getProperties(oldParameters));
		if (properties.startsWith("#")) {
			properties = properties.substring(properties.indexOf(LINE_SEPARATOR) + LINE_SEPARATOR.length());
		}
		properties = properties.replace("\\:", ":");
		return properties;

	}

	public JobParameters fromString(String params) {
		Properties properties = PropertiesConverter.stringToProperties(params);
		return converter.getJobParameters(properties);
	}


}
