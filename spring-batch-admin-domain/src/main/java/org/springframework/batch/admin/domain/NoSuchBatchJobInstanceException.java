/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.admin.domain;

/**
 * Exception thrown when there is no such batch job instance with the given instanceId.
 * 
 * @author Ilayaperumal Gopinathan
 * @since 2.0
 */
@SuppressWarnings("serial")
public class NoSuchBatchJobInstanceException extends RuntimeException {

	public NoSuchBatchJobInstanceException(long instanceId) {
		super("Batch Job instance with the id " + instanceId + " doesn't exist");
	}
}
