package org.springframework.batch.admin.sample.lead.support;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

/**
 * @author Dave Syer
 *
 */
public class StagingWriter implements ItemWriter<String> {

	private SimpleJdbcTemplate jdbcTemplate;
	
	private DataFieldMaxValueIncrementer incrementer;

	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	public void setIncrementer(DataFieldMaxValueIncrementer incrementer) {
		this.incrementer = incrementer;
	}

	public void write(List<? extends String> values) throws Exception {
		for (String value : values) {
			long id = incrementer.nextLongValue();
			jdbcTemplate.update("INSERT INTO LEAD_INPUTS (ID, DATA) values(?,?)", id, value);			
		}
	}
}
