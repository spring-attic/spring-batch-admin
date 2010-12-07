/*
 * Copyright 2006-2010 the original author or authors.
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

import org.junit.Test;
import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 *
 */
public class JobParametersExtractorTests {
	
	private JobParametersExtractor extractor = new JobParametersExtractor();

	@Test
	public void testJobWithLongParameters() throws Exception {

		String jobParameters = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,ccccccccccccccccccccccccccccccccccccccccccccccccc=dddddddddddddddddddddddddddddddddddddddd";
		String output = extractor.fromJobParameters(extractor.fromString(jobParameters));
		output = output.replace("\n", ",");
		output = output.replace("\r", "");
		if (output.endsWith(",")) {
			output = output.substring(0, output.lastIndexOf(","));
		}
		assertEquals(jobParameters, output);

	}

	@Test
	public void testJobWithEscapedLongParameters() throws Exception {

		String jobParameters = "a=http://one,b=ftp://two";
		String output = extractor.fromJobParameters(extractor.fromString(jobParameters));
		assertEquals(StringUtils.commaDelimitedListToSet(jobParameters), StringUtils.commaDelimitedListToSet(output));

	}

}
