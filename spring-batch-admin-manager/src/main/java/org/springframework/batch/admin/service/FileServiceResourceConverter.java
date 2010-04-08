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
package org.springframework.batch.admin.service;

import java.beans.PropertyEditorSupport;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Converter for String to Resource that knows about local files managed by a
 * {@link FileService}.
 * 
 * @author Dave Syer
 * 
 */
public class FileServiceResourceConverter extends PropertyEditorSupport implements Converter<String, Resource>, ResourceLoaderAware {

	// Hack for PropertyEditor.  TODO: remove static modifier.
	static private FileService fileService;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/**
	 * Convenient constructor for declarative configuration purposes.
	 */
	public FileServiceResourceConverter() {
	}

	/**
	 * @param fileService
	 */
	public FileServiceResourceConverter(FileService fileService) {
		FileServiceResourceConverter.fileService = fileService;
	}

	/**
	 * @param fileService the fileService to set
	 */
	public void setFileService(FileService fileService) {
		FileServiceResourceConverter.fileService = fileService;
	}

	/**
	 * Set the resource loader as a fallback for resources that are not managed
	 * by the {@link FileService}.
	 * 
	 * @see ResourceLoaderAware#setResourceLoader(ResourceLoader)
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Convert the source path to a Resource. If it is a managed file from the
	 * {@link FileService} then it will be returned as a wrapper.
	 * 
	 * @see Converter#convert(Object)
	 */
	public Resource convert(String source) {

		String path = source;

		if (path.startsWith("files:")) {
			Resource file = fileService.getResource(path);
			if (file != null) {
				return file;
			}
		}

		return resourceLoader.getResource(source);

	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		// TODO remove PropertyEditor support (SPR-7079)
		setValue(convert(text));
	}

}
