<#import "/spring.ftl" as spring />
{
  "feed" : {
<#assign url><@spring.url relativeUrl="/"/></#assign>
	"link" : "${url}",
	"description": "Should be overridden by host application"
  }
}