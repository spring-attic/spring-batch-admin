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

import org.springframework.ui.Model;

public class TableUtils {

	public static void addPagination(Model model, int total, int start, int number,
			String suffix) {
		model.addAttribute("total" + suffix + "s", total);
		model.addAttribute("start" + suffix, start + 1);
		int end = start + number;
		model.addAttribute("end" + suffix, end > total ? total : end);
		if (end < total) {
			model.addAttribute("next" + suffix, end);
		}
		if (start > 0) {
			int previous = start - number;
			model
					.addAttribute("previous" + suffix, previous < 0 ? 0
							: previous);
		}
	}


}
