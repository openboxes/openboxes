
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.product.label" default="Product" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest name="product" jsonUrl="/warehouse/json/findProductByName" 
							width="200" valueId="${itemInstance?.product?.id}" valueName="${itemInstance?.product?.name}"/>	
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.lotNumber.label" default="Lot / Serial Number" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="lotNumber" name="lotNumber" value="${itemInstance?.lotNumber}" size="30" /> 
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.quantity.label" default="Quantity" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="quantity" name="quantity" value="${itemInstance?.quantity}" size="3" /> 
					</td>
				</tr>  	        
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.recipient.label" default="Recipient" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
							width="200" valueId="${itemInstance?.recipient?.id}" valueName="${itemInstance?.recipient?.name}"/>							
					</td>
				</tr>
				