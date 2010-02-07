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



/**
 * @author Dave Syer
 * 
 */
public class FileInfo implements Comparable<FileInfo> {

	private final String locator;

	private final String path;

	private final boolean local;

	public FileInfo(String path) {
		this(path, path.replace("/", "|"), true);
	}

	public FileInfo(String path, String locator, boolean local) {
		super();
		this.path = path.replace("\\", "/");
		this.locator = locator;
		this.local = local;
	}
	
	public static FileInfo fromLocator(String locator) {
		return new FileInfo(locator.replace("|", "/"));
	}

	/**
	 * @return the local
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * @return the locator
	 */
	public String getLocator() {
		return locator;
	}

	public String getPath() {
		return path;
	}

	public int compareTo(FileInfo o) {
		return path.compareTo(o.path);
	}

}
