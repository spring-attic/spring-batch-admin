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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.batch.admin.partition.remote.StepExecutionRequest;
import org.springframework.batch.admin.partition.remote.StepExecutionResponse;
import org.springframework.batch.admin.partition.remote.StepService;
import org.springframework.batch.admin.partition.remote.StepServiceStatus;
import org.springframework.batch.admin.web.ServiceController;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.MapBindingResult;


public class ServiceControllerTests {

	private StepService stepService = EasyMock.createMock(StepService.class);

	private ServiceController controller = new ServiceController(stepService, stepService);

	@Test
	public void testHome() throws Exception {
		controller.home();
	}

	@Test
	public void testPing() throws Exception {

		StepExecutionRequest request = controller.getRequestTemplate();
		stepService.execute(request);
		EasyMock.expectLastCall().andReturn(new StepExecutionResponse(0, true));
		EasyMock.replay(stepService);

		StepExecutionResponse result = controller
				.remote(request, new MapBindingResult(new ExtendedModelMap(), "request"));
		assertNotNull(result);
		assertTrue(result.isRejected());

		EasyMock.verify(stepService);

	}

	@Test
	public void testStatus() throws Exception {

		stepService.getStatus();
		EasyMock.expectLastCall().andReturn(new StepServiceStatus(1));
		EasyMock.replay(stepService);

		ExtendedModelMap model = new ExtendedModelMap();
		controller.local(model, new StepExecutionRequest(), new MapBindingResult(model, "*"));
		assertEquals(1, model.size());
		assertTrue(model.containsKey("serviceStatus"));

		EasyMock.verify(stepService);

	}

}
