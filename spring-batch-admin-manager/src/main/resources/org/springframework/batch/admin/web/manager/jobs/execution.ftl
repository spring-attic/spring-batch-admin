<#import "/spring.ftl" as spring />
<div id="job-execution">

	<#if jobExecutionInfo??>
		<h2>Details for Job Execution</h2>

		<#if jobExecutionInfo.stoppable || jobExecutionInfo.abandonable>
			<#assign execution_url><@spring.url relativeUrl="/batch/jobs/executions/${jobExecutionInfo.id?c}"/></#assign>
			<form id="stopForm" action="${execution_url}" method="post">
		
				<#if stopRequest??>
					<@spring.bind path="stopRequest" />
					<@spring.showErrors separator="<br/>" classOrStyle="error" /><br/>
				</#if>
		
				<#if jobExecutionInfo.abandonable>
					<#assign stop_label="Abandon"/> 
					<#assign stop_param="abandon"/>
				<#else>
					<#assign stop_label="Stop"/>
					<#assign stop_param="stop"/>
				</#if>
				<ol>
					<li>
					<input id="stop" type="submit" value="${stop_label}" name="${stop_param}" />
					<input type="hidden" name="_method" value="DELETE"/>
					</li>
				</ol>
				<script type="text/javascript">
					Spring.addDecoration(new Spring.AjaxEventDecoration({
						elementId : "stop",
						event : "onclick",
						formId: "stopForm",
						params : {
							fragments: "body"
						}
					}));
				</script>
			
			</form>
		</#if>

		<#if jobExecutionInfo.restartable>
			<#assign jobs_url><@spring.url relativeUrl="/batch/jobs/${jobExecutionInfo.name}/${jobExecutionInfo.jobId}/executions"/></#assign>
			<form id="restartForm" action="${jobs_url}" method="post">

				<ol>
					<li>
					<input id="restart" type="submit" value="Restart" name="restart" />
					<input type="hidden" name="origin" value="execution"/>
					</li>
				</ol>
				<script type="text/javascript">
					Spring.addDecoration(new Spring.AjaxEventDecoration({
						elementId : "restart",
						event : "onclick",
						formId: "restartForm",
						params : {
							fragments: "body"
						}
					}));
				</script>
			
			</form>
		</#if>

		<table title="Job Execution Details"
			class="bordered-table">
			<tr>
				<th>Property</th>
				<th>Value</th>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>ID</td>
				<td>${jobExecutionInfo.id}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<#assign job_url><@spring.url relativeUrl="/batch/jobs/${jobExecutionInfo.name}"/></#assign>
				<td>Job Name</td>
				<td><a href="${job_url}"/>${jobExecutionInfo.name}</a></td>
			</tr>
			<tr class="name-sublevel1-odd">
				<#assign job_url><@spring.url relativeUrl="/batch/jobs/${jobExecutionInfo.name}/${jobExecutionInfo.jobId}/executions"/></#assign>
				<td>Job Instance</td>
				<td><a href="${job_url}"/>${jobExecutionInfo.jobId}</a></td>
			</tr>
			<tr class="name-sublevel1-even">
				<td>Job Parameters</td>
				<td>${jobExecutionInfo.jobParameters}</td>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>Start Date</td>
				<td>${jobExecutionInfo.startDate}</td>
			</tr>
			<tr class="name-sublevel1-even>
				<td>Start Time</td>
				<td>${jobExecutionInfo.startTime}</td>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>Duration</td>
				<td>${jobExecutionInfo.duration}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<td>Status</td>
				<td>${jobExecutionInfo.jobExecution.status}</td>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>Exit Code</td>
				<td>${jobExecutionInfo.jobExecution.exitStatus.exitCode}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<td>Step Executions Count</td>
				<td>${jobExecutionInfo.stepExecutionCount}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<#assign executions_url><@spring.url relativeUrl="/batch/jobs/executions/${jobExecutionInfo.id?c}/steps"/></#assign>
				<td><a href="${executions_url}"/>Step Executions</a></td>
				<td>[<#list jobExecutionInfo.jobExecution.stepExecutions as stepExecution><#assign steps_url><@spring.url relativeUrl="/batch/jobs/executions/${jobExecutionInfo.id?c}/steps/${stepExecution.id?c}/progress"/></#assign><#if stepExecution_index != 0>,</#if><a href="${steps_url}"/>${stepExecution.stepName}</a></#list>]</td>
			</tr>
		</table>
	
	<#else>
		<p>There is no job execution to display.</p>
	</#if>
	
</div><!-- job-execution -->
