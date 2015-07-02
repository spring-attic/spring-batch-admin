/*
 * Copyright 2015 the original author or authors.
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
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.batch.admin.domain.FileInfoResource;
import org.springframework.batch.admin.service.FileInfo;
import org.springframework.batch.admin.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

/**
 * @author Michael Minella
 */
@Controller
@RequestMapping("/batch/files")
@ExposesResourceFor(FileInfoResource.class)
public class BatchFileController extends AbstractBatchJobsController {

	private static Log logger = LogFactory.getLog(BatchFileController.class);

	@Autowired
	private FileService fileService;

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public PagedResources<FileInfoResource> list(Pageable pageable,
			PagedResourcesAssembler<FileInfo> assembler) throws IOException {

		List<FileInfo> files = fileService.getFiles(pageable.getOffset(), pageable.getPageSize());

		return assembler.toResource(
				new PageImpl<FileInfo>(files, pageable, fileService.countFiles()),
				fileInfoResourceAssembler);
	}

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public FileInfoResource delete(@RequestParam(defaultValue = "**") String pattern) throws Exception {
		FileInfo fileInfo = new FileInfo(pattern, "", true, fileService.delete(pattern));

		return fileInfoResourceAssembler.toResource(fileInfo);
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	public String uploadRequest(@RequestParam String path, @RequestParam MultipartFile file, ModelMap model,
			@RequestParam(defaultValue = "0") int startFile, @RequestParam(defaultValue = "20") int pageSize,
			@ModelAttribute("date") Date date, Errors errors) throws Exception {
		return upload(path, file, model, startFile, pageSize, date, errors);
	}

	@RequestMapping(value = "/{path}", method = RequestMethod.POST)
	public String upload(@PathVariable String path, @RequestParam MultipartFile file, ModelMap model,
			@RequestParam(defaultValue = "0") int startFile, @RequestParam(defaultValue = "20") int pageSize,
			@ModelAttribute("date") Date date, Errors errors) throws Exception {

		String originalFilename = file.getOriginalFilename();
		if (file.isEmpty()) {
			errors.reject("file.upload.empty", new Object[] { originalFilename },
					"File upload was empty for filename=[" + HtmlUtils.htmlEscape(originalFilename) + "]");
			return null;
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

		return null;
	}
}
