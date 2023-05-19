<%@ page import="org.pih.warehouse.order.OrderItemStatusCode; org.pih.warehouse.order.OrderTypeCode" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
		<g:set var="entityName" value="${warehouse.message(code: 'orderItemSummary.label', default: 'Order Item Summary')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
   	</head>
	<body>
		<div class="body">
			<div class="yui-gf">
				<div class="yui-u first">
					<g:render template="orderItemSummaryFilters" model="[actionName: actionName]"/>
				</div>
				<div class="yui-u">

					<div class="box">
						<h2>
							<warehouse:message code="default.list.label" args="[entityName]" />
						</h2>
						<table>
							<thead>
								<tr>
									<th>${warehouse.message(code: 'orderItemSummary.orderItemId.label', default: "Order Item ID")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.orderNumber.label', default: "Order Number")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.productCode.label', default: "Product Code")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.orderStatus.label', default: "Order Item Status")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.quantityOrdered.label', default: "Quantity Ordered")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.quantityShipped.label', default: "Quantity Shipped")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.quantityReceived.label', default: "Quantity Received")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.quantityCanceled.label', default: "Quantity Canceled")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.quantityInvoiced.label', default: "Quantity Invoiced")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.isItemFullyShipped.label', default: "Is fully shipped")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.isItemFullyReceived.label', default: "Is fully Received")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.isItemFullyInvoiced.label', default: "Is fully Invoiced")}</th>
									<th>${warehouse.message(code: 'orderItemSummary.derivedStatus.label', default: "Derived Status")}</th>
								</tr>
							</thead>
							<tbody>
								<g:unless test="${orderItemSummaryList}">
									<tr class="prop">
										<td colspan="15">
											<div class="empty fade center">
												<warehouse:message code="orders.none.message"/>
											</div>
										</td>
									</tr>
								</g:unless>

								<g:each var="orderItemSummary" in="${orderItemSummaryList}" status="i">

									<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
										<td class="middle">
											${orderItemSummary.id}
										</td>
										<td class="middle">
											<g:link controller="order" action="show" id="${orderItemSummary?.order?.id}">
												${orderItemSummary.orderNumber}
											</g:link>
										</td>
										<td class="middle">
											${orderItemSummary.product.productCode}
										</td>
										<td class="middle">
											${orderItemSummary.orderItemStatus}
										</td>
										<td class="middle">
											${orderItemSummary.quantityOrdered}
										</td>
										<td class="middle">
											${orderItemSummary.quantityShipped}
										</td>
										<td class="middle">
											${orderItemSummary.quantityReceived}
										</td>
										<td class="middle">
											${orderItemSummary.quantityCanceled}
										</td>
										<td class="middle">
											${orderItemSummary.quantityInvoiced}
										</td>
										<td class="middle">
											${orderItemSummary.isItemFullyShipped}
										</td>
										<td class="middle">
											${orderItemSummary.isItemFullyReceived}
										</td>
										<td class="middle">
											${orderItemSummary.isItemFullyInvoiced}
										</td>
										<td class="middle">
											${orderItemSummary.derivedStatus}
										</td>
									</tr>
								</g:each>
							</tbody>
						</table>
						<div class="paginateButtons">
							<g:set var="pageParams" value="${pageScope.variables['params']}"/>
							<g:paginate total="${orderItemSummaryList?.totalCount?:0}" params="${params}"/>
						</div>
					</div>
				</div>

			</div>
		</div>
    </body>
</html>
