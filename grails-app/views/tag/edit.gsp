
<%@ page import="org.pih.warehouse.core.Tag" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'tag.label', default: 'Tag')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${tagInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${tagInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form method="post" >
            	<fieldset>
	                <g:hiddenField name="id" value="${tagInstance?.id}" />
	                <g:hiddenField name="version" value="${tagInstance?.version}" />
	                <div class="dialog">
	                    <table>
	                        <tbody>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="tag"><warehouse:message code="tag.tag.label" default="Tag" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'tag', 'errors')}">
	                                    <g:textField name="tag" cols="40" rows="5" value="${tagInstance?.tag}" class="text" size="60" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="updatedBy"><warehouse:message code="tag.updatedBy.label" default="Updated By" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'updatedBy', 'errors')}">
	                                    <g:select name="updatedBy.id" from="${org.pih.warehouse.core.User.list()}" optionKey="id" value="${tagInstance?.updatedBy?.id}" noSelection="['null': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="createdBy"><warehouse:message code="tag.createdBy.label" default="Created By" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'createdBy', 'errors')}">
	                                    <g:select name="createdBy.id" from="${org.pih.warehouse.core.User.list()}" optionKey="id" value="${tagInstance?.createdBy?.id}" noSelection="['null': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="dateCreated"><warehouse:message code="tag.dateCreated.label" default="Date Created" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'dateCreated', 'errors')}">
	                                    <g:datePicker name="dateCreated" precision="minute" value="${tagInstance?.dateCreated}"  />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="lastUpdated"><warehouse:message code="tag.lastUpdated.label" default="Last Updated" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'lastUpdated', 'errors')}">
	                                    <g:datePicker name="lastUpdated" precision="minute" value="${tagInstance?.lastUpdated}"  />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="products"><warehouse:message code="tag.products.label" default="Products" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'products', 'errors')}">
                                    	<table>
			                                <g:each in="${tagInstance.products.sort()}" var="p">
			                                	<tr>
			                                    
			                                		<td>
			                                    		<g:link controller="inventoryItem" action="showStockCard" id="${p.id}">
			                                    			${p.productCode } ${p?.encodeAsHTML()}
			                                    		</g:link>
			                                    	</td>
			                                    </tr>
			                                </g:each>
                                		</table>
	                                </td>
	                            </tr>
	                        	                        
                            	<tr class="prop">
		                        	<td valign="top"></td>
		                        	<td valign="top">                        	
						                <div class="buttons">
						                    <g:actionSubmit class="save" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
						                    <g:actionSubmit class="delete" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
						                </div>
		    						</td>                    	
	                        	</tr>	                        
	                        </tbody>
	                    </table>
	                </div>
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
