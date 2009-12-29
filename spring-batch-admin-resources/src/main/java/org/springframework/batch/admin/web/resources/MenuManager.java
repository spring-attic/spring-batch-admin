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
package org.springframework.batch.admin.web.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aggregator for {@link Menu} contributions. Use
 * <code>menuManager.values</code> to get access to the contributed menu items.
 * 
 * @author Dave Syer
 * 
 */
@Component
public class MenuManager {

	private List<Menu> menus;

	/**
	 * The menus to manage. Autowired so the aggregation happens automatically
	 * by default.
	 * 
	 * @param menus the menus to set
	 */
	@Autowired
	public void setMenus(Collection<Menu> menus) {
		this.menus = new ArrayList<Menu>(menus);
		Collections.sort(this.menus, new MenuComparator());
	}

	public Collection<Menu> getMenus() {
		return menus;
	}

	private static class MenuComparator implements Comparator<Menu> {

		public int compare(Menu one, Menu two) {
			if (one.getOrder() == two.getOrder()) {
				return one.getLabel().compareTo(two.getLabel());
			}
			return one.getOrder() < two.getOrder() ? -1 : one.getOrder() > two.getOrder() ? 1 : 0;
		}

	}

}
