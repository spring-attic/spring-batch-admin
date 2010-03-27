<ul>
	<#assign url><@spring.url relativeUrl="${servletPath}/files"/></#assign>
	<li><a href="${url}">Files</a></li>	
	<#assign url><@spring.url relativeUrl="${servletPath}/configuration"/></#assign>
	<li><a href="${url}">Configuration</a></li>	
</ul>