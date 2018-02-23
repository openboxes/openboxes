<g:if test="${itemInstance }">
	<div class="action-menu-item">														
		<g:link action="createShipment" event="editItem" params="[itemToEditId:itemInstance?.id]">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit item" style="vertical-align: middle"/>&nbsp;
			<g:message code="shipping.editItem.label"/>
		</g:link>
	</div>
	<div class="action-menu-item">														
		<g:link action="createShipment" event="moveItem" params="[itemToMoveId:itemInstance?.id]">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'cut.png')}" alt="Split Item" style="vertical-align: middle"/>&nbsp;
			<g:message code="shipping.splitItem.label" default="Split Item"/>
		</g:link>
	</div>
	<div class="action-menu-item">														
		<g:link action="createShipment" event="deleteItem" params="['item.id':itemInstance?.id]" onclick="return confirm('Are you sure you want to delete this item?')">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete item" style="vertical-align: middle"/>&nbsp;
			<g:message code="shipping.removeItem.label"/>
		</g:link>	
	</div>
</g:if>
<g:else>
	<div class="action-menu-item">														
		<img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}" alt="No Actions" style="vertical-align: middle"/>&nbsp;
		<g:message code="default.noActions.label"/>
	</div>
</g:else>							
		
