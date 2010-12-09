<#import "/spring.ftl" as spring />
<script type="text/javascript">
	function transferJobParameters(jobParameters) {
		if (jobParameters!=null && $("#jobParameters")) {
			$("#jobParameters").text(jobParameters)
		}
	}
	$(function() {
		var jobParameters = {};
		<#if jobInfo?? && jobInstances?? && jobInstances?size!=0><#list jobInstances as jobInstanceInfo>
		jobParameters.instance${jobInstanceInfo.id?c} = "${jobInstanceInfo.jobParametersString?replace('\t','\\t')?replace('\n','\\n')?replace('\r','')}";
		</#list></#if>
		$('td.jobParameters')
			.mouseover(function() {$(this).css( 'cursor','pointer');})
			.click( function() {transferJobParameters(jobParameters[$(this).attr("id")]);})
	})
</script>
<div id="job">
	
	<#include "launch.ftl">
		
	<#if jobInfo?? && jobInstances?? && jobInstances?size!=0>
		
			<br/>
			<h2>Job Instances for Job (${jobInfo.name})</h2>
			
			<table title="Jobs Instances" class="bordered-table">
				<thead>
					<tr>
						<th>ID</th>
						<th>&nbsp;</th>
						<th>JobExecution Count</th>
						<th>Last JobExecution</th>
						<th>Parameters</th>
					</tr>
				</thead>
				<tbody>
					<#list jobInstances as jobInstanceInfo>
						<#if jobInstanceInfo_index % 2 == 0>
							<#assign rowClass="name-sublevel1-even"/>
						<#else>
							<#assign rowClass="name-sublevel1-odd"/>
						</#if>
						<#assign executions_url><@spring.url relativeUrl="${servletPath}/jobs/${jobInfo.name}/${jobInstanceInfo.id?c}"/></#assign>
						<tr class="${rowClass}">
							<td>${jobInstanceInfo.id}</td>
							<td><a href="${executions_url}">executions</a></td>
							<td>${jobInstanceInfo.jobExecutionCount}</td>
							<#if jobInstanceInfo.lastJobExecution??>
								<#assign execution_url><@spring.url relativeUrl="${servletPath}/jobs/executions/${jobInstanceInfo.lastJobExecution.id?c}"/></#assign>
								<td><a href="${execution_url}">${jobInstanceInfo.lastJobExecution.status}</a></td>
							<#else>
								<td>?</td>							
							</#if>
							<td id="instance${jobInstanceInfo.id}" class="jobParameters">${jobInstanceInfo.jobInstance.jobParameters}</td>
						</tr>
					</#list>
				</tbody>
			</table>
			<ul class="controlLinks">
				<li>Rows: ${startJobInstance}-${endJobInstance} of ${totalJobInstances}</li> 
				<#assign job_url><@spring.url relativeUrl="${servletPath}/jobs/${jobInfo.name}"/></#assign>
				<#if nextJobInstance??><li><a href="${job_url}?startJobInstance=${nextJobInstance?c}&pageSize=${pageSize!20}">Next</a></li></#if>
				<#if previousJobInstance??><li><a href="${job_url}?startJobInstance=${previousJobInstance?c}&pageSize=${pageSize!20}">Previous</a></li></#if>
				<!-- TODO: enable pageSize editing -->
				<li>Page Size: ${pageSize!20}</li>
			</ul>
	
			<p>The table above shows instances of this job with an indication of the status of the last execution.  
			If you want to look at all executions for <a href="${executions_url}">see here</a>.</p>
	
	<#else>
		<#if jobName??>
			<@spring.bind path="jobName" />
			<@spring.showErrors separator="<br/>" classOrStyle="error" /><br/>
		<#else>
			<p>There are no job instances for this job.</p>
		</#if>
	</#if>
	
</div><!-- jobs -->
