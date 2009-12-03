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

import java.beans.PropertyEditor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

public class CustomWebBindingInitializer extends ConfigurableWebBindingInitializer implements BeanFactoryAware {

	private AbstractBeanFactory beanFactory;

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (AbstractBeanFactory) beanFactory;
	}

	@Override
	public void initBinder(WebDataBinder binder, WebRequest request) {

		GenericConversionService conversionService = new GenericConversionService();
		conversionService.addConverter(new MultipartFileConverter(binder));

		binder.setConversionService(conversionService);
		if (beanFactory != null) {
			beanFactory.copyRegisteredEditorsTo(binder);
		}

		super.initBinder(binder, request);

	}

	private static class MultipartFileConverter implements Converter<MultipartFile, String> {

		private final DataBinder accessor;

		public MultipartFileConverter(DataBinder accessor) {
			this.accessor = accessor;
		}

		public String convert(MultipartFile source) {

			PropertyEditor editor = accessor.findCustomEditor(MultipartFile.class, null);
			if (editor == null) {
				throw new IllegalStateException("Cannot convert source of type " + source.getClass()
						+ " to type: String");
			}
			editor.setValue(source);
			return editor.getAsText();
		}

	}

}
