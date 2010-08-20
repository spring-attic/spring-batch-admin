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
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.springframework.batch.admin.service.FileInfo;
import org.springframework.batch.admin.service.FileService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.util.Assert;

/**
 * Adapt a {@link FileUploadRequest} to a {@link File} by writing it out to a
 * temp directory.
 * 
 * @author Dave Syer
 * 
 */
@MessageEndpoint
public class StringToFileAdapter implements InitializingBean {

	private FileService fileService;

	/**
	 * The service used to manage file lists and uploads.
	 * 
	 * @param fileService the {@link FileService} to set
	 */
	public void setFileService(FileService fileService) {
		this.fileService = fileService;
	}

	public void afterPropertiesSet() {
		Assert.state(fileService!=null, "FileService must be provided");
	}

	@ServiceActivator
	public Collection<FileInfo> convert(String request) throws Exception {

		if (request == null) {
			throw new IllegalArgumentException("Null input data");
		}

		String path = "upload.txt";
		FileInfo dest = fileService.createFile(path);
		FileUtils.writeStringToFile(fileService.getResource(dest.getPath()).getFile(), request);
		fileService.publish(dest);

		return Arrays.asList(dest);

	}

}
