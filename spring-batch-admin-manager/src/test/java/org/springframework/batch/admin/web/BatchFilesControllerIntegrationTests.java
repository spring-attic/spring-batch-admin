/*
 * Copyright 2013-2015 the original author or authors.
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.admin.service.FileInfo;
import org.springframework.batch.admin.service.FileService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Tests REST compliance of {@link BatchJobInstancesController} endpoints.
 * 
 * @author Ilayaperumal Gopinathan
 * @author Michael Minella
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDependencies.class, RestConfiguration.class})
@WebAppConfiguration
public class BatchFilesControllerIntegrationTests extends AbstractControllerIntegrationTest {

	@Autowired
	private FileService fileService;

	@Before
	public void before() throws Exception {
		Date startTime = new Date();
		Date endTime = new Date();
		JobExecution execution = new JobExecution(3L,
				new JobParametersBuilder().addString("foo", "bar").addLong("foo2", 0L).toJobParameters());
		execution.setExitStatus(ExitStatus.COMPLETED);
		execution.setStartTime(startTime);
		execution.setEndTime(endTime);
		execution.setLastUpdated(new Date());

		StepExecution stepExecution = new StepExecution("s1", execution);
		stepExecution.setLastUpdated(new Date());
		stepExecution.setId(1l);
		execution.addStepExecutions(Collections.singletonList(stepExecution));
	}

	@Test
	public void testList() throws Exception {
		List<FileInfo> files = new ArrayList<FileInfo>();
		files.add(new FileInfo("foo.txt", "sometimestamp", true, 0));
		files.add(new FileInfo("bar/foo.txt", "anothertimestamp", false, 0));
		files.add(new FileInfo("bar/baz.txt", "lasttimestamp", true, 0));

		when(fileService.getFiles(0, 10)).thenReturn(files);

		mockMvc.perform(
				get("/batch/files").param("page", "0").param("size", "10").accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.pagedResources.content.[*].timestamp", contains("sometimestamp", "anothertimestamp", "lasttimestamp")))
				.andExpect(jsonPath("$.pagedResources.content.[*].path", contains("foo.txt", "bar/foo.txt", "bar/baz.txt")))
				.andExpect(jsonPath("$.pagedResources.content.[*].shortPath", contains("foo.txt", "bar/foo.txt", "bar/baz.txt")))
				.andExpect(jsonPath("$.pagedResources.content.[*].local", contains(true, false, true)));
	}

	@Test
	public void testDelete() throws Exception {
		when(fileService.delete("pattern")).thenReturn(3);

		mockMvc.perform(
				delete("/batch/files").param("pattern", "pattern").accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.fileInfoResource.deleteCount", equalTo(3)));
	}
}
