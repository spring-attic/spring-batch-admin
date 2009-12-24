package org.springframework.batch.admin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
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
public class LocalFileServiceJobIntegrationTests {
	
	private static Log logger = LogFactory.getLog(LocalFileServiceJobIntegrationTests.class);

	@Autowired
	private LocalFileService service;
	
	private JobExecution jobExecution;

	@Autowired
	@Qualifier("job-operator")
	private SubscribableChannel operator;

	@Before
	public void setUp() throws Exception {
		FileUtils.deleteDirectory(service.getUploadDirectory());
	}

	@Test
	@Ignore
	// TODO: unignore trigger test
	public void testTrigger() throws Exception {
		operator.subscribe(new MessageHandler() {
			public void handleMessage(Message<?> message) throws MessageRejectedException, MessageHandlingException,
					MessageDeliveryException {
				logger.debug(""+message);
				jobExecution = (JobExecution) message.getPayload();
			}
		});
		FileInfo info = service.createFile("staging", "crap");
		File file = new File(info.getAbsolutePath());
		assertTrue(file.exists());
		service.createTrigger(info);
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}

}
