
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${warehouseInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${warehouseInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${warehouseInstance?.id}" />
                <g:hiddenField name="version" value="${warehouseInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="warehouse.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${warehouseInstance?.name}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="manager"><g:message code="warehouse.manager.label" default="Manager" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'manager', 'errors')}">
                                    <g:select name="manager.id" from="${org.pih.warehouse.core.User.list()}" optionKey="id" value="${warehouseInstance?.manager?.id}"  />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="manager"><g:message code="warehouse.manager.label" default="Managed Locally" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'managedLocally', 'errors')}">
                                    <g:textField name="managedLocally" value="${warehouseInstance?.managedLocally}" />
                                </td>
                            </tr>
                            <tr>
                            	<td valign="top"></td>
                            	<td valign="top">
					                <div class="buttons">
					                    <g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
					                    <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
					                </div>                            	
                            	</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </g:form>
        </div>
    </body>
</html>
