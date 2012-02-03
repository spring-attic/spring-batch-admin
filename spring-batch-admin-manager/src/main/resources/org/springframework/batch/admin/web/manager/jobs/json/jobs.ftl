<#import "/spring.ftl" as spring />
<#assign url><@spring.url relativeUrl="${servletPath}/jobs.json"/></#assign>
"jobs" : { 
    "resource" : "${baseUrl}${url}",
    "registrations" : {
<#if jobs?? && jobs?size!=0>
    <#list jobs as job>
        "${job.name}" : {
            <#assign job_url><@spring.url relativeUrl="${servletPath}/jobs/${job.name}.json"/></#assign>
            "name" : "${job.name}",
            "resource" : "${baseUrl}${job_url}",
            "description" : "<@spring.messageText code="${job.name}.description" text="No description"/>",
            "executionCount" : ${job.executionCount?c},
            "launchable" : <#if job.launchable??>${job.launchable?string}<#else>false</#if>,
            "incrementable" : <#if job.incrementable??>${job.incrementable?string}<#else>false</#if>
        }<#if job_index != jobs?size-1>,</#if>
    </#list>
</#if>
     }
  }<#if nextJob?? || previousJob??>,
  "page" : {
      "start" : ${startJob?c},
      "end" : ${endJob?c},
      "total" : ${totalJobs?c}<#if nextJob??>, 
      "next" : "${baseUrl}${url}?startJob=${nextJob?c}&pageSize=${pageSize!20}"</#if><#if previousJob??>,
      "previous" : "${baseUrl}${url}?startJob=${previousJob?c}&pageSize=${pageSize!20}"</#if>
  }
</#if>