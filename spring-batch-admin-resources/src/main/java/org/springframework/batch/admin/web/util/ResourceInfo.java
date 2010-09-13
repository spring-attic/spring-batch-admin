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
package org.springframework.batch.admin.web.util;

import org.springframework.web.bind.annotation.RequestMethod;

public class ResourceInfo implements Comparable<ResourceInfo> {

	private final String url;

	private final RequestMethod method;

	private final String description;

	public ResourceInfo(String url, RequestMethod method) {
		this(url, method, "");
	}

	public ResourceInfo(String url, RequestMethod method, String description) {
		this.url = url;
		this.method = method;
		this.description = description == null ? "" : description;
	}

	public String getUrl() {
		return url;
	}

	public RequestMethod getMethod() {
		return method;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ResourceInfo)) {
			return false;
		}
		ResourceInfo other = (ResourceInfo) obj;
		return method.equals(other.getMethod()) && url.equals(other.getUrl());
	}

	@Override
	public int hashCode() {
		return 127 + 23 * url.hashCode() + 17 * method.hashCode();
	}

	public int compareTo(ResourceInfo o) {
		int urls = url.compareTo(o.url);
		return urls != 0 ? urls : method.compareTo(o.method);
	}

	@Override
	public String toString() {
		return "ResourceInfo [method=" + method + ", url=" + url + ", " + "description=" + description + "]";
	}

}
