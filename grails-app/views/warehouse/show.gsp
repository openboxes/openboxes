
<%@ page import="org.pih.warehouse.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
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
