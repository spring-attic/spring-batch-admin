<#import "/spring.ftl" as spring />
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${stepExecutionInfo.jobExecutionId?c}/steps/${stepExecutionInfo.id?c}/progress"/></#assign>
"stepExecution" : { 
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
    "exitDescription" : "${stepExecutionInfo.stepExecution.exitStatus.exitDescription}"
  },
<#assign url><@spring.url relativeUrl="${servletPath}/jobs/executions/${stepExecutionInfo.jobExecutionId?c}.json"/></#assign>
  "jobExecution" : { "resource" : "${baseUrl}${url}" }