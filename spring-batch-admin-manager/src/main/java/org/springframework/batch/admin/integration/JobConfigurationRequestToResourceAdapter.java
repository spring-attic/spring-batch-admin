/*
 * Copyright 2009-2010 the original author or authors.
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
package org.springframework.batch.admin.integration;

import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.util.StringUtils;

/**
 * Adapt a {@link JobConfigurationRequest} to a Spring {@link Resource} so it
 * can be handled by a generic consumer.
 * 
 * @author Dave Syer
 * 
 */
@MessageEndpoint
public class JobConfigurationRequestToResourceAdapter {

	private static final String EMPTY_BEANS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<beans xmlns=\"http://www.springframework.org/schema/beans\""
			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
			+ " xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\"/>";

	@ServiceActivator
	public Resource adapt(JobConfigurationRequest request) throws DuplicateJobException {

		String filename = request.getFilename();
		if (!StringUtils.hasText(request.getXml())) {
			return new ByteArrayResource(EMPTY_BEANS.getBytes(), filename + ":empty-string");
		}
		return new ByteArrayResource(request.getXml().getBytes(), filename);

	}

}
