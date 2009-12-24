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

import java.io.File;

/**
 * @author Dave Syer
 *
 */
public class FileInfo implements Comparable<FileInfo> {
	
	private String path;
	
	private String name;
	
	private String locator;
	
	private String outputPath;

	/**
	 * @param path
	 */
	public FileInfo(File outputPath, File file) {
		this(outputPath, file, "");
	}	

	/**
	 * @param path
	 * @param locator
	 */
	public FileInfo(File outputPath, File file, String locator) {
		super();
		this.path = extractPath(outputPath, file);
		this.name = extractName(file);
		this.outputPath = extractPath(outputPath);
		this.locator = locator;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the locator
	 */
	public String getLocator() {
		return locator;
	}

	/**
	 * @return the outputPath
	 */
	public String getOutputPath() {
		return outputPath;
	}

	public String getAbsolutePath() {
		return outputPath + "/" + path;
	}
	
	public int compareTo(FileInfo o) {
		return path.compareTo(o.path);
	}

	private String extractPath(File file) {
		return file.getAbsolutePath().replace("\\", "/");
	}
	
	private String extractName(File file) {
		return file.getName();
	}
	
	private String extractPath(File parent, File file) {
		int start = parent.getAbsolutePath().length();
		return file.getAbsolutePath().substring(start + 1).replace("\\", "/");
	}

}
