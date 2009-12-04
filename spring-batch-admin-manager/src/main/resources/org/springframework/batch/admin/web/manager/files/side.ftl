<ul>
	<#assign url><@spring.url relativeUrl="/batch/messages"/></#assign>
	<li><a href="${url}">Messages</a></li>	
	<#assign url><@spring.url relativeUrl="/batch/files"/></#assign>
	<li><a href="${url}">Files</a></li>	
	<#assign url><@spring.url relativeUrl="/batch/configuration"/></#assign>
	<li><a href="${url}">Configuration</a></li>	
</ul>