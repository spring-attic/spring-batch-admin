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
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Michael Minella
 */
@Controller
@RequestMapping("/batch/files")
@ExposesResourceFor(FileInfoResource.class)
public class BatchFileController extends AbstractBatchJobsController {

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
	public int delete(@RequestParam(defaultValue = "**") String pattern) throws Exception {
		return fileService.delete(pattern);
	}
}
