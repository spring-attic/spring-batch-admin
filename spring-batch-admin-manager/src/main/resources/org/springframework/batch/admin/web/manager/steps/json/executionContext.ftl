<#import "/spring.ftl" as spring />
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecutionId}/steps/${stepExecutionId}/context.json"/></#assign>
"stepExecutionContext" : {
    "id" : "${stepExecutionId}",
    "jobExecutionId" : "${jobExecutionId}",
    "name" : "${stepName}",
    "resource" : "${baseUrl}${url}",
    "context" : "${stepExecutionContext}"
}