<div class="action-menu-item">
	<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}"
	alt="Show Shipment" style="vertical-align: middle" />&nbsp;
	<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">Show details</g:link>
</div>
<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id}">				
	<div class="action-menu-item">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}"
			alt="Edit shipment" style="vertical-align: middle" />&nbsp;
		<g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}">Edit shipment</g:link>
	</div>
</g:if>
<div class="action-menu-item">															
	<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" 
		alt="Edit Packing List" style="vertical-align: middle"/>&nbsp;
	<g:link controller="createShipmentWorkflow" action="createShipment" event="enterContainerDetails" id="${shipmentInstance?.id }" params="[skipTo:'Packing']">													
		Edit packing list
	</g:link>
</div>
<div class="action-menu-item">
	<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" 
		alt="View Packing List" style="vertical-align: middle"/>&nbsp;
	<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}" > 
		View packing list
	</g:link>		
</div>
<div class="action-menu-item">
	<fieldset>
		<legend>Documents</legend>
	
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
				Download letter
			</g:link>
		</div>
		<div class="action-menu-item">														
			<img src="${createLinkTo(dir:'images/icons/silk',file:'report_word.png')}" 
				alt="View Packing List" style="vertical-align: middle"/>&nbsp;	
			<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">													
				Download packing list
			</g:link>
		</div>

	
	</fieldset>
</div>
