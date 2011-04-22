<div class="action-menu-item">				
	<img src="${resource(dir: 'images/icons/silk', file: 'arrow_left.png')}" style="vertical-align: middle;"/>&nbsp;
	<g:link controller="inventory" action="browse" fragment="inventory">
		<span style="vertical-align: middle;">Browse Inventory</span>
	</g:link>
</div>	
<div class="action-menu-item">					
	<img src="${resource(dir: 'images/icons/silk', file: 'book.png')}"/>&nbsp;
	<g:link controller="inventoryItem" action="recordInventory" params="['product.id':commandInstance?.productInstance?.id,'inventory.id':commandInstance?.inventoryInstance?.id]">
		<span style="vertical-align: middle;">Record inventory</span>
	</g:link>
</div>
<div class="action-menu-item">
	<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" style="vertical-align: middle"/>&nbsp;
	<g:link controller="inventory" action="createTransaction" params="['product.id':commandInstance?.productInstance?.id]">
		<span style="vertical-align: middle;">Add new transaction</span>
	</g:link>
</div>				
<div class="action-menu-item">					
	<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
	<g:link controller="product" action="edit" id="${commandInstance?.productInstance?.id }">
		<span style="vertical-align: middle;">Edit product details</span>
	</g:link>
</div>
<div class="action-menu-item">					
	<img src="${resource(dir: 'images/icons/silk', file: 'cog_edit.png')}"/>&nbsp;
	<g:link controller="inventoryItem" action="editInventoryLevel" params="['product.id': commandInstance?.productInstance?.id, 'inventory.id':commandInstance?.inventoryInstance?.id]">
		<span style="vertical-align: middle;">Edit product status</span>
	</g:link>
	
	
</div>
