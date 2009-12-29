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
package org.springframework.batch.admin.web.resources;

/**
 * Convenient base class for {@link Menu} implementations.
 * 
 * @author Dave Syer
 * 
 */
public class BaseMenu implements Menu {

	private final String url;

	private final String label;

	private final int order;
	
	private String prefix = "";

	public BaseMenu(String url, String label) {
		this(url, label, 0);
	}

	public BaseMenu(String url, String label, int order) {
		super();
		this.url = url;
		this.label = label;
		this.order = order;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public String getLabel() {
		return label;
	}

	public String getUrl() {
		return prefix+url;
	}

	public int getOrder() {
		return order;
	}

	@Override
	public String toString() {
		return "Menu:" + label;
	}

}
