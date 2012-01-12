<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${params.print?'print':'custom' }" />
        <title><warehouse:message code="report.showChecklistReport.label" /></title>    
        <style>
        	.filter { padding-right: 30px; border: 0; border-right: 1px solid lightgrey; }
        	th { text-transform: uppercase; }
        	.title { text-align: center; padding: 15px; }
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
			<div class="form">
				<div class="title">
					Shipping Checklist Report
				</div>		
				<table>
					<tr class="prop">				
						<td class="name">
							<label>Shipment:</label>
						</td>
						<td class="value">
							${command?.shipment?.name }
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
						</td>
						<td class="center value">
							<table style="width:0;">
								<tr>
									<td>
										<g:checkBox name="received" class="middle"/> <label class="middle">Réception</label>
									</td>
									<td>
										<g:checkBox name="received" class="middle"/> <label class="middle">Livraison</label>
									</td>
									<td>
										<label class="middle">Containeur #:</label>
										<span class="middle">…………………………………………………………………………</span>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
						</td>				
						<td class="value">
							<div>
								<table style="width:0;">
									<tr>
										<td>
											<label>Le:</label>
										</td>
										<td>						
											 ………/………/2012					
										</td>
									</tr>
								</table>
							</div>
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
						</td>
						<td class="value">
							<div>
								<table style="width:0;">
									<tr>
										<td>
											<label>Livré le:</label>
										</td>
										<td>
											<span class="middle">…………………………………………………………………………</span>
										</td>
									</tr>
									<tr>
										<td>				
											<label>Vérifié le:</label>
										</td>
										<td>
											<span class="middle">…………………………………………………………………………</span>
										</td>
									</tr>
									<tr>
										<td>
											<label>Par:</label>
										</td>
										<td>
											<span class="middle">…………………………………………………………………………</span>
										</td>
									</tr>
								</table>
							</div>
						</td>
					</tr>
					<tr class="prop">
						<td class="name">

						</td>
						<td class="">
							<label>Origine / Destination</label> 
							<span class="middle">…………………………………………………………………………</span> 
							<label class="middle">Plaque:</label> 
							<span class="middle">…………………………………………………………………………</span>						
						</td>					
					</tr>					
					
					<tr class="prop">
						<td class="name">
							<label>Results</label>
						</td>
						<td class="value">
					    	<div class="list">
					   			<g:set var="status" value="${0 }"/>
					    		<table>
					    			<thead>
					    			
					    				<tr>
					    					<th>
					    						No.
					    					</th>
					    					<th>
					    						Description produit
					    					</th>
					    					<th>
						    					No lot
					    					</th>
					    					<th>
					    						Exp
					    					</th>
					    					<th>
					    						Q Rec
					    					</th>
					    					<th>
					    						Q Liv
					    					</th>
					    					<th>
					    						Utés
					    					</th>	
					    					<th>
						    					Qté en caisse
					 						</th>
					    				</tr>
					    			</thead>
					    		
					    			<tbody>
								    	<g:each var="checklistEntry" in="${command?.checklistReportEntryList }" status="i">
											<tr>
												<td>
													${i }							
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
													${checklistEntry?.shipmentItem?.quantity }
												</td>
												<td>
													${checklistEntry?.shipmentItem?.quantity }
												</td>
												<td>
													
												</td>
												<td>
													${checklistEntry?.shipmentItem?.quantity }
												</td>
												
											</tr>
										</g:each>
									</tbody>
								</table>
							</div>
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