<#import "/spring.ftl" as spring />
<#if jobInfo?? && jobInfo.jobInstanceId??>"jobInstance" : {
    "id" : ${jobInfo.jobInstanceId?c}, 
    "jobName" : "${jobInfo.name}"<#if jobInstanceInfo??>,
    "jobParameters" : {<#assign params=jobInstanceInfo.jobParameters/><#list params?keys as param>
        "${param}" : "${params[param]}"<#if param_index != params?size-1>,</#if></#list>
      }</#if>
    },</#if>
    <#if jobExecutions?? && jobExecutions?size!=0>
    "jobExecutions" : {<#list jobExecutions as jobExecutionInfo><#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecutionInfo.id?c}.json"/></#assign>
        "${jobExecutionInfo.id}" : {
            "status" : "${jobExecutionInfo.jobExecution.status}",
            "startTime" : "${jobExecutionInfo.startTime}",
            "duration" : "${jobExecutionInfo.duration}",
            "resource" : "${baseUrl}${url}"
        }<#if jobExecutionInfo_index != jobExecutions?size-1>,</#if></#list>
    }<#if nextJobExecution?? || previousJobExecution??>,
	<#assign executions_url><@spring.url relativeUrl="${servletPath}/jobs/executions.json"/></#assign>
    "page" : {
        "start" : ${startJobExecution?c},
        "end" : ${endJobExecution?c},
        "total" : ${totalJobExecutions?c}<#if nextJobExecution??>, 
        "next" : "${baseUrl}${executions_url}?startJobExecution=${nextJobExecution?c}&pageSize=${pageSize!20}"</#if><#if previousJobExecution??>,
        "previous" : "${baseUrl}${executions_url}?startJobExecution=${previousJobExecution?c}&pageSize=${pageSize!20}"</#if>
    }
	</#if>
</#if>