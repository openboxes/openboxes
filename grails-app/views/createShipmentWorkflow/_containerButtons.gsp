<g:if test="${container }">
	<span class="action-menu-item">
		<g:link class="button icon add" action="createShipment" event="addItemToContainer" params="['container.id':container?.id]">		
			<warehouse:message code="shipping.addItemToThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 													
	</span>
	<%-- Only allow one level deep --%> 
	<g:if test="${!container?.parentContainer }">
		<span class="action-menu-item"> 
			<g:link class="button icon add" action="createShipment" event="addBoxToContainer" params="['container.id':container?.id]">
				<warehouse:message code="shipping.addBoxToThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
			</g:link>
		</span>
	</g:if>
	<span class="action-menu-item"> 
		<g:link class="button icon edit" action="createShipment" event="editContainer" params="[containerToEditId:container?.id]">
			<warehouse:message code="shipping.editThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 	
	</span>
	<span class="action-menu-item"> 
		<g:link class="button icon move" action="createShipment" event="moveContainer" params="[containerToMoveId:container?.id]">
			<warehouse:message code="shipping.moveThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 	
	</span>
	<span class="action-menu-item"> 
		<g:link class="button icon trash" action="createShipment" event="deleteContainer" params="['container.id':container?.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteThis.message')} ${format.metadata(obj:selectedContainer?.containerType).toLowerCase()}?')">
			<warehouse:message code="shipping.deleteThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 
	</span>	
</g:if>				
<g:else>
	<span id="addItemToUnpackedItems" class="action-menu-item">
		<g:link class="button icon add" action="createShipment" event="addItemToShipment">
			<warehouse:message code="shipping.addAnItemToUnpackedItems.label"/>
		</g:link> 													
	</span>
</g:else>							
		
