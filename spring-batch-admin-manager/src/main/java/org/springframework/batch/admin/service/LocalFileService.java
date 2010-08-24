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
package org.springframework.batch.admin.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.Assert;

/**
 * An implementation of {@link FileService} that deals with files only in the
 * local file system. Files and triggers are created in subdirectories of the
 * Java temporary directory.
 * 
 * @author Dave Syer
 * 
 */
public class LocalFileService implements FileService, InitializingBean, ResourceLoaderAware {

	/**
	 * @author Dave Syer
	 * 
	 */
	private File outputDir = new File(System.getProperty("java.io.tmpdir", "/tmp"), "batch/files");

	private static final Log logger = LogFactory.getLog(LocalFileService.class);

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	private FileSender fileSender;

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setFileSender(FileSender fileSender) {
		this.fileSender = fileSender;
	}

	/**
	 * The output directory to store new files. Defaults to
	 * <code>${java.io.tmpdir}/batch/files</code>.
	 * @param outputDir the output directory to set
	 */
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.state(fileSender != null, "A FileSender must be provided");
		if (!outputDir.exists()) {
			Assert.state(outputDir.mkdirs(), "Cannot create output directory " + outputDir);
		}
		Assert.state(outputDir.exists(), "Output directory does not exist " + outputDir);
		Assert.state(outputDir.isDirectory(), "Output file is not a directory " + outputDir);
	}

	public FileInfo createFile(String path) throws IOException {

		path = sanitize(path);

		Assert.hasText(path, "The file path must not be empty");

		String name = path.substring(path.lastIndexOf("/") + 1);
		String parent = path.substring(0, path.lastIndexOf(name));
		if (parent.endsWith("/")) {
			parent = parent.substring(0, parent.length() - 1);
		}

		File directory = new File(outputDir, parent);
		directory.mkdirs();
		Assert.state(directory.exists() && directory.isDirectory(), "Could not create directory: " + directory);

		FileInfo result = new FileInfo(path);
		File dest = new File(outputDir, result.getFileName());
		dest.createNewFile();

		return result;

	}

	/**
	 * @param target the target file
	 * @return the path to the file from the base output directory
	 */
	private String extractPath(File target) {
		String outputPath = outputDir.getAbsolutePath();
		return target.getAbsolutePath().substring(outputPath.length() + 1).replace("\\", "/");
	}

	public boolean publish(FileInfo dest) throws IOException {
		String path = dest.getPath();
		fileSender.send(getResource(path).getFile());
		return true;
	}

	public int countFiles() {
		ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		Resource[] resources;
		try {
			resources = resolver.getResources("file:///" + outputDir.getAbsolutePath() + "/**");
		}
		catch (IOException e) {
			throw new IllegalStateException("Unexpected problem resolving files", e);
		}
		return resources.length;
	}

	public List<FileInfo> getFiles(int startFile, int pageSize) throws IOException {

		List<FileInfo> files = getFiles("**");

		String path = "";
		int count = 0;
		for (FileInfo info : files) {
			FileInfo shortInfo = info.shortPath();
			if (!path.equals(shortInfo.getPath())) {
				files.set(count, shortInfo);
				path = shortInfo.getPath();
			}
			count++;
		}

		return new ArrayList<FileInfo>(files.subList(startFile, Math.min(startFile + pageSize, files.size())));

	}

	private List<FileInfo> getFiles(String pattern) {
		ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		List<Resource> resources = new ArrayList<Resource>();

		if (!pattern.startsWith("/")) {
			pattern = "/" + outputDir.getAbsolutePath() + "/" + pattern;
		}
		if (!pattern.startsWith("file:")) {
			pattern = "file:///" + pattern;
		}

		try {
			resources = Arrays.asList(resolver.getResources(pattern));
		}
		catch (IOException e) {
			logger.debug("Cannot locate files " + pattern, e);
			return new ArrayList<FileInfo>();
		}

		List<FileInfo> files = new ArrayList<FileInfo>();
		for (Resource resource : resources) {
			File file;
			try {
				file = resource.getFile();
				if (file.isFile()) {
					FileInfo info = new FileInfo(extractPath(file));
					files.add(info);
				}
			}
			catch (IOException e) {
				logger.debug("Cannot locate file " + resource, e);
			}
		}

		Collections.sort(files);
		return new ArrayList<FileInfo>(files);

	}

	public int delete(String pattern) throws IOException {

		ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		if (!pattern.startsWith("/")) {
			pattern = "/" + outputDir.getAbsolutePath() + "/" + pattern;
		}
		if (!pattern.startsWith("file:")) {
			pattern = "file:///" + pattern;
		}

		Resource[] resources = resolver.getResources(pattern);

		int count = 0;
		for (Resource resource : resources) {
			File file = resource.getFile();
			if (file.isFile()) {
				count++;
				FileUtils.deleteQuietly(file);
			}
		}

		return count;

	}

	public Resource getResource(String path) {

		path = sanitize(path);
		FileInfo pattern = new FileInfo(path);
		List<FileInfo> files = getFiles(pattern.getPattern());
		FileInfo info = files.isEmpty() ? pattern : files.get(0);
		File file = new File(outputDir, info.getFileName());
		return new FileServiceResource(file, path);

	}

	public File getUploadDirectory() {
		return outputDir;
	}

	/**
	 * Normalize file separators to "/" and strip leading prefix and separators
	 * to create a simple relative path.
	 * 
	 * @param path the raw path
	 * @return a sanitized version
	 */
	private String sanitize(String path) {
		path = path.replace("\\", "/");
		if (path.startsWith("files:")) {
			path = path.substring("files:".length());
			while (path.startsWith("/")) {
				path = path.substring(1);
			}
		}
		return path;
	}

	private static class FileServiceResource extends FileSystemResource implements ContextResource {

		private final String path;

		public FileServiceResource(File file, String path) {
			super(file);
			this.path = path;
		}

		public String getPathWithinContext() {
			return path;
		}

	}

}
