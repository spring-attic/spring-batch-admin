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
import org.springframework.core.io.Resource;

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
		FileInfo info = service.createFile("spam/bucket");
		Resource file = service.getResource(info.getPath());
		assertTrue(file.exists());
		assertTrue(file.getFile().getParentFile().exists());
	}

	@Test
	public void testUploadWithExtension() throws Exception {
		FileInfo info = service.createFile("spam/bucket.txt");
		String path = info.getPath();
		System.err.println(path);
		assertTrue("Wrong path: " + path, path.matches("spam/bucket\\.[0-9]*\\.[0-9]*\\.txt"));
	}

	@Test
	public void testUploadNoDirectory() throws Exception {
		FileInfo info = service.createFile("bucket");
		Resource file = service.getResource(info.getPath());
		assertTrue(file.exists());
		assertTrue(file.getFile().getParentFile().exists());
	}

	@Test
	public void testUploadFailsForNoFileName() throws Exception {
		FileInfo info = service.createFile("");
		Resource file = service.getResource(info.getPath());
		assertTrue(file.exists());
	}

	@Test
	public void testTrigger() throws Exception {
		FileInfo info = service.createFile("spam/bucket/crap");
		Resource file = service.getResource(info.getPath());
		assertTrue(file.exists());
		service.publish(info);
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
		service.createFile("spam");
		assertEquals(1, service.getFiles(0, 20).size());
		service.afterPropertiesSet();
		service.delete("*");
		assertEquals(0, service.getFiles(0, 20).size());
	}
}
