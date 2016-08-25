<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="print" />
	    <link rel="stylesheet" href="${resource(dir:'css',file:'print.css')}" type="text/css" media="print, screen, projection" />
        <title><warehouse:message code="report.showShippingReport.label" /></title>    
        
    </head>    
    <body>

		<button type="button" id="print-button" onclick="window.print()">
	        <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
	        ${warehouse.message(code:"default.print.label")}
	    </button>    
    
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${command}">
			<div class="errors">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>

		<g:if test="${command?.shipment }">
			<div>
				<table>
					<tr>
						<td rowspan="3" class="middle left">
							<img src="${resource(dir:'images/',file:'hands.jpg')}"  width="65" height="65"/>
						</td>
						<td class="center">
							<div class="title">			
								<warehouse:message code="report.shippingReport.heading"/>	
							</div>								
							<div class="subtitle">
								<div style="line-height: 24px">
									${session?.warehouse?.name }
								</div>
								<div style="line-height: 24px">
									<warehouse:message code="report.shippingReport.title"/>	
								</div>
							</div>							
						</td>			
						<td rowspan="3" class="middle right">
							<img src="${resource(dir:'images/',file:'hands.jpg')}" width="65" height="65" />
						</td>				
					</tr>
				</table>
				
				
				<table id="details-table">
					<tr>				
						<td class="label">
							<label><warehouse:message code="report.containerNumber.label"/></label>
						</td>
						<td class="value underline">
							<span class="value">
								${command?.shipment?.name }
							</span>
						</td>
						<td class="spacer">
						
						</td>
						<td class="label">
							<label><warehouse:message code="report.plate.label"/></label>
						</td>
						<td class="value underline">
							<span class="value">${command?.shipment?.getReferenceNumber('License Plate Number')?.identifier }</span>						
						</td>
					</tr>
					<tr>
						<td class="label">
							<label><warehouse:message code="report.origin.label"/></label>
						</td>
						<td class="value underline">
							<span class="value">${command?.shipment?.origin?.name }</span>							
						</td>
						<td class="spacer">
						
						</td>
						<td class="label">
							<label><warehouse:message code="report.destination.label"/></label>
						</td>
						<td class="value underline">
							<span class="value">${command?.shipment?.destination?.name }</span>
						</td>
					</tr>
				</table>
				
				<table >
					<tr>
						<td colspan="5">
							<div class="list">
					   			<g:set var="status" value="${0 }"/>
					    		<table id="packinglist-items">
					    			<thead>
					    				<tr>
					    					<th rowspan="2" class="center bottom">
					    						<warehouse:message code="report.number.label"/><!-- No., Number -->
					    					</th>
					    					<th rowspan="2" class="left bottom" width="15%">
						    					<warehouse:message code="shipping.container.label"/>
					    					</th>
                                            <th rowspan="2" class="bottom">
                                                <warehouse:message code="product.productCode.label"/>
                                            </th>
					    					<th rowspan="2" class="bottom">
					    						<warehouse:message code="report.productDescription.label"/><!-- Description produit, Product description -->
					    					</th>
					    					<th rowspan="2" class="left bottom">
						    					<warehouse:message code="report.lotNumber.label"/><!-- No lot, Lot Number -->
					    					</th>
					    					<th rowspan="2" class="center bottom">
					    						<warehouse:message code="report.expirationDate.label"/><!-- Exp, Expiration date -->
					    					</th>
					    					<td colspan="2" class="center bottom">
												<label><warehouse:message code="report.quantityDelivered.label"/><!-- Livré, Delivered --></label>
											</td>
					    					<td colspan="2" class="center bottom">
						    					<label><warehouse:message code="report.quantityReceived.label"/><!-- Reçu, Received --></label>
					    					</td>
					    				</tr>
					    				<tr>
					    					<th class="center">
						    					<warehouse:message code="report.quantityPerBox.label"/><!-- Qté caisse, Qty per box -->
					 						</th>
					    					<th class="center">
						    					<warehouse:message code="report.quantityTotal.label"/><!-- Qté, Qty -->
					 						</th>
					    					<th class="center">
						    					<warehouse:message code="report.quantityPerBox.label"/><!-- Qté caisse, Qty per box -->
					 						</th>
					    					<th class="center">
						    					<warehouse:message code="report.quantityTotal.label"/><!-- Qté, Qty -->
					 						</th>
					    				</tr>
					    			</thead>
					    		
					    			<tbody>
					    			
									    <g:set var="previousContainer"/>
										<g:set var="shipmentItemsByContainer" value="${command?.checklistReportEntryList?.groupBy { it?.shipmentItem?.container } }"/>
								    	<g:each var="checklistEntry" in="${command?.checklistReportEntryList }" status="i">
											<g:set var="newContainer" value="${checklistEntry?.shipmentItem?.container != previousContainer }"/>	
										    <g:set var="rowspan" value="1"/>
											<tr style="${newContainer?'border-top:3px solid lightgrey':''}">
												<td class="center">
													${i+1 }							
												</td>
												<td class="left" rowspan="${rowspan }">
													<g:if test="${newContainer }">
														<g:render template="../shipment/container" model="[container:checklistEntry?.shipmentItem?.container]"/>
													</g:if>
												</td>
                                                <td>
                                                    ${checklistEntry?.shipmentItem?.inventoryItem?.product?.productCode}
                                                </td>
												<td>
                                                    <format:product product="${checklistEntry?.shipmentItem?.inventoryItem?.product}"/>
												</td>
												<td>
													${checklistEntry?.shipmentItem?.inventoryItem?.lotNumber }
												</td>							
												<td>
													<format:expirationDate obj="${checklistEntry?.shipmentItem?.inventoryItem?.expirationDate }"/>
													
												</td>	
												<%-- 						
												<td>

												</td>
												--%>
												<td>

												</td>
												<td class="center">
													${checklistEntry?.shipmentItem?.quantity }
												</td>
												<td>

												</td>
												<td class="center">
													${checklistEntry?.shipmentItem?.quantity }
												</td>
											</tr>
								    		<g:set var="previousContainer" value="${checklistEntry?.shipmentItem?.container}"/>
										</g:each>
									</tbody>
								</table>
							</div>
						</td>
					</tr>
				</table>
				
				<table id="signature-table">
					<tr>
						<td class="label">
							<label><warehouse:message code="report.preparedBy.label"/></label><!--  Préparé par, Prepared by -->
						</td>
						<td class="value underline">
						
						</td>
						<td class="spacer">
						
						</td>						
						<td class="label">
							<label><warehouse:message code="report.receivedBy.label"/></label><!-- Reçu par, Received by -->
						</td>
						<td class="value underline">
						
						</td>
					</tr>
					<tr>
						<td class="label">
							<label><warehouse:message code="report.deliveredBy.label"/></label><!-- Livré par, Delivered/supplied by -->
						</td>
						<td class="value underline">
						
						</td>
						<td class="spacer">
						
						</td>						
						<td class="label">
							<label><warehouse:message code="report.receivedOn.label"/></label><!-- Reçu le, Received on -->
						</td>
						<td class="value underline">
						
						</td>
					</tr>
					<tr>
						<td class="label">
							<label><warehouse:message code="report.deliveredOn.label"/></label><!-- Livré le, Delivered/supplied on -->
						</td>
						<td class="value underline">
						
						</td>
						<td class="spacer">
						
						</td>						
						<td class="label">
							<label><warehouse:message code="report.verifiedOn.label"/></label><!-- Vérifié le, Verified on -->
						</td>
						<td class="value underline">
						
						</td>
					</tr>
					<tr>
						<td class="label">
							<label><warehouse:message code="report.transportedBy.label"/></label><!-- Transporté, Carrier -->
						</td>
						<td class="value underline">
						
						</td>
						<td class="spacer">
						
						</td>						
						<td class="label">
							<label><warehouse:message code="report.verifiedBy.label"/></label><!-- Vérifié par, Verified by -->
						</td>
						<td class="value underline">
						
						</td>
					</tr>

				</table>
			</div>
		</g:if>
		
	    <script>
			$(document).ready(function() {
				$(".filter").change(function() { 
					$(this).closest("form").submit();
				});
			});
	    </script>
    </body>
</html>