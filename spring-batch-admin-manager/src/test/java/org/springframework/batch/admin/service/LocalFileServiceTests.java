package org.springframework.batch.admin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

public class LocalFileServiceTests {

	private LocalFileService service = new LocalFileService();
	
	private File trigger = null;

	@Before
	public void setUp() throws Exception {
		service.setFileSender(new FileSender() {
			public void send(File file) {
				trigger = file;
			}
		});
		FileUtils.deleteDirectory(service.getUploadDirectory());
	}

	@Test
	public void testUpload() throws Exception {
		FileInfo info = service.createFile("spam", "bucket");
		File file = new File(info.getAbsolutePath());
		assertTrue(file.exists());
		assertTrue(file.getParentFile().exists());
	}

	@Test(expected = IllegalStateException.class)
	public void testUploadFailsForNotAFile() throws Exception {
		FileInfo info = service.createFile("spam", "bucket/crap");
		File file = new File(info.getAbsolutePath());
		assertTrue(file.exists());
	}

	@Test
	public void testTrigger() throws Exception {
		FileInfo info = service.createFile("spam/bucket", "crap");
		File file = new File(info.getAbsolutePath());
		assertTrue(file.exists());
		service.createTrigger(info);
		assertNotNull(trigger);
	}

	@Test
	public void testList() throws Exception {
		service.setResourceLoader(new DefaultResourceLoader());
		service.afterPropertiesSet();
		List<FileInfo> uploaded = service.getFiles(0, 20);
		assertEquals(0, uploaded.size());
	}

	@Test
	public void testDeleteAll() throws Exception {
		service.setResourceLoader(new DefaultResourceLoader());
		service.afterPropertiesSet();
		service.deleteAll();
	}
}
