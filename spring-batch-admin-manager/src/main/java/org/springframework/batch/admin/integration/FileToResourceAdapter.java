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

import java.io.File;

import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

/**
 * Adapt a {@link File} to a Spring {@link Resource} so it can be consumed by a
 * generic consumer.
 * 
 * @author Dave Syer
 * 
 */
@MessageEndpoint
public class FileToResourceAdapter {

	@ServiceActivator
	public Resource adapt(File file) throws DuplicateJobException {

		return new FileSystemResource(file);

	}

}
