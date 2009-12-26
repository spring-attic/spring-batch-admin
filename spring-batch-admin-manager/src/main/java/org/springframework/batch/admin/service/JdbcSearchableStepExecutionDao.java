/*
 * Copyright 2006-2007 the original author or authors.
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.core.repository.dao.JdbcStepExecutionDao;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.support.PatternMatcher;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.incrementer.AbstractDataFieldMaxValueIncrementer;
import org.springframework.util.Assert;

/**
 * @author Dave Syer
 * 
 */
public class JdbcSearchableStepExecutionDao extends JdbcStepExecutionDao
		implements SearchableStepExecutionDao {

	private static final String STEP_EXECUTIONS_FOR_JOB = "SELECT distinct STEP_NAME from %PREFIX%STEP_EXECUTION S, %PREFIX%JOB_EXECUTION E, %PREFIX%JOB_INSTANCE I "
			+ "where S.JOB_EXECUTION_ID = E.JOB_EXECUTION_ID AND E.JOB_INSTANCE_ID = E.JOB_INSTANCE_ID AND I.JOB_NAME = ?";

	private static final String COUNT_STEP_EXECUTIONS_FOR_STEP = "SELECT COUNT(STEP_EXECUTION_ID) from %PREFIX%STEP_EXECUTION where STEP_NAME = ?";

	private static final String COUNT_STEP_EXECUTIONS_FOR_STEP_PATTERN = "SELECT COUNT(STEP_EXECUTION_ID) from %PREFIX%STEP_EXECUTION where STEP_NAME like ?";

	private static final String FIELDS = "STEP_EXECUTION_ID, STEP_NAME, START_TIME, END_TIME, STATUS, COMMIT_COUNT,"
			+ " READ_COUNT, FILTER_COUNT, WRITE_COUNT, EXIT_CODE, EXIT_MESSAGE, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT, ROLLBACK_COUNT, LAST_UPDATED, VERSION";

	private DataSource dataSource;

	/**
	 * @param dataSource
	 *            the dataSource to set
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @see JdbcJobExecutionDao#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		Assert.state(dataSource!=null, "DataSource must be provided");

		if (getJdbcTemplate()==null) {
			setJdbcTemplate(new SimpleJdbcTemplate(dataSource));
		}
		setStepExecutionIncrementer(new AbstractDataFieldMaxValueIncrementer() {
			@Override
			protected long getNextKey() {
				return 0;
			}
		});

		super.afterPropertiesSet();

	}

	public Collection<String> findStepNamesForJobExecution(String jobName,
			String excludesPattern) {

		List<String> list = getJdbcTemplate().query(
				getQuery(STEP_EXECUTIONS_FOR_JOB), new RowMapper<String>() {
					public String mapRow(java.sql.ResultSet rs, int rowNum)
							throws java.sql.SQLException {
						return rs.getString(1);
					}
				}, jobName);

		Set<String> stepNames = new LinkedHashSet<String>(list);
		for (Iterator<String> iterator = stepNames.iterator(); iterator
				.hasNext();) {
			String name = iterator.next();
			if (PatternMatcher.match(excludesPattern, name)) {
				iterator.remove();
			}
		}

		return stepNames;

	}

	public Collection<StepExecution> findStepExecutions(String stepName,
			int start, int count) {

		String whereClause = "STEP_NAME = ?";

		if (stepName.contains("*")) {
			whereClause = "STEP_NAME like ?";
			stepName = stepName.replace("*", "%");
		}

		PagingQueryProvider queryProvider = getPagingQueryProvider(whereClause);

		if (start <= 0) {
			return getJdbcTemplate().query(
					queryProvider.generateFirstPageQuery(count),
					new StepExecutionRowMapper(), stepName);
		}
		Long startAfterValue = getJdbcTemplate().queryForLong(
				queryProvider.generateJumpToItemQuery(start, count), stepName);

		return getJdbcTemplate().query(
				queryProvider.generateRemainingPagesQuery(count),
				new StepExecutionRowMapper(), stepName, startAfterValue);

	}

	public int countStepExecutions(String stepName) {
		if (stepName.contains("*")) {
			return getJdbcTemplate().queryForInt(
					getQuery(COUNT_STEP_EXECUTIONS_FOR_STEP_PATTERN),
					stepName.replace("*", "%"));
		}
		return getJdbcTemplate().queryForInt(
				getQuery(COUNT_STEP_EXECUTIONS_FOR_STEP), stepName);
	}

	/**
	 * @return a {@link PagingQueryProvider} with a where clause to narrow the
	 *         query
	 * @throws Exception
	 */
	private PagingQueryProvider getPagingQueryProvider(String whereClause) {
		SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
		factory.setDataSource(dataSource);
		factory.setFromClause(getQuery("%PREFIX%STEP_EXECUTION"));
		factory.setSelectClause(FIELDS);
		factory.setSortKey("STEP_EXECUTION_ID");
		factory.setAscending(false);
		if (whereClause != null) {
			factory.setWhereClause(whereClause);
		}
		try {
			return (PagingQueryProvider) factory.getObject();
		} catch (Exception e) {
			throw new IllegalStateException(
					"Unexpected exception creating paging query provide", e);
		}
	}

	private static class StepExecutionRowMapper implements
			RowMapper<StepExecution> {

		public StepExecution mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			StepExecution stepExecution = new StepExecution(rs.getString(2),
					null);
			stepExecution.setId(rs.getLong(1));
			stepExecution.setStartTime(rs.getTimestamp(3));
			stepExecution.setEndTime(rs.getTimestamp(4));
			stepExecution.setStatus(BatchStatus.valueOf(rs.getString(5)));
			stepExecution.setCommitCount(rs.getInt(6));
			stepExecution.setReadCount(rs.getInt(7));
			stepExecution.setFilterCount(rs.getInt(8));
			stepExecution.setWriteCount(rs.getInt(9));
			stepExecution.setExitStatus(new ExitStatus(rs.getString(10), rs
					.getString(11)));
			stepExecution.setReadSkipCount(rs.getInt(12));
			stepExecution.setWriteSkipCount(rs.getInt(13));
			stepExecution.setProcessSkipCount(rs.getInt(14));
			stepExecution.setRollbackCount(rs.getInt(15));
			stepExecution.setLastUpdated(rs.getTimestamp(16));
			stepExecution.setVersion(rs.getInt(17));
			return stepExecution;
		}

	}

}
