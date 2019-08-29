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

		<div class="dialog" >			
			<g:form action="addToShipmentPost">
					<table class="box">
					
						<tr class="prop">
							<td class="name">
								<label>${warehouse.message(code:'container.label') }</label>
							</td>
							<td class="value">
								<g:selectContainer name="shipmentContainerKey"
									noSelection="${['null':warehouse.message(code:'shipping.selectContainer.label')]}" 
									from="${shipments}"
									optionKey="id" optionValue="name" groupBy="shipment"/>	
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label>${warehouse.message(code:'default.items.label') }</label>
							</td>
							<td class="value">

								<table>
									<thead>
										<tr class="prop odd">
											<th nowrap="nowrap"><warehouse:message code="product.label"/></th>
											<th nowrap="nowrap"><warehouse:message code="product.lotNumber.label"/></th>
											<th nowrap="nowrap"><warehouse:message code="default.expires.label"/></th>
											<th class="center"><warehouse:message code="inventory.qtyShipping.label"/></th>
											<th class="center"><warehouse:message code="inventory.qtyReceiving.label"/></th>
											<th class="center"><warehouse:message code="inventory.qtyOnHand.label"/></th>
											<th class="center" style="border-left: 1px solid lightgrey;"><warehouse:message code="inventory.qtyToShip.label"/></th>
										</tr>
									</thead>						
									<g:set var="listStatus" value="${0 }"/>
									<g:set var="rowStatus" value="${0 }"/>
									<g:set var="itemsGroupedByProduct" value="${commandInstance?.items?.groupBy { it.product } }"/>
									<g:each var="product" in="${itemsGroupedByProduct.keySet() }" status="i">
										<g:set var="items" value="${itemsGroupedByProduct[product]?.sort { it?.inventoryItem?.expirationDate } }"/>			
										<tbody>	
											<g:each var="item" in="${items}" status="j">
												
													<tr class="${rowStatus++%2?'even':'odd' } prop">
														<g:if test="${j==0 }">
															<td rowspan="${items.size() }" class="name" style="border-right: 1px solid lightgrey;">
																<g:link controller="inventoryItem" action="showStockCard" id="${product?.id }" target="_blank">
																	<label><format:product product="${product}"/></label>
																</g:link>
															</td>
														</g:if>
														<td>
															${item?.lotNumber }
															<g:hiddenField name="items[${listStatus }].inventoryItem.id" value="${item?.inventoryItem?.id }"/>
															<g:hiddenField name="items[${listStatus }].lotNumber" value="${item?.lotNumber }"/>
															<g:hiddenField name="items[${listStatus }].product.id" value="${item?.product?.id }"/>
														</td>
														<td nowrap="nowrap">
															<g:if test="${item?.inventoryItem?.expirationDate }">
																<g:formatDate date="${item?.inventoryItem?.expirationDate }" format="d MMM yyyy"/>
															</g:if>
															<g:else>
																<span class="fade">${warehouse.message(code: 'default.never.label')}</span>
															</g:else>
														</td>
														<td class="center">
															${item?.quantityShipping?:"<span class='fade'>0</span>"}
															<g:hiddenField name="items[${listStatus }].quantityShipping" value="${item?.quantityShipping }"/>
														</td>
														<td class="center">
															${item?.quantityReceiving?:"<span class='fade'>0</span>"}
															<g:hiddenField name="items[${listStatus }].quantityReceiving" value="${item?.quantityReceiving }"/>
														</td>
														<td class="center">
															${item?.quantityOnHand?:"<span class='fade'>0</span>"}
															<g:hiddenField name="items[${listStatus }].quantityOnHand" value="${item?.quantityOnHand }"/>
														</td>
														<td class="center middle" style="border-left: 1px solid lightgrey; padding: 0">
															<g:if test="${item?.quantityOnHand > 0 }">
																<g:textField name="items[${listStatus }].quantity" size="10" style="text-align: center;" 
																	value="${item?.quantity }" autocomplete="off" class="text"/>
															</g:if>
															<g:else>
																0
															</g:else>
														</td>
													</tr>
													<g:set var="listStatus" value="${listStatus+1 }"/>												
											</g:each>					
											<g:unless test="${itemsGroupedByProduct[product]}">
												<tr class="${rowStatus++%2?'even':'odd' } prop">
													<td colspan="6">
														<h2>
															<format:product product="${product}"/>
														</h2>
													</td>				
													<td style="border-left: 1px solid lightgrey;" colspan="1">
													
													</td>					
												</tr>
											</g:unless>
										</g:each>
									</tbody>
									<tfoot>
										<tr>
											<td colspan="7" style="border-top: 1px solid lightgrey;">
												<div class="center">
													<button type="submit" class="button icon add">
														<warehouse:message code="shipping.addItems.label"/>
													</button>
													&nbsp;
													<g:link controller="inventory" action="browse" id="${shipmentInstance?.id}"><warehouse:message code="default.button.cancel.label"/></g:link>
												</div>																
											</td>
										</tr>
									</tfoot>	
								</table>	
							</td>
						</tr>						
					</table>								
					
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
