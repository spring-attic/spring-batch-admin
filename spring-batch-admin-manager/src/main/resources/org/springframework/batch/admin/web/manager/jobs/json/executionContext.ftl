<#import "/spring.ftl" as spring />
<#if jobExecutionContext??>
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/execution/${jobExecutionId}/context.json"/></#assign>
 "jobExecutionContext" : {
 "jobExecutionId" : "${jobExecutionId}",
 "context" : "${jobExecutionContext}"
}
</#if>
