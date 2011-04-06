<g:if test="${itemInstance }">
	<div class="action-menu-item">														
		<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit item" style="vertical-align: middle"/>&nbsp;
		<g:link action="createShipment" event="editItem" params="[itemToEditId:itemInstance?.id]">Edit Item</g:link>
	</div>
	<div class="action-menu-item">														
		<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete item" style="vertical-align: middle"/>&nbsp;
		<g:link action="createShipment" event="deleteItem" params="['item.id':itemInstance?.id]" onclick="return confirm('Are you sure you want to delete this item?')">Delete Item</g:link>	
	</div>
</g:if>						
<g:else>
	<div class="action-menu-item">														
		<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete item" style="vertical-align: middle"/>&nbsp;
		No Actions
	</div>
</g:else>							
		
