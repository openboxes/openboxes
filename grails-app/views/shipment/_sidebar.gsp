<div id="shipmentMenu" class="menu" style="width: 200px">
	<fieldset>
		<legend>Actions</legend>			
		<h3>Edit Shipment</h3>
		<table>
			<tr class="prop">
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
					<a href="${createLink(controller: "shipment", action: "addDocument", id: shipmentInstance.id)}"><img 
					src="${createLinkTo(dir:'images/icons/silk',file:'page_word.png')}" 
					alt="Add Document" style="vertical-align: middle"/> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('addDocument')}"><b>attach document</b></g:if>
						<g:else>attach document</g:else>
					</a>										
				
				</td>
			</tr>
			<tr class="prop">
				<td>
					<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}" ><img 
					src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" 
					alt="View Packing List" style="vertical-align: middle"/> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('showPackingList')}"><b>view packing list</b></g:if>
						<g:else>view packing list</g:else>
					</g:link>		
				</td>
			</tr>					
		</table>
		<br/><br/>
		<h3>Pack Shipment</h3>
		<table>
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
					<g:link controller="shipment" action="markAsReady" id="${shipmentInstance.id}"><img 
					src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" 
					alt="Add Document" style="vertical-align: middle"/> &nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('markAsReady')}"><b>ready to ship</b></g:if>
						<g:else>ready to ship</g:else>
					</g:link>
				</td>
			</tr>		
		</table>							
		<br/><br/>
		<h3>Send Shipment</h3>
		<table>
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
		</table>							
		<br/><br/>
		<h3>Receive Shipment</h3>
		<table>
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
<div style="width: 200px">
	<fieldset>
		<legend>Tracking</legend>
		<table>
			<g:each in="${shipmentInstance.events}" var="event" status="i">
				
				<tr class="">
					<td>
						<g:if test="${i == 0}"><b>${event?.eventType?.name}</b></g:if>
						<g:else><span style="font-size: 0.8em; color: #aaa;">${event?.eventType?.name}</span></g:else>
					</td>
					<td>
						
						<g:if test="${i == 0}">
							<b>
								${event.eventLocation.name}
								&nbsp; <g:formatDate format="dd/MM/yy" date="${event.eventDate}"/>
								&nbsp; <g:formatDate type="time" date="${event.eventDate}"/>
							</b>						
						</g:if>
						<g:else>
							<span style="font-size: 0.8em; color: #aaa;">
								${event.eventLocation.name}
								&nbsp; <g:formatDate format="dd/MM/yy" date="${event.eventDate}"/> 
								&nbsp; <g:formatDate type="time" date="${event.eventDate}"/>
								
							</span>
						</g:else>
					</td>
				</tr>
			</g:each>
		</table>
	</fieldset>
</div>


