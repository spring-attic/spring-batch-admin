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
package org.springframework.batch.admin.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.admin.service.FileInfo;
import org.springframework.batch.admin.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

/**
 * Controller for uploading and managing files.
 * 
 * @author Dave Syer
 * 
 */
@Controller
public class FileController {

	private static Log logger = LogFactory.getLog(FileController.class);

	private FileService fileService;

	/**
	 * The service used to manage file lists and uploads.
	 * 
	 * @param fileService the {@link FileService} to set
	 */
	@Autowired
	public void setFileService(FileService fileService) {
		this.fileService = fileService;
	}

	@RequestMapping(value = "/files", method = RequestMethod.POST)
	public String uploadRequest(@RequestParam String path, @RequestParam MultipartFile file, ModelMap model,
			@RequestParam(defaultValue = "0") int startFile, @RequestParam(defaultValue = "20") int pageSize,
			@ModelAttribute("date") Date date, Errors errors) throws Exception {
		return upload(path, file, model, startFile, pageSize, date, errors);
	}

	@RequestMapping(value = "/files/{path}", method = RequestMethod.POST)
	public String upload(@PathVariable String path, @RequestParam MultipartFile file, ModelMap model,
			@RequestParam(defaultValue = "0") int startFile, @RequestParam(defaultValue = "20") int pageSize,
			@ModelAttribute("date") Date date, Errors errors) throws Exception {

		String originalFilename = file.getOriginalFilename();
		if (file.isEmpty()) {
			errors.reject("file.upload.empty", new Object[] { originalFilename },
					"File upload was empty for filename=[" + HtmlUtils.htmlEscape(originalFilename) + "]");
			list(model, startFile, pageSize);
			return "files";
		}

		try {
			FileInfo dest = fileService.createFile(path + "/" + originalFilename);
			file.transferTo(fileService.getResource(dest.getPath()).getFile());
			fileService.publish(dest);
			model.put("uploaded", dest.getPath());
		}
		catch (IOException e) {
			errors.reject("file.upload.failed", new Object[] { originalFilename }, "File upload failed for "
					+ HtmlUtils.htmlEscape(originalFilename));
		}
		catch (Exception e) {
			String message = "File upload failed downstream processing for "
					+ HtmlUtils.htmlEscape(originalFilename);
			if (logger.isDebugEnabled()) {
				logger.debug(message, e);
			} else {
				logger.info(message);
			}
			errors.reject("file.upload.failed.downstream", new Object[] { originalFilename }, message);
		}
		
		if (errors.hasErrors()) {
			list(model, startFile, pageSize);
			return "files";			
		}

		return "redirect:files";

	}

	@RequestMapping(value = "/files", method = RequestMethod.GET)
	public void list(ModelMap model, @RequestParam(defaultValue = "0") int startFile,
			@RequestParam(defaultValue = "20") int pageSize) throws Exception {

		List<FileInfo> files = fileService.getFiles(startFile, pageSize);
		Collections.sort(files);
		model.put("files", files);

	}

	@RequestMapping(value = "/files/**", method = RequestMethod.GET)
	public String get(HttpServletRequest request, HttpServletResponse response, ModelMap model,
			@RequestParam(defaultValue = "0") int startFile, @RequestParam(defaultValue = "20") int pageSize,
			@ModelAttribute("date") Date date, Errors errors) throws Exception {

		list(model, startFile, pageSize);

		String path = request.getPathInfo().substring("/files/".length());
		Resource file = fileService.getResource(path);
		if (file == null || !file.exists()) {
			errors.reject("file.download.missing", new Object[] { path },
					"File download failed for missing file at path=" + HtmlUtils.htmlEscape(path));
			return "files";
		}

		response.setContentType("application/octet-stream");
		try {
			FileCopyUtils.copy(file.getInputStream(), response.getOutputStream());
		}
		catch (IOException e) {
			errors.reject("file.download.failed", new Object[] { path }, "File download failed for path=" + HtmlUtils.htmlEscape(path));
			logger.info("File download failed for path=" + path, e);
			return "files";
		}

		return null;

	}

	@RequestMapping(value = "/files", method = RequestMethod.DELETE)
	public String delete(ModelMap model, @RequestParam(defaultValue = "**") String pattern) throws Exception {

		int deletedCount = fileService.delete(pattern);

		model.put("files", new ArrayList<String>());
		model.put("deletedCount", deletedCount);

		return "redirect:files";

	}

}
