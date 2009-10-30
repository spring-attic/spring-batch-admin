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
package org.springframework.batch.admin.web;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyEditorSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.util.WebUtils;

public class MultiBinderTests {

	public static class TestBean {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	@Test
	public void testVanillaBinding() throws Exception {
		WebDataBinder binder = new WebDataBinder(new TestBean(), "bean");
		binder.bind(new MutablePropertyValues(Collections.singletonMap("name", "foo")));
		TestBean bean = (TestBean) binder.getTarget();
		assertEquals("foo", bean.getName());
	}

	@Test
	public void testModifiedBinding() throws Exception {
		WebDataBinder binder = new WebDataBinder(new TestBean(), "bean");
		binder.registerCustomEditor(String.class, "name", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(text.replace("name_", ""));
			}
		});
		// This is all very well but not actually what we want
		binder.bind(new MutablePropertyValues(Collections.singletonMap("name", "name_foo")));
		TestBean bean = (TestBean) binder.getTarget();
		assertEquals("foo", bean.getName());
	}

	@Test
	public void testPrefixBinding() throws Exception {
		WebDataBinder binder = new WebDataBinder(new TestBean(), "bean");
		Map<String,String> values = new HashMap<String, String>();
		values.put("name_foo", "rubbish");
		// This is what we need...
		values.put("name", WebUtils.findParameterValue(values, "name"));
		binder.bind(new MutablePropertyValues(values));
		TestBean bean = (TestBean) binder.getTarget();
		assertEquals("foo", bean.getName());
	}

}
