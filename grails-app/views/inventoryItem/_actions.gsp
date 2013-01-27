	<span class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" class="middle"/>
		</button>
		<div class="actions">
			<div class="action-menu-item">					
				<g:link controller="product" action="edit" id="${product?.id }">
					<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
					<warehouse:message code="product.edit.label"/>
				</g:link>
			</div>
			<g:if test="${actionName != 'showStockCard' }">
				<div class="action-menu-item">					
					<g:link controller="inventoryItem" action="showStockCard" params="['product.id': product?.id]">
						<img src="${resource(dir: 'images/icons/silk', file: 'book.png')}"/>&nbsp;
						<warehouse:message code="inventory.showStockCard.label"/>
					</g:link>
				</div>
			</g:if>
			<div class="action-menu-item">					
				<g:link controller="inventoryItem" action="showTransactionLog" params="['product.id': product?.id, 'disableFilter':true]">
					<img src="${resource(dir: 'images/icons/silk', file: 'book_previous.png')}"/>&nbsp;
					<warehouse:message code="inventory.showTransactionLog.label"/>
				</g:link>
			</div>
			<div class="action-menu-item">					
				<g:link controller="inventoryItem" action="showLotNumbers" params="['product.id': product?.id]">
					<img src="${resource(dir: 'images/icons', file: 'barcode.png')}"/>&nbsp;
					<warehouse:message code="inventory.showLotNumbers.label"/>
				</g:link>
			</div>
			<div class="action-menu-item">					
				<g:link controller="inventoryItem" action="showRecordInventory" params="['productInstance.id': product?.id,'inventoryInstance.id':inventory?.id]">
					<img src="${resource(dir: 'images/icons/silk', file: 'book_edit.png')}"/>&nbsp;
					<warehouse:message code="inventory.record.label"/>
				</g:link>
			</div>
			<%-- 
			<div class="action-menu-item">
				<g:link controller="inventory" action="createTransaction" params="['product.id':product?.id]">
					<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" style="vertical-align: middle"/>&nbsp;
					<warehouse:message code="transaction.addNewTransaction.label"/>
				</g:link>
			</div>
			--%>
			<%-- 
			<div class="action-menu-item">					
				<g:link controller="inventoryItem" action="editInventoryLevel" params="['product.id': product?.id, 'inventory.id':inventory?.id]">
					<img src="${resource(dir: 'images/icons/silk', file: 'heart.png')}"/>&nbsp;
					<warehouse:message code="product.editStatus.label"/>
				</g:link>
			</div>
			--%>
			<%-- 
			<div class="action-menu-item">					
				<g:link controller="product" action="addPhoto" id="${product?.id }">
					<img src="${resource(dir: 'images/icons/silk', file: 'photo_add.png')}"/>&nbsp;
					<warehouse:message code="product.addPhoto.label"/>
				</g:link>
			</div>
			--%>
			<%-- 
			<div class="action-menu-item">					
				<g:link controller="inventoryItem" action="showGraph" params="['product.id': product?.id]">
					<img src="${resource(dir: 'images/icons/silk', file: 'chart_bar.png')}"/>&nbsp;
					<warehouse:message code="inventory.showGraph.label"/>
				</g:link>
			</div>
			--%>
			<div class="action-menu-item">
				<hr/>
			</div>
			<div class="action-menu-item">				
				<g:link controller="inventory" action="browse" params="['resetSearch':'true']">
					<img src="${resource(dir: 'images/icons', file: 'indent.gif')}"/>&nbsp;
					<warehouse:message code="inventory.browse.label"/>
				</g:link>
			</div>	


		</div>
	</span>				

