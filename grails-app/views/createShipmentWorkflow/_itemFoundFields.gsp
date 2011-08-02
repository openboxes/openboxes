
				<g:hiddenField id="containerId" name="container.id" value="${addItemToContainerId }" />

				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="shipmentItem.product.label" default="Product" /></label></td>                            
					<td valign="top" class="value">
						<span id="product-text"></span>
						<g:hiddenField id="productId" name="product.id" value="" />
						<span id="lotNumber-text"></span>
						<g:hiddenField id="lotNumber-suggest" name="lotNumber" value="" />
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="shipmentItem.availableQuantity.label" default="Available Quantity" /></label></td>                            
					<td valign="top" class="value">
						<span id="quantity-on-hand"></span>
						<img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh.png')}" style="vertical-align: middle;" class="refresh"/>
					</td>
				</tr>  	        
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="shipmentItem.quantity.label" default="Quantity" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="quantity" name="quantity" value="" size="5" /> 
					</td>
				</tr>  	        
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="shipmentItem.recipient.label" default="Recipient" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
							width="200" valueId="" valueName=""/>	
					</td>
				</tr>				
				
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:if test="${itemToEdit}">
								<g:submitButton name="updateItem" value="Update Item"></g:submitButton>
								<g:submitButton name="deleteItem" value="Remove Item" onclick="return confirm('Are you sure you want to delete this item?')"></g:submitButton>
							</g:if>
							<g:else>
								<g:submitButton name="saveItem" value="Save Item"></g:submitButton>
							</g:else>
							<button name="cancelDialog" type="reset" onclick="$('.ui-dialog-titlebar-close').click();">Cancel</button>
						</div>
						<g:if test="${addItemToContainerId}">
							<div class="buttons">
								<g:submitButton name="addAnotherItem" value="Save Item and Add Another Item"></g:submitButton>
							</div>
						</g:if>
					</td>
				</tr>
