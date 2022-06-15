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
									<th>${warehouse.message(code: 'default.orderItemId.label', default: "Order Item ID")}</th>
									<th>${warehouse.message(code: 'default.orderNumber.label', default: "Order Number")}</th>
									<th>${warehouse.message(code: 'default.productCode.label', default: "Product Code")}</th>
									<th>${warehouse.message(code: 'default.orderStatus.label', default: "Order Item Status")}</th>
									<th>${warehouse.message(code: 'default.quantityOrdered.label', default: "Quantity Ordered")}</th>
									<th>${warehouse.message(code: 'default.quantityShipped.label', default: "Quantity Shipped")}</th>
									<th>${warehouse.message(code: 'default.quantityReceived.label', default: "Quantity Received")}</th>
									<th>${warehouse.message(code: 'default.quantityInvoiced.label', default: "Quantity Invoiced")}</th>
									<th>${warehouse.message(code: 'default.derivedStatus.label', default: "Derived Status")}</th>
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
											${orderItemSummary.quantityInvoiced}
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
