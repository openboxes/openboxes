<!--  Show recent shipments/receipts -->
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode"%>

<div class="box">
    <h2><warehouse:message code="shipping.label"/></h2>
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

					
	    		<table class="zebra">
                    <thead>
                        <tr class="prop odd">
                            <td colspan="3" class="left">
                                <g:set var="startDate" value="${g.formatDate(date:new Date()-7, format:'MMMMM dd')}"/>
                                <g:set var="endDate" value="${g.formatDate(date:new Date()+7, format:'MMMMM dd')}"/>
                                <div class="fade">${warehouse.message(code: 'dashboard.outgoing.label', args: [session.warehouse.name, startDate, endDate]) }</div>

                            </td>
                        </tr>
                    </thead>
                    <tbody>
						<tr>
							<td class="center" style="width: 1%">
								<img src="${createLinkTo(dir:'images/icons/silk/lorry_flatbed.png')}" class="middle"/>
							</td>
							<td>
								<g:link controller="shipment" action="list" params="['type':'outgoing','status':pending]">
									${warehouse.message(code: 'dashboard.outgoing.pending.label', args: [session.warehouse.name]) }							
								</g:link>
							</td>
							<td style="text-align: right;">
								<g:link controller="shipment" action="list" params="['type':'outgoing','status':pending]">
									${shipmentsPending.objectList.size}
								</g:link>
							</td>
						</tr>	
						<tr>
							<td class="center" style="width: 1%">
								<img src="${createLinkTo(dir:'images/icons/silk/lorry_go.png')}" class="middle"/>
							</td>
						
							<td>
								<g:link controller="shipment" action="list" params="['type':'outgoing','status':shipped]">
									${warehouse.message(code: 'dashboard.outgoing.shipped.label', args: [session.warehouse.name]) }							
								</g:link>
							</td>
							<td style="text-align: right;">
								<g:link controller="shipment" action="list" params="['type':'outgoing','status':shipped]">
									${shipmentsShipped.objectList.size}
								</g:link>
							</td>
						</tr>
							
						<tr>
							<td class="center" style="width: 1%">
								<img src="${createLinkTo(dir:'images/icons/silk/lorry_stop.png')}" class="middle"/>
							</td>
						
							<td>
								<g:link controller="shipment" action="list" params="['type':'outgoing','status':received]">
									${warehouse.message(code: 'dashboard.outgoing.received.label', args: [session.warehouse.name]) }							
								</g:link>
							</td>
							<td style="text-align: right;">
								<g:link controller="shipment" action="list" params="['type':'outgoing','status':received]">
									${shipmentsReceived.objectList.size}
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
								${outgoingShipmentsTotal }
							</th>
						</tr>
			    	</tfoot>
		    	</table>

			</g:else>
		</div>
	</div>	
</div>