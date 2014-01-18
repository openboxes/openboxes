<div class="button-group">
	<g:if test="${container }">
		<g:link class="button icon add" action="createShipment" event="addItemToContainer" params="['container.id':container?.id]">
			<warehouse:message code="shipping.addItemToThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 													
		<%-- Only allow one level deep --%> 
		<g:if test="${!container?.parentContainer }">
			<g:link class="button icon add" action="createShipment" event="addBoxToContainer" params="['container.id':container?.id]">
				<warehouse:message code="shipping.addBoxToThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
			</g:link>
		</g:if>
		<g:link class="button icon edit" action="createShipment" event="editContainer" params="[containerToEditId:container?.id]">
			<warehouse:message code="shipping.editThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 	
		<g:link class="button icon move" action="createShipment" event="moveContainer" params="[containerToMoveId:container?.id]">
			<warehouse:message code="shipping.moveThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 	
		<g:link class="button icon trash" action="createShipment" event="deleteContainer" params="['container.id':container?.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteThis.message')} ${format.metadata(obj:selectedContainer?.containerType).toLowerCase()}?')">
			<warehouse:message code="shipping.deleteThis.label"/> ${format.metadata(obj:container?.containerType).toLowerCase()  }
		</g:link> 
	</g:if>				
	<g:else>
		<g:link class="button icon add" action="createShipment" event="addItemToShipment">
			<warehouse:message code="shipping.addAnItemToUnpackedItems.label"/>
		</g:link> 													
	</g:else>							
</div>
