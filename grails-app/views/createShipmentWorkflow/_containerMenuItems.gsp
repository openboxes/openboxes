<g:if test="${selectedContainer }">
	<div class="action-menu-item">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit container" style="vertical-align: middle"/>&nbsp;
		<g:link action="createShipment" event="editContainer" params="[containerToEditId:selectedContainer?.id]">
			Edit ${selectedContainer?.containerType?.name }
		</g:link> 	
	</div>
	<div class="action-menu-item">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add an item" style="vertical-align: middle"/>&nbsp;
		<g:link action="createShipment" event="addItemToContainer" params="['container.id':selectedContainer?.id]">
		 Add an item
		</g:link> 													
	</div>
	<div class="action-menu-item">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" alt="Add a box" style="vertical-align: middle"/>&nbsp;
		<g:link action="createShipment" event="addBoxToContainer" params="['container.id':selectedContainer?.id]">
			Add a box
		</g:link>
	</div>
	<div class="action-menu-item">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete container" style="vertical-align: middle"/>&nbsp;
		<g:link action="createShipment" event="deleteContainer" params="['container.id':selectedContainer?.id]" onclick="return confirm('Are you sure you want to delete this ${selectedContainer?.containerType?.name }?')">
			Delete ${selectedContainer?.containerType?.name }
		</g:link> 	
	</div>
</g:if>				
<g:else>
	<div class="action-menu-item">														
		<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete item" style="vertical-align: middle"/>&nbsp;
		No Actions
	</div>
</g:else>							
		
