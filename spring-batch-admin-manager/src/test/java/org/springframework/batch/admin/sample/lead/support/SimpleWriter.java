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
package org.springframework.batch.admin.sample.lead.support;

import java.util.List;

import org.springframework.batch.admin.sample.lead.Lead;
import org.springframework.batch.item.ItemWriter;

/**
 * A writer for a list of leads.
 * 
 * @author Dave Syer
 * 
 */
public class SimpleWriter implements ItemWriter<Lead> {

	private StringBuffer result = new StringBuffer();

	/**
	 * Wait for the results to arrive. Each item is a future result containing a
	 * list of leads. Each lead in that list will have been processed in a
	 * different branch of the message flow.
	 * 
	 * @see ItemWriter#write(List)
	 */
	public void write(List<? extends Lead> items) throws Exception {
		for (Lead value : items) {
			String sales = value.getSalesRep();
			if (sales != null && !sales.equals("Nobody")) {
				result.append(value.getClient().getName() + "(" + sales + ")");
			}
		}
	}

	/**
	 * For testing purposes, keep an aggregate summary of all activity.
	 * 
	 * @return a summary of all the calls to write
	 */
	public String getResult() {
		return result.toString();
	}

}
