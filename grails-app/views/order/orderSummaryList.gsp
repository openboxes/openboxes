<%@ page import="org.pih.warehouse.order.OrderItemStatusCode; org.pih.warehouse.order.OrderTypeCode" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
		<g:set var="entityName" value="${warehouse.message(code: 'orderSummary.label', default: 'Order Summary')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
   	</head>
	<body>
		<div class="body">
			<div class="yui-gf">
				<div class="yui-u first">
					<g:render template="orderStatusFilters" model="[]"/>
				</div>
				<div class="yui-u">

					<div class="box">
						<h2>
							<warehouse:message code="default.list.label" args="[entityName]" />
						</h2>
						<table>
							<thead>
								<tr>
									<th>${warehouse.message(code: 'default.orderNumber.label', default: "Order Number")}</th>
									<th>${warehouse.message(code: 'default.orderStatus.label', default: "Order Status")}</th>
									<th>${warehouse.message(code: 'default.shipmentStatus.label', default: "Shipment Status")}</th>
									<th>${warehouse.message(code: 'default.receiptStatus.label', default: "Receipt Status")}</th>
									<th>${warehouse.message(code: 'default.paymentStatus.label', default: "Payment Status")}</th>
									<th>${warehouse.message(code: 'default.derivedStatus.label', default: "Derived Status")}</th>
								</tr>
							</thead>
							<tbody>
								<g:unless test="${orderSummaryList}">
									<tr class="prop">
										<td colspan="15">
											<div class="empty fade center">
												<warehouse:message code="orders.none.message"/>
											</div>
										</td>
									</tr>
								</g:unless>

								<g:each var="orderSummary" in="${orderSummaryList}" status="i">

									<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
										<td class="middle">
											<g:link action="show" id="${orderSummary?.id}">
												${fieldValue(bean: orderSummary, field: "orderNumber")}
											</g:link>
										</td>
										<td class="middle">
											${fieldValue(bean: orderSummary, field: "orderStatus")}
										</td>
										<td class="middle">
											${fieldValue(bean: orderSummary, field: "shipmentStatus")}
										</td>
										<td class="middle">
											${fieldValue(bean: orderSummary, field: "receiptStatus")}
										</td>
										<td class="middle">
											${fieldValue(bean: orderSummary, field: "paymentStatus")}
										</td>
										<td class="middle">
											${fieldValue(bean: orderSummary, field: "derivedStatus")}
										</td>
									</tr>
								</g:each>
							</tbody>
						</table>
						<div class="paginateButtons">
							<g:set var="pageParams" value="${pageScope.variables['params']}"/>
							<g:paginate total="${orderSummaryList?.totalCount?:0}" params="${params}"/>
						</div>
					</div>
				</div>

			</div>
		</div>
    </body>
</html>
