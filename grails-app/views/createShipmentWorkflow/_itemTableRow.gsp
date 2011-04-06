<tr class="" style="border: 1px solid lightgrey;">
	<td>
		<span style="padding-left: 32px;">
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
	<td style="text-align: left;">		
		<g:link action="createShipment" event="deleteItem" params="['item.id':itemInstance?.id]" onclick="return confirm('Are you sure you want to delete this item?')">	
		<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="remove item" style="vertical-align: middle"/>
		</g:link>	
	</td>
	<td>
	
	</td>
</tr>