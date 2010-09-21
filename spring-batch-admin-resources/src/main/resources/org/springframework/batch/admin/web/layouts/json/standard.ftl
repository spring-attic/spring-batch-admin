<#import "/spring.ftl" as spring />
<#assign content><#if body??><#include "${body}"></#if></#assign>
<#assign content>${content?trim}</#assign>
{<#if content?? && content?length gt 0>${content}</#if><#if errors?? && errors.allErrors?size!=0><#if content?? && content?length gt 0>,</#if>
  "errors" : {
  <#list errors.allErrors as error>
      "${error.code}" : "<@spring.messageText code=error.code text=error.defaultMessage/>"<#if error_index != errors.allErrors?size-1>,</#if>
  </#list>
  }</#if>
}