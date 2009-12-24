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

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

/**
 * Adapt a {@link FileUploadRequest} to a {@link File} by writing it out to a
 * temp directory.
 * 
 * @author Dave Syer
 * 
 */
@MessageEndpoint
public class FileUploadRequestToFileAdapter implements InitializingBean {

	private File directory = new File("target/data");

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public void afterPropertiesSet() {
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new IllegalStateException("Cannot delete directory "
						+ directory);
			}
		}
	}

	@ServiceActivator
	public File convert(FileUploadRequest request) throws Exception {

		if (request.getData() == null) {
			throw new IllegalArgumentException("Null input data");
		}

		File tempFile = File.createTempFile("lead-data-", ".txt");
		FileUtils.writeStringToFile(tempFile, request.getData());
		return tempFile;

	}

}
