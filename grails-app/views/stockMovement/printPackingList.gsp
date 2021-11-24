<%@ page defaultCodec="html" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="bootstrap" />
		<parameter name="includeHeader" value="${false}" />
	    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'print.css')}" type="text/css" media="print, screen, projection" />
        <title><warehouse:message code="packingList.label" default="Packing List"/></title>
    </head>
    <body>
			<g:if test="${stockMovement?.shipment }">
				<div class="container">

					<g:displayLogo location="${session?.warehouse?.id}" showLabel="${false}" class="responsive"/>
					<div class="float-end">
						<g:displayBarcode showData="${false}" data="${stockMovement.generateLink()}" format="QR_CODE"/>
					</div>

					<h1><warehouse:message code="report.shippingReport.title"/> </h1>
					<table class="table table-bordered">
						<tr>
							<td>
								<strong><warehouse:message code="stockMovement.identifier.label"/></strong>
							</td>
							<td>
								${stockMovement?.identifier }
							</td>
							<td>
								<strong><warehouse:message code="stockMovement.status.label"/></strong>
							</td>
							<td>
								${stockMovement?.status }
							</td>
						</tr>
						<tr>
							<td>
								<strong><warehouse:message code="report.origin.label"/></strong>
							</td>
							<td>
								${stockMovement?.shipment?.origin?.name }
							</td>
							<td>
								<strong><warehouse:message code="report.destination.label"/></strong>
							</td>
							<td>
								${stockMovement?.shipment?.destination?.name }
							</td>
						</tr>
						<tr>
							<td>
								<strong><warehouse:message code="stockMovement.expectedShippingDate.label" default="Expected Shipping Date"/></strong>
							</td>
							<td>
								<g:formatDate date="${stockMovement?.expectedShippingDate}"/>
							</td>
							<td>
								<strong><warehouse:message code="stockMovement.expectedDeliveryDate.label" default="Expected Delivery Date"/></strong>
							</td>
							<td>
								<g:formatDate date="${stockMovement?.expectedDeliveryDate}"/>
							</td>
						</tr>
					</table>

						<table class="table table-bordered">
							<thead>
								<tr>
									<th>
										<warehouse:message code="default.number.label" default="No."/>
									</th>
									<th>
										<warehouse:message code="shipping.container.label"/>
									</th>
									<th>
										<warehouse:message code="product.productCode.label"/>
									</th>
									<th>
										<warehouse:message code="product.description.label"/>
									</th>
									<th>
										<warehouse:message code="inventoryItem.lotNumber.label"/>
									</th>
									<th>
										<warehouse:message code="inventoryItem.expirationDate.label"/>
									</th>
									<th>
										<label><warehouse:message code="default.quantity.label"/></label>
									</th>
								</tr>
							</thead>

							<tbody>
								<g:each var="shipmentItem" in="${stockMovement?.shipment?.shipmentItems}" status="i">
								<tr>
									<td class="center">
										${i+1 }
									</td>
									<td class="left" rowspan="${rowspan }">
										<g:render template="../shipment/container" model="[container:checklistEntry?.shipmentItem?.container]"/>
									</td>
									<td>
										${shipmentItem?.inventoryItem?.product?.productCode}
									</td>
									<td>
										<format:product product="${shipmentItem?.inventoryItem?.product}"/>
									</td>
									<td>
										${shipmentItem?.inventoryItem?.lotNumber }
									</td>
									<td>
										<format:expirationDate obj="${shipmentItem?.inventoryItem?.expirationDate }"/>

									</td>
									<td class="center">
										${shipmentItem?.quantity }
										${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:"EA"}
									</td>
								</tr>
								</g:each>
							</tbody>
						</table>
					</div>
				</div>
			</g:if>
		</div>
    </body>
</html>
