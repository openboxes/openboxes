<!--  Show recent shipments/receipts -->
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode"%>
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
	    		<table>
	    			<thead>
	    				<tr>
	    					<th colspan="2">
		    					<warehouse:message code="shipping.shipmentsTo.label" args="[session.warehouse.name]"/> 		
		    				</th>
	    				</tr>
	    			</thead>
	    			<tbody>
	    				<g:set var="statusPending" value="${ShipmentStatusCode.PENDING}"/>
	    				<g:set var="statusShipped" value="${ShipmentStatusCode.SHIPPED}"/>
	    				<g:set var="statusReceived" value="${ShipmentStatusCode.RECEIVED}"/>
						<g:set var="shipmentsPending" value="${outgoingShipmentsByStatus[statusPending] }"/>			
						<g:set var="shipmentsShipped" value="${outgoingShipmentsByStatus[statusShipped] }"/>			
						<g:set var="shipmentsEnroute" value="${incomingShipmentsByStatus[statusShipped] }"/>			
	    				<g:set var="shipmentsReceived" value="${incomingShipmentsByStatus[statusReceived] }"/>
						<tr>
							<td>
								Enroute to ${session?.warehouse?.name }
							</td>
							<td style="text-align: right;">
								<g:link controller="shipment" action="list" params="['type':'incoming','status':statusShipped]">
									${shipmentsEnroute.objectList.size}
								</g:link>
							</td>
						</tr>				
						<tr>
							<td>
								Received by ${session?.warehouse?.name }
							</td>
							<td style="text-align: right;">
								<g:link controller="shipment" action="list" params="['type':'incoming','status':statusReceived]">
									${shipmentsReceived.objectList.size}
								</g:link>
							</td>
						</tr>							
							
			    	</tbody>
			    	<%-- 
			    	<tfoot>
				    	<tr style="border-top: 1px solid lightgrey">
				    		<td style="text-align: left;">
				    			<warehouse:message code="shipping.total.label"/>
				    		</td>
				    		<td style="text-align: right;">
				    			<g:link controller="shipment" action="list" params="[type:'outgoing']">
				    				${allOutgoingShipments.size()}
					    		</g:link>
					    	</td>
				    	</tr>
			    	</tfoot>
			    	--%>
		    	</table>
			</g:else>
		</div>
	</div>	    	
		    	
	<div class="widget-content">
		<div id="shippingsummary">
			<g:if test="${!outgoingShipmentsByStatus}">
				<div style="text-align: left; padding: 10px;" class="fade">
					(<warehouse:message code="shipping.noRecent.label"/>)
				</div>
			</g:if>	    		
			<g:else>					    	
		    	<table>
	    			<thead>
	    				<tr>
	    					<th colspan="2">
		    					<warehouse:message code="shipping.shipmentsFrom.label" args="[session.warehouse.name]"/> 		
		    				</th>
	    				</tr>
	    			</thead>
	    			<tbody>
						<tr>
							<td>
								Pending at ${session?.warehouse?.name } 
							</td>
							<td style="text-align: right;">
								<g:link controller="shipment" action="list" params="['type':'outgoing','status':statusPending]">
									${shipmentsPending.objectList.size}
								</g:link>
							</td>
						</tr>	
						<tr>
							<td>
								Shipped from ${session?.warehouse?.name } 
							</td>
							<td style="text-align: right;">
								<g:link controller="shipment" action="list" params="['type':'outgoing','status':statusShipped]">
									${shipmentsShipped.objectList.size}
								</g:link>
							</td>
						</tr>	
					</tbody>			
				</table>
		    </g:else>
		</div>
	</div>	
	
	<br clear="all"/>
	
</div>