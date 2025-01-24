<%@ page import="org.pih.warehouse.core.ActivityCode;" %>
<%@ page import="org.pih.warehouse.core.Constants;" %>
<%@ page import="org.pih.warehouse.order.OrderItemStatusCode;" %>
<%@ page import="org.pih.warehouse.order.OrderType;" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode;" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:if test="${orderType == OrderType.findByCode(Constants.PUTAWAY_ORDER)}">
            <g:set var="entityName" value="${warehouse.message(code: 'putAways.label', default: 'Putaways')}" />
        </g:if>
        <g:else>
            <g:set var="entityName" value="${warehouse.message(code: 'orders.label', default: 'Purchase orders')}" />
        </g:else>
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
   	</head>
	<body>
		<div class="body">

			<g:if test="${flash.message}">
				<div class="message" role="status" aria-label="message">${flash.message}</div>
			</g:if>

			<div class="buttonBar">
				<g:supports activityCode="${ActivityCode.PLACE_ORDER}">
					<g:link controller="order" action="create" class="button">
						<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
						<warehouse:message code="default.create.label" args="[g.message(code: 'order.label')]" default="Create purchase order" />
					</g:link>
				</g:supports>
				<g:link controller="stockMovement" action="createCombinedShipments" class="button" params="[direction:'INBOUND']">
					<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
					<warehouse:message code="default.create.label" args="[warehouse.message(code: 'shipmentFromPO.label')]"/>
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
							<small>(<g:formatNumber number="${totalPrice}"/> ${grailsApplication.config.openboxes.locale.defaultCurrencyCode})</small>
						</h2>
						<table>
							<thead>
								<tr>
									<th>${warehouse.message(code: 'default.actions.label')}</th>
									<th>${warehouse.message(code: 'default.status.label')}</th>
									<th>${warehouse.message(code: 'default.type.label')}</th>
									<th>${warehouse.message(code: 'order.orderNumber.label')}</th>
									<th>${warehouse.message(code: 'default.name.label')}</th>
                                    <g:if test="${orderType != OrderType.findByCode(Constants.PUTAWAY_ORDER)}">
                                        <th>${warehouse.message(code: 'order.origin.label')}</th>
                                        <th>${warehouse.message(code: 'order.destination.label')}</th>
                                    </g:if>
                                    <th>${warehouse.message(code: 'order.orderedBy.label')}</th>
									<th>${warehouse.message(code: 'order.dateOrdered.label')}</th>
									<th>${warehouse.message(code: 'order.orderItems.label')}</th>
									<g:if test="${orderType != OrderType.findByCode(Constants.PUTAWAY_ORDER)}">
										<th>${warehouse.message(code: 'order.ordered.label')}</th>
										<th>${warehouse.message(code: 'order.shipped.label')}</th>
										<th>${warehouse.message(code: 'order.received.label')}</th>
									</g:if>
									<th>
										<div>${warehouse.message(code: 'order.totalPrice.label')}</div>
										<small>${warehouse.message(code: 'order.localCurrency.label', default: 'Local Currency')}</small>
									</th>
									<th>
										<div>${warehouse.message(code: 'order.totalPrice.label')}</div>
										<small>${warehouse.message(code: 'order.defaultCurrency.label', default: 'Default Currency')}</small>
									</th>
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
								%{-- For fetching derived statuses (preparing the list of order ids to be sent with request) --}%
								<g:hiddenField id="orderIds" name="orderIds" value="${orders?.collect { it?.id }?.join('&order.id=')}"/>
								<g:each var="orderInstance" in="${orders}" status="i">

									<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
										<td class="middle" width="1%">
											<div class="action-menu">
												<g:render template="/order/actions" model="[orderInstance:orderInstance,hideDelete:true]"/>
											</div>
										</td>
										<td class="middle">
											<div class="tag">
												<span class="${orderInstance?.id}">${g.message(code: 'default.loading.label')}</span>
											</div>
										</td>
										<td class="middle">
											<format:metadata obj="${orderInstance?.orderType?.code}"/>
										</td>
										<td class="middle">
											<g:link action="show" id="${orderInstance.id}">
												${fieldValue(bean: orderInstance, field: "orderNumber")}
											</g:link>
										</td>
										<td class="middle">
											<g:link action="show" id="${orderInstance.id}">
												${fieldValue(bean: orderInstance, field: "name")}
											</g:link>
										</td>
                                        <g:if test="${orderType != OrderType.findByCode(Constants.PUTAWAY_ORDER)}">
                                            <td class="middle">
                                                ${fieldValue(bean: orderInstance, field: "origin.name")}
												<g:if test="origin.organization.code">
													 (${fieldValue(bean: orderInstance, field: "origin.organization.code")})
												</g:if>
                                            </td>
                                            <td class="middle">
                                                ${fieldValue(bean: orderInstance, field: "destination.name")}
												<g:if test="destination.organization.code">
													(${fieldValue(bean: orderInstance, field: "destination.organization.code")})
												</g:if>
                                            </td>
                                        </g:if>
                                        <td class="middle">
                                            ${orderInstance?.orderedBy?.name}
                                        </td>
										<td class="middle">
											<format:date obj="${orderInstance?.dateOrdered}"/>
										</td>
										<td class="center middle">
											<g:set var="lineItems" value="${orderInstance?.orderItems?.findAll { it.orderItemStatusCode != OrderItemStatusCode.CANCELED }}"/>
											${lineItems.size()?:0}
										</td>
										<g:if test="${orderType != OrderType.findByCode(Constants.PUTAWAY_ORDER)}">
											<td class="center middle">
												${orderInstance?.orderedOrderItems?.size()?:0}
											</td>
											<td class="center middle">
												${orderInstance?.shippedOrderItems?.size()?:0}
											</td>
											<td class="center middle">
												${orderInstance?.receivedOrderItems?.size()?:0}
											</td>
										</g:if>
										<td class="center middle">
											<g:formatNumber number="${orderInstance.total}"/>
											${orderInstance.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
										</td>
										<td class="center middle">
											<g:formatNumber number="${orderInstance.totalNormalized}"/>
											${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
										</td>
									</tr>
								</g:each>
							</tbody>
							<tfoot>
								<tr class="odd">
									<g:set var="colspan" value="${orderType == OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()) ? 12 : 8}"/>
									<th colspan="${colspan}"></th>
									<th><label>${warehouse.message(code:'order.totalPrice.label')}</label></th>
									<th colspan="2" class="right">
										<g:formatNumber number="${totalPrice}"/>
										${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
									</th>
								</tr>
							</tfoot>
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
				fetchOrdersDerivedStatus();

				$(".clear-all")
				.click(function () {
					$('#statusStartDate-datepicker')
					.val('');
					$('#statusEndDate-datepicker')
					.val('');
					$('#statusStartDate')
					.val('');
					$('#statusEndDate')
					.val('');
					$('#totalPrice')
					.val('');
					$('#description')
					.val('');
					$("#origin")
					.val('')
					.trigger("chosen:updated");
					$("#status")
					.val('')
					.trigger("chosen:updated");
				});

				$("#clearStartDate")
				.click(function () {
					$('#statusStartDate-datepicker')
					.datepicker('setDate', null);
				});
				$("#clearEndDate")
				.click(function () {
					$('#statusEndDate-datepicker')
					.datepicker('setDate', null);
				});
			});

			function fetchOrdersDerivedStatus() {
				const orderIds = $('#orderIds').val();
				$.ajax({
					url: "${request.contextPath}/json/getOrdersDerivedStatus",
					data: "order.id=" + orderIds,
					success: function(data, textStatus, jqXHR){
						for (const [orderId, derivedStatus] of Object.entries(data)) {
							$("." + orderId).text(derivedStatus);
						}
					},
					error: function (jqXHR, textStatus, errorThrown) {
						console.error(jqXHR, textStatus, errorThrown);
						if (jqXHR.responseText) {
							$.notify(jqXHR.responseText, "error");
						} else {
							$.notify("An error occurred", "error");
						}
					}
				});
			}
        </script>
    </body>
</html>
