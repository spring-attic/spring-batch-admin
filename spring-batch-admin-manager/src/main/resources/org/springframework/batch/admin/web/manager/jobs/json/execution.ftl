<#import "/spring.ftl" as spring />
<#if jobExecutionInfo??>
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecutionInfo.id?c}.json"/></#assign>
"jobExecution" : { 
    "resource" : "${baseUrl}${url}",
    "status" : "${jobExecutionInfo.jobExecution.status}",
    "startTime" : "${jobExecutionInfo.startTime}",
    "duration" : "${jobExecutionInfo.duration}",
    "exitCode" : "${jobExecutionInfo.jobExecution.exitStatus.exitCode}",
    "exitDescription" : "${jobExecutionInfo.jobExecution.exitStatus.exitDescription}",
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/${jobExecutionInfo.name}/${jobExecutionInfo.jobId?c}.json"/></#assign>
    "jobInstance" : { "resource" : "${baseUrl}${url}" },
<#if stepExecutionInfos?? && stepExecutionInfos?size != 0>
    "stepExecutions" : {<#list stepExecutionInfos as execution>
        "${execution.name}" : {
       	    "status" : "${execution.status}",
	        <#if execution.status != "NONE">
	        <#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecutionInfo.id?c}/steps/${execution.id?c}.json"/></#assign>
	        "resource" : "${baseUrl}${url}",
	        </#if>
	        "readCount" : "${execution.stepExecution.readCount}",
	        "writeCount" : "${execution.stepExecution.writeCount}",
	        "commitCount" : "${execution.stepExecution.commitCount}",
	        "rollbackCount" : "${execution.stepExecution.rollbackCount}",
	        "duration" : "${execution.duration}"
	    }<#if execution_index != stepExecutionInfos?size-1>,</#if></#list>
    }
<#else>
    "stepExecutions" : {<#list jobExecutionInfo.jobExecution.stepExecutions as stepExecution><#assign steps_url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecutionInfo.id?c}/steps/${stepExecution.id?c}.json"/></#assign>
        "${stepExecution.stepName}" : { "resource" : "${baseUrl}${steps_url}"}<#if stepExecution_index != jobExecutionInfo.jobExecution.stepExecutions?size-1>,</#if></#list>
    }
</#if>
  }
</#if>