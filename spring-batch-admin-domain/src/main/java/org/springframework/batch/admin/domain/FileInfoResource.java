/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.batch.admin.domain;

import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.hateoas.ResourceSupport;

/**
 * @author Michael Minella
 */
@XmlRootElement
public class FileInfoResource extends ResourceSupport {

	private String timestamp;

	private String path;

	private String shortPath;

	private boolean local;

	private int deleteCount = 0;

	public FileInfoResource() {
	}

	public FileInfoResource(String timestamp, String path, String shortPath, boolean local, int deleteCount) {
		this.timestamp = timestamp;
		this.path = path;
		this.shortPath = shortPath;
		this.local = local;
		this.deleteCount = deleteCount;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getPath() {
		return path;
	}

	public String getShortPath() {
		return shortPath;
	}

	public boolean isLocal() {
		return local;
	}

	public int getDeleteCount() {
		return deleteCount;
	}
}
