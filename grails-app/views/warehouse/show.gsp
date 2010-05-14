<%@ page import="org.pih.warehouse.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.show.label" args="[entityName]" /></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>       
		<content tag="breadcrumb"><g:render template="breadcrumb" model="[warehouse:warehouseInstance,pageTitle:pageTitle]"/></content>
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
                            <td valign="top" class="name"><g:message code="warehouse.id.label" default="Id" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "id")}</td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="warehouse.name.label" default="Name" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "name")}</td>                            
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="warehouse.city.label" default="City" /></td>
                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "city")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="warehouse.country.label" default="Country" /></td>
                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "country")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="warehouse.manager.label" default="Manager" /></td>
                            <td valign="top" class="value"><g:link controller="user" action="show" id="${warehouseInstance?.manager?.id}">${warehouseInstance?.manager?.encodeAsHTML()}</g:link></td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="warehouse.inventory.label" default="Inventory" /></td>
                            <td valign="top" class="value">
			      <g:link controller="warehouse" action="showInventory" id="${warehouseInstance?.id}">${warehouseInstance?.inventory?.encodeAsHTML()}</g:link>
			    </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="warehouse.transactions.label" default="Transactions" /></td>
                            <td valign="top" class="value">
			      <table>
				<g:each in="${warehouseInstance.transactions}" var="transaction" status="i">
				  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				    <td>${fieldValue(bean: transaction, field: "id")}</td>
				    <td>${fieldValue(bean: transaction, field: "transactionDate")}</td>
				    <td>${fieldValue(bean: transaction, field: "inventory.id")}</td>
				    <td>${fieldValue(bean: transaction, field: "localWarehouse")}</td>

				  </tr>
				</g:each>				
			      </table>

			    </td>
                        </tr>


                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${warehouseInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
