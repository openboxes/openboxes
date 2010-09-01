<div id="shipmentMenu" class="menu" style="width: 250px">
	<fieldset>
		<legend>Edit Actions</legend>			
		<table>
			<tr>
				<td>
					<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}"><img
					src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}"
					alt="Show Shipment" style="vertical-align: middle" /> &nbsp; 						
						<g:if test="${request.request.requestURL.toString().contains('showDetails')}"><b>show details</b></g:if>
						<g:else>show details</g:else>
					</g:link>
					
				</td>
			</tr>
			
			<tr class="prop">
				<td>
					<g:link controller="shipment" action="editDetails" id="${shipmentInstance.id}"><img
					src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}"
					alt="Edit Shipment" style="vertical-align: middle" /> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('editDetails')}"><b>edit details</b></g:if>
						<g:else>edit details</g:else>
					</g:link>
				
				</td>
			</tr>
			<tr class="prop">
				<td>
					<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}"><img 
					src="${createLinkTo(dir:'images/icons',file:'pack-shipment.png')}" 
					alt="Add Document" style="vertical-align: middle"/> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('editContents')}"><b>edit contents</b></g:if>
						<g:else>edit contents</g:else>
					</g:link>
				</td>
			</tr>
			<tr class="prop">
				<td>
					<a href="${createLink(controller: "shipment", action: "addDocument", id: shipmentInstance.id)}"><img 
					src="${createLinkTo(dir:'images/icons/silk',file:'page_word.png')}" 
					alt="Add Document" style="vertical-align: middle"/> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('addDocument')}"><b>add document</b></g:if>
						<g:else>add document</g:else>
					</a>										
				
				</td>
			</tr>
			<tr class="prop">
				<td>
					<a href="${createLink(controller: "shipment", action: "addEvent", id: shipmentInstance.id)}"><img 
					src="${createLinkTo(dir:'images/icons/silk',file:'calendar.png')}" 
					alt="Add Document" style="vertical-align: middle"/> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('addDocument')}"><b>add event</b></g:if>
						<g:else>add event</g:else>
					</a>										
				
				</td>
			</tr>
			<tr class="prop">
				<td>
					<a href="${createLink(controller: "shipment", action: "addComment", id: shipmentInstance.id)}"><img 
					src="${createLinkTo(dir:'images/icons/silk',file:'comment.png')}" 
					alt="Add Document" style="vertical-align: middle"/> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('addComment')}"><b>add comment</b></g:if>
						<g:else>add comment</g:else>
					</a>				
				</td>
			</tr>
		</table>
	</fieldset>	
</div>
<br/>
<div style="width: 250px" class="menu" >
	
	<fieldset>
		<legend>Workflow Actions</legend>			
		<table>
			<tr >
				<td>
					<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}" ><img 
					src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" 
					alt="View Packing List" style="vertical-align: middle"/> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('showPackingList')}"><b>view packing list</b></g:if>
						<g:else>view packing list</g:else>
					</g:link>		
				</td>
			</tr>					
			<tr class="prop">
				<td>
					<g:link controller="shipment" action="markAsReady" id="${shipmentInstance.id}"><img 
					src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" 
					alt="Add Document" style="vertical-align: middle"/> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('markAsReady')}"><b>ready to ship</b></g:if>
						<g:else>ready to ship</g:else>
					</g:link>
				</td>
			</tr>		
			<tr class="prop">
				<td>
					<g:link controller="shipment" action="sendShipment" id="${shipmentInstance.id}"><img
					src="${createLinkTo(dir:'images/icons',file:'truck.png')}"
					alt="Send Shipment" style="vertical-align: middle" /> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('sendShipment')}"><b>send shipment</b></g:if>
						<g:else>send shipment</g:else>
					</g:link>				
				</td>
			</tr>
			<tr class="prop">
				<td>
					<g:link controller="shipment" action="receiveShipment" id="${shipmentInstance.id}"><img
					src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}"
					alt="Receive Shipment" style="vertical-align: middle" /> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('receiveShipment')}"><b>receive shipment</b></g:if>
						<g:else>receive shipment</g:else>
					</g:link>				
				</td>
			</tr>
		</table>
	</fieldset>
</div>
<br/>
<div style="width: 250px" class="menu" >
	<fieldset>
		<legend>Packages</legend>
		<table>
			<tr>
				<th>Package</th>
				<th>Items</th>
				<th>Weight (lbs)</th>
			</tr>
			<g:each in="${shipmentInstance.containers}" var="container" status="i">				
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>
						<span style="font-size: 0.8em; color: #aaa;">${container?.containerType?.name} ${container?.name}</span>
					</td>
					<td style="text-align: center;">
						<span style="font-size: 0.8em; color: #aaa;">${container.shipmentItems.size()}</span>
					</td>
					<td style="text-align: center;">
						<span style="font-size: 0.8em; color: #aaa;">${container.weight} ${container.weightUnits}</span>
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
<div style="width: 250px" class="menu" >
	<fieldset>
		<legend>Summary</legend>
		<table>
			<tr>
				<th>Date</th>
				<th>Description</th>
			</tr>
			<g:each in="${shipmentInstance.events}" var="event" status="i">				
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td nowrap="nowrap">						
						<span style="font-size: 0.8em; color: #aaa;"><g:formatDate format="MMM dd" date="${event.eventDate}"/></span>						
					</td>
					<td>
						<span style="font-size: 0.8em; color: #aaa;">
							${event?.eventType?.name} ${event.eventLocation.name}
						</span>
					</td>
				</tr>
			</g:each>
		</table>
	</fieldset>
</div>


