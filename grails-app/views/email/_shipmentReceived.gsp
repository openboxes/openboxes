<%@ page contentType="text/html"%>
<g:applyLayout name="email">
	<div>
		${warehouse.message(code: 'email.shipmentReceived.message', args: [format.metadata(obj:shipmentInstance.shipmentType), shipmentInstance?.name])}
	</div>
	<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id }" absolute="true">
		${warehouse.message(code: 'email.link.label')}
	</g:link>	
	
	<h2>${warehouse.message(code:'shipping.contents.label') }</h2>
	<table>
		<tr>
			<th>
				${warehouse.message(code: 'default.item.label')}
			</th>
			<th>
				${warehouse.message(code: 'shipping.recipient.label')}
			</th>
		</tr>
		<g:each var="shipmentItem" in="${shipmentInstance.allShipmentItems }">
			<tr>
				<td>
					${format.product(product: shipmentItem?.product) }
				</td>
				<td>
					${shipmentItem?.recipient?.name}
				</td>
			</tr>
		</g:each>
	</table>
	
</g:applyLayout>
