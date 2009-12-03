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
package org.springframework.batch.admin.integration;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

/**
 * Service endpoint to remove duplicate job executions. Only executions whose id
 * and status have not been seen before will be passed on to the caller.
 * Duplicate executions might arrive if multiple wire taps are placed in a job
 * launch flow, so as a courtesy to the operator we try to remove them. In
 * practice old values are stored in memory using weak references, so duplicates
 * are only detected if the value is seen again before it is garbage collected.
 * 
 * @author Dave Syer
 * 
 */
@MessageEndpoint
public class JobExecutionDuplicateFilter {

	private static Log logger = LogFactory
			.getLog(JobExecutionDuplicateFilter.class);

	private Map<Key, JobExecution> executions = new WeakHashMap<Key, JobExecution>();

	/**
	 * Filter out previously seen executions.
	 * 
	 * @param jobExecution
	 *            a {@link JobExecution}
	 * @return the same job execution or null if it has been seen before
	 */
	@ServiceActivator
	public JobExecution filter(JobExecution jobExecution) {

		Key key = new Key(jobExecution);
		if (executions.containsKey(key)) {
			logger.debug("Already processed so nothing to do " + jobExecution);
			return null;
		}
		logger.debug("Forwarding " + jobExecution);
		executions.put(key, jobExecution);
		return (JobExecution) SerializationUtils.deserialize(SerializationUtils
				.serialize(jobExecution));

	}

	private static class Key {

		private Long id;
		private BatchStatus status;

		public Key(JobExecution jobExecution) {
			id = jobExecution.getId();
			status = jobExecution.getStatus();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result
					+ ((status == null) ? 0 : status.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (status == null) {
				if (other.status != null)
					return false;
			} else if (!status.equals(other.status))
				return false;
			return true;
		}

	}

}
