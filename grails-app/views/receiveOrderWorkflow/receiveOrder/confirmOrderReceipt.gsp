<%@ page defaultCodec="html" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.confirmOrderReceipt.label"/></title>
<style>
</style>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${orderCommand}">
			<div class="errors">
				<g:renderErrors bean="${orderCommand}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${shipment}">
			<div class="errors">
				<g:renderErrors bean="${shipment}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${receipt}">
			<div class="errors">
				<g:renderErrors bean="${receipt}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${order}">
			<div class="errors">
				<g:renderErrors bean="${order}" as="list" />
			</div>
		</g:hasErrors>

		<g:form action="receiveOrder" method="post">
			<g:render template="../order/summary" model="[orderInstance:order,currentState:'confirmOrderReceipt']"/>

			<div class="dialog">
				<fieldset>

					<table>
						<tbody>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='summary'><warehouse:message code="default.summary.label"/></label>
								</td>
								<td valign='top'class='value'>
									<warehouse:message code="order.youAreAboutToCreateANewShipment.message"
										args="[format.metadata(obj:orderCommand?.shipmentType), orderCommand?.order?.origin?.name?.encodeAsHTML(),
										orderCommand?.order?.destination?.name?.encodeAsHTML(),format.date(obj:orderCommand?.deliveredOn)]"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='id'>Order Number:</label></td>
								<td valign='top' class='value'>
									<g:if test="${orderCommand?.order?.orderNumber }">
										${orderCommand?.order?.orderNumber }
									</g:if>
									<g:else>
										<span class="fade">New Order</span>
									</g:else>
								</td>
							</tr>

							<tr class='prop'>
								<td valign='top' class='name'><label for='source'>Order from:</label></td>
								<td valign='top' class='value'>
									${orderCommand?.order?.origin?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination">Destination:</label></td>
								<td valign='top' class='value'>
									${orderCommand?.order?.destination?.name?.encodeAsHTML()}
								</td>
							</tr>


							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='orderedBy'>Shipment type</label>
								</td>
								<td valign='top'class='value'>
									${orderCommand?.shipmentType?.name}
								</td>
							</tr>

							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='shippedOn'>Shipped on</label>
								</td>
								<td valign='top'class='value'>
									<format:date obj="${orderCommand?.shippedOn}"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='deliveredOn'>Delivered on</label>
								</td>
								<td valign='top'class='value'>
									<format:date obj="${orderCommand?.deliveredOn}"/>
								</td>
							</tr>
						</tbody>
					</table>

					<g:if test="${orderItems }">
						<div class="box">



							<h2>
								<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="received" style="vertical-align: middle"/>
								<warehouse:message code="order.itemsReceived.label"/>

							</h2>

							<table id="orderItems">
								<thead>
									<tr class="even">
										<th></th>
										<th><warehouse:message code="product.productCode.label"/></th>
										<th><warehouse:message code="product.name.label"/></th>
										<th><warehouse:message code="product.uom.label"/></th>
										<th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
										<th><warehouse:message code="inventoryItem.expirationDate.label"/></th>
										<th class="center"><warehouse:message code="order.ordered.label"/></th>
										<th class="center"><warehouse:message code="order.received.label"/></th>
									</tr>
								</thead>
								<tbody>
										<g:set var="i" value="${0 }"/>
										<g:each var="entrymap" in="${orderItems?.groupBy { it?.orderItem } }">
											<g:each var="orderItem" in="${entrymap.value}">

											<g:if test="${orderItem?.quantityReceived > 0}">
												<tr class="">
													<td>
														<a name="orderItems${i }"></a>
														<g:hiddenField class="orderItemId" name="orderItems[${i }].orderItem.id" value="${orderItem?.orderItem?.id }"/>
														<g:hiddenField name="orderItems[${i }].primary" value="${orderItem?.primary }"/>
														<g:hiddenField name="orderItems[${i }].type" value="${orderItem?.type }"/>
														<g:hiddenField name="orderItems[${i }].description" value="${orderItem?.description }"/>
														<g:hiddenField name="orderItems[${i }].quantityOrdered" value="${orderItem?.quantityOrdered }"/>
													</td>
													<td>
														${orderItem?.productReceived?.productCode }
													</td>
													<td>
														${orderItem?.productReceived?.name }
													</td>
													<td>
														${orderItem?.productReceived?.unitOfMeasure?:"each"}
													</td>
													<td>
														${orderItem?.lotNumber}
													</td>
													<td>
														<g:formatDate date="${orderItem?.expirationDate }" format="d MMM yyyy"/>
													</td>
													<td class="center">
														${orderItem?.quantityOrdered}
													</td>
													<td class="center">
														${orderItem?.quantityReceived }
													</td>
												</tr>
											</g:if>
											<g:set var="i" value="${i + 1}"/>
										</g:each>
									</g:each>
								</tbody>
							</table>
						</div>
					</g:if>
					<g:else>
						<span class="fade"><warehouse:message code="order.noItems.label"/></span>
					</g:else>


					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<span class="formButton">
							<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}" class="button"></g:submitButton>
							<g:submitButton name="submit" value="${warehouse.message(code:'default.button.finish.label')}" class="button"></g:submitButton>

						</span>
					</div>
				</fieldset>
			</div>


		</g:form>
	</div>

		<script>
			$(document).ready(function() {
				jQuery.fn.alternateRowColors = function() {
					$('tbody tr:odd', this).removeClass('odd').addClass('even');
					$('tbody tr:even', this).removeClass('even').addClass('odd');
					return this;
				};

				$("table #orderItems").alternateRowColors();

	    	});
	    </script>
</body>
</html>
