<#import "/spring.ftl" as spring />
<#if jobInfo??>
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/${jobInfo.name}.json"/></#assign>
"job" : { 
    "resource" : "${baseUrl}${url}",
    "name" : "${jobInfo.name}",
    "jobInstances" : {
<#if jobInstances?? && jobInstances?size!=0>
    <#list jobInstances as jobInstanceInfo>
        "${jobInstanceInfo.id?c}" : {
            <#assign job_url><@spring.url relativeUrl="${servletPath}/jobs/${jobInfo.name}/${jobInstanceInfo.id?c}/executions.json"/></#assign>
            "resource" : "${baseUrl}${job_url}",
            "executionCount" : ${jobInstanceInfo.jobExecutionCount}<#if jobInstanceInfo.lastJobExecution??>,
            <#assign execution_url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobInstanceInfo.lastJobExecution.id?c}.json"/></#assign>
            "lastJobExecution" : "${baseUrl}${execution_url}",
            "lastJobExecutionStatus" : "${jobInstanceInfo.lastJobExecution.status}"</#if>
        }<#if jobInstanceInfo_index != jobInstances?size-1>,</#if>
    </#list>
</#if>
     }<#if nextJobInstance?? || previousJobInstance??>,
	<#assign executions_url><@spring.url relativeUrl="${servletPath}/jobs/${jobInfo.name}.json"/></#assign>
    "page" : {
        "start" : ${startJobInstance?c},
        "end" : ${endJobInstance?c},
        "total" : ${totalJobInstances?c}<#if nextJobInstance??>, 
        "next" : "${baseUrl}${executions_url}?startJobInstance=${nextJobInstance?c}&pageSize=${pageSize!20}"</#if><#if previousJobInstance??>,
        "previous" : "${baseUrl}${executions_url}?startJobInstance=${previousJobInstance?c}&pageSize=${pageSize!20}"</#if>
    }
	</#if>
  }
</#if>