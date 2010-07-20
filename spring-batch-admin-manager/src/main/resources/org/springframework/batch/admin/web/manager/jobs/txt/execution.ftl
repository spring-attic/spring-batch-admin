<#import "/spring.ftl" as spring />
<@spring.messageText code=titleCode!"title" text=titleText!"Spring Batch Admin"/>

<#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobExecution.id?c}.txt"/></#assign>
Job Execution: link=${baseUrl}${url}
  status=${jobExecution.status}
  exitCode=${jobExecution.exitStatus.exitCode}
  exitDescription=${jobExecution.exitStatus.exitDescription}
<#if jobExecutionInfo??>
  startTime=${jobExecutionInfo.startTime}
  duration=${jobExecutionInfo.duration}
</#if>
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/${jobExecution.jobInstance.jobName}/${jobExecution.jobId?c}/executions.txt"/></#assign>
Job Instance: link=${baseUrl}${url}