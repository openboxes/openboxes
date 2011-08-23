<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${warehouse.message(code: 'default.dashboard.label', default: 'Dashboard')}</title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">${warehouse.message(code: 'default.dashboard.label', default: 'Dashboard')}</content>
    </head>
    <body>        
		<div class="body">				
	    	<div id="dashboard">
				<div class="widget-small">
					<div class="widget-header"><h2><warehouse:message code="order.summary.label" args="[session.warehouse.name]"/></h2></div>
	    			
	    			<div class="widget-content">	    					    			
	    				<div id="receivingsummary">	
		    				<g:if test="${!incomingOrders}">
   								<div style="text-align: left; padding: 10px;" class="fade">
   									(<warehouse:message code="order.noRecent.label"/>)
   								</div>
   							</g:if>	    		
   							<g:else>			
					    		<table>
						    		<thead>
					    				<tr class="">
					    					<td colspan="2">
					    						<warehouse:message code="order.ordersInto.label"/> ${session.warehouse.name }
					    					</td>
					    				</tr>
					    			</thead>				
					    			<tbody>
										<g:each var="entry" in="${incomingOrders}" status="i">	 
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td>${format.metadata(obj:entry.key)}</td>
												<td style="text-align: center;"><g:link controller="order" action="list" params="['status':entry?.key]">
													${entry?.value?.size}</g:link></td>
											</tr>	
								    	</g:each>
							    	</tbody>
							    	<tfoot>
								    	<tr style="border-top: 1px solid lightgrey">
								    		<th style="text-align: right;"><warehouse:message code="shipping.total.label"/></td>							    		
								    		<th style="text-align: center;"><g:link controller="order" action="list" params="">${incomingOrders?.values()?.flatten()?.size()}</g:link></td>
								    	</tr>
							    	</tfoot>
						    	</table>
						    </g:else>
						</div>
	    			</div>
	    			<br clear="all"/>
	    			
				</div>					
				<!--  Show recent shipments/receipts -->
				<div class="widget-small">
					<div class="widget-header"><h2><warehouse:message code="shipping.summary.label" args="[session.warehouse.name]"/></h2></div>
	    			<div class="widget-content">
	    				<div id="shippingsummary">
		    				<g:if test="${!outgoingShipmentsByStatus}">
   								<div style="text-align: left; padding: 10px;" class="fade">
   									(<warehouse:message code="shipping.noRecent.label"/>)
   								</div>
   							</g:if>	    		
   							<g:else>			
					    		<table >
					    			<thead>
					    				<tr class="">
					    					<td colspan="2">
					    						
					    						<warehouse:message code="shipping.shippingFrom.label"/> ${session.warehouse.name }
					    					</td>
					    				</tr>
					    			</thead>				    			
					    			<tbody>
										<g:each var="entry" in="${outgoingShipmentsByStatus}" status="i">											
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><format:metadata obj="${entry.key}"/></td>
												<td style="text-align: center;"><g:link controller="shipment" action="list" params="['status':entry.key]">${entry.value.objectList.size}</g:link></td>
											</tr>	
											
								    	</g:each>
							    	</tbody>
							    	<tfoot>
								    	<tr style="border-top: 1px solid lightgrey">
								    		<th style="text-align: right;"><warehouse:message code="shipping.total.label"/></th>
								    		<th style="text-align: center;"><g:link controller="shipment" action="list" params="">${allOutgoingShipments.size()}</g:link></th>
								    	</tr>
							    	</tfoot>
						    	</table>
						    </g:else>
						</div>
	    			</div>

	    			<div class="widget-content">	    					    			
	    				<div id="receivingsummary">	
		    				<g:if test="${!incomingShipmentsByStatus}">
   								<div style="text-align: left; padding: 10px;" class="fade">
   									(<warehouse:message code="receiving.noRecent.label"/>)
   								</div>
   							</g:if>	    		
   							<g:else>			
					    		<table>
						    		<thead>
					    				<tr class="">
					    					<td colspan="2">
					    						<warehouse:message code="shipping.receivingInto.label"/> ${session.warehouse.name }
					    					</td>
					    				</tr>
					    			</thead>				
					    			<tbody>
										<g:each var="entry" in="${incomingShipmentsByStatus}" status="i">	 
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><format:metadata obj="${entry.key}"/></td>
												<td style="text-align: center;"><g:link controller="shipment" action="list" params="[type: 'incoming', status: entry.key]">
													${entry.value.objectList.size}</g:link></td>
											</tr>	
								    	</g:each>
							    	</tbody>
							    	<tfoot>
								    	<tr style="border-top: 1px solid lightgrey">
								    		<th style="text-align: right;"><warehouse:message code="shipping.total.label"/></td>							    		
								    		<th style="text-align: center;"><g:link controller="shipment" action="list" params="[type: 'incoming']">${allIncomingShipments.size()}</g:link></td>
								    	</tr>
							    	</tfoot>
						    	</table>
						    </g:else>
						</div>
	    			</div>
	    			<br clear="all"/>
	    			
				</div>
	    	</div>
		</div>
    </body>
</html>

