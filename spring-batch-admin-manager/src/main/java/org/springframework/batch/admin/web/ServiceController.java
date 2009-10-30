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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.admin.partition.remote.StepExecutionRequest;
import org.springframework.batch.admin.partition.remote.StepExecutionResponse;
import org.springframework.batch.admin.partition.remote.StepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * Simple "ping" test for the locally installed {@link StepService}.
 * 
 * @author Dave Syer
 * 
 */
@Controller
public class ServiceController {

	private Log logger = LogFactory.getLog(getClass());

	private final StepService remoteService;

	private final StepService localService;

	@Autowired
	public ServiceController(@Qualifier("stepService") StepService localService, @Qualifier("remoteStepService") StepService remoteService) {
		this.localService = localService;
		this.remoteService = remoteService;
	}

	@ModelAttribute("request")
	public StepExecutionRequest getRequestTemplate() {
		return new StepExecutionRequest(0L, -1L, "step1");
	}

	@RequestMapping(value = "/services/remote")
	public @ModelAttribute("response")
	StepExecutionResponse remote(@ModelAttribute("request") StepExecutionRequest request, Errors errors) throws Exception {
		try {
			return remoteService.execute(request);
		}
		catch (Exception e) {
			logger.debug("Could not contact remote service", e);
			errors.reject("remote.service.error", "Could not contact the remote service");
			return new StepExecutionResponse(0, false);
		}
	}

	@RequestMapping(value = "/services/local")
	public void local(Model model, @ModelAttribute("request") StepExecutionRequest request, Errors errors) throws Exception {
		try {
			model.addAttribute("serviceStatus", localService.getStatus());
		}
		catch (Exception e) {
			logger.debug("Could not contact local service", e);
			errors.reject("remote.service.error", "Could not contact the local service");
		}
	}

	@RequestMapping(value = "/services")
	public void home() throws Exception {
	}

}
