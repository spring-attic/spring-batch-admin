<#import "/spring.ftl" as spring />
<#assign url><@spring.url relativeUrl="${servletPath}/files"/></#assign>
"files" : {
      "resource" : "${baseUrl}${url}",
      "uploaded" : {
<#if files?? && files?size!=0>
	<#list files as file>
		"${file.fileName}" : {
			"resource" : "${baseUrl}${url}/${file.path}",
			"local" : ${file.local?string},
			"path" : "files://${file.path?string}"
		}<#if file_index != files?size-1>,</#if>
	</#list>	
</#if>
      }
   }