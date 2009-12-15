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
package org.springframework.batch.admin.service;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

/**
 * @author Dave Syer
 *
 */
public class FileInfoTests {

	@Test
	public void testFileInfoFileFileFile() {
		File dir = new File(System.getProperty("java.io.tmpdir"));
		FileInfo info = new FileInfo(dir, dir, new File(dir, "foo"));
		assertEquals(dir, new File(info.getOutputPath()));
		assertEquals(dir, new File(info.getTriggerPath()));
		assertEquals(new File("foo"), new File(info.getPath()));
		assertEquals("", info.getLocator());
	}

	@Test
	public void testIsTrigger() {
		File dir = new File(System.getProperty("java.io.tmpdir"));
		FileInfo info = new FileInfo(dir, dir, new File(dir, "foo"));
		assertEquals(false, info.isTrigger());
	}

	@Test
	public void testCompareTo() {
		File dir = new File(System.getProperty("java.io.tmpdir"));
		FileInfo info1 = new FileInfo(dir, dir, new File(dir, "foo"));
		FileInfo info2 = new FileInfo(dir, dir, new File(dir, "bar"));
		assertEquals(4, info1.compareTo(info2));
	}

}
