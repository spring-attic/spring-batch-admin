<#import "/spring.ftl" as spring />
<#macro url value>${springMacroRequestContext.getContextPath()}${value?html}</#macro>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Spring Batch Admin</title>
<#assign url><@spring.url relativeUrl="/resources/dijit/themes/tundra/tundra.css"/></#assign>
<link rel="stylesheet" href="${url}" type="text/css"></link>
<#assign url><@spring.url relativeUrl="/resources/styles/main.css"/></#assign>
<link rel="stylesheet" href="${url}" type="text/css"></link>
<#assign url><@spring.url relativeUrl="/resources/styles/colors.css"/></#assign>
<link rel="stylesheet" href="${url}" type="text/css"></link>
<#assign url><@spring.url relativeUrl="/resources/styles/local.css"/></#assign>
<link rel="stylesheet" href="${url}" type="text/css"></link>
<#assign url><@spring.url relativeUrl="/resources/styles/print.css"/></#assign>
<link rel="stylesheet" href="${url}" type="text/css" media="print"></link>
<#assign url><@spring.url relativeUrl="/resources/dojo/dojo.js"/></#assign>
<script type="text/javascript" src="${url}"></script>
<#assign url><@spring.url relativeUrl="/resources/spring/Spring.js"/></#assign>
<script type="text/javascript" src="${url}"></script>
<#assign url><@spring.url relativeUrl="/resources/spring/Spring-Dojo.js"/></#assign>
<script type="text/javascript" src="${url}"></script>
<#if feedPath??>
<#assign url><@spring.url relativeUrl="${feedPath}"/></#assign>
	<link rel="alternate" type="application/rss+xml" title="RSS Feed" href="${url}">
</#if>
<!-- 
Some icons from Silk icon set 1.3 by Mark James, http://www.famfamfam.com/lab/icons/silk/
 -->
</head>
<body class="main tundra">
<div id="page"><#include "header.ftl"> <#include "navigation.ftl">
<div id="container">
<#if side??>
<div id="secondary-navigation">
	<#include "${side}">
</div>
</#if>
<#assign class><#if side??><#else>class="no-side-nav"</#if></#assign>
<div id="content" ${class}>
<div id="body"><#include "${body}"></div>
</div>
<!-- /content --></div>
<!-- /container --> <#include "footer.ftl"></div>
<!-- /page -->
<script type="text/javascript">
			dojo.require("dojo.NodeList-fx");
			dojo.addOnLoad(function(){
				<#assign url><@spring.url relativeUrl="/resources/images/ajax-loader.gif"/></#assign>
				var loadingImg = "<img id='loading_indicator' src='${url}' alt='Loading...' />";
				var parentNode = dojo.query("#content h1").slice(0,1);
				parentNode.addClass('loading_indicator_parent');
				parentNode.addContent(loadingImg);
				var loadingNode = dojo.query('#loading_indicator');
				var fadeInAnimation = loadingNode.fadeIn();
				var fadeOutAnimation = loadingNode.fadeOut();
				dojo.connect(Spring.remoting, "getResource", fadeInAnimation, "play");
				dojo.connect(Spring.remoting, "submitForm", fadeInAnimation, "play");
				dojo.connect(Spring.remoting, "handleResponse", fadeOutAnimation, "play");
				dojo.connect(Spring.remoting, "handleError", fadeOutAnimation, "play");
			});
		</script>
</body>
</html>
