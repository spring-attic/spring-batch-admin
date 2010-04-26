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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * @author Dave Syer
 * 
 */
public class FileInfoTests {

	@Test
	public void testFileInfoFromFile() {
		FileInfo info = new FileInfo("foo");
		assertEquals("foo", info.getPath());
		assertNotNull(info.getTimestamp());
	}

	@Test
	public void testFileInfoWithTimestamp() {
		FileInfo info = new FileInfo("foo.20100101.123000").shortPath();
		assertEquals("foo", info.getPath());
		assertEquals("foo.20100101.123000", info.getFileName());
		assertEquals("20100101.123000", info.getTimestamp());
	}

	@Test
	public void testFileInfoWithTimestampAndExtension() {
		FileInfo info = new FileInfo("foo.20100101.123000.txt").shortPath();
		assertEquals("foo.txt", info.getPath());
		assertEquals("foo.20100101.123000.txt", info.getFileName());
		assertEquals("20100101.123000", info.getTimestamp());
	}

	@Test
	public void testPattern() {
		FileInfo info = new FileInfo("foo.20100101.123000");
		assertEquals("foo.20100101.123000", info.getPattern());
	}

	@Test
	public void testPatternWithExtension() {
		FileInfo info = new FileInfo("foo.20100101.123000.txt");
		assertEquals("foo.20100101.123000.txt", info.getPattern());
	}

	@Test
	public void testPatternForAll() {
		FileInfo info = new FileInfo("foo");
		assertEquals("foo.*.*", info.getPattern());
	}

	@Test
	public void testPatternForAllWithExtesion() {
		FileInfo info = new FileInfo("foo.txt");
		assertEquals("foo.*.*.txt", info.getPattern());
	}

	@Test
	public void testCompareTo() {
		FileInfo info1 = new FileInfo("foo");
		FileInfo info2 = new FileInfo("bar");
		assertEquals(4, info1.compareTo(info2));
	}

	@Test
	public void testCompareToWithTimestamp() throws Exception {
		FileInfo info1 = new FileInfo("foo", "19990101.124500", false);
		FileInfo info2 = new FileInfo("foo", "19990101.124400", false);
		assertEquals(-1, info1.compareTo(info2));

		ArrayList<FileInfo> list = new ArrayList<FileInfo>(Arrays.asList(info1, info2));
		Collections.sort(list);
		assertEquals(
				"[FileInfo [path=foo, timestamp=19990101.124500, local=false], FileInfo [path=foo, timestamp=19990101.124400, local=false]]",
				list.toString());
	}

}
