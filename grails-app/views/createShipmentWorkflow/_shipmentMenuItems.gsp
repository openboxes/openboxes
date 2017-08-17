<g:if test="${shipmentWorkflow?.containerTypes }">
	<g:each var="containerType" in="${shipmentWorkflow?.containerTypes}">
		<div class="action-menu-item" id="add${format.metadata(obj:containerType)}ToShipment">
			<g:link action="createShipment" event="addContainer" params="[containerTypeToAddId:containerType.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" style="vertical-align: middle"/>&nbsp;
				<warehouse:message code="shipping.addAToThisShipment.label" args="[format.metadata(obj:containerType).toLowerCase()]"/>
			</g:link>
		</div>
	</g:each>	
</g:if>
<g:else>
	<div class="action-menu-item">														
		<a href="javascript:void(0);">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}" alt="" style="vertical-align: middle"/>&nbsp;
			<warehouse:message code="shipping.noActions.label" args="${[format.metadata(obj:shipmentInstance?.shipmentType)]}"/>
		</a>
	</div>
</g:else>							

