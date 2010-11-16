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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.integration.http.converter.MultipartAwareFormHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

public class BindingHttpMessageConverter<T> implements HttpMessageConverter<T>, BeanFactoryAware {

	private AbstractBeanFactory beanFactory;

	private MultipartAwareFormHttpMessageConverter delegate = new MultipartAwareFormHttpMessageConverter();

	private Class<T> targetType;

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (AbstractBeanFactory) beanFactory;
	}

	/**
	 * @param targetType the tergetType to set
	 */
	public void setTargetType(Class<T> targetType) {
		this.targetType = targetType;
	}

	public List<MediaType> getSupportedMediaTypes() {
		return Arrays.asList(MediaType.TEXT_PLAIN);
	}

	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return clazz.isAssignableFrom(targetType);
	}

	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return false;
	}

	public T read(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException {
		MultiValueMap<String, ?> map = delegate.read(null, inputMessage);
		BeanWrapperImpl beanWrapper = new BeanWrapperImpl(targetType);
		initBeanWrapper(beanWrapper);
		Map<String, Object> props = new HashMap<String, Object>();
		for (String key : map.keySet()) {
			if (beanWrapper.isWritableProperty(key)) {
				List<?> list = map.get(key);
				props.put(key, map.get(key).size()>1 ? list : map.getFirst(key));
			}
		}
		beanWrapper.setPropertyValues(props);
		@SuppressWarnings("unchecked")
		T result = (T) beanWrapper.getWrappedInstance();
		return result;
	}

	public void write(T input, MediaType contentType, HttpOutputMessage outputMessage) throws IOException,
			HttpMessageNotWritableException {
		throw new UnsupportedOperationException();
	}

	private void initBeanWrapper(BeanWrapperImpl beanWrapper) {

		GenericConversionService conversionService = new GenericConversionService();
		conversionService.addConverter(new MultipartFileConverter(beanWrapper));

		beanWrapper.setConversionService(conversionService);
		if (beanFactory != null) {
			beanFactory.copyRegisteredEditorsTo(beanWrapper);
		}

	}

	private static class MultipartFileConverter implements Converter<MultipartFile, String> {

		private final PropertyEditorRegistry accessor;

		public MultipartFileConverter(PropertyEditorRegistry accessor) {
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
