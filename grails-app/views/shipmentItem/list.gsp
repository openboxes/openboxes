
<%@ page import="org.pih.warehouse.shipping.ShipmentItem" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem')}" />
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
	            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="['shipmentItem']"/></g:link>
	            	</span>
            	</div>
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${warehouse.message(code: 'shipmentItem.id.label', default: 'Id')}" />
                        
                            <th><warehouse:message code="shipmentItem.container.label" default="Container" /></th>
                   	    
                            <th><warehouse:message code="shipmentItem.product.label" default="Product" /></th>
                   	    
                            <g:sortableColumn property="lotNumber" title="${warehouse.message(code: 'shipmentItem.lotNumber.label', default: 'Lot Number')}" />
                        
                            <g:sortableColumn property="expirationDate" title="${warehouse.message(code: 'shipmentItem.expirationDate.label', default: 'Expiration Date')}" />
                        
                            <g:sortableColumn property="quantity" title="${warehouse.message(code: 'shipmentItem.quantity.label', default: 'Quantity')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${shipmentItemInstanceList}" status="i" var="shipmentItemInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="edit" id="${shipmentItemInstance.id}">${fieldValue(bean: shipmentItemInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: shipmentItemInstance, field: "container")}</td>
                        
                            <td>${fieldValue(bean: shipmentItemInstance, field: "product")}</td>
                        
                            <td>${fieldValue(bean: shipmentItemInstance, field: "lotNumber")}</td>
                        
                            <td><format:date obj="${shipmentItemInstance.expirationDate}" /></td>
                        
                            <td>${fieldValue(bean: shipmentItemInstance, field: "quantity")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${shipmentItemInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
