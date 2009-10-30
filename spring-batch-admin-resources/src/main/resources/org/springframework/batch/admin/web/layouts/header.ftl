<#import "/spring.ftl" as spring />
<#assign home_url><@spring.url relativeUrl="/"/></#assign>
<div id="header">
	<div id="name-and-company">
		<div id='site-name'>
			<a href=${home_url} title="Site Name" rel="home">
				Spring Batch Admin
			</a>
		</div>
		<div id='company-name'>
			<a href="http://www.springsource.com/" title="SpringSource">
				SpringSource
			</a>
		</div>         
	</div> <!-- /name-and-company -->
</div> <!-- /header -->
