package org.springframework.batch.admin.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.SubscribableChannel;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageDeliveryException;
import org.springframework.integration.message.MessageHandler;
import org.springframework.integration.message.MessageHandlingException;
import org.springframework.integration.message.MessageRejectedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class LocalFileServiceIntegrationTests {
	
	private static Log logger = LogFactory.getLog(LocalFileServiceIntegrationTests.class);

	@Autowired
	private LocalFileService service;

	@Autowired
	@Qualifier("input-files")
	private SubscribableChannel files;

	private static File trigger;

	private static MessageHandler handler = new MessageHandler() {
		public void handleMessage(Message<?> message) throws MessageRejectedException, MessageHandlingException,
				MessageDeliveryException {
			logger.debug("Handled " + message);
			trigger = (File) message.getPayload();
		}
	};

	@Before
	public void setUp() throws Exception {
		FileUtils.deleteDirectory(service.getUploadDirectory());
		files.unsubscribe(handler);
		files.subscribe(handler);
	}

	@Test
	public void testUpload() throws Exception {
		FileInfo info = service.createFile("spam", "bucket");
		File file = new File(info.getAbsolutePath());
		assertTrue(file.exists());
		assertTrue(file.getParentFile().exists());
	}

	@Test
	public void testTrigger() throws Exception {
		FileInfo info = service.createFile("spam/bucket", "crap");
		File file = new File(info.getAbsolutePath());
		assertTrue(file.exists());
		service.createTrigger(info);
		assertNotNull(trigger);
	}

}
