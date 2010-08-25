
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.create.label" args="[entityName]" /></content>
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
            <g:hasErrors bean="${warehouseInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${warehouseInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
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
                                    <label for="city"><g:message code="warehouse.city.label" default="City" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'city', 'errors')}">
                                    <g:textField name="city" value="${warehouseInstance?.city}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="country"><g:message code="warehouse.country.label" default="Country" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'country', 'errors')}">
                                    <g:textField name="country" value="${warehouseInstance?.country}" />
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

                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
