<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <g:set var="pageTitle" value="${message(code: 'default.list.label' args="[entityName]")}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>        
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>                        
                            <g:sortableColumn property="name" title="${message(code: 'warehouse.name.label', default: 'Name')}" />
                            <th><g:message code="warehouse.manager.label" default="Manager" /></th>
                            <th><g:message code="warehouse.local.label" default="Managed Locally?" /></th>
                            <%-- 
                            <th><g:message code="warehouse.inventory.label" default="Inventory" /></th>
                            <th><g:message code="warehouse.transactions.label" default="Transactions" /></th>
                            --%>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${warehouseInstanceList}" status="i" var="warehouseInstance">
						<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td>
								<g:link action="show" id="${warehouseInstance.id}">${fieldValue(bean: warehouseInstance, field: "name")}</g:link>
							</td>
                            <td>${fieldValue(bean: warehouseInstance, field: "manager")}</td>                            
                            <td>${fieldValue(bean: warehouseInstance, field: "local")}</td>                            
                            
                            <%-- 
                            <td>
								<g:link action="showInventory" id="${warehouseInstance.id}">Show Inventory</g:link>
							</td>
                            <td>
								<g:link action="showTransactions" id="${warehouseInstance.id}">Show Transactions</g:link>
							</td>
							--%>
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
