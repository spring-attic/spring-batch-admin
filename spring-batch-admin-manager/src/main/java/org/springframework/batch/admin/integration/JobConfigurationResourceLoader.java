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

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.web.JobInfo;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.ClassPathXmlApplicationContextFactory;
import org.springframework.batch.core.configuration.support.JobLoader;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

/**
 * Load XML from a Spring {@link Resource} and create an
 * {@link ApplicationContext}, registering any {@link Job} instances defined in
 * a {@link JobRegistry}. If this component is itself configured with Spring,
 * then its own ApplicationContext will be the parent for the new one, and the
 * child will inherit AOP configuration, as well as property placeholder and
 * custom editor configuration from the parent.
 * 
 * @author Dave Syer
 * 
 */
@MessageEndpoint
public class JobConfigurationResourceLoader implements ApplicationContextAware {

	private JobLoader jobLoader;

	private JobService jobService;

	private ApplicationContext parent;

	public void setJobLoader(JobLoader jobLoader) {
		this.jobLoader = jobLoader;
	}
	
	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.parent = applicationContext;
	}

	@ServiceActivator
	public Collection<JobInfo> loadJobs(Resource resource) throws DuplicateJobException {

		Collection<JobInfo> result = new ArrayList<JobInfo>();

		ApplicationContextFactory factory = createApplicationContextFactory(parent, resource);
		Collection<Job> jobs = jobLoader.reload(factory);

		for (Job job : jobs) {
			String name = job.getName();
			int count = 0;
			try {
				count = jobService.countJobExecutionsForJob(name);
			}
			catch (NoSuchJobException e) {
				// shouldn't happen
			}
			boolean launchable = jobService.isLaunchable(name);
			boolean incrementable = jobService.isIncrementable(name);
			result.add(new JobInfo(name, count, null, launchable, incrementable));
		}

		return result;

	}

	/**
	 * Create an application context from the resource provided. Extension point
	 * for subclasses if they need to customize the context in any way. The
	 * default uses a {@link ClassPathXmlApplicationContextFactory}.
	 * 
	 * @param parent the parent application context (or null if there is none)
	 * @param resource the location of the XML configuration
	 * 
	 * @return an application context containing jobs
	 */
	protected ApplicationContextFactory createApplicationContextFactory(ApplicationContext parent, Resource resource) {
		ClassPathXmlApplicationContextFactory applicationContextFactory = new ClassPathXmlApplicationContextFactory();
		applicationContextFactory.setResource(resource);
		if (parent != null) {
			applicationContextFactory.setApplicationContext(parent);
		}
		return applicationContextFactory;
	}

}
