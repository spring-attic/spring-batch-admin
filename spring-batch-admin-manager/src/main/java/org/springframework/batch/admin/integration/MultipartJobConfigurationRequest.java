/*
 * Copyright 2006-2010 the original author or authors.
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

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Dave Syer
 *
 */
public class MultipartJobConfigurationRequest {
	
	private MultipartFile file;

	/**
	 * @param file the file to set
	 */
	public void setFile(MultipartFile file) {
		this.file = file;
	}
	
	/**
	 * @return the file
	 */
	public MultipartFile getFile() {
		return file;
	}
	
	/**
	 * Extract the relevant data from the multipart and generate a new request.
	 * 
	 * @return a {@link JobConfigurationRequest}
	 */
	public JobConfigurationRequest getJobConfigurationRequest() {
		JobConfigurationRequest jobConfigurationRequest = new JobConfigurationRequest();
		if (file==null) {
			jobConfigurationRequest.setXml("");
			return jobConfigurationRequest;
		}
		try {
			jobConfigurationRequest.setXml(new String(file.getBytes()));
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Cannot extract file from multipart", e);
		}
		jobConfigurationRequest.setFileName(file.getOriginalFilename());
		return jobConfigurationRequest;
	}

}
