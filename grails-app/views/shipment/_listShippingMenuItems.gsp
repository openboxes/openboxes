<div class="action-menu-item">
	<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}"
	alt="Show Shipment" style="vertical-align: middle" />&nbsp;
	<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">Show Details</g:link>
</div>
<!-- you can only edit a shipment or it's packing list if you are at the origin warehouse, or if the origin is not a warehouse, and you are at the destination warehouse -->
<g:if test="${(session?.warehouse?.id == shipmentInstance?.origin?.id) || (!shipmentInstance?.origin?.isWarehouse() && session?.warehouse?.id == shipmentInstance?.destination?.id)}">				
	<div class="action-menu-item">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}"
			alt="Edit shipment" style="vertical-align: middle" />&nbsp;
		<g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}">Edit Shipment</g:link>
	</div>
	<div class="action-menu-item">															
		<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" 
			alt="Edit Packing List" style="vertical-align: middle"/>&nbsp;
		<g:link controller="createShipmentWorkflow" action="createShipment" event="enterContainerDetails" id="${shipmentInstance?.id }" params="[skipTo:'Packing']">													
			Edit Packing List
		</g:link>
	</div>
</g:if>
<div class="action-menu-item">
	<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" 
		alt="View Packing List" style="vertical-align: middle"/>&nbsp;
	<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}" > 
		View Packing List
	</g:link>		
</div>
<div class="action-menu-item">
	<h2>Documents</h2>
	<g:each in="${shipmentInstance.documents}" var="document" status="j">
		<div class="action-menu-item">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Document" style="vertical-align: middle"/>
			<g:link controller="document" action="download" id="${document.id}">
				<g:if test="${document?.filename}">
					Download ${document?.filename } <span class="fade">${document?.documentType?.name}</span>
				</g:if>
				<g:else>
					Download ${document?.documentType?.name}  
				</g:else>
			</g:link>
		</div>
	</g:each>							
	<div class="action-menu-item">														
		<img src="${createLinkTo(dir:'images/icons/silk',file:'report_word.png')}" 
			alt="View Packing List" style="vertical-align: middle"/>&nbsp;	
		<g:link controller="doc4j" action="downloadLetter" id="${shipmentInstance?.id }">													
			Download Letter
		</g:link> (.docx)
	</div>
	<div class="action-menu-item">														
		<img src="${createLinkTo(dir:'images/icons/silk',file:'report.png')}" 
			alt="View Packing List" style="vertical-align: middle"/>&nbsp;	
		<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">													
			Download Packing List
		</g:link> (.xls)
	</div>

</div>
