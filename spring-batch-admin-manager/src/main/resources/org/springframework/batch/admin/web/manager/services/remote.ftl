<#import "/spring.ftl" as spring />
	
<div id="remote">
	
	<#assign ping_url><@spring.url relativeUrl="/batch/services/remote"/></#assign>
	<form id="pingForm" action="${ping_url}" method="GET">
		<p>
			<input id="executeUrl" type="submit" value="Remote" />
			Press to verify remote service execution.
		</p>
	</form>
	<script type="text/javascript">
		Spring.addDecoration(new Spring.AjaxEventDecoration({
			elementId : "executeUrl",
			event : "onclick",
			formId: "pingForm",
			params : {
				fragments: "body"
			}
		}));
	</script>

	<p>
		<#if response??>
			<#if response.rejected>
				Ping test passed OK:
			<#else>
				Ping test unexpected result:
			</#if>
			capacity=${response.capacity}, rejected=${response.rejected?string}
		</#if>
	</p>
</div>