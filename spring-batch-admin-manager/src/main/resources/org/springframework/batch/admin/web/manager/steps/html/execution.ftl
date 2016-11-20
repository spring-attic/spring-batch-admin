<#import "/spring.ftl" as spring />
<div id="step-execution">

	<#if stepExecutionInfo??>
		<h2>Details for Step Execution</h2>

		<table title="Step Execution Details"
			class="bordered-table">
			<tr>
				<th>Property</th>
				<th>Value</th>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>ID</td>
				<td>${stepExecutionInfo.id}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<#assign execution_url><@spring.url relativeUrl="${servletPath}/jobs/executions/${stepExecutionInfo.jobExecutionId?c}"/></#assign>
				<td>Job Execution</td>
				<td><a href="${execution_url}">${stepExecutionInfo.jobExecutionId}</a></td>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>Job Name</td>
				<td>${stepExecutionInfo.jobName}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<td>Step Name</td>
				<td>${stepExecutionInfo.name}</td>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>Start Date</td>
				<td>${stepExecutionInfo.startDate}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<td>Start Time</td>
				<td>${stepExecutionInfo.startTime}</td>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>Duration</td>
				<td>${stepExecutionInfo.duration}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<td>Status</td>
				<td>${stepExecutionInfo.stepExecution.status}</td>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>Reads</td>
				<td>${stepExecutionInfo.stepExecution.readCount}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<td>Writes</td>
				<td>${stepExecutionInfo.stepExecution.writeCount}</td>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>Filters</td>
				<td>${stepExecutionInfo.stepExecution.filterCount}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<td>Read Skips</td>
				<td>${stepExecutionInfo.stepExecution.readSkipCount}</td>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>Write Skips</td>
				<td>${stepExecutionInfo.stepExecution.writeSkipCount}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<td>Process Skips</td>
				<td>${stepExecutionInfo.stepExecution.processSkipCount}</td>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>Commits</td>
				<td>${stepExecutionInfo.stepExecution.commitCount}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<td>Rollbacks</td>
				<td>${stepExecutionInfo.stepExecution.rollbackCount}</td>
			</tr>
			<tr class="name-sublevel1-odd">
				<td>Exit Code</td>
				<td>${stepExecutionInfo.stepExecution.exitStatus.exitCode}</td>
			</tr>
			<tr class="name-sublevel1-even">
				<td>Exit Message</td>
				<td>${stepExecutionInfo.stepExecution.exitStatus.exitDescription!}</td>
			</tr>
		</table>
	
	<#else>
		<p>There is no job execution to display.</p>
	</#if>
	
</div><!-- job-execution -->
