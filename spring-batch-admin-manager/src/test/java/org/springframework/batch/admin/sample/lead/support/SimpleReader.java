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

import org.springframework.batch.admin.sample.lead.Client;
import org.springframework.batch.admin.sample.lead.Lead;
import org.springframework.batch.admin.sample.lead.Product;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * A dummy reader for testing purposes.
 * 
 * @author Dave Syer
 *
 */
public class SimpleReader implements ItemReader<Lead> {
	
	private int maxCount = 10;
	
	private int count=0;

	public Lead read() throws Exception, UnexpectedInputException, ParseException {
		if (count>=maxCount) {
			return null;
		}
		++count;
		return new Lead(123L+count, new Client("Client"+count, "UK"), new Product("Foo"));
	}

}
