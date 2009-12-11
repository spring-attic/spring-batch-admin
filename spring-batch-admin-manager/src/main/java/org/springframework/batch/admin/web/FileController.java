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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
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
public class FileController implements InitializingBean, ResourceLoaderAware {

	private File outputDir = new File(System.getProperty("java.io.tmpdir", "/tmp"), "batch/files");

	private File triggerDir = new File(System.getProperty("java.io.tmpdir", "/tmp"), "batch/triggers");

	private ResourceLoader resourceLoader;

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void afterPropertiesSet() throws Exception {
		if (!outputDir.exists()) {
			Assert.state(outputDir.mkdirs(), "Cannot create output directory " + outputDir);
		}
		Assert.state(outputDir.exists(), "Output directory does not exist " + outputDir);
		Assert.state(outputDir.isDirectory(), "Output file is not a directory " + outputDir);
	}

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

		File directory = new File(outputDir, path);
		directory.mkdir();
		Assert.state(directory.exists() && directory.isDirectory(), "Could not create directory: " + directory);

		File dest = File.createTempFile(file.getOriginalFilename() + getSuffix(), "", directory);
		try {
			file.transferTo(dest);
		}
		catch (IOException e) {
			errors.reject("file.upload.failed", new Object[] { file.getOriginalFilename() }, "File upload failed for "
					+ file.getOriginalFilename());
			return "files";
		}
		FileUtils.writeStringToFile(new File(triggerDir, dest.getName()), dest.getAbsolutePath());

		model.put("uploaded", dest.getAbsolutePath());
		return "redirect:files";

	}

	private String getSuffix() {
		return "." + (new SimpleDateFormat("yyyyMMdd").format(new Date())) + ".";
	}

	@RequestMapping(value = "/files", method = RequestMethod.GET)
	public void list(ModelMap model, @RequestParam(defaultValue = "0") int startFile,
			@RequestParam(defaultValue = "20") int pageSize) throws Exception {

		ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		Resource[] resources = resolver.getResources("file:///" + outputDir.getAbsolutePath() + "/**");
		int start = outputDir.getAbsolutePath().length();

		List<String> files = new ArrayList<String>();
		for (int i = startFile; i<startFile+pageSize && i<resources.length; i++) {
			Resource resource = resources[i];
			File file = resource.getFile();
			if (file.isFile()) {
				files.add(file.getAbsolutePath().substring(start + 1).replace("\\", "/"));
			}
		}
		Collections.sort(files);

		model.put("files", files);
		model.put("outputDir", outputDir.getAbsolutePath().replace("\\", "/"));
		model.put("triggerDir", triggerDir.getAbsolutePath().replace("\\", "/"));

	}

	@RequestMapping(value = "/files", method = RequestMethod.DELETE)
	public String deleteAll(ModelMap model) throws Exception {

		ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		Resource[] resources = resolver.getResources("file:///" + outputDir.getAbsolutePath() + "/**");

		for (Resource resource : resources) {
			File file = resource.getFile();
			if (file.isFile()) {
				FileUtils.deleteQuietly(file);
			}
		}

		model.put("files", new ArrayList<String>());
		model.put("outputDir", outputDir.getAbsolutePath().replace("\\", "/"));
		model.put("triggerDir", triggerDir.getAbsolutePath().replace("\\", "/"));
		
		return "redirect:files";

	}

}
