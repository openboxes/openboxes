<!-- Only allow the originating warehouse to edit the shipment -->

	<div id="shipmentMenu" class="menu" style="width: 250px">
		<fieldset>
			
			<table>
				<thead>
					<tr>
						<th>Actions</th>
					</tr>
				</thead>
				<tbody>	
					<tr>
						<td>
							<g:link controller="shipment" action="listShipping"> 						
								<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_left.png')}" 
									alt="Show Shipment" style="vertical-align: middle" /> &nbsp;
								List Shipments
							</g:link>
						</td>
					</tr>					
					<tr class="prop">
						<td>
							<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}"> 						
								<g:if test="${request.request.requestURL.toString().contains('showDetails')}">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" 
										alt="Show Shipment" style="vertical-align: middle" /> &nbsp;
									<b>Show details</b>
								</g:if>
								<g:else>
									<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_left.png')}" 
											alt="Show Shipment" style="vertical-align: middle" /> &nbsp;
									Back to details
								</g:else>
							</g:link>
						</td>
					</tr>	
					<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id}">				
						<tr class="prop">
							<td>
								<g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}"><img
								src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}"
								alt="Edit Suitcase" style="vertical-align: middle" /> &nbsp; 
									<g:if test="${request.request.requestURL.toString().contains('createShipment')}"><b>Edit shipment</b></g:if>
									<g:else>Edit shipment</g:else>
								</g:link>
							</td>
						</tr>
					</g:if>
					<tr class="prop">
						<td>
							<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}" ><img 
							src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" 
							alt="View Packing List" style="vertical-align: middle"/> &nbsp; 
								<g:if test="${request.request.requestURL.toString().contains('showPackingList')}"><b>View packing list</b></g:if>
								<g:else>View packing list</g:else>
							</g:link>		
						</td>
					</tr>					
					<tr class="prop">
						<td>
							<img src="${createLinkTo(dir:'images/icons/silk',file:'report_word.png')}" 
								alt="View Packing List" style="vertical-align: middle"/>&nbsp;	
							<g:link controller="doc4j" action="downloadLetter" id="${shipmentInstance?.id }">													
								Download letter
							</g:link>
						</td>
					</tr>
					<tr class="prop">
						<td>
							<img src="${createLinkTo(dir:'images/icons/silk',file:'report.png')}" 
								alt="View Packing List" style="vertical-align: middle"/>&nbsp;	
							<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">													
								Download packing list
							</g:link>
						</td>
					</tr>
		
					<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id || 
									(!(shipmentInstance?.origin?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.destination?.id)}">
						<tr class="prop">
							<td>
								<g:if test="${!shipmentInstance?.hasShipped()}">
									<g:link controller="shipment" action="sendShipment" id="${shipmentInstance.id}">
										<img src="${createLinkTo(dir:'images/icons',file:'truck.png')}"
										alt="Send Shipment" style="vertical-align: middle" /> &nbsp; 
											<g:if test="${request.request.requestURL.toString().contains('sendShipment')}"><b>Send shipment</b></g:if>
											<g:else>Send shipment</g:else>
									</g:link>				
								</g:if>
								<g:else>
									<img src="${createLinkTo(dir:'images/icons',file:'truck.png')}"
										alt="Send Shipment" style="vertical-align: middle" /> &nbsp; 
									<span class="fade">Send shipment</span>
								</g:else>
							</td>
						</tr>
					</g:if>

					<g:if test="${session?.warehouse?.id == shipmentInstance?.destination?.id ||
								(!(shipmentInstance?.destination?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.origin?.id)}">
						<tr class="prop">
							<td>
							
								<g:if test="${shipmentInstance.hasShipped() && !shipmentInstance.wasReceived()}">
									<g:link controller="shipment" action="receiveShipment" params="${ [shipmentId : shipmentInstance.id] }">
									<img src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}"
									alt="Receive Shipment" style="vertical-align: middle" /> &nbsp; 
										<g:if test="${request.request.requestURL.toString().contains('receiveShipment')}"><b>Receive shipment</b></g:if>
										<g:else>Receive shipment</g:else>
									</g:link>				
								</g:if>
								<g:else>
									<img src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}"
									alt="Receive Shipment" style="vertical-align: middle" /> &nbsp; 
									<span class="fade">Receive shipment</span>
								
								</g:else>
							</td>
						</tr>
					</g:if>
					<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id ||
						(!(shipmentInstance?.origin?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.destination?.id)}">	
						<tr class="prop">
							<td>
								<g:link controller="shipment" action="deleteShipment" id="${shipmentInstance.id}"><img
								src="${createLinkTo(dir:'images/icons',file:'trash.png')}"
								alt="Delete Shipment" style="vertical-align: middle" /> &nbsp; 
									<g:if test="${request.request.requestURL.toString().contains('deleteShipment')}"><b>Delete shipment</b></g:if>
									<g:else>Delete shipment</g:else>
								</g:link>				
							</td>
						</tr>
					</g:if>
				</tbody>
			</table>
		</fieldset>
	</div>

<%--  
<br/>
<div style="width: 250px" class="menu" >
	<fieldset>
		<table>
			<tr>
				<th>Package</th>
				<th>Items</th>
				<th>Weight (lbs)</th>
			</tr>
			<g:if test="${!shipmentInstance.containers }">
				<tr class="odd">
					<td colspan="3">
						<span class="fade">No contents</span>
					</td>
				</tr>			
			</g:if>
			<g:each in="${shipmentInstance.containers}" var="container" status="i">				
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>
						<span style="font-size: 0.8em; color: #aaa;">${container?.containerType?.name} ${container?.name}</span>
					</td>
					<td style="text-align: center;">
						<span style="font-size: 0.8em; color: #aaa;">${container?.shipmentItems?.size()}</span>
					</td>
					<td style="text-align: center;">
						<span style="font-size: 0.8em; color: #aaa;">${container?.weight} ${container?.weightUnits}</span>
					</td>
				</tr>
			</g:each>
			<tr>
				<th>

				</th>
				<th>
				
				</th>
				<th>
					
				</th>			
			</tr>
			
		</table>
	</fieldset>
</div>
<br/>
--%>


<%--
<div style="width: 250px" class="menu" >
	<fieldset>
		<table>
			<tr>
				<th>Date</th>
				<th>Description</th>
			</tr>
			<g:if test="${!shipmentInstance.events }">
				<tr class="odd">
					<td colspan="2">
						<span class="fade">No events</span>
					</td>
				</tr>			
			</g:if>
			<g:each in="${shipmentInstance.events}" var="event" status="i">				
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td nowrap="nowrap">						
						<span style="font-size: 0.9em; color: #aaa;"><g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_HOUR_MONTH_DATE_FORMAT}" date="${event.eventDate}"/></span>						
					</td>
					<td>
						<span style="font-size: 0.9em; color: #aaa;">
							<b>${event?.eventType?.eventCode?.name}</b> at ${event?.eventLocation?.name}
						</span>
					</td>
				</tr>
			</g:each>
		</table>
	</fieldset>
</div>
--%>
