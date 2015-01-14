/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.batch.admin.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * @author Michael Minella
 */
public class CompositeApplicationContextFactory implements FactoryBean<ApplicationContextFactory[]>, InitializingBean {

	private List<FactoryBean<ApplicationContextFactory[]>> factoryBeans;

	private List<ApplicationContextFactory> delegateFactories;

	public void setFactoryBeans(List<FactoryBean<ApplicationContextFactory[]>> factoryBeans) {
		this.factoryBeans = factoryBeans;
	}

	public void setFactories(List<ApplicationContextFactory> delegateFactories) {
		this.delegateFactories = delegateFactories;
	}

	@Override
	public ApplicationContextFactory[] getObject() throws Exception {
		List<ApplicationContextFactory> factories = new ArrayList<ApplicationContextFactory>();

		if(factoryBeans != null) {
			for (FactoryBean<ApplicationContextFactory[]> factory : factoryBeans) {
				factories.addAll(Arrays.asList(factory.getObject()));
			}
		}

		if(delegateFactories != null) {
			factories.addAll(delegateFactories);
		}

		return factories.toArray(new ApplicationContextFactory[factories.size()]);
	}

	@Override
	public Class<?> getObjectType() {
		return ApplicationContextFactory[].class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.state(!CollectionUtils.isEmpty(factoryBeans) || !CollectionUtils.isEmpty(delegateFactories),
				"A factory or factoryBean is required");
	}
}
