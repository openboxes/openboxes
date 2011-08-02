
<%@ page import="org.pih.warehouse.inventory.InventoryLevel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel')}" />
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
                            <td valign="top" class="name"><warehouse:message code="inventoryLevel.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: inventoryLevelInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="inventoryLevel.product.label" default="Product" /></td>
                            
                            <td valign="top" class="value"><g:link controller="product" action="show" id="${inventoryLevelInstance?.product?.id}">${inventoryLevelInstance?.product?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="inventoryLevel.supported.label" default="Supported" /></td>
                            
                            <td valign="top" class="value"><g:formatBoolean boolean="${inventoryLevelInstance?.supported}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="inventoryLevel.minQuantity.label" default="Min Quantity" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: inventoryLevelInstance, field: "minQuantity")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="inventoryLevel.reorderQuantity.label" default="Reorder Quantity" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: inventoryLevelInstance, field: "reorderQuantity")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="inventoryLevel.dateCreated.label" default="Date Created" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${inventoryLevelInstance?.dateCreated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="inventoryLevel.inventory.label" default="Inventory" /></td>
                            
                            <td valign="top" class="value"><g:link controller="inventory" action="show" id="${inventoryLevelInstance?.inventory?.id}">${inventoryLevelInstance?.inventory?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="inventoryLevel.lastUpdated.label" default="Last Updated" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${inventoryLevelInstance?.lastUpdated}" /></td>
                            
                        </tr>
                    
                    
						<tr class="prop">
                        	<td valign="top"></td>
                        	<td valign="top">                         
					            <div class="buttons">
					                <g:form>
					                    <g:hiddenField name="id" value="${inventoryLevelInstance?.id}" />
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
