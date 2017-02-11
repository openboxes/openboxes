<!--  Show recent shipments/receipts -->
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode"%>

<div class="box">
    <h2>
		<warehouse:message code="shipping.summary.label"/>
		<small>${warehouse.message(code: 'dashboard.outbound.label', args: [session.warehouse.name, (end-start)]) }</small>
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
				<g:set var="shipmentsPending" value="${outgoingShipmentsByStatus[pending] }"/>			
				<g:set var="shipmentsShipped" value="${outgoingShipmentsByStatus[shipped] }"/>		
				<g:set var="shipmentsReceived" value="${outgoingShipmentsByStatus[received] }"/>		
				<g:set var="outgoingShipmentsTotal" value="${shipmentsPending.objectList.size + shipmentsShipped.objectList.size + shipmentsReceived.objectList.size }"/>	
                <g:set var="dateCreatedFrom" value="${start.format('MM/dd/yyyy')}"/>
                <g:set var="dateCreatedTo" value="${end.format('MM/dd/yyyy')}"/>
	    		<table class="table">
                    <tbody>
						<tr>
							<td style="padding: 10px; margin: 10px">
								<p class="center">
									<g:link controller="shipment" action="list"
											params="['type':'outgoing','status':pending, 'dateCreatedFrom':dateCreatedFrom, 'dateCreatedTo':dateCreatedTo]">
										${warehouse.message(code: 'dashboard.outbound.pending.label', args: [session.warehouse.name]) }</g:link>
								</p>
                                <p class="indicator">
                                    <g:link controller="shipment" action="list"
                                            params="['type':'outgoing','status':pending, 'dateCreatedFrom':dateCreatedFrom, 'dateCreatedTo':dateCreatedTo]">
                                        ${shipmentsPending.objectList.size}
                                    </g:link>
                                </p>
							</td>

							<td style="padding: 10px; margin: 10px">
								<p class="center">
									<g:link controller="shipment" action="list"
											params="['type':'outgoing','status':shipped, 'dateCreatedFrom':dateCreatedFrom, 'dateCreatedTo':dateCreatedTo]">
										${warehouse.message(code: 'dashboard.outbound.shipped.label', args: [session.warehouse.name]) }
									</g:link>
								</p>
                                <p class="indicator">
                                    <g:link controller="shipment" action="list"
                                            params="['type':'outgoing','status':shipped, 'dateCreatedFrom':dateCreatedFrom, 'dateCreatedTo':dateCreatedTo]">
                                        ${shipmentsShipped.objectList.size}
                                    </g:link>
                                </p>

							</td>

							<td style="padding: 10px; margin: 10px">
								<p class="center">
									<g:link controller="shipment" action="list"
											params="['type':'outgoing', 'status':received, 'dateCreatedFrom':dateCreatedFrom, 'dateCreatedTo':dateCreatedTo]">
										${warehouse.message(code: 'dashboard.outbound.received.label', args: [session.warehouse.name]) }
									</g:link>
								</p>
                                <p class="indicator">
                                    <g:link controller="shipment" action="list"
                                            params="['type':'outgoing','status':received, 'dateCreatedFrom':dateCreatedFrom, 'dateCreatedTo':dateCreatedTo]">
                                        ${shipmentsReceived.objectList.size}
                                    </g:link>
                                </p>
							</td>
						</tr>
			    	</tbody>			    	
			    	<tfoot>
						<tr class="odd">
							<th colspan="2">
								<warehouse:message code="default.total.label"/>
							</th>
							<th class="right">
								<div class="indicator">
									${outgoingShipmentsTotal }
								</div>
							</th>
						</tr>
			    	</tfoot>
		    	</table>

			</g:else>
		</div>
	</div>	
</div>