<#import "/spring.ftl" as spring />
<div id="messages">

	<h2>Recent Messages</h2>
	
	<#if messages?? && messages?size!=0>
		<p/>
		<table title="Recent Messages"
			class="bordered-table">
			<tr>
				<th>ID</th>
				<th>Timestamp</th>
				<th>Payload</th>
				<th>Correlation</th>
			</tr>
			<#list messages as message>
				<#if message_index % 2 == 0>
					<#assign rowClass="name-sublevel1-even" />
				<#else>
					<#assign rowClass="name-sublevel1-odd" />
				</#if>
				<tr class="${rowClass}">
					<td>${message.headers.$id!}</td>
					<td>${message.headers.timestamp!?datetime}</td>
					<td>${message.payload!}</td>
					<td>${message.headers.$correlationId!}</td>
				</tr>
			</#list>
		</table>

	<#else>
		<p>There are no messages to display.</p>
	</#if>

</div><!-- messages -->
