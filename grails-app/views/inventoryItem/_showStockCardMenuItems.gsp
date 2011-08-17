<div class="action-menu-item">				
	<g:link controller="inventory" action="browse" fragment="inventory">
		<img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" style="vertical-align: middle;"/>&nbsp;
		<warehouse:message code="inventory.browse.label"/>
	</g:link>
</div>	
<div class="action-menu-item">
	<hr/>
</div>
<div class="action-menu-item">					
	<g:link controller="inventoryItem" action="showStockCard" params="['product.id': product?.id]">
		<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;
		<warehouse:message code="inventory.showStockCard.label"/>
	</g:link>
</div>
<div class="action-menu-item">					
	<g:link controller="inventoryItem" action="recordInventory" params="['product.id': product?.id,'inventory.id':inventory?.id]">
		<img src="${resource(dir: 'images/icons/silk', file: 'book.png')}"/>&nbsp;
		<warehouse:message code="inventory.record.label"/>
	</g:link>
</div>
<div class="action-menu-item">
	<g:link controller="inventory" action="createTransaction" params="['product.id':product?.id]">
		<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" style="vertical-align: middle"/>&nbsp;
		<warehouse:message code="transaction.addNewTransaction.label"/>
	</g:link>
</div>				
<div class="action-menu-item">					
	<g:link controller="product" action="edit" id="${product?.id }">
		<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
		<warehouse:message code="product.edit.label"/>
	</g:link>
</div>
<div class="action-menu-item">					
	<g:link controller="inventoryItem" action="editInventoryLevel" params="['product.id': product?.id, 'inventory.id':inventory?.id]">
		<img src="${resource(dir: 'images/icons/silk', file: 'cog_edit.png')}"/>&nbsp;
		<warehouse:message code="product.editStatus.label"/>
	</g:link>
</div>
<div class="action-menu-item">					
	<g:link controller="inventoryItem" action="showLotNumbers" params="['product.id': product?.id]">
		<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;
		<warehouse:message code="inventory.showLotNumbers.label"/>
	</g:link>
</div>
