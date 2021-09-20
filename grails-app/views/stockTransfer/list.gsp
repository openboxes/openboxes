<%@ page import="org.pih.warehouse.order.OrderItemStatusCode;" %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'inventory.stockTransfers.label', default: 'Stock Transfers')}" />
	<title><warehouse:message code="default.list.label" args="[entityName]" /></title>
</head>
<body>
<div class="body">

	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>

	<div class="buttonBar">
		<g:link controller="stockTransfer" action="list" class="button">
			<img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
			<warehouse:message code="default.list.label" args="[g.message(code: 'inventory.stockTransfers.label')]" default="List Stock Transfers"/>
		</g:link>
		<g:link controller="stockTransfer" action="index" class="button">
			<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
			<warehouse:message code="default.create.label" args="[warehouse.message(code: 'inventory.stockTransfer.label')]"/>
		</g:link>
	</div>

	<div class="yui-gf">
		<div class="yui-u first">
			<g:render template="filters" model="[]"/>
		</div>
		<div class="yui-u">

			<div class="box">
				<h2>
					<warehouse:message code="default.list.label" args="[entityName]" />
				</h2>
				<table>
					<thead>
					<tr>
						<th>${warehouse.message(code: 'default.status.label')}</th>
						<th>${warehouse.message(code: 'order.orderNumber.label')}</th>
						<th>${warehouse.message(code: 'order.createdBy.label')}</th>
						<th>${warehouse.message(code: 'order.creationDate.label')}</th>
						<th>${warehouse.message(code: 'order.orderItems.label')}</th>
					</tr>
					</thead>
					<tbody>
					<g:unless test="${orders}">
						<tr class="prop">
							<td colspan="15">
								<div class="empty fade center">
									<warehouse:message code="orders.none.message"/>
								</div>
							</td>
						</tr>
					</g:unless>

					<g:each var="orderInstance" in="${orders}" status="i">

						<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
							<td class="middle">
								<div class="tag">
									<format:metadata obj="${orderInstance?.status}"/>
								</div>
							</td>
							<td class="middle">
								<g:link action="show" id="${orderInstance.id}">
									${fieldValue(bean: orderInstance, field: "orderNumber")}
								</g:link>
							</td>
							<td class="middle">
								${orderInstance?.createdBy?.name}
							</td>
							<td class="middle">
								<format:date obj="${orderInstance?.dateCreated}"/>
							</td>
							<td class="middle">
								<g:set var="lineItems" value="${orderInstance?.orderItems?.findAll { it.orderItemStatusCode != OrderItemStatusCode.CANCELED }}"/>
								${lineItems.size()?:0}
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="paginateButtons">
					<g:set var="pageParams" value="${pageScope.variables['params']}"/>
					<g:paginate total="${orders.totalCount}" params="${params}"/>
				</div>
			</div>
		</div>

	</div>
</div>

<script type="text/javascript">
	$(document).ready(function() {
		$("#clearStartDate")
				.click(function () {
					$('#lastUpdatedStartDate-datepicker')
							.datepicker('setDate', null);
				});
		$("#clearEndDate")
				.click(function () {
					$('#lastUpdatedEndDate-datepicker')
							.datepicker('setDate', null);
				});
	});
</script>
</body>
</html>
