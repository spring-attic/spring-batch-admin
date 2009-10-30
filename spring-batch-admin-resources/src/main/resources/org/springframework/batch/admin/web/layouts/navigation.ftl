<div id="primary-navigation">
	<div id="primary-left">
<ul>
	<#list menuManager.menus as menu>
	<#assign menu_url><@spring.url relativeUrl="${menu.url}"/></#assign>
	<li><a href="${menu_url}">${menu.label}</a></li>
	</#list>
</ul>
	</div>
	<div id="primary-right">
		<ul>
			<li><a href="http://www.springsource.com">SpringSource</a></li>
			<li><a href="http://static.springframework.org/spring-batch">Spring Batch</a></li>
		</ul>
	</div>
</div><!-- /primary-navigation -->