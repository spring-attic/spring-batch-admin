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
	
	private String locator;
	
	private boolean trigger;
	
	private String outputPath;

	private String triggerPath;

	/**
	 * @param path
	 */
	public FileInfo(File triggerPath, File outputPath, File file) {
		this(triggerPath, outputPath, file, "");
	}	

	/**
	 * @param path
	 * @param locator
	 */
	public FileInfo(File triggerPath, File outputPath, File file, String locator) {
		super();
		this.path = extractPath(outputPath, file);
		this.outputPath = extractPath(outputPath);
		setTriggerPath(triggerPath);
		this.locator = locator;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the locator
	 */
	public String getLocator() {
		return locator;
	}

	/**
	 * @param locator the locator to set
	 */
	public void setLocator(String locator) {
		this.locator = locator;
	}

	/**
	 * @return the trigger
	 */
	public boolean isTrigger() {
		return trigger;
	}

	/**
	 * @param trigger the trigger to set
	 */
	public void setTriggerPath(File parent) {
		File file = new File(parent, path);
		this.trigger = file.exists();
		this.triggerPath = extractPath(parent);
	}

	/**
	 * @return the outputPath
	 */
	public String getOutputPath() {
		return outputPath;
	}

	/**
	 * @return the triggerPath
	 */
	public String getTriggerPath() {
		return triggerPath;
	}

	public int compareTo(FileInfo o) {
		return path.compareTo(o.path);
	}

	private String extractPath(File file) {
		return file.getAbsolutePath().replace("\\", "/");
	}
	
	private String extractPath(File parent, File file) {
		int start = parent.getAbsolutePath().length();
		return file.getAbsolutePath().substring(start + 1).replace("\\", "/");
	}
	
}
