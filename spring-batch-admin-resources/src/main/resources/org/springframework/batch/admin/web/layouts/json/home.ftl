<#import "/spring.ftl" as spring />
{
  "feed" : {
<#assign url><@spring.url relativeUrl="${servletPath}/home.json"/></#assign>
	"link" : "${baseUrl}${url}",
	"description": "Spring Batch Admin is a web UI and JSON service for running and monitoring batch jobs.  The following URLs are available, and while they are not all capable of serving JSON content you can request JSON by adding '.json' to the URL (and if it is not supported the result will be HTML).",
	"resources" : {<#if resources??><#list resources as resource>
		"${resource.url}" : {
		<#assign url><@spring.url relativeUrl="${servletPath}${resource.url}"/></#assign>
			"uri" : "${baseUrl}${url}",
			"method" : "${resource.method}",
			"description" : ""
	 	}<#if resource_index != resources?size-1>,</#if></#list>
</#if>
	}
  }
}