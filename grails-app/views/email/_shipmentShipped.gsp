<%@ page contentType="text/html"%>
<g:applyLayout name="email">
	<h3><warehouse:message code="shipping.status.label"/></h3>
	<div>
		${warehouse.message(code: 'email.shipmentShipped.message', args: [format.metadata(obj:shipmentInstance.shipmentType), shipmentInstance?.name])}
		&nbsp;
		<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id }" absolute="true">
			${warehouse.message(code: 'email.link.label', args: [shipmentInstance?.name])}
		</g:link>
	</div>
	
	<h3>${warehouse.message(code:'shipping.contents.label') }</h3>
	<table>
		<thead>
			<tr>
				<th style="text-align: left;">
					${warehouse.message(code: 'default.item.label')}
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
					${format.product(product: shipmentItem?.product) }
				</td>
				<td style="text-align: center;">
					${shipmentItem?.quantity }
				</td>
				<td>
					${shipmentItem?.recipient?.name}
				</td>
			</tr>
		</g:each>
	</table>	
</g:applyLayout>
