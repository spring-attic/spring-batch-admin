<#import "/spring.ftl" as spring />
<channel>
  <title>Empty Feed from Spring Batch Admin</title>
  <#assign url><@spring.url relativeUrl="${servletPath}/"/></#assign>
  <link>${url}</link>
  <description>Should be overridden by host application</description>
</channel>
