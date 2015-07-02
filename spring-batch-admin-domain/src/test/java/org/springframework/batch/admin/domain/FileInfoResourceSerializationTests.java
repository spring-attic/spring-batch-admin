/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.batch.admin.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.test.util.JsonPathExpectationsHelper;

/**
 * @author Michael Minella
 */
public class FileInfoResourceSerializationTests extends AbstractSerializationTests<FileInfoResource> {

	private String timestamp = formatter.format(new Date());

	private static DateFormat formatter = new SimpleDateFormat("");

	@Override
	public void assertJson(String json) throws Exception {
		new JsonPathExpectationsHelper("$.timestamp").assertValue(json, timestamp);
		new JsonPathExpectationsHelper("$.path").assertValue(json, "file://foo/bar/baz.txt");
		new JsonPathExpectationsHelper("$.shortPath").assertValue(json, "foo/bar/baz.txt");
		new JsonPathExpectationsHelper("$.local").assertValue(json, true);
	}

	@Override
	public void assertObject(FileInfoResource fileInfoResource) throws Exception {
		assertEquals(timestamp, fileInfoResource.getTimestamp());
		assertEquals("file://foo/bar/baz.txt", fileInfoResource.getPath());
		assertEquals("foo/bar/baz.txt", fileInfoResource.getShortPath());
		assertTrue(fileInfoResource.isLocal());
	}

	@Override
	public FileInfoResource getSerializationValue() {
		FileInfoResource resource =
				new FileInfoResource(timestamp,
						"file://foo/bar/baz.txt",
						"foo/bar/baz.txt",
						true,
						0);
		return resource;
	}
}
