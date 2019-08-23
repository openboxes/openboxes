<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="shipping.packingList.label"/></content>
	
	<style>
		.container { border-right: 1px solid lightgrey; background-color: f7f7f7; }
		.newContainer { border-top: 1px solid lightgrey }
	</style>	
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
		<table>
			<tr>
				<td>
					<fieldset>						
						<g:render template="summary"/>
						<g:if test="${shipmentInstance.shipmentItems}">
							<g:set var="shipmentItemsByContainer" value="${shipmentInstance?.shipmentItems?.groupBy { it.container } }"/>
							<div id="items" class="section">											
								<table>		
									<tr>
										<th><warehouse:message code="shipping.container.label"/></th>
										<th><warehouse:message code="product.label"/></th>
										<th class="center"><warehouse:message code="default.lotSerialNo.label"/></th>
										<th class="center"><warehouse:message code="default.expires.label"/></th>
										<th class="center"><warehouse:message code="shipping.shipped.label"/></th>
										<g:if test="${shipmentInstance?.wasReceived()}">
											<th class="center"><warehouse:message code="shipping.received.label"/></th>
											<th class="center"><warehouse:message code="shipping.totalReceived.label"/></th>
										</g:if>
										<th><warehouse:message code="shipping.recipient.label"/></th>
									</tr>
									<g:set var="count" value="${0 }"/>
									<g:set var="previousContainer"/>
									<g:set var="shipmentItems" value="${shipmentInstance.shipmentItems.sort()}"/>
									<g:each var="shipmentItem" in="${shipmentItems}" status="i">
										<g:set var="rowspan" value="${shipmentItemsByContainer[shipmentItem?.container]?.size() }"/>												
										<g:set var="newContainer" value="${previousContainer != shipmentItem?.container }"/>
										<tr class="${(count++ % 2 == 0)?'odd':'even'} ${newContainer?'newContainer':''}">
											<g:if test="${newContainer }">
												<td class="container top left" rowspan="${rowspan }">
													<label>
														<g:if test="${shipmentItem?.container?.parentContainer}">${shipmentItem?.container?.parentContainer?.name } &rsaquo;</g:if>
														<g:if test="${shipmentItem?.container?.name }">${shipmentItem?.container?.name }</g:if>
														<g:else><warehouse:message code="shipping.unpacked.label"/></g:else>
													</label>
													<br/>
													<span class="fade">
										 				<g:if test="${shipmentItem?.container?.weight || shipmentItem?.container?.width || shipmentItem?.container?.length || shipmentItem?.container?.height}">
											 				<g:if test="${shipmentItem?.container?.weight}">
											 					${shipmentItem?.container?.weight} ${shipmentItem?.container?.weightUnits}
											 				</g:if>
											 				<g:if test="${shipmentItem?.container?.height && shipmentItem?.container?.width && shipmentItem?.container?.length }">
																${shipmentItem?.container.height} ${shipmentItem?.container?.volumeUnits}
																x
																${shipmentItem?.container.width} ${shipmentItem?.container?.volumeUnits}
																x
																${shipmentItem?.container.length} ${shipmentItem?.container?.volumeUnits}
															</g:if>
														</g:if>
													</span>	
												</td>
											</g:if>												
																								
											<td>
												<g:link controller="inventoryItem" action="showStockCard" id="${shipmentItem?.product?.id}">
													<format:product product="${shipmentItem?.product}"/>
												</g:link>
											</td>
											<td class="center">
												${shipmentItem?.lotNumber}
											</td>
											<td class="center">
												<g:formatDate date="${shipmentItem?.expirationDate}" format="d MMM yyyy"/>
											</td>
											<td class="center">
												${shipmentItem?.quantity}
											</td>
											<g:if test="${shipmentInstance?.wasReceived()}">
                                                ${shipmentItem?.quantityReceived() }
											</g:if>														
											<td>
												${shipmentItem?.recipient?.name}
											</td>
										</tr>
										<g:set var="previousContainer" value="${shipmentItem.container }"/>
									</g:each>
								</table>							
							</div>
						</g:if>							
					</fieldset>		
				</td>
			</tr>
		</table>

	</div>
</body>
</html>
