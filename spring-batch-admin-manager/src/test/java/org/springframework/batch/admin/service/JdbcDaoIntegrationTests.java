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

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.batch.admin.service.ActiveProfileSuite.ActiveProfile;
import org.springframework.batch.admin.service.JdbcDaoIntegrationTests.H2Test;
import org.springframework.batch.admin.service.JdbcDaoIntegrationTests.HsqlTest;

/**
 * @author Dave Syer
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ HsqlTest.class, H2Test.class })
public class JdbcDaoIntegrationTests {

	@RunWith(ActiveProfileSuite.class)
	@SuiteClasses({ JdbcSearchableJobInstanceDaoTests.class, JdbcSearchableJobExecutionDaoTests.class,
			JdbcSearchableStepExecutionDaoTests.class })
	public static abstract class BaseTest {
	}

	@ActiveProfile("hsql")
	@Ignore
	public static class HsqlTest extends BaseTest {
	}

	@ActiveProfile("derby")
	@Ignore
	public static class DerbyTest extends BaseTest {
	}

	@ActiveProfile("h2")
	@Ignore
	public static class H2Test extends BaseTest {
	}

	@ActiveProfile("mysql")
	@Ignore
	public static class MysqlTest extends BaseTest {
	}

}
