
<%@ page import="org.pih.warehouse.inventory.InventoryLevel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventoryLevel.label', default: 'InventoryLevel')}" />
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
            
				<div>            	
	            	<span class="menuButton">
	            		<g:link class="new" action="create"><g:message code="default.add.label" args="['inventoryLevel']"/></g:link>
	            	</span>
            	</div>
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'inventoryLevel.id.label', default: 'Id')}" />
                        
                            <th><g:message code="inventoryLevel.product.label" default="Product" /></th>
                   	    
                            <g:sortableColumn property="supported" title="${message(code: 'inventoryLevel.supported.label', default: 'Supported')}" />
                        
                            <g:sortableColumn property="minQuantity" title="${message(code: 'inventoryLevel.minQuantity.label', default: 'Min Quantity')}" />
                        
                            <g:sortableColumn property="reorderQuantity" title="${message(code: 'inventoryLevel.reorderQuantity.label', default: 'Reorder Quantity')}" />
                        
                            <g:sortableColumn property="dateCreated" title="${message(code: 'inventoryLevel.dateCreated.label', default: 'Date Created')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${inventoryLevelInstanceList}" status="i" var="inventoryLevelInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="edit" id="${inventoryLevelInstance.id}">${fieldValue(bean: inventoryLevelInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: inventoryLevelInstance, field: "product")}</td>
                        
                            <td><g:formatBoolean boolean="${inventoryLevelInstance.supported}" /></td>
                        
                            <td>${fieldValue(bean: inventoryLevelInstance, field: "minQuantity")}</td>
                        
                            <td>${fieldValue(bean: inventoryLevelInstance, field: "reorderQuantity")}</td>
                        
                            <td><format:date obj="${inventoryLevelInstance.dateCreated}" /></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${inventoryLevelInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
