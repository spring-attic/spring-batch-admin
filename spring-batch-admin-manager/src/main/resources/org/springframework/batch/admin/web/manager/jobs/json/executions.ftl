<#import "/spring.ftl" as spring />
<#if jobInfo?? && jobInfo.jobInstanceId??>"jobInstance" : {
    "id" : ${jobInfo.jobInstanceId?c}, 
    "jobName" : "${jobInfo.name}"
    },</#if>
    <#if jobExecutions?? && jobExecutions?size!=0>
    "jobExecutions" : {<#list jobExecutions as jobExecutionInfo><#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecutionInfo.id?c}.json"/></#assign>
        "${jobExecutionInfo.id}" : {
            "status" : "${jobExecutionInfo.jobExecution.status}",
            "startTime" : "${jobExecutionInfo.startTime}",
            "duration" : "${jobExecutionInfo.duration}",
            "resource" : "${baseUrl}${url}"
        }<#if jobExecutionInfo_index != jobExecutions?size-1>,</#if></#list>
    }
</#if>