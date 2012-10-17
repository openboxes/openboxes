<span class="action-menu">
	<button name="actionButtonDropDown" class="action-btn">
		<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
	</button>
	<div class="actions">
		<div class="action-menu-item">
			<g:link controller="product" action="edit" id="${productInstance?.id }">
				<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
				<warehouse:message code="product.edit.label"/>
			</g:link>
		</div>
		<g:if test="${actionName != 'showStockCard' }">
			<div class="action-menu-item">
				<g:link controller="inventoryItem" action="showStockCard" params="['product.id': productInstance?.id]">
					<img src="${resource(dir: 'images/icons/silk', file: 'book.png')}"/>&nbsp;
					<warehouse:message code="inventory.showStockCard.label"/>
				</g:link>
			</div>
		</g:if>
		<div class="action-menu-item">
			<g:link controller="inventoryItem" action="showTransactionLog" params="['product.id': productInstance?.id, 'disableFilter':true]">
				<img src="${resource(dir: 'images/icons/silk', file: 'book_previous.png')}"/>&nbsp;
				<warehouse:message code="inventory.showTransactionLog.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="inventoryItem" action="showLotNumbers" params="['product.id': productInstance?.id]">
				<img src="${resource(dir: 'images/icons', file: 'barcode.png')}"/>&nbsp;
				<warehouse:message code="inventory.showLotNumbers.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link name="recordInventoryLink" controller="inventoryItem" action="showRecordInventory" params="['productInstance.id': productInstance?.id,'inventoryInstance.id':inventoryInstance?.id]">
				<img src="${resource(dir: 'images/icons/silk', file: 'book_edit.png')}"/>&nbsp;
				<warehouse:message code="inventory.record.label"/>
			</g:link>
		</div>

		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">
			<g:link controller="inventory" action="browse" params="['resetSearch':'true']">
				<img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}"/>&nbsp;
				<warehouse:message code="inventory.browse.label"/>
			</g:link>
		</div>
	</div>
</span>
