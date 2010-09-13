<#import "/spring.ftl" as spring />
<#if resources??>
	<table title="Available Resources"
		class="bordered-table">
		<tr>
			<th>Resource</th>
			<th>Method</th>
			<th>Description</th>
		</tr>
		<#list resources as resource>
			<#if resource_index % 2 == 0>
				<#assign rowClass="name-sublevel1-even" />
			<#else>
				<#assign rowClass="name-sublevel1-odd" />
			</#if>
			<tr class="${rowClass}">
				<#assign key>${resource.url}</#assign>
				<#assign code>${resource.method}${resource.url}</#assign>
				<#assign base_url><@spring.url relativeUrl="${servletPath}${key}"/></#assign>
				<td><#if !key?contains("{") && !key?contains("**") && resource.method=="GET"><a href="${base_url}">${key}</a><#else>${key}</#if></td>
				<td>${resource.method}</td>
				<td><@spring.messageText code=code text=resource.description!""/></td>
			</tr>
		</#list>
	</table>
<#else>
<p>No request method mappings were found.</p>
</#if>