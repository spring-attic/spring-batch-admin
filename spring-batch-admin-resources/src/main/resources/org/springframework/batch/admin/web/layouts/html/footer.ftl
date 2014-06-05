<#assign copyright><@spring.messageText code="copyright" text=copyright!"Copyright 2009-2014 Pivotal. All Rights Reserved."/></#assign>
<#assign company_url><@spring.messageText code="company.contact.url" text=companyContactUrl!"http://www.spring.io"/></#assign>
<#assign company_contact><@spring.messageText code="company.contact" text=companyContact!"Contact Spring"/></#assign>
<div id="footer-wrapper">
	<div id="footer-left">
		&copy; ${copyright}
	</div>
	<div id="footer-right">
		<a href="${company_url}">${company_contact}</a>
	</div> 
</div> <!-- /footer-wrapper -->
