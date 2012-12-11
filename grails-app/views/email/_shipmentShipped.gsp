<%@ page contentType="text/html"%>
<g:applyLayout name="email">	
	<span>
		${warehouse.message(code: 'email.shipmentShipped.message', args: [format.metadata(obj:shipmentInstance.shipmentType), shipmentInstance?.name])}
	</span>
	<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id }" absolute="true">
		${warehouse.message(code: 'email.link.label', args: [shipmentInstance?.name])}
	</g:link>
	
	<h3>${warehouse.message(code:'shipping.details.label') }</h3>
	<table border="1" cellpadding="2">
		<tr>
			<td><label>${warehouse.message(code: 'shipping.origin.label') }</label></td>
			<td>${shipmentInstance?.origin?.name }</td>			
		</tr>
		<tr>
			<td><label>${warehouse.message(code: 'shipping.destination.label') }</label></td>
			<td>${shipmentInstance?.destination?.name }</td>
		</tr>
		<tr>
			<td><label>${warehouse.message(code: 'shipping.shippingDate.label') }</label></td>
			<td>			
				<g:formatDate date="${shipmentInstance?.actualShippingDate }" format="dd MMM yyyy"/>
			</td>
		</tr>
		<tr>
			<td><label>${warehouse.message(code: 'shipping.expectedDeliveryDate.label') }</label></td>
			<td><g:formatDate date="${shipmentInstance?.expectedDeliveryDate }" format="dd MMM yyyy"/></td>
		</tr>
	</table>
	

	<h3>${warehouse.message(code:'shipping.contents.label') }</h3>
	<table border="1" style="width: 100%;" cellpadding="2">
		<thead>
			<tr>
				<th style="text-align: left;">
					${warehouse.message(code: 'default.item.label')}
				</th>
				<th style="text-align: left;">
					${warehouse.message(code: 'inventoryItem.lotNumber.label')}
				</th>
				<th style="text-align: left;">
					${warehouse.message(code: 'inventoryItem.expirationDate.label')}
				</th>
				<th>
					${warehouse.message(code: 'default.quantity.label')}
				</th>
				<th>
					${warehouse.message(code: 'shipping.recipient.label')}
				</th>
			</tr>
		</thead>
		<g:each var="shipmentItem" in="${shipmentInstance.allShipmentItems }">
			<tr>
				<td>
					${format.product(product: shipmentItem?.inventoryItem?.product) }
				</td>
				<td>
					${shipmentItem?.inventoryItem?.lotNumber}
				</td>
				<td>
					<g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}" format="MMM yyyy"/>
				</td>
				<td style="text-align: center;">
					<g:formatNumber number="${shipmentItem?.quantity}" format="###,##0" />
					${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
				</td>
				<td style="text-align: center">
					${shipmentItem?.recipient?.name}
				</td>
			</tr>
		</g:each>
	</table>	
</g:applyLayout>
