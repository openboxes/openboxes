<div class="action-menu-item">
	<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">
	<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}"
		alt="Show Shipment" style="vertical-align: middle" />&nbsp;<warehouse:message code="shipping.showDetails.label"/></g:link>
</div>
<!-- you can only edit a shipment or its packing list if you are at the origin warehouse, or if the origin is not a warehouse, and you are at the destination warehouse -->
<g:if test="${(session?.warehouse?.id == shipmentInstance?.origin?.id) || (!shipmentInstance?.origin?.isWarehouse() && session?.warehouse?.id == shipmentInstance?.destination?.id)}">				
	<div class="action-menu-item">
		<g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}"
			alt="Edit shipment" style="vertical-align: middle" />&nbsp;<warehouse:message code="shipping.editShipment.label"/></g:link>
	</div>
	<div class="action-menu-item">															
		<g:link controller="createShipmentWorkflow" action="createShipment" event="enterContainerDetails" id="${shipmentInstance?.id }" params="[skipTo:'Packing']">													
			<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" 
				alt="Edit Packing List" style="vertical-align: middle"/>&nbsp;<warehouse:message code="shipping.editPackingList.label"/>
		</g:link>
	</div>
</g:if>

<g:if test="${shipmentInstance?.hasShipped() && !shipmentInstance.wasReceived() }">
	<div class="action-menu-item">															
		<g:link controller="shipment" action="markAsReceived" id="${shipmentInstance?.id }">													
			<img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" 
				alt="Mark as received" style="vertical-align: middle"/>&nbsp;<warehouse:message code="shipping.markAsReceived.label"/>
		</g:link>
	</div>
</g:if>
<%-- 
<div class="action-menu-item">
	<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}" > 
		<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" 
			alt="View Packing List" style="vertical-align: middle"/>&nbsp;<warehouse:message code="shipping.viewPackingList.label"/>
	</g:link>		
</div>
--%>
<div class="action-menu-item">
	<hr/>
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
<div class="action-menu-item">
	<hr/>
</div>
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
	<g:link controller="doc4j" action="downloadLetter" id="${shipmentInstance?.id }">													
		<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_word.png')}"
			alt="Download Certificate of Donation" style="vertical-align: middle"/>&nbsp;<warehouse:message code="shipping.downloadCertificateOfDonation.label"/> (.docx)
	</g:link> 
</div>
<div class="action-menu-item">														
	<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">													
		<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}"
			alt="View Packing List" style="vertical-align: middle"/>&nbsp;<warehouse:message code="shipping.downloadPackingList.label"/> (.xls)
	</g:link> 
</div>

