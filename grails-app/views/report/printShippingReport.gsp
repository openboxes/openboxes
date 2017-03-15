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

        <hr/>
    
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${command}">
			<div class="errors">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>


	hello world
		<g:if test="${command?.shipment }">
            <div class="center">
                <table id="summary-table" style="width: auto;">
					<tr>
						<td class="top left">
                            <div class="logo">
                                <g:displayLogo location="${session?.warehouse?.id}"/>
                            </div>
						</td>
						<td class="middle left">
                            <div class="title">
                                <warehouse:message code="report.shippingReport.heading"/>
                            </div>
							<div class="subtitle">
								<div style="line-height: 24px">
									${session?.warehouse?.name }
								</div>
							</div>
						</td>
					</tr>
				</table>
				
				
				<table id="details-table">
					<tr>				
						<th>
							<label><warehouse:message code="report.containerNumber.label"/></label>
						</th>
						<td class="value underline">
							<span class="value">
								${command?.shipment?.name }
							</span>
						</td>
						<th>
							<label><warehouse:message code="report.plate.label"/></label>
						</th>
						<td class="value underline">
							<span class="value">${command?.shipment?.getReferenceNumber('License Plate Number')?.identifier }</span>						
						</td>
					</tr>
					<tr>
						<th>
							<label><warehouse:message code="report.origin.label"/></label>
						</th>
						<td class="value underline">
							<span class="value">${command?.shipment?.origin?.name }</span>							
						</td>
						<th>
							<label><warehouse:message code="report.destination.label"/></label>
						</th>
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
					    					<th colspan="2" class="center bottom">
												<label><warehouse:message code="report.quantityDelivered.label"/><!-- Livré, Delivered --></label>
											</th>
					    					<th colspan="2" class="center bottom">
						    					<label><warehouse:message code="report.quantityReceived.label"/><!-- Reçu, Received --></label>
					    					</th>
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
												<td>

												</td>
												<td class="center">
													${checklistEntry?.shipmentItem?.quantity }
                                                    ${checklistEntry?.shipmentItem?.product?.unitOfMeasure?:"EA" }
												</td>
												<td>

												</td>
												<td class="center">
													${checklistEntry?.shipmentItem?.quantity }
                                                    ${checklistEntry?.shipmentItem?.product?.unitOfMeasure?:"EA" }
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
						<th width="15%">
							<label><warehouse:message code="report.preparedBy.label"/></label><!--  Préparé par, Prepared by -->
						</th>
						<td class="value underline">
						
						</td>
						<th width="15%">
							<label><warehouse:message code="report.receivedBy.label"/></label><!-- Reçu par, Received by -->
						</th>
						<td class="value underline">
						
						</td>
					</tr>
					<tr>
						<th>
							<label><warehouse:message code="report.deliveredBy.label"/></label><!-- Livré par, Delivered/supplied by -->
						</th>
						<td class="value underline">
						
						</td>
						<th>
							<label><warehouse:message code="report.receivedOn.label"/></label><!-- Reçu le, Received on -->
						</th>
						<td class="value underline">
						
						</td>
					</tr>
					<tr>
						<th>
							<label><warehouse:message code="report.deliveredOn.label"/></label><!-- Livré le, Delivered/supplied on -->
						</td>
						<td class="value underline">
						
						</td>
						<th>
							<label><warehouse:message code="report.verifiedOn.label"/></label><!-- Vérifié le, Verified on -->
						</td>
						<td class="value underline">
						
						</td>
					</tr>
					<tr>
						<th>
							<label><warehouse:message code="report.transportedBy.label"/></label><!-- Transporté, Carrier -->
						</th>
						<td class="value underline">
						
						</td>
						<th>
							<label><warehouse:message code="report.verifiedBy.label"/></label><!-- Vérifié par, Verified by -->
						</th>
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