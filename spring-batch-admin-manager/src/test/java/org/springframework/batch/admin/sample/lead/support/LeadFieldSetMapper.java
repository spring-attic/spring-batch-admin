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
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

public class LeadFieldSetMapper implements FieldSetMapper<Lead> {

	public Lead mapFieldSet(FieldSet fieldSet) {
		Lead lead = new Lead(fieldSet.readLong("id"), new Client(fieldSet.readString("client.name"), fieldSet
				.readString("client.country")), new Product(fieldSet.readString("product.name")));
		lead.setAmount(fieldSet.readDouble("amount"));
		lead.setQuery(fieldSet.readString("query"));
		return lead;
	}

}
