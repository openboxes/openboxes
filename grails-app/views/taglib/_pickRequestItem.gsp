<g:if test="${attrs.products }">
	<table>
		<g:each var="inventoryItem" in="${attrs.inventoryItems }">
			<tr>
				<td>
					<g:textField class="text" name="picklistItems[${attrs.status }].quantity" value="${attrs.requestItem?.quantity }" size="5"/>				
				</td>
				<td>${inventoryItem.product.name }</td>	
				<td>${inventoryItem.lotNumber }</td>			
				<td>${inventoryItem.expirationDate}</td>			
			</tr>
		</g:each>	
	</table>
	
</g:if>
<g:else>
<td>
	<div>
		<label>${attrs.product }</label>
	</div>
	<g:hiddenField name="requestItem.id" value="${attrs?.requestItem?.id }"/>
	<g:textField class="text" name="quantity" 
		value="${attrs.requestItem?.quantity }" size="5"/>

	<g:select name="inventoryItem.id" 
		from="${attrs.inventoryItems }" 
		optionKey="id" 
		optionValue="${{'LOT ' + it.lotNumber.toUpperCase() + (!it?.expirationDate?' | Never expires':it?.expirationDate?.before(new Date())?' | EXPIRED: '+it.expirationDate:' | Expires: '+it.expirationDate) + ' | QoH: ' + it.quantityOnHand + ' | ATP: ' + it.quantityAvailableToPromise}}"
		value="${attrs?.inventoryItem?.id }"/>

	<g:submitButton name="pickRequestItem" value="${warehouse.message(code:'request.pick.label', default:'Pick')}"></g:submitButton>
</td>
</g:else>
