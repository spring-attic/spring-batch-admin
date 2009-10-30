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
package org.springframework.batch.admin.util;

import static org.junit.Assert.*;

import org.junit.Test;


public class RegExpTests {
	
	private static final String NAME = "[\\w\\.-_]+";

	@Test
	public void testPropertiesPattern() throws Exception {
		String regex = NAME+"=.*";
		assertTrue("foo=bar".matches(regex));
		assertTrue("foo.bar=spam".matches(regex));
		assertFalse("foo".matches(regex));
	}

	@Test
	public void testMultiPropertiesPattern() throws Exception {
		String regex = "("+NAME+"=.*[,\\n])*("+NAME+"=.*)";
		assertTrue("foo=bar".matches(regex));
		assertTrue("foo=bar,spam=baz".matches(regex));
	}

}
