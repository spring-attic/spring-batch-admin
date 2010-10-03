package org.springframework.batch.admin.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.admin.service.FileSender;
import org.springframework.batch.admin.service.LocalFileService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

public class FileControllerTests {

	private FileController controller = new FileController();
	private LocalFileService fileService;

	@Before
	public void setUp() throws Exception {
		fileService = new LocalFileService();
		fileService.setFileSender(new FileSender() {
			public void send(File file) {
			}
		});
		controller.setFileService(fileService);
		FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir", "/tmp"), "batch/files"));
	}

	@Test
	public void testUpload() throws Exception {
		MockMultipartFile file = new MockMultipartFile("foo", "foo.properties", "text/plain", "bar".getBytes());
		ExtendedModelMap model = new ExtendedModelMap();
		Date date = new Date();
		controller.upload("spam", file, model, 0, 20, date, new BindException(date, "date"));
		String uploaded = (String) model.get("uploaded");
		// System.err.println(uploaded);
		assertTrue("Wrong filename: " + uploaded, uploaded.matches("spam.foo.*\\.properties$"));
	}

	@Test
	public void testEmptyUpload() throws Exception {
		MockMultipartFile file = new MockMultipartFile("foo", "foo.properties", "text/plain", "".getBytes());
		ExtendedModelMap model = new ExtendedModelMap();
		Date date = new Date();
		BindException errors = new BindException(date, "date");
		controller.upload("spam", file, model, 0, 20, date, errors);
		assertTrue(errors.hasErrors());
	}

	@Test
	public void testDownload() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		ExtendedModelMap model = new ExtendedModelMap();
		Date date = new Date();
		BindException errors = new BindException(date, "date");
		request.setPathInfo("/files/"+fileService.createFile("sample/foo.txt").getPath());
		controller.get(request, response, model, 0, 20, date, errors);
		assertFalse(errors.hasErrors());
		assertEquals("application/octet-stream", response.getContentType());
	}

	@Test
	public void testDownloadWithScript() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		ExtendedModelMap model = new ExtendedModelMap();
		Date date = new Date();
		BindException errors = new BindException(date, "date");
		request.setPathInfo("/files/><script>alert(45530)</script>");
		controller.get(request, response, model, 0, 20, date, errors);
		assertTrue(errors.hasErrors());
		ObjectError error = (ObjectError) errors.getAllErrors().get(0);
		assertTrue("Wrong message: "+error.getDefaultMessage(), error.getDefaultMessage().contains("&gt;&lt;script"));
	}

	@Test
	public void testList() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.list(model, 0, 20);
		@SuppressWarnings("unchecked")
		List<String> uploaded = (List<String>) model.get("files");
		assertEquals(0, uploaded.size());
	}

	@Test
	public void testDeleteAll() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.delete(model, "**");
		@SuppressWarnings("unchecked")
		List<String> uploaded = (List<String>) model.get("files");
		assertEquals(0, uploaded.size());
		int deletedCount = (Integer) model.get("deletedCount");
		assertEquals(0, deletedCount);
	}
}
