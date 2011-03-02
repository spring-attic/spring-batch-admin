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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 * 
 */
@SuppressWarnings("serial")
public class FileInfo implements Comparable<FileInfo>, Serializable {

	private final static String TIMESTAMP_PATTERN = ".*\\.[0-9]*\\.[0-9]*";

	private final String timestamp;

	private final String path;

	private final String shortPath;

	private final boolean local;

	public FileInfo(String path) {
		this(path, null, true);
	}

	public FileInfo(String path, String timestamp, boolean local) {
		super();
		this.path = path.replace("\\", "/");
		this.shortPath = extractPath(path, timestamp);
		this.timestamp = extractTimestamp(path, timestamp);
		this.local = local;
	}
	
	public FileInfo shortPath() {
		FileInfo info = new FileInfo(shortPath, timestamp, local);
		return info;
	}
	
	public String getPattern() {
		if (path.matches(TIMESTAMP_PATTERN)) {
			return path;
		}
		String extension = StringUtils.getFilenameExtension(path);
		String prefix = extension == null ? path : path.substring(0, path.length() - extension.length() - 1);
		if (prefix.matches(TIMESTAMP_PATTERN)) {
			return path;
		}
		return prefix + ".*.*" + (extension==null ? "" : "." + extension);
	}

	private String extractPath(String path, String timestamp) {
		if (path.matches(TIMESTAMP_PATTERN)) {
			return path.substring(0, path.length() - 16);
		}
		String extension = StringUtils.getFilenameExtension(path);
		String prefix = extension == null ? path : path.substring(0, path.length() - extension.length() - 1);
		if (prefix.matches(TIMESTAMP_PATTERN)) {
			return prefix.substring(0, prefix.length() - 16) + "." + extension;
		}
		return path;
	}

	private String extractTimestamp(String path, String timestamp) {
		if (timestamp != null) {
			return timestamp;
		}
		if (path.matches(TIMESTAMP_PATTERN)) {
			return path.substring(path.length() - 15, path.length());
		}
		String extension = StringUtils.getFilenameExtension(path);
		String prefix = extension == null ? path : path.substring(0, path.length() - extension.length() - 1);
		if (prefix.matches(TIMESTAMP_PATTERN)) {
			return prefix.substring(prefix.length() - 15, prefix.length());
		}
		return new SimpleDateFormat("yyyyMMdd.HHmmss").format(new Date());
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
	public String getTimestamp() {
		return timestamp;
	}

	public String getPath() {
		return path;
	}

	public String getFileName() {
		if (path.matches(TIMESTAMP_PATTERN)) {
			return path;
		}
		String extension = StringUtils.getFilenameExtension(path);
		String prefix = extension == null ? path : path.substring(0, path.length() - extension.length() - 1);
		if (prefix.matches(TIMESTAMP_PATTERN)) {
			return path;
		}
		return prefix + getSuffix() + (extension == null ? "" : "." + extension);
	}

	private String getSuffix() {
		return "." + timestamp;
	}

	public int compareTo(FileInfo o) {
		return shortPath.equals(o.shortPath) ? -timestamp.compareTo(o.timestamp) : path.compareTo(o.path);
	}

	public String toString() {
		return "FileInfo [path=" + path + ", timestamp=" + timestamp + ", local=" + local + "]";
	}

}
