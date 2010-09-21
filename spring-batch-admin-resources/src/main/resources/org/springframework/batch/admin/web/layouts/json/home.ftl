<#import "/spring.ftl" as spring />
  "feed" : {
<#assign url><@spring.url relativeUrl="${servletPath}/home.json"/></#assign>
	"link" : "${baseUrl}${url}",
	"description": "Spring Batch Admin is a web UI and JSON service for running and monitoring batch jobs.  The following URLs are available, and while they are not all capable of serving JSON content you can request JSON by adding '.json' to the URL (and if it is not supported the result will be HTML).",
	"resources" : {<#if resources??><#list resources as resource>
		<#assign url><@spring.url relativeUrl="${servletPath}${resource.url}"/></#assign>
		<#assign key>${resource.url}</#assign>
		<#assign code>${resource.method}${resource.url}</#assign>
		<#assign description><@spring.messageText code=code text=""/></#assign>
		<#if code?ends_with('.json')>
			<#assign code>${code?substring(0,code?last_index_of('.json'))}</#assign>
			<#assign key>${key?substring(0,key?last_index_of('.json'))}</#assign>
		<#else>
			<#assign url>${url}.json</#assign>
		</#if>
		<#if description?length==0>
			<#assign description><@spring.messageText code=code text=resource.description!""/></#assign>
		</#if>
		"${key}" : {
			"uri" : "${baseUrl}${url}",
			"method" : "${resource.method}",
			"description" : "${description}"
	 	}<#if resource_index != resources?size-1>,</#if></#list>
</#if>
	}
  }