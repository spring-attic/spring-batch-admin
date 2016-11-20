<#import "/spring.ftl" as spring />
<#if stepExecutionContext??>
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecutionId}/steps/${stepExecutionId}/execution-context.json"/></#assign>
"stepExecutionContext" : {
    "id" : "${stepExecutionId}",
    "jobExecutionId" : "${jobExecutionId}",
    "name" : "${stepName}",
    "resource" : "${baseUrl}${url}",
    "context" : ${stepExecutionContext}
}
</#if>