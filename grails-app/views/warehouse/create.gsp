
<%@ page import="org.pih.warehouse.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
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
                                    <g:select name="manager.id" from="${org.pih.warehouse.User.list()}" optionKey="id" value="${warehouseInstance?.manager?.id}"  />
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
