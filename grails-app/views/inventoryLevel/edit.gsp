
<%@ page import="org.pih.warehouse.inventory.InventoryLevel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${inventoryLevelInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${inventoryLevelInstance}" as="list" />
	            </div>
            </g:hasErrors>

            <div class="buttonBar">
                <g:link action="list" class="button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" default="List" args="[g.message(code: 'inventoryLevels.label')]"/>
                </g:link>
                <g:link action="create" class="button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                    <warehouse:message code="default.add.label" default="Add" args="[g.message(code: 'inventoryLevel.label')]"/>
                </g:link>

            </div>

            <div class="box">
                <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>

                <g:render template="../inventoryLevel/form" model="[productInstance:inventoryLevelInstance.product,inventoryLevelInstance:inventoryLevelInstance]"/>

            </div>


            <%--
            <g:form method="post" controller="inventoryLevel" action="save">
            	<fieldset>
	                <g:hiddenField name="id" value="${inventoryLevelInstance?.id}" />
	                <g:hiddenField name="version" value="${inventoryLevelInstance?.version}" />
                    <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
	                <div class="dialog">
	                    <table>
	                        <tbody>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="product"><warehouse:message code="inventoryLevel.product.label" default="Product" /></label>
	                                </td>
                                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'product', 'errors')}">
                                        <g:if test="${inventoryLevelInstance?.product}">
                                            <g:hiddenField name="product.id" value="${inventoryLevelInstance?.product?.id}"/>
                                            <format:product product="${inventoryLevelInstance?.product}"/>
                                        </g:if>
                                        <g:else>
                                            <g:select name="product.id" class="chzn-select" from="${org.pih.warehouse.product.Product.list()}" optionKey="id" value="${inventoryLevelInstance?.product?.id}"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="product.id"><warehouse:message code="inventoryLevel.status.label" default="Status" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'product', 'errors')}">
                                        <g:select name="status"
                                                  from="${org.pih.warehouse.inventory.InventoryStatus.list()}"
                                                  optionValue="${{format.metadata(obj:it)}}" value="${inventoryLevelInstance?.status}"
                                                  noSelection="['':warehouse.message(code:'inventoryLevel.chooseStatus.label')]" />
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="inventory.id"><warehouse:message code="inventoryLevel.inventory.label" default="Inventory" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'inventory', 'errors')}">
                                        <g:select name="inventory.id" from="${org.pih.warehouse.inventory.Inventory.list()}" optionKey="id" value="${inventoryLevelInstance?.inventory?.id}"  />
                                    </td>
                                </tr>


	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="minQuantity"><warehouse:message code="inventoryLevel.minQuantity.label" default="Min Quantity" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'minQuantity', 'errors')}">
	                                    <g:textField name="minQuantity" value="${inventoryLevelInstance?.minQuantity }" size="10" class="text"/>	                                    
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="reorderQuantity"><warehouse:message code="inventoryLevel.reorderQuantity.label" default="Reorder Quantity" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'reorderQuantity', 'errors')}">	                                    
	                                    <g:textField name="reorderQuantity" value="${inventoryLevelInstance?.reorderQuantity }" size="10" class="text"/>
	                                </td>
	                            </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="maxQuantity"><warehouse:message code="inventoryLevel.maximumQuantity.label" default="Max Quantity" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'maxQuantity', 'errors')}">
                                        <g:textField name="maxQuantity" value="${inventoryLevelInstance?.maxQuantity }" size="10" class="text"/>
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
            --%>
        </div>
    </body>
</html>
