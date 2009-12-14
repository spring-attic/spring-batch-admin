package org.springframework.batch.admin.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindException;

public class FileControllerTests {

	private FileController controller = new FileController();

	@Before
	public void setUp() throws Exception {
		FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir",
				"/tmp"), "batch/files"));
	}

	@Test
	public void testUpload() throws Exception {
		MockMultipartFile file = new MockMultipartFile("foo", "foo.properties",
				"text/plain", "bar".getBytes());
		ExtendedModelMap model = new ExtendedModelMap();
		Date date = new Date();
		controller.upload("spam", file, model, date, new BindException(date, "date"));
		String uploaded = (String) model.get("uploaded");
		// System.err.println(uploaded);
		assertTrue("Wrong filename: " + uploaded, uploaded
				.matches(".*batch.files.spam.foo.properties\\..*"));
	}

	@Test
	public void testEmptyUpload() throws Exception {
		MockMultipartFile file = new MockMultipartFile("foo", "foo.properties",
				"text/plain", "".getBytes());
		ExtendedModelMap model = new ExtendedModelMap();
		Date date = new Date();
		BindException errors = new BindException(date, "date");
		controller.upload("spam", file, model, date, errors);
		assertTrue(errors.hasErrors());
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
		controller.deleteAll(model);
		@SuppressWarnings("unchecked")
		List<String> uploaded = (List<String>) model.get("files");
		assertEquals(0, uploaded.size());
	}
}
