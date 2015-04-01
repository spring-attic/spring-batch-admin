<#import "/spring.ftl" as spring />
<#if jobExecutionInfo??>
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecutionInfo.id?c}.json"/></#assign>
"jobExecution" : { 
    "resource" : "${baseUrl}${url}",
    "id" : "${jobExecutionInfo.id?c}",
    "name" : "${jobExecutionInfo.name}",
    "status" : "${jobExecutionInfo.jobExecution.status}",
    "startDate" : "${jobExecutionInfo.startDate}",
    "startTime" : "${jobExecutionInfo.startTime}",
    "duration" : "${jobExecutionInfo.duration}",
    "exitCode" : "${jobExecutionInfo.jobExecution.exitStatus.exitCode}",
    "exitDescription" : "${jobExecutionInfo.jobExecution.exitStatus.exitDescription?replace('\t','\\t')?replace('\n','\\n')?replace('\r','')?replace('\"','\\"')}",
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/${jobExecutionInfo.name}/${jobExecutionInfo.jobId?c}.json"/></#assign>
    "jobInstance" : { "resource" : "${baseUrl}${url}" },
<#if stepExecutionInfos?? && stepExecutionInfos?size != 0>
    "stepExecutions" : {<#list stepExecutionInfos as execution>
        "${execution.name}" : {
       	    "status" : "${execution.status}",
       	    "exitCode" : "${execution.exitCode}",
	        <#if execution.status != "NONE">
       	    "id" : "${execution.stepExecution.id?c}",
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
        "${stepExecution.stepName}" : { 
        	"resource" : "${baseUrl}${steps_url}",
        	"status" : "${stepExecution.status}",
 			"exitCode" : "${stepExecution.exitStatus.exitCode}"
        }<#if stepExecution_index != jobExecutionInfo.jobExecution.stepExecutions?size-1>,</#if></#list>
    }
</#if>
  }
</#if>