<div class="action-menu-item">
	<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">
	<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}"
		alt="Show Shipment" style="vertical-align: middle" />&nbsp;<warehouse:message code="shipping.showDetails.label"/></g:link>
</div>

<g:isUserInRole roles="[org.pih.warehouse.core.RoleType.ROLE_ADMIN]">
	<g:if test="${shipmentInstance.hasShipped()}">
		<div class="action-menu-item">
			<g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" class="middle" />&nbsp;
				<g:if test="${request.request.requestURL.toString().contains('createShipment')}"><warehouse:message code="shipping.editShipment.label"/></g:if>
				<g:else><warehouse:message code="shipping.editShipment.label"/></g:else>
			</g:link>
		</div>
	</g:if>
</g:isUserInRole>
<g:if test="${!shipmentInstance.hasShipped() }">
	<!-- you can only edit a shipment or its packing list if you are at the origin warehouse, or if the origin is not a warehouse, and you are at the destination warehouse -->
	<g:if test="${(session?.warehouse?.id == shipmentInstance?.origin?.id) || (!shipmentInstance?.origin?.isWarehouse() && session?.warehouse?.id == shipmentInstance?.destination?.id)}">
		<div class="action-menu-item">
			<g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}"
				alt="Edit shipment" style="vertical-align: middle" />&nbsp;<warehouse:message code="shipping.editShipment.label"/></g:link>
		</div>
	</g:if>
	<g:if test="${(session?.warehouse?.id == shipmentInstance?.origin?.id) || (!shipmentInstance?.origin?.isWarehouse() && session?.warehouse?.id == shipmentInstance?.destination?.id)}">
		<div class="action-menu-item">
			<g:link controller="createShipmentWorkflow" action="createShipment" event="enterTrackingDetails" id="${shipmentInstance?.id }" params="[skipTo:'Tracking']">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'map.png')}"
					class="middle"/>&nbsp;<warehouse:message code="shipping.enterTrackingDetails.label"/>
			</g:link>
		</div>
	</g:if>
	<g:if test="${(session?.warehouse?.id == shipmentInstance?.origin?.id) || (!shipmentInstance?.origin?.isWarehouse() && session?.warehouse?.id == shipmentInstance?.destination?.id)}">
		<div class="action-menu-item">
			<g:link controller="createShipmentWorkflow" action="createShipment" event="enterContainerDetails" id="${shipmentInstance?.id }" params="[skipTo:'Packing']">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}"
					class="middle"/>&nbsp;<warehouse:message code="shipping.editPackingList.label"/>
			</g:link>
		</div>
	</g:if>

	<g:if test="${shipmentInstance?.origin?.id == session?.warehouse?.id || shipmentInstance?.destination?.id == session?.warehouse?.id }">
		<div class="action-menu-item">
			<g:if test="${shipmentInstance?.isSendAllowed()}">
				<g:link controller="createShipmentWorkflow" action="createShipment" event="sendShipment" id="${shipmentInstance.id}" params="[skipTo:'Sending']">
					<img src="${createLinkTo(dir:'images/icons',file:'truck.png')}" class="middle" />&nbsp;
					<warehouse:message code="shipping.sendShipment.label"/>
				</g:link>
			</g:if>
			<g:else>
				<g:set var="message" value="Shipment cannot be sent yet"/>
				<g:if test="${shipmentInstance?.hasShipped() }">
					<g:set var="message" value="Shipment has already been shipped!"/>
				</g:if>
				<g:elseif test="${shipmentInstance?.wasReceived() }">
					<g:set var="message" value="Shipment has already been received!"/>
				</g:elseif>

				<a href="javascript:void(0);" onclick="alert('${message}')">
					<img src="${createLinkTo(dir:'images/icons',file:'truck.png')}" class="middle" />&nbsp;
					<span class="fade">
						<warehouse:message code="shipping.sendShipment.label"/>
					</span>
				</a>
			</g:else>
		</div>
	</g:if>
</g:if>
<g:each in="${shipmentInstance.documents}" var="document" status="j">
	<div class="action-menu-item">
		<g:link controller="document" action="download" id="${document.id}">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Document" style="vertical-align: middle"/>
			<g:if test="${document?.filename}">
				<warehouse:message code="document.download.label"/> ${document?.filename } <span class="fade"><format:metadata obj="${document?.documentType}"/></span>
			</g:if>
			<g:else>
				<warehouse:message code="document.download.label"/> <format:metadata obj="${document?.documentType?.name}"/>
			</g:else>
		</g:link>
	</div>
</g:each>
<div class="action-menu-item">
	<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}"
			alt="View Packing List" style="vertical-align: middle"/>&nbsp;<warehouse:message code="shipping.downloadPackingList.label"/> (.xls)
	</g:link>
</div>
<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id ||
	(!(shipmentInstance?.origin?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.destination?.id)}">
	<div class="action-menu-item">
		<g:link controller="shipment" action="deleteShipment" id="${shipmentInstance.id}"><img
		src="${createLinkTo(dir:'images/icons',file:'trash.png')}"
		alt="Delete Shipment" style="vertical-align: middle" />&nbsp;
			<warehouse:message code="shipping.deleteShipment.label"/>
		</g:link>
	</div>
</g:if>

