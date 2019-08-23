<%-- Same hack employed for refreshing shipmentInstance in enterContainerDetails. Need to figure out
how to prevent the lazy initialization errors so we don't need to resort to refreshing objects in the GSP. --%>
<g:if test="${shipmentWorkflow?.containerTypes }">
	<div class="button-group">
		<g:each var="containerType" in="${shipmentWorkflow?.containerTypes}">
			<g:link class="button icon add" action="createShipment" event="addContainer" params="[containerTypeToAddId:containerType.id]">
				<warehouse:message code="shipping.addAToThisShipment.label" args="[format.metadata(obj:containerType).toLowerCase()]"/>
			</g:link>
		</g:each>
	</div>
</g:if>
