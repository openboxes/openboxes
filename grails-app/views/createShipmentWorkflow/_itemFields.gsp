
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.product.label" default="Product" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest name="product" jsonUrl="/warehouse/json/findProductByName" 
							width="200" valueId="${item?.product?.id}" valueName="${item?.product?.name}"/>
						<g:link controller="product" action="create" target="_blank"><span class="small">Add a New Product</span></g:link>											
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.lotNumber.label" default="Lot / Serial Number" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="lotNumber" name="lotNumber" value="${item?.lotNumber}" size="30" /> <br/>
						<g:link controller="inventory" action="createTransaction" target="_blank"><span class="small">Update Inventory</span></g:link>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.quantity.label" default="Quantity" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="quantity" name="quantity" value="${item?.quantity}" size="3" /> 
					</td>
				</tr>  	        
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.recipient.label" default="Recipient" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
							width="200" valueId="${item?.recipient?.id}" valueName="${item?.recipient?.name}"/>							
					</td>
				</tr>
				