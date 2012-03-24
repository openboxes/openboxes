
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="product.label" default="Product" /></label></td>                            
					<td valign="top" class="value">
						<format:product product="${item?.product}"/>
						<g:hiddenField id="productId" name="product.id" value="${item?.product?.id }"/>
						<%-- 
						<g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName" 
							width="300" valueId="${item?.product?.id}" valueName="${format.product(product:item?.product}"/>	
						<g:link controller="product" action="create" target="_blank"><span class="small"><warehouse:message code="product.add.label"/></span></g:link>											
						--%>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="default.lotSerialNo.label" /></label></td>                            
					<td valign="top" class="value">
						<%-- <g:textField id="lotNumber" name="lotNumber" width="200" value="${item?.lotNumber}"/>--%>
						<g:autoSuggestString id="lotNumber" name="lotNumber" jsonUrl="${request.contextPath }/json/findLotsByName?productId=${item?.product?.id }" 
							width="200" value="${item?.lotNumber}" styleClass="text"/> 
						<!-- <g:link controller="inventory" action="createTransaction" target="_blank"><span class="small">Update Inventory</span></g:link> -->
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
						<g:textField id="quantity" name="quantity" value="${item?.quantity}" size="5" class="text" /> 
					</td>
				</tr>  	        
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="shipping.recipient.label"/></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest id="recipient" name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName" 
							width="200" valueId="${item?.recipient?.id}" valueName="${item?.recipient?.name}" styleClass="text"/>							
					</td>
				</tr>
				<tr>
					<td class="name"></td>
					<td>
						<div class="buttons left">
							<g:if test="${itemToEdit}">
								<g:submitButton name="updateItem" value="${warehouse.message(code:'shipping.saveItem.label')}"></g:submitButton>
								
								<%-- 
								<g:submitButton name="deleteItem" value="${warehouse.message(code:'shipping.removeItem.label')}" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteItem.message')}')"></g:submitButton>
								--%>
							</g:if>
							<g:else>
								<g:submitButton name="saveItem" value="${warehouse.message(code:'shipping.saveItem.label')}"></g:submitButton>
							</g:else>
							<button name="cancelDialog" type="reset" onclick="$('#dlgEditItem').dialog('close');"><warehouse:message code="default.button.cancel.label"/></button>
						</div>
						<g:if test="${addItemToContainerId}">
							<div class="buttons left">
								<g:submitButton name="addAnotherItem" value="${warehouse.message(code:'shipping.saveItemAndAddAnother.label')}"></g:submitButton>
							</div>
						</g:if>
					</td>
				</tr>
				
				<script>
					$(document).ready(function() {
						updateQuantityOnHand();
						$(".refresh").click(function() { 
							$("#quantity-on-hand").html("refreshing ...");
							updateQuantityOnHand();
						});


						$("#quantity").livequery(function(){
							$(this).blur(function(event) {					
								if (!validateQuantity()) { 
									alert("Please enter a valid quantity");
									$(this).val('');
									$(this).focus();
								}
							});
					    });

						$("#lotNumber-suggest").livequery(function(){
							$(this).bind( "autocompletechange", function(event, ui) {
								updateQuantityOnHand();
								$("#quantity").focus();
							});
						});
					});


					function validateQuantity() { 
						updateQuantityOnHand();
						var quantityEntered = $("#quantity").val()
						var quantityOnHand = $("#quantity-on-hand").html();
						if (quantityEntered > parseInt(quantityOnHand)) {
							return false;
						}
						return true;
					}
					
					function updateQuantityOnHand() { 
						var productId = $("#productId").val();
						var lotNumber = $("#lotNumber-suggest").val();				
						$("#quantity-on-hand").load("${request.contextPath }/json/getQuantity?productId=" + productId + "&lotNumber=" + lotNumber);
					}
				</script>

			
			