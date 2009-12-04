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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.WebApplicationContextLoader;
import org.springframework.web.servlet.View;

@ContextConfiguration(loader = WebApplicationContextLoader.class, inheritLocations = false, locations = "AbstractIntegrationViewTests-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class MessagesViewTests extends AbstractManagerViewTests {

	private final HashMap<String, Object> model = new HashMap<String, Object>();

	@Autowired
	@Qualifier("messages")
	private View view;

	@Test
	public void testMessages() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.setEndTime(new Date());
		Message<String> message = MessageBuilder.withPayload("foo").setHeader(MessageHeaders.ID, "FOO").setHeader(
				MessageHeaders.CORRELATION_ID, "BAR").setHeaderIfAbsent("timestamp", new Date(100)).build();
		assertEquals("FOO", message.getHeaders().getId());
		@SuppressWarnings("unchecked")
		List<Message<String>> messages = Arrays.asList(message);
		model.put("messages", messages);
		model.put("feedPath", "feed.rss");
		view.render(model, request, response);
		String content = response.getContentAsString();
		System.err.println(content);
		assertTrue(content.contains("Recent Messages"));
		assertTrue(content.contains("<table title=\"Recent Messages\""));
		assertTrue(content.contains("FOO"));
		assertTrue(content.matches("(?s).*td>Jan 1.*</td>.*"));
	}

}
