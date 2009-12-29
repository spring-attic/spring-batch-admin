<#import "/spring.ftl" as spring />
<channel>
	<title>Message Feed from SpringSource Batch</title>
	<#assign url>${baseUrl}<@spring.url relativeUrl="${servletPath}/messages"/></#assign>
	<link>${url}</link>
	<description>Recent Messages</description>
	<#if messages?? && messages?size!=0>
		<#if messages[0].headers.timeStamp??>
			<#assign pubdate>${messages[0].headers.timeStamp?string("yyyy-MM-dd'T'HH:mm:ssZ")}</#assign>
		<#else>
			<#assign pubdate>${currentTime?string("yyyy-MM-dd'T'HH:mm:ssZ")}</#assign>
		</#if>
		<lastBuildDate>${pubdate}</lastBuildDate>
		<#list messages as message>
			<item>
				<title>${message.headers.$id!}:(${message.payload!})</title>
				<#assign execution_url>${baseUrl}<@spring.url relativeUrl="${servletPath}/messages"/></#assign>
				<link>${execution_url}</link>
				<#if message.headers.timestamp??>
					<#assign pubdate>${message.headers.timestamp?string("yyyy-MM-dd'T'HH:mm:ssZ")}</#assign>
				<#else>
					<#assign pubdate>${currentTime?string("yyyy-MM-dd'T'HH:mm:ssZ")}</#assign>
				</#if>
				<pubDate>${pubdate}</pubDate>
				<description>${message.payload!}</description>
			</item>
		</#list>
	<#else>
		<lastBuildDate>2008-12-31T00:00:00+0000</lastBuildDate>	
	</#if>
</channel>
