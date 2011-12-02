
				<g:hiddenField id="containerId" name="container.id" value="${addItemToContainerId }" />

				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="product.label" default="Product" /></label></td>                            
					<td valign="top" class="value">
						<span id="product-text"></span>
						<g:hiddenField id="productId" name="product.id" value="" />
						<span id="lotNumber-text"></span>
						<g:hiddenField id="lotNumber-suggest" name="lotNumber" value="" />
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="shipping.availableQuantity.label" /></label></td>                            
					<td valign="top" class="value">
						<span id="quantity-on-hand"></span>
						<img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh.png')}" style="vertical-align: middle;" class="refresh"/>
					</td>
				</tr>  	        
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="default.quantity.label" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="quantity" name="quantity" value="" size="5" /> 
					</td>
				</tr>  	        
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="shipping.recipient.label" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName" 
							width="200" valueId="" valueName=""/>	
					</td>
				</tr>				
				
				<tr>
					<td></td>
					<td>
						<div class="buttons left">
							<g:if test="${itemToEdit}">
								<g:submitButton name="updateItem" value="${warehouse.message(code:'shipping.updateItem.label')}"></g:submitButton>
								<g:submitButton name="deleteItem" value="${warehouse.message(code:'shipping.removeItem.label')}" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteItem.message')}')"></g:submitButton>
							</g:if>
							<g:else>
								<g:submitButton name="saveItem" value="${warehouse.message(code:'shipping.saveItem.label')}"></g:submitButton>
							</g:else>
							<g:if test="${addItemToContainerId}">
								<g:submitButton name="addAnotherItem" value="${warehouse.message(code:'shipping.saveItemAndAddAnother.label')}"></g:submitButton>
							</g:if>
							<button name="cancelDialog" type="reset" onclick="$('.ui-dialog-titlebar-close').click();"><warehouse:message code="default.button.cancel.label"/></button>
						</div>
					</td>
				</tr>
