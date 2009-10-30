<#import "/spring.ftl" as spring />
<channel>
  <title>Empty Feed from SpringSource Batch</title>
  <#assign url><@spring.url relativeUrl="/"/></#assign>
  <link>${url}</link>
  <description>Should be overridden by host application</description>
</channel>
