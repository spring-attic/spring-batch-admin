<#import "/spring.ftl" as spring />
<#if job??>
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/${job.name}.json"/></#assign>
"job" : { 
    "resource" : "${baseUrl}${url}",
    "name" : "${job.name}",
    "jobInstances" : {
<#if job?? && jobInstances?? && jobInstances?size!=0>
    <#list jobInstances as jobInstanceInfo>
    <#assign params=jobInstanceInfo.jobInstance.jobParameters.parameters/>
        "${jobInstanceInfo.id?c}" : {
            <#assign job_url><@spring.url relativeUrl="${servletPath}/jobs/${job.name}/${jobInstanceInfo.id?c}/executions.json"/></#assign>
            "resource" : "${baseUrl}${job_url}",
            "executionCount" : ${jobInstanceInfo.jobExecutionCount},<#if jobInstanceInfo.lastJobExecution??>
            <#assign execution_url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobInstanceInfo.lastJobExecution.id?c}.json"/></#assign>
            "lastJobExecution" : "${execution_url}",
            "lastJobExecutionStatus" : "${jobInstanceInfo.lastJobExecution.status}",</#if>
            "jobParameters" : {<#list params?keys as param>
               "${param}" : "${params[param]}"<#if param_index != params?size-1>,</#if>
             </#list>
            }
        }<#if jobInstanceInfo_index != jobInstances?size-1>,</#if>
    </#list>
</#if>
     }
  }
</#if>