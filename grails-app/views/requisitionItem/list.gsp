
<%@ page import="org.pih.warehouse.requisition.RequisitionItem" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div>            	
	            	<span class="linkButton">
	            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="['requisitionItem']"/></g:link>
	            	</span>
            	</div>
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${warehouse.message(code: 'requisitionItem.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="description" title="${warehouse.message(code: 'requisitionItem.description.label', default: 'Description')}" />
                        
                            <th><warehouse:message code="requisitionItem.category.label" default="Category" /></th>
                   	    
                            <th><warehouse:message code="requisitionItem.product.label" default="Product" /></th>
                   	    
                            <th><warehouse:message code="requisitionItem.productGroup.label" default="Product Group" /></th>
                   	    
                            <th><warehouse:message code="requisitionItem.inventoryItem.label" default="Inventory Item" /></th>
                   	    
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${requisitionItemInstanceList}" status="i" var="requisitionItemInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="edit" id="${requisitionItemInstance.id}">${fieldValue(bean: requisitionItemInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: requisitionItemInstance, field: "description")}</td>
                        
                            <td>${fieldValue(bean: requisitionItemInstance, field: "category")}</td>
                        
                            <td>${fieldValue(bean: requisitionItemInstance, field: "product")}</td>
                        
                            <td>${fieldValue(bean: requisitionItemInstance, field: "productGroup")}</td>
                        
                            <td>${fieldValue(bean: requisitionItemInstance, field: "inventoryItem")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${requisitionItemInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
