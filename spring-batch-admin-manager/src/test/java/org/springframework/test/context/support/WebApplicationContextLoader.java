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
package org.springframework.test.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class WebApplicationContextLoader extends AbstractContextLoader {

	protected static final Log logger = LogFactory.getLog(WebApplicationContextLoader.class);

	/**
	 * <p>
	 * Creates a new {@link XmlBeanDefinitionReader}.
	 * </p>
	 * 
	 * @return a new XmlBeanDefinitionReader.
	 * @see AbstractGenericContextLoader#createBeanDefinitionReader(GenericApplicationContext)
	 * @see XmlBeanDefinitionReader
	 */
	protected BeanDefinitionReader createBeanDefinitionReader(final GenericApplicationContext context) {
		return new XmlBeanDefinitionReader(context);
	}

	/**
	 * Returns &quot;<code>-context.xml</code>&quot;.
	 * 
	 * @see org.springframework.test.context.support.AbstractContextLoader#getResourceSuffix()
	 */
	@Override
	public String getResourceSuffix() {
		return "-context.xml";
	}

	public ApplicationContext loadContext(String... locations) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Loading ApplicationContext for locations ["
					+ StringUtils.arrayToCommaDelimitedString(locations) + "].");
		}
		GenericWebApplicationContext context = new GenericWebApplicationContext();
		prepareContext(context);
		customizeBeanFactory(context.getDefaultListableBeanFactory());
		createBeanDefinitionReader(context).loadBeanDefinitions(locations);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		customizeContext(context);
		context.refresh();
		context.registerShutdownHook();
		return context;
	}

	protected void customizeContext(GenericWebApplicationContext context) {
	}

	protected void customizeBeanFactory(DefaultListableBeanFactory defaultListableBeanFactory) {
	}

	private void prepareContext(GenericWebApplicationContext context) {
		MockServletContext servletContext = new MockServletContext();
		context.setServletContext(servletContext);
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
	}

}
