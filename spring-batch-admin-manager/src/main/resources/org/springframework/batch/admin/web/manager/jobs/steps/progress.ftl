<#import "/spring.ftl" as spring />
<div id="progress">
	<h2>Step Execution Progress</h2>
	<p>${stepExecutionProgress.estimatedPercentCompleteMessage.defaultMessage}</p>
</div>
<#include "${history}">
<#include "${execution}">