<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title><warehouse:message code="order.shipOrder.label" default="Ship Order" /></title>
	<style>
		.import-template {
			width: 0.1px;
			height: 0.1px;
			opacity: 0;
			overflow: hidden;
			position: absolute;
			z-index: -1;
		}
		.import-template + label {
			display: inline-block;
		}
	</style>
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
		<g:hasErrors bean="${flash.errors}">
			<div class="errors">
				<g:renderErrors bean="${flash.errors}" as="list" />
			</div>
		</g:hasErrors>

		<div class="dialog">

			<g:render template="summary" model="[orderInstance:command?.order,currentState:'shipOrder']" />

			<g:form action="saveShipmentItems">
				<div class="box">
					<h2>
						<warehouse:message code="order.shipOrder" default="Ship Order" />
						<g:if test="${command.shipment}">
							&rsaquo; ${command.shipment.shipmentNumber} ${command?.shipment?.name} (${command?.shipment?.status})
						</g:if>
						<div style="float: right; margin: 8px; display: flex;" class="button-group">
							<g:link class="button" type="button" action="exportTemplate" params="['order.id':command?.order?.id]">
								<g:message code="default.exportTemplate.label" default="Export Template"/>
							</g:link>
							<input class="import-template" type="file" name="importTemplate" id="importTemplate" accept=".csv" />
							<label for="importTemplate" class="button">
								<warehouse:message code="default.importTemplate.label" default="Import template"/>
							</label>
							<button class="button" type="button" onclick="autofillQuantity()">
								<g:message code="order.autofillQuantities.label" default="Autofill quantities"/>
							</button>
						</div>
					</h2>
					<g:hiddenField id="orderId"  name="order.id" value="${command?.order?.id}" />
					<g:hiddenField name="shipment.id" value="${command?.shipment?.id}" />

					<table id="shipOrder">
						<thead>
							<tr>
								<th><g:message code="product.label"/></th>
								<th><g:message code="product.unitOfMeasure.label" default="Unit of Measure"/></th>
								<th class="center"><g:message code="orderItem.quantityOrdered.label" default="Ordered"/></th>
								<th class="center"><g:message code="shipmentItem.quantityShipped.label" default="Shipped"/></th>
								<th class="center"><g:message code="orderItem.quantityRemaining.label" default="Remaining"/></th>
								<g:if test="${command.shipment}">
									<th class="center"><g:message code="shipmentItem.packLevel.label" default="Pack Level"/></th>
									<th class="center"><g:message code="inventoryItem.lotNumber.label" default="Lot number"/></th>
									<th class="center"><g:message code="inventoryItem.expirationDate.label" default="Expiration date"/></th>
								</g:if>
								<th class="left"><g:message code="orderItem.quantityToShip.label" default="Quantity To Ship"/></th>
							</tr>
						</thead>
						<tbody>
							<g:set var="status" value="${0}"/>
							<g:each var="orderItem" in="${command?.order?.orderItems.sort()}" status="i">
								<g:set var="shipOrderItemsByOrderItem" value="${command?.getShipOrderItemsByOrderItem(orderItem)}"/>
								<g:if test="${shipOrderItemsByOrderItem}">
									<g:each var="shipOrderItem" in="${shipOrderItemsByOrderItem}" status="j">
										<g:hiddenField name="shipOrderItems[${status}].orderItem.id" value="${shipOrderItem.orderItem.id}"/>
										<g:hiddenField name="shipOrderItems[${status}].shipmentItem.id" value="${shipOrderItem?.shipmentItem?.id}"/>
										<tr class="${status%2?'odd':'even'} ${!j?'prop':''}">
											<td class="middle">
												<g:if test="${!j}">
													${orderItem.product?.productCode}
													${orderItem.product?.name}
												</g:if>
											</td>
											<td class="middle">
												<g:if test="${!j}">
												${orderItem?.unitOfMeasure}
												</g:if>
											</td>
											<td class="center middle">
												<g:if test="${!j}">
												${orderItem?.quantity}
												</g:if>
											</td>
											<td class="center middle">
												<g:if test="${!j}">
												${orderItem?.quantityShipped}
												</g:if>
											</td>
											<td class="center middle" id="quantityRemaining${status}">
												<g:if test="${!j}">
													<g:if test="${orderItem.quantityRemaining>0}">
														${orderItem?.quantityRemaining}
													</g:if>
													<g:else>0</g:else>
												</g:if>
											</td>
											<g:if test="${command.shipment}">
												<td class="center middle">
													<g:if test="${shipOrderItem?.shipmentItem?.container}">
														<g:if test="${shipOrderItem?.shipmentItem?.container?.parentContainer}">
															${shipOrderItem?.shipmentItem?.container?.parentContainer} &rsaquo;
														</g:if>
														${shipOrderItem?.shipmentItem?.container}
													</g:if>
													<g:else>
														<div class="fade">(${g.message(code: 'default.blank.label')})</div>
													</g:else>
												</td>
												<td class="center middle">
													<g:if test="${shipOrderItem?.shipmentItem?.inventoryItem?.lotNumber}">
														${shipOrderItem?.shipmentItem?.inventoryItem?.lotNumber}
													</g:if>
													<g:else>
														<div class="fade">(${g.message(code: 'default.blank.label')})</div>
													</g:else>
												</td>
												<td class="center middle">
													<g:if test="${shipOrderItem?.shipmentItem?.inventoryItem?.expirationDate}">
														<g:formatDate date="${shipOrderItem?.shipmentItem?.inventoryItem?.expirationDate}" format="dd/MMM/yyyy"/>
													</g:if>
													<g:else>
														<div class="fade">(${g.message(code: 'default.blank.label')})</div>
													</g:else>
												</td>
											</g:if>
											<td>
												<input type="number"
													   class="text"
													   size="10"
													   data-default-value="${shipOrderItem?.quantityToShip}"
													   id="orderItems-quantityToShip${status}"
													   name="shipOrderItems[${status}].quantityToShip"
													   min="${shipOrderItem.quantityMinimum}"
													   max="${shipOrderItem?.quantityMaximum}"
													   value="${shipOrderItem?.quantityToShip ?: ''}"/>
												<a href="#" class="change-quantity"
												   data-id="#orderItems-quantityToShip${status}"
												   data-delta="-1"
												   data-min="${shipOrderItem.quantityMinimum}"
												   data-max="${shipOrderItem.quantityMaximum}">
													<img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" /></a>
												<a href="#" class="change-quantity"
												   data-id="#orderItems-quantityToShip${status}"
												   data-delta="1"
												   data-min="${shipOrderItem.quantityMinimum}"
												   data-max="${shipOrderItem.quantityMaximum}">
													<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" /></a>

												<a href="#" class="change-quantity"
												   data-id="#orderItems-quantityToShip${status}"
												   data-delta="0"
												   data-min="${shipOrderItem.quantityMinimum}"
												   data-max="${shipOrderItem.quantityMaximum}">
													<img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" /></a>

											</td>
										</tr>
										<g:set var="status" value="${status+1}"/>
									</g:each>
								</g:if>

							</g:each>

						</tbody>
					</table>
				</div>
				<div class="buttons">
					<div class="left">
						<g:link controller="purchaseOrder"
								action="edit"
								id="${command?.order.id}"
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
<script type="text/javascript">

$(document).ready(function() {
	$(".change-quantity").click(function() {
		var id = $(this).data("id");
		var minimumValue = parseInt($(this).data("min"));
		var maximumValue = parseInt($(this).data("max"));
		var changeValue = parseInt($(this).data("delta"));
		var oldValue = parseInt($(id).val());
		var newValue = changeValue ? (oldValue + changeValue) : 0;
		if (newValue <= maximumValue && newValue >= minimumValue) {
			$(id).val(newValue);
		}
	});

  $("#importTemplate").change(function(){
    var orderId = $("#orderId").val();
    var fileInput = document.getElementById('importTemplate');
    if (orderId && fileInput.files && fileInput.files.length > 0) {
      var file = fileInput.files[0];
      var importData = new FormData();
      importData.append('fileContents', file);
      importData.append('id', orderId);
      $.ajax({
        type: 'POST',
        url: '${g.createLink(controller:'order', action:'importTemplate')}',
        data: importData,
        enctype: "multipart/form-data",
        contentType: false,
        processData: false,
        success: function () {
          $("#importTemplate").val('');
          $.notify("Successfully uploaded template", "success");
          location.reload();
        },
        error: function (jqXHR, textStatus, errorThrown) {
          $("#importTemplate").val('');
          if (jqXHR.responseText) {
            $.notify(jqXHR.responseText, "error");
          }
          else {
            $.notify("An error occurred", "error");
          }
          location.reload();
        }
      });
    }
  });
});

function autofillQuantity() {
	var rowCount = document.getElementById('shipOrder').tBodies[0].rows.length;
	for (var i=0; i<rowCount; i++) {
		var quantityRemaining = parseInt($("#quantityRemaining" + i).text());
		var quantityToShip = $("#orderItems-quantityToShip" + i);
		if (!quantityToShip.val()) {
			quantityToShip.val(quantityRemaining);
		}
	}
}
</script>
</body>
</html>
