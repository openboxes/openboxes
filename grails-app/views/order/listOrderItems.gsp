
<%@ page import="org.pih.warehouse.order.Order" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'order.label', default: 'Order')}" />
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
			<th> </th>
			<g:sortableColumn property="order"
				title="${message(code: 'orderItem.order.label', default: 'Order')}" />

			<g:sortableColumn property="description"
				title="${message(code: 'orderItem.description.label', default: 'Description')}" />
				
			<g:sortableColumn property="status"
				title="${message(code: 'orderItem.status.label', default: 'Status')}" />
				
		</tr>
	</thead>
	<tbody>
		<g:each in="${orderItems}" status="i" var="orderItem">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td>
					
				</td>
				<td>		
					<g:link controller="order" action="show" id="${orderItem?.order?.id}">	
						${fieldValue(bean: orderItem, field: "order.description")}
					</g:link>
				</td>
				<td>
					${fieldValue(bean: orderItem, field: "description")}
				</td>
				<td>
					${fieldValue(bean: orderItem, field: "quantity")}
				</td>
				<td>
					${(orderItem?.isComplete())?"Complete":"Pending" }
				</td>

			</tr>
		</g:each>
	</tbody>
</table>

			</div>
        </div>
    </body>
</html>
