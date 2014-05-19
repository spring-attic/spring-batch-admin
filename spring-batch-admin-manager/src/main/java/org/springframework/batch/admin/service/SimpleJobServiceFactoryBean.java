/*
 * Copyright 2009-2013 the original author or authors.
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

import static org.springframework.batch.support.DatabaseType.SYBASE;

import java.sql.Types;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.*;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JdbcExecutionContextDao;
import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.core.repository.dao.JdbcStepExecutionDao;
import org.springframework.batch.core.repository.dao.XStreamExecutionContextStringSerializer;
import org.springframework.batch.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A factory for a {@link JobService} that makes the configuration of its
 * various ingredients as convenient as possible.
 * 
 * @author Dave Syer
 * 
 */
public class SimpleJobServiceFactoryBean implements FactoryBean<JobService>, InitializingBean {
	private static final Log logger = LogFactory.getLog(SimpleJobServiceFactoryBean.class);

	private DataSource dataSource;

	private JdbcOperations jdbcTemplate;

	private String databaseType;

	private String tablePrefix = AbstractJdbcBatchMetadataDao.DEFAULT_TABLE_PREFIX;

	private DataFieldMaxValueIncrementerFactory incrementerFactory;

	private int maxVarCharLength = AbstractJdbcBatchMetadataDao.DEFAULT_EXIT_MESSAGE_LENGTH;

	private LobHandler lobHandler;

	private JobRepository jobRepository;

	private JobLauncher jobLauncher;

	private ListableJobLocator jobLocator;

	private ExecutionContextSerializer serializer;

	/**
	 * A special handler for large objects. The default is usually fine, except
	 * for some (usually older) versions of Oracle. The default is determined
	 * from the data base type.
	 * 
	 * @param lobHandler the {@link LobHandler} to set
	 * 
	 * @see LobHandler
	 */
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	/**
	 * Public setter for the length of long string columns in database. Do not
	 * set this if you haven't modified the schema. Note this value will be used
	 * for the exit message in both {@link JdbcJobExecutionDao} and
	 * {@link JdbcStepExecutionDao} and also the short version of the execution
	 * context in {@link JdbcExecutionContextDao} . For databases with
	 * multi-byte character sets this number can be smaller (by up to a factor
	 * of 2 for 2-byte characters) than the declaration of the column length in
	 * the DDL for the tables.
	 * 
	 * @param maxVarCharLength the exitMessageLength to set
	 */
	public void setMaxVarCharLength(int maxVarCharLength) {
		this.maxVarCharLength = maxVarCharLength;
	}

	/**
	 * Public setter for the {@link DataSource}.
	 * @param dataSource a {@link DataSource}
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Sets the database type.
	 * @param dbType as specified by
	 * {@link DefaultDataFieldMaxValueIncrementerFactory}
	 */
	public void setDatabaseType(String dbType) {
		this.databaseType = dbType;
	}

	/**
	 * Sets the table prefix for all the batch meta-data tables.
	 * @param tablePrefix
	 */
	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	/**
	 * A factory for incrementers (used to build primary keys for meta data).
	 * Defaults to {@link DefaultDataFieldMaxValueIncrementerFactory}.
	 * @param incrementerFactory the incrementer factory to set
	 */
	public void setIncrementerFactory(DataFieldMaxValueIncrementerFactory incrementerFactory) {
		this.incrementerFactory = incrementerFactory;
	}

	/**
	 * The repository used to store and update jobs and step executions.
	 * 
	 * @param jobRepository the {@link JobRepository} to set
	 */
	public void setJobRepository(JobRepository jobRepository) {
		this.jobRepository = jobRepository;
	}

	/**
	 * The launcher used to run jobs.
	 * @param jobLauncher a {@link JobLauncher}
	 */
	public void setJobLauncher(JobLauncher jobLauncher) {
		this.jobLauncher = jobLauncher;
	}

	/**
	 * A registry that can be used to locate jobs to run.
	 * @param jobLocator a {@link JobLocator}
	 */
	public void setJobLocator(ListableJobLocator jobLocator) {
		this.jobLocator = jobLocator;
	}


	/**
	 * A custom implementation of the {@link ExecutionContextSerializer}. The
	 * default, if not injected, is the
	 * {@link XStreamExecutionContextStringSerializer}.
	 * 
	 * @param serializer
	 *            the serializer to set
	 * @see ExecutionContextSerializer
	 */
	public void setSerializer(ExecutionContextSerializer serializer) {
		this.serializer = serializer;
	}

	public void afterPropertiesSet() throws Exception {

		Assert.notNull(dataSource, "DataSource must not be null.");
		Assert.notNull(jobRepository, "JobRepository must not be null.");
		Assert.notNull(jobLocator, "JobLocator must not be null.");
		Assert.notNull(jobLauncher, "JobLauncher must not be null.");

		jdbcTemplate = new JdbcTemplate(dataSource);

		if (incrementerFactory == null) {
			incrementerFactory = new DefaultDataFieldMaxValueIncrementerFactory(dataSource);
		}

		if (databaseType == null) {
			databaseType = DatabaseType.fromMetaData(dataSource).name();
			logger.info("No database type set, using meta data indicating: " + databaseType);
		}

		if (lobHandler == null) {
			lobHandler = new DefaultLobHandler();
		}


		if (serializer == null) {
			XStreamExecutionContextStringSerializer defaultSerializer = new XStreamExecutionContextStringSerializer();
			defaultSerializer.afterPropertiesSet();

			serializer = defaultSerializer;
		}

		Assert.isTrue(incrementerFactory.isSupportedIncrementerType(databaseType), "'" + databaseType
				+ "' is an unsupported database type.  The supported database types are "
				+ StringUtils.arrayToCommaDelimitedString(incrementerFactory.getSupportedIncrementerTypes()));

	}

	protected SearchableJobInstanceDao createJobInstanceDao() throws Exception {
		JdbcSearchableJobInstanceDao dao = new JdbcSearchableJobInstanceDao();
		dao.setJdbcTemplate(jdbcTemplate);
		dao.setJobIncrementer(incrementerFactory.getIncrementer(databaseType, tablePrefix + "JOB_SEQ"));
		dao.setTablePrefix(tablePrefix);
		dao.afterPropertiesSet();
		return dao;
	}

	protected SearchableJobExecutionDao createJobExecutionDao() throws Exception {
		JdbcSearchableJobExecutionDao dao = new JdbcSearchableJobExecutionDao();
		dao.setDataSource(dataSource);
		dao.setJobExecutionIncrementer(incrementerFactory.getIncrementer(databaseType, tablePrefix
				+ "JOB_EXECUTION_SEQ"));
		dao.setTablePrefix(tablePrefix);
		dao.setClobTypeToUse(determineClobTypeToUse(this.databaseType));
		dao.setExitMessageLength(maxVarCharLength);
		dao.afterPropertiesSet();
		return dao;
	}

	protected SearchableStepExecutionDao createStepExecutionDao() throws Exception {
		JdbcSearchableStepExecutionDao dao = new JdbcSearchableStepExecutionDao();
		dao.setDataSource(dataSource);
		dao.setStepExecutionIncrementer(incrementerFactory.getIncrementer(databaseType, tablePrefix
				+ "STEP_EXECUTION_SEQ"));
		dao.setTablePrefix(tablePrefix);
		dao.setClobTypeToUse(determineClobTypeToUse(this.databaseType));
		dao.setExitMessageLength(maxVarCharLength);
		dao.afterPropertiesSet();
		return dao;
	}

	protected ExecutionContextDao createExecutionContextDao() throws Exception {
		JdbcExecutionContextDao dao = new JdbcExecutionContextDao();
		dao.setJdbcTemplate(jdbcTemplate);
		dao.setTablePrefix(tablePrefix);
        dao.setSerializer(serializer);
		dao.setClobTypeToUse(determineClobTypeToUse(this.databaseType));
		if (lobHandler != null) {
			dao.setLobHandler(lobHandler);
		}
		dao.setSerializer(serializer);
		dao.afterPropertiesSet();
		// Assume the same length.
		dao.setShortContextLength(maxVarCharLength);
		return dao;
	}

	private int determineClobTypeToUse(String databaseType) {
		if (SYBASE == DatabaseType.valueOf(databaseType.toUpperCase())) {
			return Types.LONGVARCHAR;
		}
		else {
			return Types.CLOB;
		}
	}

	/**
	 * Create a {@link SimpleJobService} from the configuration provided.
	 * 
	 * @see FactoryBean#getObject()
	 */
	public JobService getObject() throws Exception {
		return new SimpleJobService(createJobInstanceDao(), createJobExecutionDao(), createStepExecutionDao(),
				jobRepository, jobLauncher, jobLocator, createExecutionContextDao());
	}

	/**
	 * Tells the containing bean factory what kind of object is the product of
	 * {@link #getObject()}.
	 * 
	 * @return SimpleJobService
	 * @see FactoryBean#getObjectType()
	 */
	public Class<? extends JobService> getObjectType() {
		return SimpleJobService.class;
	}

	/**
	 * Allows optimisation in the containing bean factory.
	 * 
	 * @return true
	 * @see FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

}
