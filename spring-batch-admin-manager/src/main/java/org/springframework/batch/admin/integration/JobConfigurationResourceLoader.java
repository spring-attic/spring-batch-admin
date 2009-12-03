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
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.ClassPathXmlApplicationContextFactory;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
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

	private static Log logger = LogFactory.getLog(JobConfigurationResourceLoader.class);

	private JobRegistry jobRegistry;

	private ApplicationContext parent;

	private Collection<ConfigurableApplicationContext> contexts = new HashSet<ConfigurableApplicationContext>();

	public void setJobRegistry(JobRegistry jobRegistry) {
		this.jobRegistry = jobRegistry;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.parent = applicationContext;
	}

	@ServiceActivator
	public String loadJobs(Resource resource) throws DuplicateJobException {

		Collection<String> result = new ArrayList<String>();

		ConfigurableApplicationContext context = createApplicationContext(parent, resource);

		contexts.add(context);
		String[] names = context.getBeanNamesForType(Job.class);
		for (String name : names) {
			logger.debug("Registering job: " + name + " from context: " + resource);
			JobFactory jobFactory = new ReferenceJobFactory((Job) context.getBean(name));
			// TODO: Deal with name changes in bean post processor
			if (!jobRegistry.getJobNames().contains(jobFactory.getJobName())) {
				jobRegistry.register(jobFactory);
			}
			result.add(jobFactory.getJobName());
		}

		return "Registered jobs: "+result;

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
	protected ConfigurableApplicationContext createApplicationContext(ApplicationContext parent, Resource resource) {
		ClassPathXmlApplicationContextFactory applicationContextFactory = new ClassPathXmlApplicationContextFactory() {
			@Override
			protected void prepareContext(ConfigurableApplicationContext parent, ConfigurableApplicationContext context) {

				super.prepareContext(parent, context);

				String[] names = context.getBeanNamesForType(Job.class, false, false);
				for (String name : names) {
					logger.debug("Unregistering job: " + name + " from context: " + context);
					jobRegistry.unregister(name);
				}

			}
		};
		applicationContextFactory.setPath(resource);
		if (parent != null) {
			applicationContextFactory.setApplicationContext(parent);
		}
		return applicationContextFactory.createApplicationContext();
	}

}
