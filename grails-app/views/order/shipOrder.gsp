<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title><warehouse:message code="order.shipOrder.label" default="Ship Order" /></title>
</head>

<body>

	<div class="body">

		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${orderInstance}">
			<div class="errors">
				<g:renderErrors bean="${orderInstance}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${command}">
			<div class="errors">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>

		<div class="dialog">

			<g:render template="summary" model="[orderInstance:orderInstance]" />

			<g:form action="saveShipmentItems">
				<div class="box">
					<h2><warehouse:message code="order.shipOrder" default="Ship Order" /></h2>
					<g:hiddenField name="order.id" value="${orderInstance?.id}" />

					<table>
						<thead>
							<tr>
								<th width="60%"><g:message code="product.label"/></th>
								<th><g:message code="orderItem.quantityOrdered.label" default="Ordered"/></th>
								<th><g:message code="shipmentItem.quantityShipped.label" default="Shipped"/></th>
								<th><g:message code="orderItem.quantityRemaining.label" default="Remaining"/></th>
								<th><g:message code="orderItem.quantityToShip.label" default="Quantity To Ship"/></th>
							</tr>
						</thead>
						<tbody>
							<g:each var="orderItem" in="${orderInstance.orderItems.sort { it.id }}" status="status">
								<g:hiddenField name="orderItems[${status}].orderItem.id" value="${orderItem.id}"/>
								<tr class="${status%2?'odd':'even'}">
									<td class="middle">
										${orderItem.product}
									</td>
									<td class="middle">
										${orderItem.quantity}
									</td>
									<td class="middle">
										${orderItem.quantityFulfilled()}
									</td>
									<td class="middle">
										<g:if test="${orderItem.quantityRemaining()>0}">
											${orderItem?.quantityRemaining()}
										</g:if>
										<g:else>0</g:else>
									</td>
									<td>
										<input type="number" class="large text" size="3"
											   name="orderItems[${status}].quantityToShip"
											   min="0"
											   max="${orderItem.quantityRemaining()>0?orderItem.quantityRemaining():0}"
											   value="${orderItem.quantityRemaining()>0?orderItem.quantityRemaining():0}"/>
									</td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div>
				<div class="buttons">
					<div class="left">
						<g:link controller="purchaseOrderWorkflow"
								action="purchaseOrder"
								id="${orderInstance?.id}"
								event="enterOrderDetails"
								params="[skipTo:'items']"
								class="button">
							<img src="${resource(dir: 'images/icons/silk', file: 'resultset_previous.png')}" />&nbsp;
							<warehouse:message code="default.back.label" default="Back"/>
						</g:link>
					</div>
					<div class="right">
						<button type="submit" class="button">
							<warehouse:message code="default.button.next.label"/>&nbsp;
							<img src="${resource(dir: 'images/icons/silk', file: 'resultset_next.png')}" />&nbsp;
						</button>
					</div>
				</div>
			</g:form>
		</div>

	</div>
</body>
</html>
