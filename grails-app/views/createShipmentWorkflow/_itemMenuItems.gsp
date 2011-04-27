<g:if test="${itemInstance }">
	<div class="action-menu-item">														
		<g:link action="createShipment" event="editItem" params="[itemToEditId:itemInstance?.id]">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit item" style="vertical-align: middle"/>&nbsp;
			Edit Item
		</g:link>
	</div>
	<div class="action-menu-item">														
		<g:link action="createShipment" event="moveItem" params="[itemToMoveId:itemInstance?.id]">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_out.png')}" alt="Move Item" style="vertical-align: middle"/>&nbsp;
			Move Item
		</g:link>
	</div>
	<div class="action-menu-item">														
		<g:link action="createShipment" event="deleteItem" params="['item.id':itemInstance?.id]" onclick="return confirm('Are you sure you want to delete this item?')">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete item" style="vertical-align: middle"/>&nbsp;
			Delete Item
		</g:link>	
	</div>
</g:if>						
<g:else>
	<div class="action-menu-item">														
		<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="No Actions" style="vertical-align: middle"/>&nbsp;
		No Actions
	</div>
</g:else>							
		
