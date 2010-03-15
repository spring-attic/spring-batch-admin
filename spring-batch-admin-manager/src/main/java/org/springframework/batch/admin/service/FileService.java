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

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;

/**
 * Encapsulation of file storage and creation hiding the actual location of the
 * files, but allowing their contents to be accessed by clients.
 * 
 * @author Dave Syer
 * 
 */
public interface FileService {

	FileInfo createFile(String path) throws IOException;

	boolean publish(FileInfo target) throws IOException;

	List<FileInfo> getFiles(int startFile, int pageSize) throws IOException;

	int countFiles();

	int delete(String pattern) throws IOException;

	Resource getResource(String path);

}
