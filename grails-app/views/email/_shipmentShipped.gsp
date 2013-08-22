<%@ page contentType="text/html"%>
<g:applyLayout name="email">	
	<span>
		${warehouse.message(code: 'email.shipmentShipped.message', args: [format.metadata(obj:shipmentInstance.shipmentType), shipmentInstance?.name])}
	</span>
	<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id }" absolute="true">
		${warehouse.message(code: 'email.link.label', args: [shipmentInstance?.name])}
	</g:link>
	
	<h3>${warehouse.message(code:'shipping.details.label') }</h3>
	<table border="1">
		<tr>
			<td><label>${warehouse.message(code: 'shipping.origin.label') }</label></td>
			<td>${shipmentInstance?.origin?.name }</td>			
		</tr>
		<tr>
			<td>	
				<label>${warehouse.message(code: 'shipping.destination.label') }</label>
			</td>
			<td>
				${shipmentInstance?.destination?.name }
			</td>
		</tr>
		<tr>
			<td>
				<label>${warehouse.message(code: 'shipping.shippingDate.label') }</label>
			</td>
			<td>			
				<g:formatDate date="${shipmentInstance?.actualShippingDate }" format="dd MMM yyyy"/>
			</td>
		</tr>
		<tr>
			<td>
				<label>${warehouse.message(code: 'shipping.expectedDeliveryDate.label') }</label>
			</td>
			<td>
				<g:formatDate date="${shipmentInstance?.expectedDeliveryDate }" format="dd MMM yyyy"/>
			</td>
		</tr>

		<g:if test="${shipmentInstance?.referenceNumbers }">
			<g:each var="referenceNumber" in="${shipmentInstance?.referenceNumbers}" status="i">
				<tr class="prop">								
					<td valign="top" class="name">
						<label>
							<format:metadata obj="${referenceNumber?.referenceNumberType}"/>
						</label>
					</td>
					<td valign="top" class="value">
						${referenceNumber?.identifier }
					</td>
				</tr>
			</g:each>		
		</g:if>		
		<g:if test="${userInstance}">
			<tr>
				<td>
					<label>${warehouse.message(code: 'shipping.preparedBy.label') }</label>
				</td>
				<td>
					${userInstance?.name }
					<a href="mailto:${userInstance?.email }">${userInstance?.email }</a>
				</td>
			</tr>
		</g:if>
		<g:if test="${shipmentInstance?.carrier}">
			<tr>
				<td>
					<label>${warehouse.message(code: 'shipping.carriedBy.label') }</label>
				</td>
				<td>
					${shipmentInstance?.carrier?.name }
					<a href="mailto:${shipmentInstance?.carrier?.email }">${shipmentInstance?.carrier?.email }</a>
				</td>		
			</tr>
		</g:if>				
		
	</table>

	<h3>${warehouse.message(code:'shipping.contents.label') }</h3>
	<table border="1">
		<thead>
			<tr>
				<th style="text-align: left;">
					${warehouse.message(code: 'container.label')}
				</th>
                <th style="text-align: left;">
                    ${warehouse.message(code: 'product.productCode.label')}
                </th>
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
					${warehouse.message(code: 'default.units.label')}
				</th>
                <th>
                    ${warehouse.message(code: 'product.coldChain.label')}
                </th>
				<th>
					${warehouse.message(code: 'shipping.recipient.label')}
				</th>
			</tr>
		</thead>
		<g:if test="${shipmentInstance.shipmentItems}">
			<g:if test="${shipmentInstance.shipmentItems}">
				<g:each var="shipmentItem" in="${shipmentInstance.shipmentItems.sort() }">
					<tr>
						<td>
							${shipmentItem?.container?.name?:warehouse.message(code:'shipping.unpackedItems.label') }
						</td>
                        <td>
                            ${shipmentItem?.inventoryItem?.product?.productCode}
                        </td>
						<td>
							${format.product(product: shipmentItem?.inventoryItem?.product) }
						</td>
						<td>
							${shipmentItem?.inventoryItem?.lotNumber}
						</td>
						<td>
							<g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}" format="MMM yyyy"/>
						</td>
						<td class="center">
							<g:formatNumber number="${shipmentItem?.quantity}" format="###,##0" />
						</td>
						<td class="center">
							${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
						</td>
                        <td class="center">
                            <g:if test="${shipmentItem?.inventoryItem?.product?.coldChain}">
                                ${warehouse.message(code:'default.yes.label')}
                            </g:if>
                            <g:else>
                                ${warehouse.message(code:'default.no.label')}
                            </g:else>
                        </td>
						<td class="center">
							${shipmentItem?.recipient?.name?:warehouse.message(code:'default.none.label')}
						</td>
					</tr>
				</g:each>
			</g:if>
		</g:if>
	</table>	
</g:applyLayout>
