<#import "/spring.ftl" as spring />
	
<div id="status">

	<h1>Ping Test</h1>
	<div class="messages">This page is a "ping" test for a StepService
	deployed as part of the application.</div>
	
	<#assign local_url><@spring.url relativeUrl="/batch/services/local"/></#assign>
	<a id="local" href="${local_url}">Local service status
		<script type="text/javascript">
			new Spring.RemotingHandler().getLinkedResource("local", {fragments: "body"}, false);
		</script>
	</a>

	<#assign ping_url><@spring.url relativeUrl="/batch/services/remote"/></#assign>
	<a id="remote" href="${ping_url}">Remote service status
		<script type="text/javascript">
			new Spring.RemotingHandler().getLinkedResource("remote", {fragments: "body"}, false);
		</script>
	</a>

</div>