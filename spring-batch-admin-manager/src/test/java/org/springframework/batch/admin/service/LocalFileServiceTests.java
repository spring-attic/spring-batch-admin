package org.springframework.batch.admin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

public class LocalFileServiceTests {

	private LocalFileService service = new LocalFileService();

	@Before
	public void setUp() throws Exception {
		FileUtils.deleteDirectory(service.getUploadDirectory());
	}

	@Test
	public void testUpload() throws Exception {
		File file = service.createFile("spam", "bucket");
		assertTrue(file.exists());
		assertTrue(file.getParentFile().exists());
	}

	@Test
	public void testList() throws Exception {
		service.setResourceLoader(new DefaultResourceLoader());
		service.afterPropertiesSet();
		List<String> uploaded = service.getFiles(0, 20);
		assertEquals(0, uploaded.size());
	}

	@Test
	public void testDeleteAll() throws Exception {
		service.setResourceLoader(new DefaultResourceLoader());
		service.afterPropertiesSet();
		service.deleteAll();
	}
}
