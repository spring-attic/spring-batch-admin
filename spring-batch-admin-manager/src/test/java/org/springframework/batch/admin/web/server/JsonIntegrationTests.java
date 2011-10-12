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

package org.springframework.batch.admin.web.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.batch.admin.ServerRunning;
import org.springframework.batch.admin.web.JsonWrapper;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.poller.DirectPoller;
import org.springframework.batch.poller.Poller;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * @author Dave Syer
 * 
 */
public class JsonIntegrationTests {

	@Rule
	public static ServerRunning serverRunning = ServerRunning
			.isRunning("${SERVER_URL:http://localhost:8080/spring-batch-admin-sample}");

	@Test
	public void testHomePage() throws Exception {
		RestTemplate template = new RestTemplate();
		ResponseEntity<String> result = template.exchange(serverRunning.getUrl() + "/home.json", HttpMethod.GET, null,
				String.class);
		JsonWrapper wrapper = new JsonWrapper(result.getBody());
		assertNotNull(wrapper.get("feed.resources"));
		assertNotNull(wrapper.get("feed.resources['/files'].uri"));
	}

	@Test
	public void testJobsPage() throws Exception {
		RestTemplate template = new RestTemplate();
		ResponseEntity<String> result = template.exchange(serverRunning.getUrl() + "/jobs.json", HttpMethod.GET, null,
				String.class);
		JsonWrapper wrapper = new JsonWrapper(result.getBody());
		// System.err.println(wrapper);
		assertNotNull(wrapper.get("jobs.resource"));
		assertNotNull(wrapper.get("jobs.registrations.infinite.name"));
	}

	@Test
	public void testJobConfigurationUpload() throws Exception {
		RestTemplate template = new RestTemplate();
		HttpEntity<String> request = new HttpEntity<String>(FileUtils.readFileToString(new File(
				"src/test/resources/test-job-context.xml")));
		ResponseEntity<String> result = template.exchange(serverRunning.getUrl() + "/job-configuration.json",
				HttpMethod.POST, request, String.class);
		JsonWrapper wrapper = new JsonWrapper(result.getBody());
		// System.err.println(wrapper);
		assertNotNull(wrapper.get("jobs.resource"));
		assertNotNull(wrapper.get("jobs.registrations['test-job'].name"));
	}

	@Test
	public void testJobLaunch() throws Exception {

		RestTemplate template = new RestTemplate();
		ResponseEntity<String> result = template.exchange(serverRunning.getUrl() + "/jobs/job2.json?jobParameters=fail=true", HttpMethod.POST,
				null, String.class);
		JsonWrapper wrapper = new JsonWrapper(result.getBody());
		// System.err.println(wrapper);
		assertNotNull(wrapper.get("jobExecution.resource"));
		assertNotNull(wrapper.get("jobExecution.status"));
		assertNotNull(wrapper.get("jobExecution.id"));

		// Poll for the completed job execution
		final String resource = wrapper.get("jobExecution.resource", String.class);
		Poller<JsonWrapper> poller = new DirectPoller<JsonWrapper>(100L);
		Future<JsonWrapper> poll = poller.poll(new Callable<JsonWrapper>() {
			public JsonWrapper call() throws Exception {
				RestTemplate template = new RestTemplate();
				ResponseEntity<String> result = template.exchange(resource, HttpMethod.GET, null, String.class);
				JsonWrapper wrapper = new JsonWrapper(result.getBody());
				// System.err.println(wrapper);
				Map<?, ?> map = wrapper.get("jobExecution.stepExecutions", Map.class);
				return map.isEmpty() || wrapper.get("jobExecution.stepExecutions['job2.step1']['resource']") == null ? null
						: wrapper;
			}
		});
		JsonWrapper jobExecution = poll.get(500L, TimeUnit.MILLISECONDS);
		assertNotNull(jobExecution);
		// System.err.println(jobExecution);

		// Verify that there is a step execution in the result
		result = template.exchange(
				jobExecution.get("jobExecution.stepExecutions['job2.step1'].resource", String.class), HttpMethod.GET,
				null, String.class);
		wrapper = new JsonWrapper(result.getBody());
		// System.err.println(wrapper);
		assertNotNull(wrapper.get("stepExecution.id"));
		assertNotNull(wrapper.get("stepExecution.status"));
		assertNotNull(wrapper.get("jobExecution.resource"));
		assertNotNull(wrapper.get("jobExecution.status"));
		assertNotNull(wrapper.get("jobExecution.id"));

	}

	@Test
	public void testJobStop() throws Exception {

		RestTemplate template = new RestTemplate();
		ResponseEntity<String> result = template.exchange(serverRunning.getUrl()
				+ "/jobs/infinite.json?jobParameters=timestamp=" + System.currentTimeMillis(), HttpMethod.POST, null,
				String.class);
		JsonWrapper wrapper = new JsonWrapper(result.getBody());
		// System.err.println(wrapper);
		assertNotNull(wrapper.get("jobExecution.resource"));
		assertNotNull(wrapper.get("jobExecution.status"));
		assertNotNull(wrapper.get("jobExecution.id"));

		template.exchange(wrapper.get("jobExecution.resource", String.class), HttpMethod.DELETE, null, String.class);

		// Poll for the completed job execution
		final String resource = wrapper.get("jobExecution.resource", String.class);
		Poller<JsonWrapper> poller = new DirectPoller<JsonWrapper>(100L);
		Future<JsonWrapper> poll = poller.poll(new Callable<JsonWrapper>() {
			public JsonWrapper call() throws Exception {
				RestTemplate template = new RestTemplate();
				ResponseEntity<String> result = template.exchange(resource, HttpMethod.GET, null, String.class);
				JsonWrapper wrapper = new JsonWrapper(result.getBody());
				// System.err.println(wrapper);
				BatchStatus status = wrapper.get("jobExecution.status", BatchStatus.class);
				return status.isGreaterThan(BatchStatus.STOPPING) ? wrapper : null;
			}
		});
		JsonWrapper jobExecution = poll.get(500L, TimeUnit.MILLISECONDS);
		assertNotNull(jobExecution);

		BatchStatus status = jobExecution.get("jobExecution.status", BatchStatus.class);
		assertEquals(BatchStatus.STOPPED, status);

	}
	
	@Test
	public void testListedResourcesWithGet() throws Exception {
		
		Map<String,String> params = new HashMap<String, String>();
		params.put("jobName", "job2");
		// These should be there if the previous test cases worked
		params.put("jobInstanceId", "0");
		params.put("jobExecutionId", "0");
		params.put("stepExecutionId", "0");

		RestTemplate template = new RestTemplate();

		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource("/org/springframework/batch/admin/web/manager/json-resources.properties"));
		propertiesFactoryBean.afterPropertiesSet();
		Properties properties = propertiesFactoryBean.getObject();

		for (String path : properties.stringPropertyNames()) {
			if (!StringUtils.hasText(path) || !path.startsWith("GET")) {
				continue;
			}
			path = path.substring(path.indexOf("/"));
			ResponseEntity<String> result = template.exchange(serverRunning.getUrl() + path, HttpMethod.GET,
					null, String.class, params);
			JsonWrapper wrapper = new JsonWrapper(result.getBody());
			// System.err.println(wrapper);
			assertNotNull(wrapper);
		}
	}

}
