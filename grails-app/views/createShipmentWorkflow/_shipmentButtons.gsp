
<g:if test="${shipmentWorkflow?.containerTypes }">
	<div class="button-group">
		<g:each var="containerType" in="${shipmentWorkflow?.containerTypes}">
			<span class="action-menu-item" id="add${format.metadata(obj:containerType)}ToShipment">
				<g:link class="button" action="createShipment" event="addContainer" params="[containerTypeToAddId:containerType.id]">
					<warehouse:message code="shipping.addAToThisShipment.label" args="[format.metadata(obj:containerType).toLowerCase()]"/>
				</g:link>
			</span>
		</g:each>	
	</div>
</g:if>
