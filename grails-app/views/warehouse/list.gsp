
<%@ page import="org.pih.warehouse.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>                        
                            <g:sortableColumn property="name" title="${message(code: 'warehouse.name.label', default: 'Name')}" />
                            <g:sortableColumn property="city" title="${message(code: 'warehouse.city.label', default: 'City')}" />
                            <g:sortableColumn property="country" title="${message(code: 'warehouse.country.label', default: 'Country')}" />
                            <th><g:message code="warehouse.manager.label" default="Manager" /></th>
                            <g:sortableColumn property="id" title="${message(code: 'warehouse.id.label', default: 'Show Details')}" />
                            <th><g:message code="warehouse.inventory.label" default="Inventory" /></th>
                            <th><g:message code="warehouse.transactions.label" default="Transactions" /></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${warehouseInstanceList}" status="i" var="warehouseInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td>${fieldValue(bean: warehouseInstance, field: "name")}</td>                        
                            <td>${fieldValue(bean: warehouseInstance, field: "city")}</td>
                            <td>${fieldValue(bean: warehouseInstance, field: "country")}</td>                        
                            <td>${fieldValue(bean: warehouseInstance, field: "manager")}</td>
                            <td>
			      <g:link action="show" id="${warehouseInstance.id}">Show Details</g:link>
			    </td>
                            <td>
			      <g:link action="showInventory" id="${warehouseInstance.id}">Show Inventory</g:link>
			    </td>
                            <td>
			      <g:link action="showTransactions" id="${warehouseInstance.id}">Show Transactions</g:link>
			    </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${warehouseInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
