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
            <div class="list">
                <table>
                    <thead>
                        <tr>                        
                            <g:sortableColumn property="name" title="${message(code: 'warehouse.name.label', default: 'Name')}" />
                            <g:sortableColumn property="city" title="${message(code: 'warehouse.city.label', default: 'City')}" />
                            <g:sortableColumn property="country" title="${message(code: 'warehouse.country.label', default: 'Country')}" />
                            <th><g:message code="warehouse.manager.label" default="Manager" /></th>
                            <th><g:message code="warehouse.inventory.label" default="Inventory" /></th>
                            <th><g:message code="warehouse.transactions.label" default="Transactions" /></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${warehouseInstanceList}" status="i" var="warehouseInstance">
						<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td>
								<g:link action="show" id="${warehouseInstance.id}">${fieldValue(bean: warehouseInstance, field: "name")}</g:link>
							</td>
                            <td>${fieldValue(bean: warehouseInstance, field: "city")}</td>
                            <td>${fieldValue(bean: warehouseInstance, field: "country")}</td>                        
                            <td>${fieldValue(bean: warehouseInstance, field: "manager")}</td>                            
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
