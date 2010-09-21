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
package org.springframework.batch.admin.web.views;

import java.util.Map;

import org.codehaus.jackson.map.MappingJsonFactory;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class JsonWrapper {

	private final SpelExpressionParser parser;

	private final StandardEvaluationContext context;

	private final Map<String, Object> target;

	@SuppressWarnings("unchecked")
	public JsonWrapper(String content) throws Exception {
		target = new MappingJsonFactory().createJsonParser(content).readValueAs(Map.class);
		context = new StandardEvaluationContext();
		context.addPropertyAccessor(new MapAccessor());
		parser = new SpelExpressionParser();
	}

	public Object get(String expression) throws Exception {
		return get(expression, Object.class);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String expression, Class<T> type) throws Exception {
		return (T) parser.parseExpression(expression).getValue(context, target);
	}
	
	@Override
	public String toString() {
		return target.toString();
	}

}