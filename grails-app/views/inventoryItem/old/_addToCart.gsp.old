
<script type="text/javascript">
	$(document).ready(function(){
		$("#btnAddToCart-${itemInstance?.id}").click(function() { $("#dlgAddToCart-${itemInstance?.id}").dialog('open'); });									
		$("#dlgAddToCart-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '400px' });				
	});
</script>	


<g:form controller="cart" action="addToCart">
	<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id}"/>
	<g:hiddenField name="inventory.id" value="${commandInstance?.inventoryInstance?.id}"/>
	<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
	<g:hiddenField name="quantity" value="${1 }"/>
	<g:hiddenField name="redirectUrl" value="${createLinkTo(controller: 'inventoryItem', action: 'showStockCard', id: commandInstance?.productInstance?.id) }"/>
	
	<button id="btnAddToCart-${itemInstance?.id}" class="action-btn">
		<img src="${resource(dir: 'images/icons/silk', file: 'cart_add.png')}"/> Add to cart
	</button>

</g:form>

