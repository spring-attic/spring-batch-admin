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

import java.util.Collection;
import java.util.HashSet;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.core.step.StepLocator;

/**
 * @author Dave Syer
 * 
 */
public class JobLocatorStepLocator implements StepLocator {

	private ListableJobLocator jobLocator;

	/**
	 * Create an instance from this {@link JobLocator}.
	 * 
	 * @param jobLocator a {@link JobLocator}
	 */
	public JobLocatorStepLocator(ListableJobLocator jobLocator) {
		super();
		this.jobLocator = jobLocator;
	}

	/**
	 * Convenience constructor for declarative configuration.
	 */
	public JobLocatorStepLocator() {
		super();
	}

	/**
	 * @param jobLocator the jobLocator to set
	 */
	public void setJobLocator(ListableJobLocator jobLocator) {
		this.jobLocator = jobLocator;
	}

	/**
	 * Locate a step by referencing it through its parent job with a separator,
	 * e.g. <code>job-name/step-name</code>. The separator defaults to a forward
	 * slash.
	 * 
	 * @see StepLocator#getStep(String)
	 */
	public Step getStep(String path) throws NoSuchStepException {
		String jobName = path.substring(0, path.indexOf("/"));
		String stepName = path.substring(jobName.length() + 1);
		Job job;
		try {
			job = jobLocator.getJob(jobName);
		}
		catch (NoSuchJobException e) {
			throw new NoSuchStepException("No step could be located because no job was found with name=" + jobName);
		}
		String prefix = jobName+".";
		if (job instanceof StepLocator) {
			if (((StepLocator) job).getStepNames().contains(stepName)) {
				return ((StepLocator) job).getStep(stepName);
			}
			// TODO: remove this workaround for BATCH-1507
			if (((StepLocator) job).getStepNames().contains(prefix + stepName)) {
				return ((StepLocator) job).getStep(prefix + stepName);
			}
			throw new NoSuchStepException("No step could be located: "+path);
		}
		throw new NoSuchStepException("No step could be located because the job was not a StepLocator.");
	}

	/**
	 * Loop through all the jobs and pull out their step names. The result is in
	 * the form that would be appropriate for {@link #getStep(String)} (i.e.
	 * with a separator).
	 * 
	 * @see StepLocator#getStepNames()
	 */
	public Collection<String> getStepNames() {
		Collection<String> result = new HashSet<String>();
		for (String jobName : jobLocator.getJobNames()) {
			Job job;
			try {
				job = jobLocator.getJob(jobName);
			}
			catch (NoSuchJobException e) {
				throw new IllegalStateException("Job not found although it was listed with name=" + jobName);
			}
			String prefix = jobName + ".";
			if (job instanceof StepLocator) {
				for (String stepName : ((StepLocator) job).getStepNames()) {
					if (stepName.startsWith(prefix)) {
						stepName = stepName.substring(prefix.length());
					}
					result.add(jobName + "/" + stepName);
				}
			}
		}
		return result;
	}

}
