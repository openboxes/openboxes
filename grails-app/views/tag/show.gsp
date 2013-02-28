
<%@ page import="org.pih.warehouse.core.Tag" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'tag.label', default: 'Tag')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="tag.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: tagInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="tag.tag.label" default="Tag" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: tagInstance, field: "tag")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="tag.updatedBy.label" default="Updated By" /></td>
                            
                            <td valign="top" class="value"><g:link controller="user" action="show" id="${tagInstance?.updatedBy?.id}">${tagInstance?.updatedBy?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="tag.createdBy.label" default="Created By" /></td>
                            
                            <td valign="top" class="value"><g:link controller="user" action="show" id="${tagInstance?.createdBy?.id}">${tagInstance?.createdBy?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="tag.dateCreated.label" default="Date Created" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${tagInstance?.dateCreated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="tag.lastUpdated.label" default="Last Updated" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${tagInstance?.lastUpdated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="tag.products.label" default="Products" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <table>
                                <g:each in="${tagInstance.products}" var="p">
                                	<tr>
                                    	<td>
                                    		${p.productCode }
                                    	</td>
                                		<td>
                                    		<g:link controller="product" action="show" id="${p.id}">${p?.encodeAsHTML()}</g:link>
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
					                <g:form>
					                    <g:hiddenField name="id" value="${tagInstance?.id}" />
					                    <g:actionSubmit class="edit" action="edit" value="${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}" />
					                    <g:actionSubmit class="delete" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					                </g:form>
					            </div>
							</td>
						</tr>                    
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
