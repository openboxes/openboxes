<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipmentItem.label', default: 'Shipment item')}" />
	<title><warehouse:message code="default.add.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="shipping.addToShipments.label"/></content>
</head>

<body>

	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${flash.errors}">
			<div class="errors">
				<g:renderErrors bean="${flash.errors}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${commandInstance.errors}">
			<div class="errors">
				<g:renderErrors bean="${commandInstance.errors}" as="list" />
			</div>
		</g:hasErrors>

		<div class="dialog">			
			<g:form action="addToShipmentPost">

				<fieldset>
					<div class="action-menu" style="padding: 10px;">
						<button class="action-btn">
							<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>							
							<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
						</button>
						<div class="actions">
							<div class="action-menu-item">
								<g:link controller="inventory" action="browse" id="${shipmentInstance?.id}">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="${warehouse.message(code: 'shipping.returnToInventoryBrowser.label') }" style="vertical-align: middle" />	
									&nbsp; <warehouse:message code="shipping.returnToInventoryBrowser.label"/>
								</g:link>
							</div>
						</div>
					</div>					
					
					

					<table>
						<tbody>	
							<tr>
								<th><warehouse:message code="product.label"/></th>
								<th nowrap="nowrap"><warehouse:message code="product.lotNumber.label"/></th>
								<th nowrap="nowrap"><warehouse:message code="default.expires.label"/></th>
								<th class="center"><warehouse:message code="inventory.qtyShipping.label"/></th>
								<th class="center"><warehouse:message code="inventory.qtyReceiving.label"/></th>
								<th class="center"><warehouse:message code="inventory.qtyOnHand.label"/></th>
								<th class="center" style="border-left: 1px solid lightgrey;"><warehouse:message code="inventory.qtyToShip.label"/></th>
								<th><warehouse:message code="shipping.shipment.label"/></th>
							</tr>
							<g:set var="status" value="${0 }"/>
							<g:set var="itemsGroupedByProduct" value="${commandInstance?.items?.groupBy { it.product } }"/>
							<g:each var="product" in="${products }" status="i">
								<g:each var="item" in="${itemsGroupedByProduct[product]?.sort { it?.inventoryItem?.expirationDate } }" status="j">
									<g:if test="${item?.quantityOnHand > 0 }">
										<tr class="${i%2?'even':'odd' }">
											<td>
												<g:if test="${j==0 }">
													<g:link controller="inventoryItem" action="showStockCard" id="${item?.product?.id }" target="_blank">
														<format:product product="${item?.product}"/>
													</g:link>
													<g:hiddenField name="product.id" value="${item?.product?.id }"/>	<%-- used when redirecting to page on error --%>
												</g:if>
												<g:hiddenField name="items[${status }].product.id" value="${item?.product?.id }"/>
											</td>
											<td>
												${item?.lotNumber }
												<g:hiddenField name="items[${status }].lotNumber" value="${item?.lotNumber }"/>
											</td>
											<td nowrap="nowrap">
												<g:if test="${item?.inventoryItem?.expirationDate }">
													<g:formatDate date="${item?.inventoryItem?.expirationDate }" format="MMM yyyy"/>								
												</g:if>
												<g:else>
													<span class="fade">never</span>
												</g:else>
											</td>
											<td class="center">
												${item?.quantityShipping?:"<span class='fade'>0</span>"}
												<g:hiddenField name="items[${status }].quantityShipping" value="${item?.quantityShipping }"/>
											</td>
											<td class="center">
												${item?.quantityReceiving?:"<span class='fade'>0</span>"}
												<g:hiddenField name="items[${status }].quantityReceiving" value="${item?.quantityReceiving }"/>
											</td>
											<td class="center">
												${item?.quantityOnHand?:"<span class='fade'>0</span>"}
												<g:hiddenField name="items[${status }].quantityOnHand" value="${item?.quantityOnHand }"/>
											</td>
											<td class="center middle" style="border-left: 1px solid lightgrey; padding: 0">
												<g:if test="${item?.quantityOnHand > 0 }">
													<g:textField name="items[${status }].quantity" size="1" style="text-align: center;" 
														value="${item?.quantity }" autocomplete="off"/>
												</g:if>
											</td>
											<td class="center middle" style="padding: 0">										
												<g:select name="items[${status }].shipment.id" from="${shipments }" 
													noSelection="${['null':'Select a shipment...']}" value="${item?.shipment?.id }"
													optionKey="id" optionValue="name" />
												<g:set var="status" value="${status+1 }"/>
											</td>
										</tr>
									</g:if>
									<g:else>
										<tr class="${i%2?'even':'odd' }">
											<td>
												<g:if test="${j==0 }">
													<g:link controller="inventoryItem" action="showStockCard" id="${item?.product?.id }" target="_blank">
														<format:product product="${item?.product}"/>
													</g:link>
													<g:hiddenField name="product.id" value="${item?.product?.id }"/>	<%-- used when redirecting to page on error --%>
												</g:if>
											</td>
											<td>
												${item?.lotNumber }
											</td>
											<td nowrap="nowrap">
												<g:if test="${item?.inventoryItem?.expirationDate }">
													<g:formatDate date="${item?.inventoryItem?.expirationDate }" format="MMM yyyy"/>								
												</g:if>
												<g:else>
													<span class="fade">never</span>
												</g:else>
											</td>
											<td class="center">
												<span class="${item?.quantityShipping?'':'fade'}">${item?.quantityShipping?:0}</span>
											</td>
											<td class="center">
												<span class="${item?.quantityReceiving?'':'fade'}">${item?.quantityReceiving?:0}</span>
											</td>
											<td class="center">
												<span class="${item?.quantityOnHand?'':'fade'}">${item?.quantityOnHand?:0}</span>
											</td>
											<td class="center" style="border-left: 1px solid lightgrey;">
												&nbsp;
											</td>
											<td class="center">										
												&nbsp;
											</td>
										</tr>
									</g:else>
								</g:each>					
								<g:unless test="${itemsGroupedByProduct[product]}">
									<tr class="${status%2==0?'odd':'even' }">
										<td colspan="6">
											<format:product product="${product}"/>
										</td>				
										<td style="border-left: 1px solid lightgrey;" colspan="2">
										
										</td>					
									</tr>
								</g:unless>
							</g:each>
						</tbody>
						<tfoot>
							<tr>
								<td colspan="8" style="border-top: 1px solid lightgrey;">
									<div class="center">
										<button type="submit"><img
											src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}"
											class="middle btn" alt="save" />&nbsp;<warehouse:message code="shipping.addToShipments.label"/></button>
										&nbsp;
										<g:link controller="inventory" action="browse" id="${shipmentInstance?.id}"><warehouse:message code="default.button.cancel.label"/></g:link>
									</div>				
								</td>
							</tr>
						</tfoot>	
					</table>						
				</fieldset>
			</g:form>
		</div>
	</div>
	
	<script>
		$(document).ready(function() {	
			$("form:not(.filter) :input:visible:enabled:first").focus();
		});
	</script>
</body>
</html>
