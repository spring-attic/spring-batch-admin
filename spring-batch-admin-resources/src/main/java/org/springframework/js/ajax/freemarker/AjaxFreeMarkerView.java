/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.js.ajax.freemarker;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.js.ajax.AjaxHandler;
import org.springframework.js.ajax.SpringJavascriptAjaxHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

/**
 * Tiles view implementation that is able to handle partial rendering for Spring
 * Javascript Ajax requests.
 * 
 * <p>
 * This implementation uses the {@link SpringJavascriptAjaxHandler} by default
 * to determine whether the current request is an Ajax request. On an Ajax
 * request, a "fragments" parameter will be extracted from the request in order
 * to determine which attributes to render from the current view.
 * </p>
 * 
 * @author Dave Syer
 */
public class AjaxFreeMarkerView extends FreeMarkerView {

	private static final String FRAGMENTS_PARAM = "fragments";

	private AjaxHandler ajaxHandler = new SpringJavascriptAjaxHandler();

	private ViewResolver viewResolver;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
	}

	public void setViewResolver(ViewResolver viewResolver) {
		this.viewResolver = viewResolver;
	}

	public AjaxHandler getAjaxHandler() {
		return ajaxHandler;
	}

	public void setAjaxHandler(AjaxHandler ajaxHandler) {
		this.ajaxHandler = ajaxHandler;
	}

	protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		if (ajaxHandler.isAjaxRequest(request, response)) {

			String[] attrNames = getRenderFragments(model, request, response);
			if (attrNames.length == 0) {
				logger.warn("An Ajax request was detected, but no fragments were specified to be re-rendered.  "
						+ "Falling back to full page render.");
				super.renderMergedTemplateModel(model, request, response);
			}

			// initialize the session before rendering any fragments. Otherwise
			// views that require the session which has
			// not otherwise been initialized will fail to render
			request.getSession();
			response.flushBuffer();
			for (int i = 0; i < attrNames.length; i++) {
				View fragmentView = null;

				try {
					fragmentView = (View) getApplicationContext().getBean(attrNames[i], View.class);
				}
				catch (Exception e) {
					if (getAttributesMap().containsKey(attrNames[i])) {
						String viewName = (String) getAttributesMap().get(attrNames[i]);
						// TODO: use a locale resolver
						fragmentView = viewResolver.resolveViewName(viewName, request.getLocale());
					}
				}

				if (fragmentView == null) {
					throw new ServletException("No View with a name of '" + attrNames[i] + "' could be found: " + this);
				}
				else {
					fragmentView.render(model, request, response);
				}
			}
		}
		else {
			super.renderMergedTemplateModel(model, request, response);
		}
	}

	protected String[] getRenderFragments(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
		String attrName = request.getParameter(FRAGMENTS_PARAM);
		String[] renderFragments = StringUtils.commaDelimitedListToStringArray(attrName);
		return StringUtils.trimArrayElements(renderFragments);
	}

}
