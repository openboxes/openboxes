
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.product.label" default="Product" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest name="product" jsonUrl="/warehouse/json/findProductByName" 
							width="300" valueId="${item?.product?.id}" valueName="${item?.product?.name}"/>	
						<g:link controller="product" action="create" target="_blank"><span class="small">Add a New Product</span></g:link>											
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.lotNumber.label" default="Lot / Serial Number" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="lotNumber" name="lotNumber" width="200" value="${item?.lotNumber}"/>
					<!--  <g:autoSuggestString name="lotNumber" jsonUrl="/warehouse/json/findLotsByName" 
							width="200" value="${item?.lotNumber}"/>  -->	
	<!--  					<g:link controller="inventory" action="createTransaction" target="_blank"><span class="small">Update Inventory</span></g:link> -->
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.quantity.label" default="Quantity" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="quantity" name="quantity" value="${item?.quantity}" size="5" /> 
					</td>
				</tr>  	        
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.recipient.label" default="Recipient" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
							width="200" valueId="${item?.recipient?.id}" valueName="${item?.recipient?.name}"/>							
					</td>
				</tr>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="saveItem" value="Save Item"></g:submitButton>
							<g:if test="${itemToEdit}">
								<g:submitButton name="deleteItem" value="Remove Item" onclick="return confirm('Are you sure you want to delete this item?')"></g:submitButton>
							</g:if>
							<button name="cancelDialog" type="reset" onclick="$('#dlgEditItem').dialog('close');">Cancel</button>
						</div>
						<g:if test="${addItemToContainerId}">
							<div class="buttons">
								<g:submitButton name="addAnotherItem" value="Save Item and Add Another Item"></g:submitButton>
							</div>
						</g:if>
					</td>
				</tr>

			
			