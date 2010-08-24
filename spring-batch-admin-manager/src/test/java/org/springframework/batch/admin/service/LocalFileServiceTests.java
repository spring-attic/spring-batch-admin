package org.springframework.batch.admin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.support.SerializationUtils;
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
		String path = info.getFileName();
		// System.err.println(path);
		assertEquals("spam/bucket.txt", info.getPath());
		assertTrue("Wrong path: " + path, path.matches("spam/bucket\\.[0-9]*\\.[0-9]*\\.txt"));
	}

	@Test
	public void testGetNonExistent() throws Exception {
		FileInfo info = service.createFile("spam/bucket.txt");
		Resource file = service.getResource("spam/bucket.19990505.211145.txt");
		assertFalse("File exists: " + file.getFilename() + " when non-existent timestamp supplied", file.exists());
		assertNotSame(info.getPath(), "spam/" + file.getFilename());
	}

	@Test
	public void testUploadAndGetLatest() throws Exception {
		FileInfo info = service.createFile("spam/bucket.txt");
		Resource file = service.getResource("spam/bucket.txt");
		assertTrue("File doesn't exist: " + file + " for " + info.getPath(), file.exists());
		assertEquals(info.getFileName(), "spam/" + file.getFilename());
		String path = file.getFilename();
		assertTrue("Wrong path: " + path, path.matches("bucket\\.[0-9]*\\.[0-9]*\\.txt"));
	}

	@Test
	public void testUploadAndGetLatestWithAlternative() throws Exception {
		service.createFile("spam/bucket.20100423.123000.txt");
		FileInfo info = service.createFile("spam/bucket.20100423.123002.txt");
		Resource file = service.getResource("spam/bucket.txt");
		assertTrue("File doesn't exist: " + file + " for " + info.getPath(), file.exists());
		assertEquals(info.getFileName(), "spam/" + file.getFilename());
		String path = file.getFilename();
		assertTrue("Wrong path: " + path, path.matches("bucket\\.[0-9]*\\.[0-9]*\\.txt"));
	}

	@Test
	public void testUploadNoDirectory() throws Exception {
		FileInfo info = service.createFile("bucket");
		Resource file = service.getResource(info.getPath());
		assertTrue(file.exists());
		assertTrue(file.getFile().getParentFile().exists());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUploadFailsForNoFileName() throws Exception {
		FileInfo info = service.createFile("");
		Resource file = service.getResource(info.getPath());
		assertTrue(file.exists());
	}

	@Test
	public void testPublish() throws Exception {
		FileInfo info = service.createFile("spam/bucket/crap");
		Resource file = service.getResource(info.getPath());
		assertTrue(file.exists());
		service.publish(info);
		assertNotNull(trigger);
	}

	@Test
	public void testGetFilesEmpty() throws Exception {
		service.setResourceLoader(new DefaultResourceLoader());
		service.afterPropertiesSet();
		List<FileInfo> uploaded = service.getFiles(0, 20);
		assertEquals(0, uploaded.size());
	}

	@Test
	public void testGetFilesNotEmpty() throws Exception {
		service.setResourceLoader(new DefaultResourceLoader());
		service.afterPropertiesSet();
		service.createFile("foo");
		service.createFile("bar");
		service.createFile("spam");
		List<FileInfo> files = service.getFiles(0, 20);
		assertEquals(3, files.size());
		assertEquals("bar", files.get(0).getPath());
	}

	@Test
	public void testGetFilesSerializable() throws Exception {
		service.setResourceLoader(new DefaultResourceLoader());
		service.afterPropertiesSet();
		service.createFile("foo");
		service.createFile("bar");
		service.createFile("spam");
		@SuppressWarnings("unchecked")
		List<FileInfo> files = (List<FileInfo>) SerializationUtils.deserialize(SerializationUtils.serialize(service.getFiles(0, 20)));
		assertEquals(3, files.size());
		assertEquals("bar", files.get(0).getPath());
	}

	@Test
	public void testGetFilesPagination() throws Exception {
		service.setResourceLoader(new DefaultResourceLoader());
		service.afterPropertiesSet();
		service.createFile("foo");
		service.createFile("bar");
		service.createFile("spam");
		List<FileInfo> files = service.getFiles(2, 2);
		assertEquals(1, files.size());
		assertEquals("spam", files.get(0).getPath());
	}

	@Test
	public void testGetFilesShortPath() throws Exception {
		service.setResourceLoader(new DefaultResourceLoader());
		service.afterPropertiesSet();
		service.createFile("foo.20100505.123000");
		service.createFile("foo.20100505.123001");
		service.createFile("foo.20100505.123002");
		service.createFile("bar.20100505.123000");
		List<FileInfo> files = service.getFiles(0, 20);
		assertEquals(4, files.size());
		assertEquals("bar", files.get(0).getPath());
		assertEquals("foo", files.get(1).getPath());
		assertEquals("foo.20100505.123001", files.get(2).getPath());
		assertEquals("foo.20100505.123000", files.get(3).getPath());
	}

	@Test
	public void testCount() throws Exception {
		service.setResourceLoader(new DefaultResourceLoader());
		service.afterPropertiesSet();
		assertEquals(0, service.countFiles());
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
