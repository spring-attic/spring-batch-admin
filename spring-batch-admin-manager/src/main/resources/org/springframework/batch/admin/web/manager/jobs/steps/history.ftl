<#import "/spring.ftl" as spring />
<div id="history">
	<h2>History of Step Execution for Step=${stepExecutionHistory.stepName}</h2>

	<p>Summary after total of ${stepExecutionHistory.count} executions:</p>
	<table title="Step Execution History"
		class="bordered-table">
		<tr>
			<th>Property</th>
			<th>Min</th>
			<th>Max</th>
			<th>Mean</th>
			<th>Sigma</th>
		</tr>
		<tr class="name-sublevel1-odd">
			<td>Duration per Read</td>
			<td>${stepExecutionHistory.durationPerRead.min}</td>
			<td>${stepExecutionHistory.durationPerRead.max}</td>
			<td>${stepExecutionHistory.durationPerRead.mean}</td>
			<td>${stepExecutionHistory.durationPerRead.standardDeviation}</td>
		</tr>
		<tr class="name-sublevel1-odd">
			<td>Duration</td>
			<td>${stepExecutionHistory.duration.min}</td>
			<td>${stepExecutionHistory.duration.max}</td>
			<td>${stepExecutionHistory.duration.mean}</td>
			<td>${stepExecutionHistory.duration.standardDeviation}</td>
		</tr>
		<tr class="name-sublevel1-even">
			<td>Commits</td>
			<td>${stepExecutionHistory.commitCount.min}</td>
			<td>${stepExecutionHistory.commitCount.max}</td>
			<td>${stepExecutionHistory.commitCount.mean}</td>
			<td>${stepExecutionHistory.commitCount.standardDeviation}</td>
		</tr>
		<tr class="name-sublevel1-odd">
			<td>Rollbacks</td>
			<td>${stepExecutionHistory.rollbackCount.min}</td>
			<td>${stepExecutionHistory.rollbackCount.max}</td>
			<td>${stepExecutionHistory.rollbackCount.mean}</td>
			<td>${stepExecutionHistory.rollbackCount.standardDeviation}</td>
		</tr>
		<tr class="name-sublevel1-even">
			<td>Reads</td>
			<td>${stepExecutionHistory.readCount.min}</td>
			<td>${stepExecutionHistory.readCount.max}</td>
			<td>${stepExecutionHistory.readCount.mean}</td>
			<td>${stepExecutionHistory.readCount.standardDeviation}</td>
		</tr>
		<tr class="name-sublevel1-odd">
			<td>Writes</td>
			<td>${stepExecutionHistory.writeCount.min}</td>
			<td>${stepExecutionHistory.writeCount.max}</td>
			<td>${stepExecutionHistory.writeCount.mean}</td>
			<td>${stepExecutionHistory.writeCount.standardDeviation}</td>
		</tr>
		<tr class="name-sublevel1-even">
			<td>Filters</td>
			<td>${stepExecutionHistory.filterCount.min}</td>
			<td>${stepExecutionHistory.filterCount.max}</td>
			<td>${stepExecutionHistory.filterCount.mean}</td>
			<td>${stepExecutionHistory.filterCount.standardDeviation}</td>
		</tr>
		<tr class="name-sublevel1-odd">
			<td>Read Skips</td>
			<td>${stepExecutionHistory.readSkipCount.min}</td>
			<td>${stepExecutionHistory.readSkipCount.max}</td>
			<td>${stepExecutionHistory.readSkipCount.mean}</td>
			<td>${stepExecutionHistory.readSkipCount.standardDeviation}</td>
		</tr>
		<tr class="name-sublevel1-even">
			<td>Write Skips</td>
			<td>${stepExecutionHistory.writeSkipCount.min}</td>
			<td>${stepExecutionHistory.writeSkipCount.max}</td>
			<td>${stepExecutionHistory.writeSkipCount.mean}</td>
			<td>${stepExecutionHistory.writeSkipCount.standardDeviation}</td>
		</tr>
		<tr class="name-sublevel1-odd">
			<td>Process Skips</td>
			<td>${stepExecutionHistory.processSkipCount.min}</td>
			<td>${stepExecutionHistory.processSkipCount.max}</td>
			<td>${stepExecutionHistory.processSkipCount.mean}</td>
			<td>${stepExecutionHistory.processSkipCount.standardDeviation}</td>
		</tr>
	</table>	
</div>
