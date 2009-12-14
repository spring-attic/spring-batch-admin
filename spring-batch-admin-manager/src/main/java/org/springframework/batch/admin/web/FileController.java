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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.batch.admin.service.FileService;
import org.springframework.batch.admin.service.LocalFileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for uploading files.
 * 
 * @author Dave Syer
 * 
 */
@Controller
public class FileController {
	
	private FileService fileService = new LocalFileService();
	
	@RequestMapping(value = "/files", method = RequestMethod.POST)
	public String uploadRequest(@RequestParam String path, @RequestParam MultipartFile file, ModelMap model,
			@ModelAttribute("date") Date date, Errors errors) throws Exception {
		return upload(path, file, model, date, errors);
	}

	@RequestMapping(value = "/files/{path}", method = RequestMethod.POST)
	public String upload(@PathVariable String path, @RequestParam MultipartFile file, ModelMap model,
			@ModelAttribute("date") Date date, Errors errors) throws Exception {

		if (file.isEmpty()) {
			errors.reject("file.upload.empty", new Object[] { file.getOriginalFilename() },
					"File upload was empty for filename=[" + file.getOriginalFilename() + "]");
			return "files";
		}

		try {
			File dest = fileService.createFile(path, file.getOriginalFilename());
			file.transferTo(dest);
			fileService.createTrigger(dest);
			model.put("uploaded", dest.getAbsolutePath());
		}
		catch (IOException e) {
			errors.reject("file.upload.failed", new Object[] { file.getOriginalFilename() }, "File upload failed for "
					+ file.getOriginalFilename());
			return "files";
		}

		return "redirect:files";

	}

	@RequestMapping(value = "/files", method = RequestMethod.GET)
	public void list(ModelMap model, @RequestParam(defaultValue = "0") int startFile,
			@RequestParam(defaultValue = "20") int pageSize) throws Exception {

		model.put("files", fileService.getFiles(startFile, pageSize));
		model.put("outputDir", fileService.getUploadDirectory().getAbsolutePath().replace("\\", "/"));
		model.put("triggerDir", fileService.getTriggerDirectory().getAbsolutePath().replace("\\", "/"));

	}

	@RequestMapping(value = "/files", method = RequestMethod.DELETE)
	public String deleteAll(ModelMap model) throws Exception {

		fileService.deleteAll();

		model.put("files", new ArrayList<String>());
		model.put("outputDir", fileService.getUploadDirectory().getAbsolutePath().replace("\\", "/"));
		model.put("triggerDir", fileService.getTriggerDirectory().getAbsolutePath().replace("\\", "/"));
		
		return "redirect:files";

	}

}
