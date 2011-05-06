<!-- Only allow the originating warehouse to edit the shipment -->
	<span id="shipment-action-menu" class="action-menu" >
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		<div class="actions">
			<div class="action-menu-item">
				<g:link controller="shipment" action="listShipping">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_left.png')}" alt="Show Shipment" style="vertical-align: middle" />&nbsp;List Shipments
				</g:link>
			</div>
			<div class="action-menu-item">
				<hr/>
			</div>
			<div class="action-menu-item">
				<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}"> 						
					<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="Show Details" style="vertical-align: middle" />&nbsp;
					<g:if test="${request.request.requestURL.toString().contains('showDetails')}">Show Details</g:if>
					<g:else>Show Details</g:else>
				</g:link>
			</div>
			<!-- you can only edit a shipment or it's packing list if you are at the origin warehouse, or if the origin is not a warehouse, and you are at the destination warehouse -->
			<g:if test="${(session?.warehouse?.id == shipmentInstance?.origin?.id) || (!shipmentInstance?.origin?.isWarehouse() && session?.warehouse?.id == shipmentInstance?.destination?.id)}">				
				<div class="action-menu-item">
					<g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_edit.png')}" alt="Edit Shipment" style="vertical-align: middle" />&nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('createShipment')}">Edit Shipment</g:if>
						<g:else>Edit Shipment</g:else>
					</g:link>
				</div>
				<div class="action-menu-item">
					<g:link controller="createShipmentWorkflow" action="createShipment" event="enterContainerDetails"  id="${shipmentInstance?.id }" params="[skipTo:'Packing']">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit Packing List" style="vertical-align: middle"/>&nbsp;
						Edit Packing List</g:link>					
				</div>
			</g:if>
			<div class="action-menu-item">		
				<a href="${createLink(controller: "shipment", action: "addDocument", id: shipmentInstance.id)}">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page_add.png')}" alt="Upload a Document" style="vertical-align: middle"/>&nbsp;Upload a Document</a>
			</div>
			<div class="action-menu-item">
				<a href="${createLink(controller: "shipment", action: "addComment", id: shipmentInstance.id)}">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'note_add.png')}" alt="Add Note" style="vertical-align: middle"/>&nbsp;Add a Note</a>													
			</div>
			<div class="action-menu-item">
				<hr/>
			</div>
			
			<div class="action-menu-item">
				<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="View Packing List" style="vertical-align: middle"/>&nbsp;
					<g:if test="${request.request.requestURL.toString().contains('showPackingList')}">View Packing List</g:if>
					<g:else>View Packing List</g:else>
				</g:link>		
			</div>
			<div class="action-menu-item">
				<g:link controller="doc4j" action="downloadLetter" id="${shipmentInstance?.id }">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_word.png')}" alt="View Packing List" style="vertical-align: middle"/>&nbsp;
					Download Letter <span class="fade">(.docx)</span></g:link> 
			</div>
			<div class="action-menu-item">
				<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}" alt="View Packing List" style="vertical-align: middle"/>&nbsp;	
					Download Packing List <span class="fade">(.xls)</span></g:link> 
			</div>
			<div class="action-menu-item">
				<hr/>
			</div>
		
			<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id || 
							(!(shipmentInstance?.origin?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.destination?.id)}">
				<div class="action-menu-item">		
					<g:if test="${!shipmentInstance?.hasShipped()}">
						<g:link controller="shipment" action="sendShipment" id="${shipmentInstance.id}">
							<img src="${createLinkTo(dir:'images/icons',file:'truck.png')}" alt="Send Shipment" style="vertical-align: middle" />&nbsp; 
								<g:if test="${request.request.requestURL.toString().contains('sendShipment')}">Send Shipment</g:if>
								<g:else>Send Shipment</g:else>
						</g:link>				
					</g:if>
					<g:else>
						<img src="${createLinkTo(dir:'images/icons',file:'truck.png')}" alt="Send Shipment" style="vertical-align: middle" />&nbsp; 
						<span class="fade">Send Shipment</span>
					</g:else>
				</div>
			</g:if>

			<g:if test="${session?.warehouse?.id == shipmentInstance?.destination?.id ||
						(!(shipmentInstance?.destination?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.origin?.id)}">

				<div class="action-menu-item">
					<g:if test="${shipmentInstance.hasShipped() && !shipmentInstance.wasReceived()}">
						<g:link controller="shipment" action="receiveShipment" params="${ [shipmentId : shipmentInstance.id] }">
							<img src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}" alt="Receive Shipment" style="vertical-align: middle" />&nbsp;
							<g:if test="${request.request.requestURL.toString().contains('receiveShipment')}">Receive Shipment</g:if>
							<g:else>Receive Shipment</g:else>
						</g:link>				
					</g:if>
					<g:else>
						<img src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}" alt="Receive Shipment" style="vertical-align: middle" />&nbsp;
						<span class="fade">Receive Shipment</span>
					</g:else>
				</div>
			</g:if>
			<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id ||
				(!(shipmentInstance?.origin?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.destination?.id)}">	
				<div class="action-menu-item">		
					<g:link controller="shipment" action="deleteShipment" id="${shipmentInstance.id}"><img
					src="${createLinkTo(dir:'images/icons',file:'trash.png')}"
					alt="Delete Shipment" style="vertical-align: middle" />&nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('deleteShipment')}">Delete Shipment</g:if>
						<g:else>Delete Shipment</g:else>
					</g:link>				
				</div>
			</g:if>
		</div>
	</span>
		
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
						<span style="font-size: 0.9em; color: #aaa;"><g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}" date="${event.eventDate}"/></span>						
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
