/*
 * Copyright 2006-2010 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * A JUnit test runner that can be used to set an environment variable for the
 * duration of a suite of tests.
 * 
 * @author Dave Syer
 * 
 */
public class ActiveProfileSuite extends Suite {

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ActiveProfile {
		String value();
	}

	public ActiveProfileSuite(Class<?> klass) throws InitializationError {
		super(klass, new AllDefaultPossibilitiesBuilder(true));
	}

	@Override
	protected Statement classBlock(RunNotifier notifier) {
		String environment = null;
		for (Annotation annotation : getTestClass().getAnnotations()) {
			if (annotation instanceof ActiveProfileSuite.ActiveProfile) {
				environment = ((ActiveProfileSuite.ActiveProfile) annotation).value();
			}
		}
		Statement statement = super.classBlock(notifier);
		if (environment != null) {
			final Statement inner = statement;
			final String value = environment;
			statement = new Statement() {
				public void evaluate() throws Throwable {

					String original = System.getProperty("ENVIRONMENT");
					try {
						System.setProperty("ENVIRONMENT", value);
						inner.evaluate();
					}
					finally {
						if (original != null) {
							System.setProperty("ENVIRONMENT", original);
						}
						else {
							System.clearProperty("ENVIRONMENT");
						}
					}
				}
			};
		}
		return statement;
	}
}