
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.product.label" default="Product" /></label></td>                            
					<td valign="top" class="value">
						${item?.product?.name }
						<g:hiddenField id="productId" name="product.id" value="${item?.product?.id }"/>
						<%-- 
						<g:autoSuggest id="product" name="product" jsonUrl="/warehouse/json/findProductByName" 
							width="300" valueId="${item?.product?.id}" valueName="${item?.product?.name}"/>	
						<g:link controller="product" action="create" target="_blank"><span class="small">Add a New Product</span></g:link>											
						--%>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.lotNumber.label" default="Lot / Serial Number" /></label></td>                            
					<td valign="top" class="value">
						<%-- <g:textField id="lotNumber" name="lotNumber" width="200" value="${item?.lotNumber}"/>--%>
						<g:autoSuggestString id="lotNumber" name="lotNumber" jsonUrl="/warehouse/json/findLotsByName?productId=${item?.product?.id }" 
							width="200" value="${item?.lotNumber}"/> 
						<!-- <g:link controller="inventory" action="createTransaction" target="_blank"><span class="small">Update Inventory</span></g:link> -->
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.availableQuantity.label" default="Available Quantity" /></label></td>                            
					<td valign="top" class="value">
						<span id="quantity-on-hand"></span>
						<img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh.png')}" style="vertical-align: middle;" class="refresh"/>
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
						<g:autoSuggest id="recipient" name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
							width="200" valueId="${item?.recipient?.id}" valueName="${item?.recipient?.name}"/>							
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
							<button name="cancelDialog" type="reset" onclick="$('#dlgEditItem').dialog('close');">Cancel</button>
						</div>
						<g:if test="${addItemToContainerId}">
							<div class="buttons">
								<g:submitButton name="addAnotherItem" value="Save Item and Add Another Item"></g:submitButton>
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
						$("#quantity-on-hand").load("/warehouse/json/getQuantity?productId=" + productId + "&lotNumber=" + lotNumber);
					}
				</script>

			
			