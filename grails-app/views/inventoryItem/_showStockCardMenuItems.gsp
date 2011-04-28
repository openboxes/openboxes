<div class="action-menu-item">				
	<g:link controller="inventory" action="browse" fragment="inventory">
		<img src="${resource(dir: 'images/icons/silk', file: 'arrow_left.png')}" style="vertical-align: middle;"/>&nbsp;
		Browse Inventory
	</g:link>
</div>	
<div class="action-menu-item">					
	<g:link controller="inventoryItem" action="recordInventory" params="['product.id':commandInstance?.productInstance?.id,'inventory.id':commandInstance?.inventoryInstance?.id]">
		<img src="${resource(dir: 'images/icons/silk', file: 'book.png')}"/>&nbsp;
		Record inventory
	</g:link>
</div>
<div class="action-menu-item">
	<g:link controller="inventory" action="createTransaction" params="['product.id':commandInstance?.productInstance?.id]">
		<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" style="vertical-align: middle"/>&nbsp;
		Add new transaction
	</g:link>
</div>				
<div class="action-menu-item">					
	<g:link controller="product" action="edit" id="${commandInstance?.productInstance?.id }">
		<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
		Edit product details
	</g:link>
</div>
<div class="action-menu-item">					
	<g:link controller="inventoryItem" action="editInventoryLevel" params="['product.id': commandInstance?.productInstance?.id, 'inventory.id':commandInstance?.inventoryInstance?.id]">
		<img src="${resource(dir: 'images/icons/silk', file: 'cog_edit.png')}"/>&nbsp;
		Edit product status
	</g:link>
</div>
