<#if jobInfo?? && jobInfo.launchable>
	<p/>
	<#assign launch_url><@spring.url relativeUrl="${servletPath}/jobs/${jobInfo.name}"/></#assign>
	<form id="launchForm" action="${launch_url}" method="POST">

		<#if launchRequest??>
			<@spring.bind path="launchRequest" />
			<@spring.showErrors separator="<br/>" classOrStyle="error" /><br/>
		</#if>

		<label for="launch">Job name=${jobInfo.name}</label><input id="launch" type="submit" value="Launch" name="launch" />
		<ol>
			<li><label for="jobParameters">Job Parameters (key=value
			pairs)</label><textarea id="jobParameters" name="jobParameters" class="jobParameters">${jobParameters}</textarea> 
			(<#if jobInfo.incrementable>Incrementable<#else>Not incrementable</#if>)</li>
		</ol>

		<br/><#if jobInfo.incrementable>
		<p>If the parameters are marked as "Incrementable" then the launch button launches either the <em>next</em> 
		instance of the job in the sequence defined by the incrementer, or if the last execution failed it restarts it.  
		The old parameters are shown above, and they will passed into the configured incrementer. You can always add 
		new parameters if you want to (but not to a restart).</p>
		<#else>
		<p>If the parameters are marked as "Not incrementable" then the launch button launches an 
		instance of the job with the parameters shown (which might be a restart if the last execution failed).
		You can always add new parameters if you want to (but not if you want to restart).</p>
		</#if>

		<input type="hidden" name="origin" value="job"/>
	</form>
	<script type="text/javascript">
		<#assign message><@spring.messageText code="invalid.job.parameters" text="Invalid Job Parameters (use comma or new-line separator)"/></#assign>
		$.validator.addMethod('jobParameters', function (value) { 
		    return !value || /([\w\.-_\)\(]+=[^,\n]*[,\n])*([\w\.-_\)\(]+=[^,]*$)/m.test(value); 
		}, '${message}');
		$(function(){
		   $("#launchForm").validate();
		});
	</script>
</#if>

