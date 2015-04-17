<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="print" />
	    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'print.css')}" type="text/css" media="print, screen, projection" />
        <title><warehouse:message code="report.showShippingReport.label" /></title>    
        <style>
        	.header { font-size: 24px; padding: 10px; }
        
        </style>
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
				<div class="list">
		   			<g:set var="status" value="${0 }"/>
			    	<g:set var="packingListByContainer" value="${command?.checklistReportEntryList?.groupBy { it?.shipmentItem?.container } }"/>
		   			<g:each var="packingListEntry" in="${packingListByContainer}">
		   			
		   				<div class="page" style="page-break-after: always;">
			   			
				    		<table id="packinglist-items" width="100%" class="fs-repeat-header">
				    			<thead>
				    				<tr>
				    					<td colspan="7" class="center" width="99%">
					    					<table border="0">
												<thead>
													<tr>
														<td class="left middle" width="1%">
                                                            <g:displayLogo location="${session?.warehouse?.id}"/>
														</td>
														<td class="left middle">
															<div class="title">
                                                                <warehouse:message code="report.shippingReport.title"/>
															</div>
															<div class="subtitle">
																${command?.shipment?.name?.encodeAsHTML() }
															</div>
														</td>
                                                        <td class="center middle" width="1%">
                                                            <g:displayBarcode data="${command?.shipment?.shipmentNumber}" height="50" width="120" showData="${true}"/>
                                                        </td>
													</tr>
												</thead>
											</table>
                                            <br/>
                                            <div class="center">

                                                <table style="width: 50%">
                                                    <tr class="prop">
                                                        <td>
                                                            <label><warehouse:message code="shipping.shipmentNumber.label"/></label>

                                                        </td>
                                                        <td>
                                                            ${command?.shipment?.shipmentNumber?.encodeAsHTML() }
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td>
                                                            <label><warehouse:message code="shipping.expectedShippingDate.label"/></label>

                                                        </td>
                                                        <td>
                                                            <g:formatDate date="${command?.shipment?.expectedShippingDate }"/>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td>
                                                            <label><warehouse:message code="shipping.expectedDeliveryDate.label"/></label>

                                                        </td>
                                                        <td>
                                                            <g:formatDate date="${command?.shipment?.expectedDeliveryDate }"/>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td>
                                                            <label><warehouse:message code="shipping.origin.label"/></label>

                                                        </td>
                                                        <td>
                                                            ${command?.shipment?.origin?.name?.encodeAsHTML() }
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td>
                                                            <label><warehouse:message code="shipping.destination.label"/></label>
                                                        </td>
                                                        <td>
                                                            ${command?.shipment?.destination?.name?.encodeAsHTML() }
                                                        </td>
                                                    </tr>
                                                </table>
                                            </div>


                                        </td>
				    				</tr>
				    				<tr>
				    					<td colspan="7" class="center">
				    						<div class="header">
                                                <g:render template="/shipment/container" model="[container:packingListEntry?.key]"/>

							   				</div>
				    					</td>
				    				</tr>
				    				<tr>
				    					<th class="center bottom">
				    						<warehouse:message code="report.number.label"/>
				    					</th>
                                        <th class="">

                                        </th>
                                        <th class="bottom">
                                            <warehouse:message code="product.productCode.label"/>
                                        </th>
				    					<th class="bottom">
				    						<warehouse:message code="product.description.label"/>
				    					</th>
				    					<th class="center bottom">
					    					<warehouse:message code="inventoryItem.lotNumber.label"/>
				    					</th>
				    					<th class="center bottom">
				    						<warehouse:message code="inventoryItem.expirationDate.label"/>
				    					</th>
				    					<th class="center bottom">
				    						<warehouse:message code="shipping.recipient.label"/>
				    					</th>
				    				
				    					<th class="center">
					    					<warehouse:message code="report.quantityPerBox.label"/>
				 						</th>
				    					<th class="center">
					    					<warehouse:message code="report.quantityTotal.label"/>
				 						</th>
				    				</tr>
				    			</thead>
				    		
				    			<tbody>
				    				<g:each var="checklistEntry" in="${packingListEntry?.value }" status="i">
										<tr class="${i % 2 ? 'even' : 'odd' }">
											<td class="center">
												${i+1 }							
											</td>
                                            <td>
                                                <g:displayBarcode data="${checklistEntry?.shipmentItem?.inventoryItem?.product?.productCode?:checklistEntry?.shipmentItem?.product?.productCode}"
                                                                  height="50" width="120" showData="${false}"/>
                                            </td>
                                            <td>
                                                ${checklistEntry?.shipmentItem?.inventoryItem?.product?.productCode?:checklistEntry?.shipmentItem?.product?.productCode}
                                            </td>
											<td>
												<format:product product="${checklistEntry?.shipmentItem?.inventoryItem?.product?:checklistEntry?.shipmentItem?.product}"/>
											</td>
											<td>
												${checklistEntry?.shipmentItem?.inventoryItem?.lotNumber?:checklistEntry?.shipmentItem?.lotNumber  }
											</td>							
											<td>
												<format:expirationDate obj="${checklistEntry?.shipmentItem?.inventoryItem?.expirationDate?:checklistEntry?.shipmentItem?.expirationDate }"/>												
											</td>												
											<td>
												<g:if test="${checklistEntry?.shipmentItem?.recipient }">
													${checklistEntry?.shipmentItem?.recipient?.name?.encodeAsHTML()  }
												</g:if>
												<g:elseif test="${checklistEntry?.shipmentItem?.container?.recipient }">
													${checklistEntry?.shipmentItem?.container?.recipient?.name?.encodeAsHTML()  }
												</g:elseif>
												<g:elseif test="${checklistEntry?.shipmentItem?.shipment?.recipient }">
													${checklistEntry?.shipmentItem?.shipment?.recipient?.name?.encodeAsHTML()  }
												</g:elseif>
											</td>
											<td>
			
											</td>
											<td class="center">
												${checklistEntry?.shipmentItem?.quantity }
											</td>
										</tr>
									</g:each>
								</tbody>
							</table>								
							<div style="margin: 10px;">
								<label class="block">
									<warehouse:message code="default.comments.label"/>
								</label>
								<div style="border: 1px solid lightgrey; width: 100%; height: 200px;">
									&nbsp;
								</div>
							</div>
						</div>
						
					</g:each>
				</div>		
			</div>
		</g:if>
    </body>
</html>