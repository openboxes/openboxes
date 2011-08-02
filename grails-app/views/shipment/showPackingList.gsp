<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Packing List</content>
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
						<table style="padding: 0px; margin: 0px;">
							<tbody>
								<tr style="height: 30px;">
									<th>Package</th>
									<th>Dimensions</th>
									<th>Weight</th>
									<th>Item</th>
									<th>Lot/Serial No</th>										
								</tr>				
								
								<g:set var="counter" value="${0 }"/>
								<g:findAll var="item" in="${shipmentInstance?.shipmentItems}" expr="${!it.container}" status="itemStatus">
									<tr class="${(counter++ % 2) == 0 ? 'odd' : 'even'}">
										<td width="10%">
											<b>Unpacked items</b>
										</td>																
										<td width="10%">
										</td>
										<td width="10%">
											
										</td>
										<td width="20%">${item?.product.name} (${item?.quantity})</td>
										<td width="10%">${item?.lotNumber}</td>
									</tr>																			
								</g:findAll>	
								
								
								<g:each var="container" in="${shipmentInstance.containers}" status="containerStatus">
									<g:if test="${!container.shipmentItems}">
										<tr class="${(counter++ % 2) == 0 ? 'odd' : 'even'}">
											<td width="10%">
												<b>${container?.containerType?.name}-${container?.name}</b>
											</td>																
											<td width="10%">
												<g:if test="${container?.height && container?.width && container?.length}">	
													${container?.height} x ${container?.width} x ${container?.length}
												</g:if>
												<g:else>
													&nbsp;
												</g:else>
											</td>
											<td width="10%">
												<g:if test="${container?.weight }">
													${container?.weight} ${container?.weightUnits}
												</g:if>
											</td>
											<td width="20%">(empty)</td>
											<td width="10%">&nbsp;</td>
										</tr>																	
									</g:if>
									<g:each var="item" in="${container.shipmentItems}" status="itemStatus">
										<tr class="${(counter++ % 2) == 0 ? 'odd' : 'even'}">
											<td width="10%">
												<b>${container?.containerType?.name} ${container?.name}</b>
											</td>																
											<td width="10%">
												<g:if test="${container?.height && container?.width && container?.length}">	
													${container?.height} x ${container?.width} x ${container?.length}
												</g:if>
												<g:else>&nbsp;</g:else>
											</td>
											<td width="10%">
												<g:if test="${container?.weight }">
													${container?.weight} ${container?.weightUnits}
												</g:if>
											</td>
											<td width="20%">${item?.product.name} (${item?.quantity})</td>
											<td width="10%">${item?.lotNumber}</td>
										</tr>																			
									</g:each>	
									<g:each var="innerContainer" in="${container.containers}" status="innerContainerStatus">
										<g:if test="${!innerContainer.shipmentItems}">
											<tr class="${(counter++ % 2) == 0 ? 'odd' : 'even'}">
												<td width="10%">
													<b>${innerContainer?.containerType?.name}-${container?.name}</b>
												</td>																
												<td width="10%">
													<g:if test="${innerContainer?.height && innerContainer?.width && innerContainer?.length}">	
														${innerContainer?.height} x ${innerContainer?.width} x ${innerContainer?.length}
													</g:if>
													<g:else>
														&nbsp;
													</g:else>
												</td>
												<td width="10%">
													<g:if test="${innerContainer?.weight }">
														${innerContainer?.weight} ${innerContainer?.weightUnits}
													</g:if>
												</td>
												<td width="20%">(empty)</td>
												<td width="10%">&nbsp;</td>
											</tr>																	
										</g:if>
										<g:each var="innerItem" in="${innerContainer.shipmentItems}" status="innerItemStatus">
											<tr class="${(counter++ % 2) == 0 ? 'odd' : 'even'}">
												<td width="10%">
													<b>${innerContainer?.containerType?.name} ${innerContainer?.name}</b>
												</td>																
												<td width="10%">
													<g:if test="${innerContainer?.height && innerContainer?.width && innerContainer?.length}">	
														${innerContainer?.height} x ${innerContainer?.width} x ${innerContainer?.length}
													</g:if>
													<g:else>&nbsp;</g:else>
												</td>
												<td width="10%">
													<g:if test="${innerContainer?.weight }">
														${innerContainer?.weight} ${innerContainer?.weightUnits}
													</g:if>
												</td>
												<td width="20%">${innerItem?.product.name} (${innerItem?.quantity})</td>
												<td width="10%">${innerItem?.lotNumber}</td>
											</tr>																			
										</g:each>										
									</g:each>							
								</g:each>		
							</tbody>
						</table>
						<%-- 
						<div style="text-align: right; padding: 10px;">
							<g:link controller="shipment" action="downloadPackingList" id="${shipmentInstance.id}" ><img 
							src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}" 
							alt="Export Packing List" style="vertical-align: middle"/> Download</g:link>		
						</div>
						--%>														
					</fieldset>		
				</td>
			</tr>
		</table>

	</div>
</body>
</html>
