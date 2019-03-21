/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.admin.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

/**
 * @author Dave Syer
 * 
 */
public class JobExecutionInfoTests {

	private JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();

	@Test
	public void testDurationAndTime() throws Exception {
		jobExecution.setStartTime(new Date());
		jobExecution.setEndTime(new Date(jobExecution.getStartTime().getTime() + 30000));
		JobExecutionInfo info = new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT"));
		assertEquals("00:00:30", info.getDuration());
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		assertEquals(timeFormat.format(jobExecution.getStartTime()), info.getStartTime());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		assertEquals(dateFormat.format(jobExecution.getStartTime()), info.getStartDate());
	}

	@Test
	public void testDurationAndTimeWithTimezone() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("1997-05-05 23:00");
		jobExecution.setStartTime(startTime);
		String before = new SimpleDateFormat("yyyy-MM-dd").format(startTime);
		TimeZone.setDefault(TimeZone.getTimeZone("Japan"));
		String after = new SimpleDateFormat("yyyy-MM-dd").format(startTime);
		assertTrue("Dates should be different: " + before, !before.equals(after));
		jobExecution.setEndTime(new Date(jobExecution.getStartTime().getTime() + 30000));
		JobExecutionInfo info = new JobExecutionInfo(jobExecution, TimeZone.getTimeZone("GMT"));
		assertEquals("00:00:30", info.getDuration());
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		assertEquals(timeFormat.format(jobExecution.getStartTime()), info.getStartTime());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		assertEquals(dateFormat.format(jobExecution.getStartTime()), info.getStartDate());
		TimeZone.setDefault(null);
	}

}
