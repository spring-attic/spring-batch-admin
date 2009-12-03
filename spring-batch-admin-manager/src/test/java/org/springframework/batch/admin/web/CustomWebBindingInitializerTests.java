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

import java.util.Collections;

import org.junit.Test;
import org.springframework.batch.admin.web.CustomWebBindingInitializer;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StringMultipartFileEditor;

public class CustomWebBindingInitializerTests {

	private CustomWebBindingInitializer initializer = new CustomWebBindingInitializer();

	private String name;

	public void setName(String name) {
		this.name = name;
	}

	@Test
	public void testInitBinderVanilla() {
		initializer.setBeanFactory(new DefaultListableBeanFactory());
		ServletRequestDataBinder binder = new ServletRequestDataBinder(this, "test");
		initializer.initBinder(binder, new ServletWebRequest(new MockHttpServletRequest()));
		binder.bind(new MutablePropertyValues(Collections.singletonMap("name", "foo")));
		assertEquals("foo", name);
	}

	@Test
	public void testInitBinderFile() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerCustomEditor(MultipartFile.class, StringMultipartFileEditor.class);
		initializer.setBeanFactory(beanFactory);
		ServletRequestDataBinder binder = new ServletRequestDataBinder(this, "test");
		initializer.initBinder(binder, new ServletWebRequest(new MockHttpServletRequest()));
		binder.bind(new MutablePropertyValues(Collections.singletonMap("name", new MockMultipartFile("foo","bar".getBytes()))));
		assertEquals("bar", name);
	}

}
