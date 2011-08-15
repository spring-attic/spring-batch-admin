<#import "/spring.ftl" as spring />
<#if jobExecutionInfo??>"jobExecution" : {
    "id" : "${jobExecutionInfo.id?c}",
    "name" : "${jobExecutionInfo.name}",
    <#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecutionInfo.id?c}.json"/></#assign>
    "resource" : "${baseUrl}${url}"
},
</#if>
"stepExecutions" : {<#if stepExecutions?? && stepExecutions?size!=0>
	<#list stepExecutions as stepExecutionInfo>
	"${stepExecutionInfo.stepExecution.id?c}" : {
    <#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${stepExecutionInfo.jobExecutionId?c}/steps/${stepExecutionInfo.id?c}/progress"/></#assign>
    "id" : "${stepExecutionInfo.stepExecution.id?c}",
    "name" : "${stepExecutionInfo.name}",
    "resource" : "${baseUrl}${url}",
    "status" : "${stepExecutionInfo.stepExecution.status}",
    "startTime" : "${stepExecutionInfo.startTime}",
    "duration" : "${stepExecutionInfo.duration}",
    "readCount" : ${stepExecutionInfo.stepExecution.readCount?c},
    "writeCount" : ${stepExecutionInfo.stepExecution.writeCount?c},
    "filterCount" : ${stepExecutionInfo.stepExecution.filterCount?c},
    "readSkipCount" : ${stepExecutionInfo.stepExecution.readSkipCount?c},
    "writeSkipCount" : ${stepExecutionInfo.stepExecution.writeSkipCount?c},
    "processSkipCount" : ${stepExecutionInfo.stepExecution.processSkipCount?c},
    "commitCount" : ${stepExecutionInfo.stepExecution.commitCount?c},
    "rollbackCount" : ${stepExecutionInfo.stepExecution.rollbackCount?c},
    "exitCode" : "${stepExecutionInfo.stepExecution.exitStatus.exitCode}",
    "exitDescription" : "${stepExecutionInfo.stepExecution.exitStatus.exitDescription?replace('\t','\\t')?replace('\n','\\n')?replace('\r','')}"
}<#if stepExecutionInfo_index != stepExecutions?size-1>,</#if></#list>
</#if>
}