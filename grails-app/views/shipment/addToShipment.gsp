<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipmentItem.label', default: 'Shipment item')}" />
	<title><g:message code="default.add.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Add to shipment(s)</content>
</head>

<body>

	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>

		<div class="dialog">			
			<g:form action="addToShipmentPost">

				<g:link controller="inventory" action="browse" id="${shipmentInstance?.id}">&lsaquo; Return to inventory browser</g:link>
				
				<table>
					<tbody>	
						<tr>
							<th>Product</th>
							<th>Lot Number</th>
							<th>Qty</th>
							<th style="border-left: 1px solid lightgrey;">Shipment</th>
							<th>Qty To Ship</th>
						</tr>
						<g:each var="item" in="${commandInstance?.items }" status="i">
							<tr class="${i%2==0?'odd':'even' }">
								<td>
									<g:link controller="inventoryItem" action="showStockCard" id="${item?.product?.id }" target="_blank">
										${item?.product?.name }
									</g:link>
									<g:textField name="items[${i }].product.id" value="${item?.product?.id }"/>
								</td>
								<td>
									${item?.lotNumber }
									<g:textField name="items[${i }].lotNumber" value="${item?.lotNumber }"/>
								</td>
								<td class="center">
									${quantityMap[item.inventoryItem]?:'Not available' }
								</td>
								<td style="border-left: 1px solid lightgrey;">
									<g:select name="items[${i }].shipment.id" from="${shipments }" 
										noSelection="${['null':'Select a shipment...']}" value="${item?.shipment?.id }"
										optionKey="id" optionValue="name" />
								</td>
								<td>
									<g:textField name="items[${i }].quantity" size="4" style="text-align: center;" 
										value="${item?.quantity }"/>
								</td>
								
							</tr>
						</g:each>
					</tbody>
					<tfoot>
					
						<td colspan="5" style="border-top: 1px solid lightgrey;">
							<div class="center">
								<button type="submit" ><img
									src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
									class="btn" alt="save" /> Add to shipment(s)</button>
								&nbsp;
								<g:link controller="inventory" action="browse" id="${shipmentInstance?.id}">&lsaquo; Cancel</g:link>
							</div>				
						</td>
					</tfoot>	
				</table>						
				
			</g:form>
		</div>
	</div>
</body>
</html>
