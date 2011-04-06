<g:if test="${shipmentWorkflow?.containerTypes }">
	<g:each var="containerType" in="${shipmentWorkflow?.containerTypes}">
		<div class="action-menu-item">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" style="vertical-align: middle"/>&nbsp;
			<g:link action="createShipment" event="addContainer" params="[containerTypeToAddName:containerType.name]">Add a ${containerType.name.toLowerCase()}
			</g:link>
		</div>
	</g:each>	
</g:if>
<g:else>
	<div class="action-menu-item">														
		<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete item" style="vertical-align: middle"/>&nbsp;
		No Actions
	</div>
</g:else>							
		
	