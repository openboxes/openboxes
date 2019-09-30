<!-- Only allow the originating warehouse to edit the shipment -->
	<span id="shipment-action-menu" class="action-menu" >
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		<div class="actions">
			<g:if test="${shipmentInstance?.origin?.id == session.warehouse.id}">
				<div class="action-menu-item">
					<g:link controller="shipment" action="list">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" style="vertical-align: middle" />&nbsp;
						<warehouse:message code="shipping.listOutgoing.label"/>
					</g:link>
				</div>
			</g:if>
			<g:if test="${shipmentInstance?.destination?.id == session.warehouse.id}">
				<div class="action-menu-item">
					<g:link controller="shipment" action="list" params="[type: 'incoming']">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" style="vertical-align: middle" />&nbsp;
						<warehouse:message code="shipping.listIncoming.label"/>
					</g:link>
				</div>
			</g:if>
			<div class="action-menu-item">
				<hr/>
			</div>
			<div class="action-menu-item">
				<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}"> 						
					<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="Show Details" style="vertical-align: middle" />&nbsp;
					<g:if test="${request.request.requestURL.toString().contains('showDetails')}"><warehouse:message code="shipping.showDetails.label"/></g:if>
					<g:else><warehouse:message code="shipping.showDetails.label"/></g:else>
				</g:link>
			</div>
			<!-- you can only edit a shipment or it's packing list if you are at the origin warehouse, or if the origin is not a warehouse, and you are at the destination warehouse -->
			<g:if test="${(session?.warehouse?.id == shipmentInstance?.origin?.id) || (!shipmentInstance?.origin?.isWarehouse() && session?.warehouse?.id == shipmentInstance?.destination?.id)}">				
				<div class="action-menu-item">
					<g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_edit.png')}" alt="Edit Shipment" style="vertical-align: middle" />&nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('createShipment')}"><warehouse:message code="shipping.editShipment.label"/></g:if>
						<g:else><warehouse:message code="shipping.editShipment.label"/></g:else>
					</g:link>
				</div>
				<div class="action-menu-item">
					<g:link controller="createShipmentWorkflow" action="createShipment" event="enterContainerDetails"  id="${shipmentInstance?.id }" params="[skipTo:'Packing']">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit Packing List" style="vertical-align: middle"/>&nbsp;
						<warehouse:message code="shipping.editPackingList.label"/></g:link>					
				</div>
			</g:if>
			<div class="action-menu-item">		
				<a href="${createLink(controller: "shipment", action: "addDocument", id: shipmentInstance.id)}">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page_add.png')}" alt="Upload a Document" style="vertical-align: middle"/>&nbsp;<warehouse:message code="shipping.uploadADocument.label"/></a>
			</div>
			<div class="action-menu-item">
				<a href="${createLink(controller: "shipment", action: "addComment", id: shipmentInstance.id)}">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'note_add.png')}" alt="Add Note" style="vertical-align: middle"/>&nbsp;<warehouse:message code="shipping.addNote.label"/></a>													
			</div>
			<div class="action-menu-item">
				<hr/>
			</div>
			
			<div class="action-menu-item">
				<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="View Packing List" style="vertical-align: middle"/>&nbsp;
					<g:if test="${request.request.requestURL.toString().contains('showPackingList')}"><warehouse:message code="shipping.viewPackingList.label"/></g:if>
					<g:else><warehouse:message code="shipping.viewPackingList.label"/></g:else>
				</g:link>		
			</div>
			<div class="action-menu-item">
				<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}" alt="View Packing List" style="vertical-align: middle"/>&nbsp;	
					<warehouse:message code="shipping.downloadPackingList.label"/> <span class="fade">(.xls)</span></g:link> 
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
								<g:if test="${request.request.requestURL.toString().contains('sendShipment')}"><warehouse:message code="shipping.sendShipment.label"/></g:if>
								<g:else><warehouse:message code="shipping.sendShipment.label"/></g:else>
						</g:link>				
					</g:if>
					<g:else>
						<img src="${createLinkTo(dir:'images/icons',file:'truck.png')}" alt="Send Shipment" style="vertical-align: middle" />&nbsp; 
						<span class="fade"><warehouse:message code="shipping.sendShipment.label"/></span>
					</g:else>
				</div>
			</g:if>

			<g:if test="${session?.warehouse?.id == shipmentInstance?.destination?.id ||
						(!(shipmentInstance?.destination?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.origin?.id)}">

				<div class="action-menu-item">
					<g:if test="${shipmentInstance.hasShipped() && !shipmentInstance.wasReceived()}">
						<g:link controller="shipment" action="receiveShipment" params="${ [shipmentId : shipmentInstance.id] }">
							<img src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}" alt="Receive Shipment" style="vertical-align: middle" />&nbsp;
							<g:if test="${request.request.requestURL.toString().contains('receiveShipment')}"><warehouse:message code="shipping.receiveShipment.label"/></g:if>
							<g:else><warehouse:message code="shipping.receiveShipment.label"/></g:else>
						</g:link>				
					</g:if>
					<g:else>
						<g:set var="message" value="Shipment cannot be received yet"/>
						<g:if test="${!shipmentInstance?.hasShipped() }">
							<g:set var="message" value="Shipment has not been shipped!"/>
						</g:if>
						<g:elseif test="${shipmentInstance?.wasReceived() }">
							<g:set var="message" value="Shipment was already received!"/>							
						</g:elseif>
						<a href="javascript:void(0);" onclick="alert('${message}')">
							<img src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}" alt="Receive Shipment" style="vertical-align: middle" />&nbsp;
							<span class="fade"><warehouse:message code="shipping.receiveShipment.label"/></span>
						</a>
					</g:else>
				</div>
			</g:if>
			<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id ||
				(!(shipmentInstance?.origin?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.destination?.id)}">	
				<div class="action-menu-item">		
					<g:link controller="shipment" action="deleteShipment" id="${shipmentInstance.id}"><img
					src="${createLinkTo(dir:'images/icons',file:'trash.png')}"
					alt="Delete Shipment" style="vertical-align: middle" />&nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('deleteShipment')}"><warehouse:message code="shipping.deleteShipment.label"/></g:if>
						<g:else><warehouse:message code="shipping.deleteShipment.label"/></g:else>
					</g:link>				
				</div>
			</g:if>
		</div>
	</span>
	