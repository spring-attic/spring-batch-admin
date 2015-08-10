<#import "/spring.ftl" as spring />
<channel>
	<title>JobExecution Feed from Spring Batch Admin</title>
	<#assign url>${baseUrl}<@spring.url relativeUrl="${servletPath}/jobs/executions"/></#assign>
	<link>${url}</link>
	<description>Recent and Current Job Executions</description>
	<#if jobExecutions?? && jobExecutions?size!=0>
		<#if jobExecutions[0].jobExecution.endTime??>
			<#assign pubdate>${jobExecutions[0].jobExecution.endTime?string("yyyy-MM-dd'T'HH:mm:ssZ")}</#assign>
		<#else>
			<#assign pubdate>${currentTime?string("yyyy-MM-dd'T'HH:mm:ssZ")}</#assign>
		</#if>
		<lastBuildDate>${pubdate}</lastBuildDate>
		<#list jobExecutions as execution>
			<item>
				<title>${execution.jobExecution.status}:(${execution.jobId},${execution.name},${execution.duration})</title>
				<#assign execution_url>${baseUrl}<@spring.url relativeUrl="${servletPath}/jobs/executions/${execution.id?c}"/></#assign>
				<link>${execution_url}</link>
				<#if execution.jobExecution.endTime??>
					<#assign pubdate>${execution.jobExecution.endTime?string("yyyy-MM-dd'T'HH:mm:ssZ")}</#assign>
				<#else>
					<#assign pubdate>${currentTime?string("yyyy-MM-dd'T'HH:mm:ssZ")}</#assign>
				</#if>
				<pubDate>${pubdate}</pubDate>
				<description>${execution.jobExecution}</description>
			</item>
		</#list>
	<#else>
		<lastBuildDate>2008-12-31T00:00:00+0000</lastBuildDate>	
	</#if>
</channel>
