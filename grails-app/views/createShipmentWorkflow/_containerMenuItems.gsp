<g:if test="${container }">
	<div class="action-menu-item">
		<g:link action="createShipment" event="addItemToContainer" params="['container.id':container?.id]">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add an item" style="vertical-align: middle"/>&nbsp;
			<warehouse:message code="shipping.addItemToThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 													
	</div>

	<%-- Only allow one level deep --%> 
	<g:if test="${!container?.parentContainer }">
		<div class="action-menu-item">
			<g:link action="createShipment" event="addBoxToContainer" params="['container.id':container?.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" alt="Add a box" style="vertical-align: middle"/>&nbsp;
				<warehouse:message code="shipping.addBoxToThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
			</g:link>
		</div>
	</g:if>
	<div class="action-menu-item">
		<g:link action="createShipment" event="editContainer" params="[containerToEditId:container?.id]">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit this container" style="vertical-align: middle"/>&nbsp;
			<warehouse:message code="shipping.editThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 	
	</div>
	<div class="action-menu-item">
		<g:link action="createShipment" event="moveContainer" params="[containerToMoveId:container?.id]">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'package_go.png')}" alt="Move this container" style="vertical-align: middle"/>&nbsp;
			<warehouse:message code="shipping.moveThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 	
	</div>
	<div class="action-menu-item">
		<g:link action="createShipment" event="deleteContainer" params="['container.id':container?.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteThis.message')} ${format.metadata(obj:selectedContainer?.containerType).toLowerCase()}?')">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete container" style="vertical-align: middle"/>&nbsp;
			<warehouse:message code="shipping.deleteThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 	
	</div>
</g:if>				
<g:else>
	<div class="action-menu-item"  id="addItemToUnpackedItems">
		<g:link action="createShipment" event="addItemToShipment">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add an item" style="vertical-align: middle"/>&nbsp;
			<warehouse:message code="shipping.addAnItemToUnpackedItems.label"/>
		</g:link> 													
	</div>
</g:else>							
		
