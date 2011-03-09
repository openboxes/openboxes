
		
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.product.label" default="Product" /></label></td>                            
					<td valign="top" class="value">
						<span id="product-text"></span>
						<g:hiddenField id="product-id" name="product.id" value="${item?.product?.id}" size="20" />
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.lotNumber.label" default="Lot / Serial Number" /></label></td>                            
					<td valign="top" class="value">
						<span id="lotNumber-text"></span>
						<g:hiddenField id="lotNumber" name="lotNumber" value="${item?.lotNumber}" size="20" />
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.quantity.label" default="Quantity" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="found-quantity" name="quantity" value="${item?.quantity}" size="3" /> 
						&nbsp [ Available Qty: <span id="quantity-text"></span> ]
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
				