<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
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
				<td colspan="2">
					<div style="padding-bottom: 10px;">
						<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
						 &nbsp; &raquo; &nbsp; 
						<span style="font-size: 90%">View Packing List</span>
					</div>					
				</td>
			</tr>
	
			<tr>
				<td>
					<fieldset>	
					
						<g:render template="summary"/>
											
						<br/>
						<table>
							<tr>
								<td>
									<div id="containers" class="section">			
															
										<table border="1" style="padding: 0px; margin: 0px">
											<tbody>
												<tr>
													<th>Shipment Unit</th>
													<th>Dimensions</th>
													<th>Weight</th>
													<th>Item</th>
													<th>Serial No.</th>										
												</tr>				
												<g:each var="container" in="${shipmentInstance.containers}" status="i">
													<g:each var="item" in="${container.shipmentItems}" status="j">
														<tr>
															<td width="10%">
																<g:if test="${j==0}">
																	${container?.containerType?.name} #${container?.name}
																</g:if>
															</td>																
															<td width="5%">
																<g:if test="${j==0}">
																	<g:if test="${container?.height && container?.width && container?.length}">	
																		${container?.height} x ${container?.width} x ${container?.length}
																	</g:if>
																	<g:else>
																		(empty)
																	</g:else>
																</g:if>
															</td>
															<td width="5%">
																<g:if test="${j==0}">
																	${container?.weight} ${container?.weightUnits}
																</g:if>
															</td>
															<td width="20%" style="border:1px solid black">${item?.quantity} x ${item?.product.name}</td>
															<td width="15%" style="border:1px solid black">${item?.serialNumber}</td>
														</tr>																			
													</g:each>										
												</g:each>		
											</tbody>
										</table>
									</div>
									<div style="text-align: right; padding: 10px;">
										<g:link controller="shipment" action="downloadPackingList" id="${shipmentInstance.id}" ><img 
										src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}" 
										alt="Export Packing List" style="vertical-align: middle"/> Download</g:link>		
									</div>						
											
								</td>
							</tr>
						</table>
					</fieldset>		
		



				
				</td>
		
		
				<td width="20%">
					<g:render template="sidebar"/>
				</td>
			</tr>
		</table>

	</div>
</body>
</html>
