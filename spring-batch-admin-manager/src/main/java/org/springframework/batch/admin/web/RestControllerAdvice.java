/*
 * Copyright 2013-2014 the original author or authors.
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

import org.springframework.batch.admin.domain.NoSuchBatchJobException;
import org.springframework.batch.admin.domain.NoSuchBatchJobInstanceException;
import org.springframework.batch.admin.service.NoSuchStepExecutionException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Central class for behavior common to all REST controllers.
 *
 * @author Eric Bottard
 * @author Gunnar Hillert
 * @author Ilayaperumal Gopinathan
 * @author Michael Minella
 * @since 2.0
 */
@ControllerAdvice
public class RestControllerAdvice {

	private final Log logger = LogFactory.getLog(this.getClass());

	/*
	 * Note that any controller-specific exception handler is resolved first. So for example, having a
	 * onException(Exception e) resolver at a controller level will prevent the one from this class to be triggered.
	 */

	/**
	 * Handles the case where client submitted an ill valued request (missing parameter).
	 *
	 * @param e exception to be handled
	 *
	 * @return VndErrors see {@link VndErrors}
	 */
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public VndErrors onMissingServletRequestParameterException(MissingServletRequestParameterException e) {
		String logref = logDebug(e);
		return new VndErrors(logref, e.getMessage());
	}

	/**
	 * Handles the general error case. Report server-side error.
	 *
	 * @param e the exception to be handled
	 *
	 * @return VndErrors see {@link VndErrors}
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public VndErrors onException(Exception e) {
		String logref = logError(e);
		String msg = StringUtils.hasText(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
		return new VndErrors(logref, msg);
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public VndErrors onNoSuchJobExecutionException(NoSuchJobExecutionException e) {
		String logref = logDebug(e);
		return new VndErrors(logref, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public VndErrors onJobExecutionNotRunningException(JobExecutionNotRunningException e) {
		String logref = logDebug(e);
		return new VndErrors(logref, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public VndErrors onNoSuchStepExecutionException(NoSuchStepExecutionException e) {
		String logref = logDebug(e);
		return new VndErrors(logref, e.getMessage());
	}

	private String logDebug(Throwable t) {
		logger.debug("Caught exception while handling a request", t);
		// TODO: use a more semantically correct VndError 'logref'
		return t.getClass().getSimpleName();
	}

	private String logError(Throwable t) {
		logger.error("Caught exception while handling a request", t);
		// TODO: use a more semantically correct VndError 'logref'
		return t.getClass().getSimpleName();
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public VndErrors onJobExecutionAlreadyRunningException(JobExecutionAlreadyRunningException e) {
		String logref = logDebug(e);
		return new VndErrors(logref, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public VndErrors onJobRestartException(JobRestartException e) {
		String logref = logDebug(e);
		return new VndErrors(logref, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public VndErrors onJobInstanceAlreadyCompleteException(JobInstanceAlreadyCompleteException e) {
		String logref = logDebug(e);
		return new VndErrors(logref, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public VndErrors onNoSuchJobException(NoSuchBatchJobException e) {
		String logref = logDebug(e);
		return new VndErrors(logref, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public VndErrors onNoSuchJobInstanceException(NoSuchBatchJobInstanceException e) {
		String logref = logDebug(e);
		return new VndErrors(logref, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public VndErrors onJobParametersInvalidException(JobParametersInvalidException e) {
		String logref = logDebug(e);
		return new VndErrors(logref, e.getMessage());
	}
}
