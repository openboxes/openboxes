<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${params.print?'print':'custom' }" />
        <title><warehouse:message code="report.showChecklistReport.label" /></title>    
        <style>
        	body, td, th, div { font-family: 'Times New Roman'; }
        	.filter { padding-right: 30px; border: 0; border-right: 1px solid lightgrey; }
        	/*th { text-transform: uppercase; }*/
        	th { border-bottom: 1px solid black; }
        	.title { text-align: center; padding: 5px; font-size: 3em; }
        	.subtitle { text-align: center; padding: 15px; font-size: 2em; }
        	.underline { border-bottom: 1px dashed black; }
        	.label { width: 15%; text-align: left; vertical-align: bottom; }
        	.value { font-weight: bold; width: 30%; }
        	.spacer { width: 10%; }
        	td { padding: 10px; }
        	table { margin-left: auto; margin-right: auto; }
        	
        </style>
    </head>    
    <body>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${command}">
			<div class="errors">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>
	   	<g:if test="${!params.print}">
			<div class="form" >
				<g:form controller="report" action="showChecklistReport" method="GET">
					<%-- 
					<span class="filter">
						<label>Location</label>
						<g:selectLocation class="filter" name="location.id" noSelection="['null':'']" value="${command?.location?.id}"/>
					</span>	
					--%>
					<table>
						<tr class="prop">
							<td>
								<label>Shipment</label>
								<g:selectShipment class="filter" name="shipment.id" noSelection="['null':'']" value="${command?.shipment?.id}"/>
							</td>
							<%-- 
							<span class="filter">
								<label>Start date</label>
								<g:jqueryDatePicker class="filter" id="startDate" name="startDate" value="${command?.startDate }" format="MM/dd/yyyy"/>
							</span>					
							<span class="filter">
								<label>End date</label>
								<g:jqueryDatePicker class="filter" id="endDate" name="endDate" value="${command?.endDate }" format="MM/dd/yyyy"/>
							</span>
							<span>
								<button type="submit" class="btn">Run Report</button>
							</span>
							--%>
						</tr>	
				    	<tr class="prop">
				    		<td>
								<label>Export as</label>
								<g:if test="${command?.shipment }">
						   			<g:link target="_blank" controller="report" action="showChecklistReport" params="[print:'true','shipment.id':command?.shipment?.id]">HTML</g:link> 
						   			|
						   			<g:link target="_blank" controller="report" action="downloadChecklistReport" params="[format:'pdf',url:request.forwardURI,'shipment.id':command?.shipment?.id]">PDF</g:link>
						   			|
						   			<g:link target="_blank" controller="report" action="downloadChecklistReport" params="[format:'docx',url:request.forwardURI,'shipment.id':command?.shipment?.id]">DOCX</g:link>
						   		</g:if>
						   		<g:else>
						   			Please select a shipment from above.
						   		</g:else>
						   		
					   		</td>
						</tr>
					</table>
					
				</g:form>				
			</div>
		</g:if>
		<g:else>
		</g:else>

		<g:if test="${command?.shipment }">
			<div>
				<table border="0">
					<tr>
						<td colspan="5">
							<div class="title">				
								PIH / Zanmi Lasante
							</div>								
						</td>							
					</tr>
					<tr>
						<td colspan="5">
							<div class="subtitle">
								Bordereau de livraison			
							</div>							
						</td>
					</tr>
					<tr>
						<td colspan="5" style="margin: 0; padding: 0;">
							<hr/>
						</td>
					</tr>					
					<tr>				
						<td class="label">
							<label>Containeur #:</label>
						</td>
						<td class="value underline">
							<span class="value">${command?.shipment?.name }</span>
						</td>
						<td class="spacer">
						
						</td>
						<td class="label">
							<label>Plaque:</label>
						</td>
						<td class="value underline">
							<span class="value"></span>						
						</td>
					</tr>
					<tr>
						<td class="label">
							<label>Origine:</label>				
						</td>
						<td class="value underline">
							<span class="value">${command?.shipment?.origin?.name }</span>							
						</td>
						<td class="spacer">
						
						</td>
						<td class="label">
							<label>Destination:</label>
						</td>
						<td class="value underline">
							<span class="value">${command?.shipment?.destination?.name }</span>
						</td>
					</tr>
					<tr>
						<td colspan="5">
							<div class="list">
					   			<g:set var="status" value="${0 }"/>
					    		<table border="1">
					    			<thead>
					    				<tr>
					    					<th rowspan="2" class="center" class="bottom">
					    						No.<!-- Number -->
					    					</th>
					    					<th rowspan="2" class="bottom">
					    						Description produit<!-- Product description -->
					    					</th>
					    					<th rowspan="2" class="center bottom">
						    					No lot<!-- Qty -->
					    					</th>
					    					<th rowspan="2" class="center bottom">
					    						Exp<!-- Expiration date -->
					    					</th>
					    					<th rowspan="2" class="center bottom">
					    						Qté en caisse<!-- Qty in case -->
					    					</th>
					    					<th colspan="2" class="center bottom">
												Livré<!-- Delivered -->
											</th>
					    					<th colspan="2" class="center bottom">
						    					Reçu<!-- Received -->
					    					</th>
					    				</tr>
					    				<tr>
					    					<th class="center">
						    					Qté caisse<!-- Box Qty -->
					 						</th>
					    					<th class="center">
						    					Qté<!-- Qty -->
					 						</th>
					    					<th class="center">
						    					Qté caisse<!-- Box Qty -->
					 						</th>
					    					<th class="center">
						    					Qté<!-- Qty -->
					 						</th>
					    				</tr>
					    			</thead>
					    		
					    			<tbody>
								    	<g:each var="checklistEntry" in="${command?.checklistReportEntryList }" status="i">
											<tr>
												<td>
													${i+1 }							
												</td>
												<td>	    	
													${checklistEntry?.shipmentItem?.product?.name }
												</td>
												<td>
													${checklistEntry?.shipmentItem?.lotNumber }
												</td>							
												<td>
													${checklistEntry?.shipmentItem?.expirationDate }
												</td>							
												<td>

												</td>
												<td>

												</td>
												<td>
													
												</td>
												<td>

												</td>
												<td>
												
												</td>
											</tr>
										</g:each>
									</tbody>
								</table>
							</div>
						</td>
					</tr>
					<tr>
						<td class="label">
							<label>Préparé par:</label><!--  Prepared by -->
						</td>
						<td class="value underline">
						
						</td>
						<td class="spacer">
						
						</td>						
						<td class="label">
							<label>Reçu par:</label><!-- Received by -->
						</td>
						<td class="value underline">
						
						</td>
					</tr>
					<tr>
						<td class="label">
							<label>Livré par:</label><!--  Delivered/supplied by -->
						</td>
						<td class="value underline">
						
						</td>
						<td class="spacer">
						
						</td>						
						<td class="label">
							<label>Reçu le:</label><!-- Received on -->
						</td>
						<td class="value underline">
						
						</td>
					</tr>
					<tr>
						<td class="label">
							<label>Livré le:</label><!-- Delivered/supplied on -->
						</td>
						<td class="value underline">
						
						</td>
						<td class="spacer">
						
						</td>						
						<td class="label">
							<label>Vérifié le:</label><!-- Verified on -->
						</td>
						<td class="value underline">
						
						</td>
					</tr>
					<tr>
						<td class="label">
							<label>Transporté:</label><!-- Carrier -->
						</td>
						<td class="value underline">
						
						</td>
						<td class="spacer">
						
						</td>						
						<td class="label">
							<label>Vérifié par:</label><!-- Verified by -->
						</td>
						<td class="value underline">
						
						</td>
					</tr>

				</table>
			</div>
		</g:if>
		
		<%--
    	<div class="list">
   			<g:set var="status" value="${0 }"/>
	    	<g:each var="productEntry" in="${command?.productsByCategory }" status="i">
	    		<g:set var="category" value="${productEntry.key }"/>
	    		<div style="page-break-after: always">		    		
			    	<table>
			    		<thead>
				    		<tr style="border-top: 1px solid lightgrey;">
								<th class="left">								
									${category?.parentCategory?.name?.encodeAsHTML() } /
				    				${category?.name?.encodeAsHTML() }					    			
					    			<g:if test="${!params.print }">
						    			[<g:link controller="report" action="showTransactionReport" params="['location.id':command.location?.id,'category.id':category.id,'startDate':command.startDate,'endDate':command.endDate]" style="display: inline">show</g:link>]
					    			</g:if>
								</th>
								<th class="center" style="border-right: 1px solid lightgrey">Start</th>
								<g:each var="location" in="${transferInLocations }">
									<th class="center">${location.name.substring(0,3) }</th>
								</g:each>
								<th class="center">Transfer In</th>	
								<th class="center" style="border-right: 1px solid lightgrey">Total In</th>	
								<g:each var="location" in="${transferOutLocations }">
									<th class="center">${location.name.substring(0,3) }</th>
								</g:each>
								<th class="center">Transfer Out</th>
								<th class="center">Expired</th>
								<th class="center">Consumed</th>
								<th class="center">Damaged</th>
								<th class="center" style="border-right: 1px solid lightgrey">Total Out</th>
								<th class="center nowrap">Found</th>	
								<th class="center">Lost</th>
								<th class="center" style="border-right: 1px solid lightgrey">Total Adjusted</th>
								<th class="center">End</th>
				    		</tr>
				    	</thead>
				    	<tbody>
					    	<g:each var="product" in="${productEntry.value }" status="j">
					    		<g:set var="entry" value="${command.inventoryReportEntryMap[product] }"/>
								<tr class="${status++%2 ? 'even' : 'odd' }">
									<td class="left">
										<g:if test="${!params.print }">
											<g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]" fragment="inventory">   	
									    		${product?.name.encodeAsHTML() }
								    		</g:link>
											[<g:link controller="report" action="showProductReport" params="['product.id':product?.id,'location.id':session?.warehouse?.id,startDate:params.startDate,endDate:params.endDate]" fragment="inventory">details</g:link>]
							    		</g:if>
							    		<g:else>
								    		${product?.name.encodeAsHTML() }
							    		</g:else>
						    		</td>
									<td class="center" style="border-right: 1px solid lightgrey">	    	
							    		<strong>${entry?.quantityInitial ?: 0}</strong>
						    		</td>
									<g:each var="location" in="${transferInLocations }">
										<td class="center">${entry.quantityTransferredInByLocation[location]?:0}</td>
									</g:each>
									<td class="center">	    	
							    		${entry?.quantityTransferredIn ?: 0}
									</td>
									<td class="center" style="border-right: 1px solid lightgrey">	    	
							    		${entry?.quantityTotalIn ?: 0}
									</td>
									<g:each var="location" in="${transferOutLocations }">
										<td class="center">${entry.quantityTransferredOutByLocation[location]?:0}</td>
									</g:each>
									<td class="center">	    	
										${entry?.quantityTransferredOut ?: 0}
									</td>
									<td class="center">	    	
							    		${entry?.quantityExpired ?: 0}
									</td>
									<td class="center">	    	
							    		${entry?.quantityConsumed ?: 0}
									</td>
									<td class="center">	    	
							    		${entry?.quantityDamaged ?: 0}
									</td>
									<td class="center" style="border-right: 1px solid lightgrey">
										${entry?.quantityTotalOut ?: 0 }
									</td>
									<td class="center">	    	
							    		${entry?.quantityFound ?: 0}
									</td>
									<td class="center">	    	
							    		${entry?.quantityLost ?: 0}
									</td>
									<td class="center" style="border-right: 1px solid lightgrey">	    	
							    		${entry?.quantityTotalAdjusted ?: 0}
									</td>
									<td class="center">	    	
							    		<strong>${entry?.quantityFinal ?: 0}</strong>
									</td>
						    	</tr>
					    	</g:each>
						</tbody>
					</table>
				</div>
			</g:each>
    	</div>
    	 --%>
	    <script>
			$(document).ready(function() {
				$(".filter").change(function() { 
					$(this).closest("form").submit();
				});
			});
	    </script>
    </body>
</html>