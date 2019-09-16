<g:hiddenField id="containerId" name="container.id" value="${containerId?:'' }" />

<tr class="prop">
	<td valign="top" class="name">
		<label><warehouse:message code="product.label" default="Product" /></label>
	</td>                            
	<td valign="top" class="value">
		<span id="product-text"></span>
		<g:hiddenField id="productId" name="product.id" value="" />
	</td>
</tr>
<tr class="prop">
	<td valign="top" class="name">
		<label><warehouse:message code="inventoryItem.lotNumber.label" /></label>
	</td>                            
	<td valign="top" class="value">
		<span id="lotNumber-text"></span>
		<g:hiddenField id="lotNumber-suggest" name="lotNumber" value="" />
		<g:hiddenField id="inventoryItemId" name="inventoryItem.id" value="" />
	</td>
</tr>				
<tr class="prop">
	<td valign="top" class="name">
		<label><warehouse:message code="inventoryItem.expirationDate.label" /></label>
	</td>                            
	<td valign="top" class="value">
		<span id="expirationDate-text"></span>
	</td>
</tr>				
<tr class="prop">
	<td valign="top" class="name">
		<label><warehouse:message code="shipping.availableQuantity.label" /></label>
	</td>                            
	<td valign="top" class="value">
		<span id="quantity-on-hand"></span>
		<img src="${resource(dir: 'images/icons/silk', file: 'reload.png')}" style="vertical-align: middle;" class="refresh"/>
	</td>
</tr>  	        
<tr class="prop">
	<td valign="top" class="name"><label><warehouse:message code="default.quantity.label" /></label></td>                            
	<td valign="top" class="value">
		<g:textField id="dialog-quantity" name="quantity" value="" size="10" class="text"/>
	</td>
</tr>  	        
<tr class="prop">
	<td valign="top" class="name"><label><warehouse:message code="shipping.recipient.label" /></label></td>                            
	<td valign="top" class="value">
		<g:autoSuggest name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName" 
			width="200" valueId="" valueName="" styleClass="text"/>	
	</td>
</tr>				

<tr>
	
	<td colspan="2">
		<div class="buttons center">
			<g:if test="${itemToEdit}">
				<g:submitButton name="updateItem" value="${warehouse.message(code:'shipping.saveItem.label')}" class="button"></g:submitButton>
			</g:if>
			<g:else>
				<g:submitButton name="saveItem" value="${warehouse.message(code:'shipping.saveItem.label')}" class="button"></g:submitButton>
			</g:else>
			<g:if test="${containerId}">
				<g:submitButton name="addAnotherItem" value="${warehouse.message(code:'shipping.saveItemAndAddAnother.label')}" class="button"></g:submitButton>
			</g:if>
			<button name="cancelDialog" type="reset" onclick="$('.ui-dialog-titlebar-close').click();" class="button"><warehouse:message code="default.button.cancel.label"/></button>
			<button class="show-search-form button">&lsaquo; <warehouse:message code="shipping.returnToSearch.label"/></button>
		</div>
	</td>
</tr>

<script>
	$(document).ready(function() {
		updateQuantityOnHand();
		$(".refresh").click(function() { 
			$("#quantity-on-hand").html("refreshing ...");
			updateQuantityOnHand();
		});


		$("#dialog-quantity").livequery(function(){
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
				$("#dialog-quantity").focus();
			});
		});
	});


	function validateQuantity() { 
		updateQuantityOnHand();
		var quantityEntered = $("#dialog-quantity").val();
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
