<g:if test="${shipmentWorkflow?.containerTypes }">
	<g:each var="containerType" in="${shipmentWorkflow?.containerTypes}">
		<div class="action-menu-item">
			<g:link action="createShipment" event="addContainer" params="[containerTypeToAddId:containerType.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" style="vertical-align: middle"/>&nbsp;
				<warehouse:message code="shipping.addAToThisShipment.label" args="[format.metadata(obj:containerType).toLowerCase()]"/>
			</g:link>
		</div>
	</g:each>	
</g:if>
<g:else>
	<div class="action-menu-item">														
		<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete item" style="vertical-align: middle"/>&nbsp;
		<warehouse:message code="default.noActions.label"/>
	</div>
</g:else>							
		
	