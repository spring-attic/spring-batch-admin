/*
 * Copyright 2006-2011 the original author or authors.
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

package org.springframework.batch.admin.jmx;

import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * @author Dave Syer
 * 
 */
public class IntegrationAndBatchMBeanExporterIntegrationTests {

	@Test
	public void testMBeanExporters() throws Exception {
		String base = "/META-INF/spring/batch/";
		String bootstrap = base + "bootstrap/";
		GenericXmlApplicationContext context = new GenericXmlApplicationContext( //
				bootstrap + "manager/execution-context.xml", //
				bootstrap + "manager/jmx-context.xml", //
				bootstrap + "manager/env-context.xml", //
				bootstrap + "manager/data-source-context.xml", //
				bootstrap + "integration/jmx-context.xml" //
		);

		context.close();
	}

}
