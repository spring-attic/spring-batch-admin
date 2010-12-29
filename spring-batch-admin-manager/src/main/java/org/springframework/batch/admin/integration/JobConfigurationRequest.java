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

/**
 * Convenient model object for binding XML configuration data from a form
 * submission.
 * 
 * @author Dave Syer
 * 
 */
public class JobConfigurationRequest {

	private String xml;

	private String filename = "unknown-origin.xml";

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getXml() {
		return xml;
	}

	@Override
	public String toString() {
		return "[filename: " + filename + ", xml: " + (xml == null ? null : xml.substring(0, Math.min(255, xml.length())))+"]";
	}

	/**
	 * @param filename the file name to load
	 */
	public void setFileName(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the filename loaded
	 */
	public String getFilename() {
		return filename;
	}

}
