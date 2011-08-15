
<%@ page import="org.pih.warehouse.order.Order" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'orders.label', default: 'Orders').toLowerCase()}" />
        <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">

				
				<div id="outgoingOrders" class="list">            
	           		<h3><warehouse:message code="order.ordersPlacedWith.label"/> ${session.warehouse?.name }</h3>
					<g:render template="list" model="[orderInstanceList:outgoingOrders,orderType:'outgoing']"/>
				</div>
				
				<br/>
				
				
				<div id="incomingOrders" class="list">            
	            	<h3><warehouse:message code="order.ordersPlacedBy.label"/> ${session.warehouse?.name }</h3>
					<g:render template="list" model="[orderInstanceList:incomingOrders,orderType:'incoming']"/>
				</div>
				
			</div>
        </div>
    </body>
</html>
