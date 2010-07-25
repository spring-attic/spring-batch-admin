<#import "/spring.ftl" as spring />
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecutionInfo.id?c}.json"/></#assign>
{ "jobExecution" : { 
    "resource" : "${baseUrl}${url}",
    "status" : "${jobExecutionInfo.jobExecution.status}",
    "startTime" : "${jobExecutionInfo.startTime}",
    "duration" : "${jobExecutionInfo.duration}",
    "exitCode" : "${jobExecutionInfo.jobExecution.exitStatus.exitCode}",
    "exitDescription" : "${jobExecutionInfo.jobExecution.exitStatus.exitDescription}",
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/${jobExecutionInfo.name}/${jobExecutionInfo.jobId?c}/executions.json"/></#assign>
    "jobInstance" : { "resource" : "${baseUrl}${url}" },
    "stepExecutions" : {<#list jobExecutionInfo.jobExecution.stepExecutions as stepExecution><#assign steps_url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecutionInfo.id?c}/steps/${stepExecution.id?c}.json"/></#assign>
        "${stepExecution.stepName}" : { "resource" : "${baseUrl}${steps_url}"}<#if stepExecution_index != jobExecutionInfo.jobExecution.stepExecutions?size-1>,</#if></#list>
    }
  }
}