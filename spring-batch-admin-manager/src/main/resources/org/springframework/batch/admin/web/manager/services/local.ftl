<#import "/spring.ftl" as spring />
<div id="local">	

	<#if request??>
		<@spring.bind path="request" />
		<@spring.showErrors separator="<br/>" classOrStyle="error" />
	</#if>
	
	<#assign status_url><@spring.url relativeUrl="/batch/services/local"/></#assign>
	<form id="statusForm" action="${status_url}" method="GET">
		<p>
			<input id="statusUrl" type="submit" value="Local"/>
			<script type="text/javascript">
				Spring.addDecoration(new Spring.AjaxEventDecoration({
					elementId : "statusUrl",
					event : "onclick",	
					formId: "statusForm",
					params : {
						fragments: "body"
					}
				}));
			</script>
			Press to get current service status.
		</p> 
	</form>

	<#if serviceStatus??>
		<p>Status: available=${serviceStatus.available?string}, load=${serviceStatus.load},  authoritative=${serviceStatus.authoritative?string}</p>
	</#if>
	
</div>
