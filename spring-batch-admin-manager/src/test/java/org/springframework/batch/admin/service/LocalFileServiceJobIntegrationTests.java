package org.springframework.batch.admin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.integration.MessageRejectedException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class LocalFileServiceJobIntegrationTests {
	
	private static Log logger = LogFactory.getLog(LocalFileServiceJobIntegrationTests.class);

	@Autowired
	private LocalFileService service;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	@Qualifier("staging")
	private Job job;
	
	private JobExecution jobExecution;

	@Autowired
	@Qualifier("job-operator")
	private SubscribableChannel operator;

	@Before
	public void setUp() throws Exception {
		FileUtils.deleteDirectory(service.getUploadDirectory());
	}

	@Test
	public void testTrigger() throws Exception {
		operator.subscribe(new MessageHandler() {
			public void handleMessage(Message<?> message) throws MessageRejectedException, MessageHandlingException,
					MessageDeliveryException {
				logger.debug(""+message);
				jobExecution = (JobExecution) message.getPayload();
			}
		});
		FileInfo info = service.createFile("staging/crap");
		Resource file = service.getResource(info.getPath());
		assertTrue(file.exists());
		service.publish(info);
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		Thread.sleep(1000L);
	}
	
	@Test
	public void testResourceConverter() throws Exception {
		FileInfo info = service.createFile("foo/crap");
		JobExecution jobExecution = jobLauncher.run(job, new JobParametersBuilder().addString("input.file", "files://"+info.getPath()).toJobParameters());
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}

}
