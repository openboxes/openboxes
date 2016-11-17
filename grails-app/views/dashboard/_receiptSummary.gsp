<!--  Show recent shipments/receipts -->
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode"%>


<div class="box">
    <h2>
		<warehouse:message code="receiving.summary.label"/>
		<small>${warehouse.message(code: 'dashboard.inbound.label', args: [session.warehouse.name, (end-start)]) }</small>

	</h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="shippingsummary">
			<g:if test="${!outgoingShipmentsByStatus}">
				<div style="text-align: left; padding: 10px;" class="fade">
					(<warehouse:message code="shipping.noRecent.label"/>)
				</div>
				
			</g:if>	    		
			<g:else>	
			
  				<g:set var="pending" value="${ShipmentStatusCode.PENDING}"/>
  				<g:set var="shipped" value="${ShipmentStatusCode.SHIPPED}"/>
  				<g:set var="received" value="${ShipmentStatusCode.RECEIVED}"/>
  				
				<g:set var="shipmentsPending" value="${incomingShipmentsByStatus[pending] }"/>			
				<g:set var="shipmentsEnroute" value="${incomingShipmentsByStatus[shipped] }"/>			
  				<g:set var="shipmentsReceived" value="${incomingShipmentsByStatus[received] }"/>
				<g:set var="incomingShipmentsTotal" value="${shipmentsPending.objectList.size + shipmentsEnroute.objectList.size + shipmentsReceived.objectList.size }"/>
                <g:set var="dateCreatedFrom" value="${start.format('MM/dd/yyyy')}"/>
                <g:set var="dateCreatedTo" value="${end.format('MM/dd/yyyy')}"/>

					
	    		<table class="table">
	    			<tbody>
						<tr>
							<td class="center" style="width: 1%">
								<p class="title">
									<g:link controller="shipment" action="list"
                                            params="[type:'incoming', status:pending, dateCreatedFrom:dateCreatedFrom, dateCreatedTo:dateCreatedTo]">
										${shipmentsPending.objectList.size}
									</g:link>

								</p>
                                <g:link controller="shipment" action="list"
                                        params="['type':'incoming','status':pending, dateCreatedFrom:dateCreatedFrom, dateCreatedTo:dateCreatedTo]">
                                    ${warehouse.message(code: 'dashboard.inbound.pending.label', args: [session.warehouse.name]) }
                                </g:link>

							</td>

							<td class="center" style="width: 1%">
								<p class="title">
									<g:link controller="shipment" action="list"
                                            params="['type':'incoming','status':shipped, dateCreatedFrom:dateCreatedFrom, dateCreatedTo:dateCreatedTo]">
										${shipmentsEnroute.objectList.size}
									</g:link>
								</p>
								<g:link controller="shipment" action="list"
                                        params="['type':'incoming','status':shipped, dateCreatedFrom:dateCreatedFrom, dateCreatedTo:dateCreatedTo]">
									${warehouse.message(code: 'dashboard.inbound.shipped.label', args: [session.warehouse.name]) }
								</g:link>

							</td>

							<td class="center" style="width: 1%">
								<p class="title">
									<g:link controller="shipment" action="list"
                                            params="['type':'incoming','status':received, dateCreatedFrom:dateCreatedFrom, dateCreatedTo:dateCreatedTo]">
										${shipmentsReceived.objectList.size}
									</g:link>
                                </p>

                                <g:link controller="shipment" action="list"
                                        params="['type':'incoming','status':received, dateCreatedFrom:dateCreatedFrom, dateCreatedTo:dateCreatedTo]">
                                    ${warehouse.message(code: 'dashboard.inbound.received.label', args: [session.warehouse.name]) }
                                </g:link>

							</td>
						</tr>							
			    	</tbody>
			    	<tfoot>
						<tr class="odd">
							<th colspan="2">
								<warehouse:message code="default.total.label"/>
							</th>
							<th class="right">
								${incomingShipmentsTotal }
							</th>
						</tr>
			    	</tfoot>
			    	
		    	</table>

			</g:else>
		</div>
	</div>	
</div>