<#import "/spring.ftl" as spring />
<div id="step-executions">

	<#if jobExecutionInfo??>
		<#assign job_info> for Job = ${jobExecutionInfo.name}, JobExecution = ${jobExecutionInfo.id}</#assign>
	</#if>

	<#if stepExecutions?? && stepExecutions?size!=0>
		<h2>Step Executions${job_info!}</h2>
	
		<table title="Step Executions"
			class="bordered-table">
			<tr>
				<th>ID</th>
				<th/>
				<th>Job&nbsp;Execution</th>
				<th>Name</th>
				<th>Date</th>
				<th>Start</th>
				<th>Duration</th>
				<th>Status</th>
				<th>Reads</th>
				<th>Writes</th>
				<th>Skips</th>
				<th>ExitCode</th>
			</tr>
			<#list stepExecutions as execution>
				<#if execution_index % 2 == 0>
					<#assign rowClass="name-sublevel1-even" />
				<#else>
					<#assign rowClass="name-sublevel1-odd" />
				</#if>
				<tr class="${rowClass}">
					<td>${execution.id}</td>
					<#assign execution_url><@spring.url relativeUrl="${servletPath}/jobs/executions/${execution.jobExecutionId?c}/steps/${execution.id?c}/progress"/></#assign>
					<td><a href="${execution_url}">detail</a></td>
					<td>${execution.jobExecutionId}</td>
					<td>${execution.name}</td>
					<td>${execution.startDate}</td>
					<td>${execution.startTime}</td>
					<td>${execution.duration}</td>
					<td>${execution.stepExecution.status}</td>
					<td>${execution.stepExecution.readCount}</td>
					<td>${execution.stepExecution.writeCount}</td>
					<td>${execution.stepExecution.skipCount}</td>
					<td>${execution.stepExecution.exitStatus.exitCode}</td>
				</tr>
			</#list>
		</table>

	<#else>
		<p>There are no step executions to display ${job_info!}.</p>
	</#if>
	
</div><!-- step-executions -->
