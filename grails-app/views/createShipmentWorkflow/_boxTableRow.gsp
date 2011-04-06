<tr class="" style="border: 1px solid lightgrey;">
	<td>
		<span style="padding-left: 32px;">
		<g:link action="createShipment" event="editBox" params="[boxToEditId:boxInstance?.id]">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="Package" style="vertical-align: middle"/>
			&nbsp;${boxInstance?.name}
		</g:link>
		</span>
	</td>
	<td></td>
	<td></td>
	<td></td>
	<td style="text-align: left;" nowrap="nowrap">
		<g:link action="createShipment" event="addItemToContainer" params="['container.id':boxInstance.id]">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add an item" style="vertical-align: middle"/>
		</g:link> 		
	</td>
	<td nowrap="nowrap">
		<g:link action="createShipment" event="deleteBox" params="['box.id':boxInstance?.id]" onclick="return confirm('Are you sure you want to delete this box?')">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'package_delete.png')}" alt="Add an item" style="vertical-align: middle"/>
		</g:link>				
	</td>
</tr>
<%-- Display each item in this box --%>
<g:each var="itemInstance" in="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == boxInstance?.id})?.sort()}">	
	<tr class="">
		<td>
			<span style="padding-left: 64px;">
				<g:link action="createShipment" event="editItem" params="[itemToEditId:itemInstance?.id]">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Item" style="vertical-align: middle"/>
					&nbsp;${itemInstance?.product?.name }
				</g:link>																	
			</span>
		</td>
		<td style="text-align:center;">
			${itemInstance?.quantity}
		</td>
		<td style="text-align:center;">
			${itemInstance?.lotNumber}
		</td>
		<td style="text-align:left;">
			${itemInstance?.recipient?.name}
		</td>
		<td style="text-align: left;" nowrap="true">		
			<g:link action="createShipment" event="deleteItem" params="['item.id':itemInstance?.id]" onclick="return confirm('Are you sure you want to delete this item?')">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="remove item" style="vertical-align: middle"/>
			</g:link>	
		</td>
		<td>
		
		</td>
	</tr>
</g:each>												
