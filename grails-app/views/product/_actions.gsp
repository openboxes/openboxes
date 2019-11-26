<span class="action-menu">
	<button name="actionButtonDropDown" class="action-btn" id="product-action">
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
	</button>
	<div class="actions">
		<div class="action-menu-item">
			<g:link controller="inventory" action="browse" params="['resetSearch':'true']">
				<img src="${resource(dir: 'images/icons/silk', file: 'text_list_numbers.png')}"/>&nbsp;
				<warehouse:message code="inventory.browse.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">
			<g:link controller="inventoryItem" action="showStockCard" params="['product.id': productInstance?.id]">
				<img src="${resource(dir: 'images/icons/silk', file: 'clipboard.png')}"/>&nbsp;
				<warehouse:message code="inventory.showStockCard.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="product" action="edit" id="${productInstance?.id }">
				<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
				<warehouse:message code="product.edit.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="inventoryItem" action="showTransactionLog" params="['product.id': productInstance?.id, 'disableFilter':true]">
				<img src="${resource(dir: 'images/icons/silk', file: 'calendar.png')}"/>&nbsp;
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
			<hr />
		</div>
		<div class="action-menu-item">
			<g:link name="recordInventoryLink" controller="inventoryItem" action="showRecordInventory"
					params="['product.id': productInstance?.id,'inventory.id':inventoryInstance?.id]">
				<img src="${resource(dir: 'images/icons/silk', file: 'book.png')}"/>&nbsp;
				<warehouse:message code="inventory.record.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="inventory" action="createInboundTransfer" params="['product.id':productInstance?.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'package_in.png')}"/>&nbsp;
				<warehouse:message code="inventory.incomingTransfer.label" />
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="inventory" action="createOutboundTransfer" params="['product.id':productInstance?.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'package_go.png')}"/>&nbsp;
				<warehouse:message code="inventory.outgoingTransfer.label" />
			</g:link>
		</div>
		<g:supports activityCode="${org.pih.warehouse.core.ActivityCode.ADJUST_INVENTORY}">
			<div class="action-menu-item">
				<g:link controller="inventory" action="createAdjustment" params="['product.id':productInstance?.id]">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'package_green.png')}"/>&nbsp;
					<g:message code="inventory.inventoryAdjusted.label" />
				</g:link>
			</div>
		</g:supports>
		<div class="action-menu-item">
			<g:link controller="inventory" action="createTransaction" params="['product.id':productInstance?.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'package_delete.png')}"/>&nbsp;
				<g:message code="default.create.label" args="[g.message(code: 'default.debit.label').toLowerCase()]"/>
			</g:link>
		</div>
		<g:supports activityCode="${org.pih.warehouse.core.ActivityCode.CONSUME_STOCK}">
			<div class="action-menu-item">
				<g:link controller="inventory" action="createConsumed" params="['product.id':productInstance?.id]">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'package_stop.png')}"/>&nbsp;
					<warehouse:message code="inventory.inventoryConsumed.label" />
				</g:link>
			</div>
		</g:supports>
		<div class="action-menu-item">
			<g:link controller="inventory" action="createExpired" params="['product.id':productInstance?.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'hourglass.png')}"/>&nbsp;
				<warehouse:message code="inventory.inventoryExpired.label" />
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="inventory" action="createDamaged" params="['product.id':productInstance?.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'package_delete.png')}"/>&nbsp;
				<warehouse:message code="inventory.inventoryDamaged.label" />
			</g:link>
		</div>
		<div class="action-menu-item">
			<hr />
		</div>
		<div class="action-menu-item">
			<g:link controller="shipment" action="addToShipment" params="['product.id':productInstance?.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry_add.png')}"/>&nbsp;
				<warehouse:message code="inventory.addToShipment.label" />
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="productGroup" action="addToProductGroup" params="['product.id':productInstance?.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'link_add.png')}"/>&nbsp;
				<warehouse:message code="productGroup.addToProductGroup.label" default="Add to product group"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<hr />
		</div>
		<div class="action-menu-item">
			<g:link controller="inventoryLevel" action="markAsSupported" params="['product.id':productInstance?.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}"/>&nbsp;
				<warehouse:message code="inventory.markAsSupported.label" />
			</g:link>

		</div>
		<div class="action-menu-item">
			<g:link controller="inventoryLevel" action="markAsNotSupported" params="['product.id':productInstance?.id]">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}"/>&nbsp;
				<warehouse:message code="inventory.markAsNotSupported.label" />
			</g:link>
		</div>
		<g:if test="${productInstance.id}">
			<div class="action-menu-item">
				<g:link controller="product" action="delete" id="${productInstance.id}"
						onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}" alt="${warehouse.message(code: 'default.button.delete.label') }"
						style="vertical-align: middle" />&nbsp;
					<warehouse:message code="product.delete.label" default="Delete product"/>
				</g:link>
			</div>
			<div class="action-menu-item">
				<g:link controller="product" action="renderCreatedEmail" id="${productInstance?.id}">
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'email.png')}" class="middle"/>&nbsp;
					<warehouse:message code="product.productCreated.label" default="Product created email"/></g:link>
			</div>
		</g:if>

	</div>
</span>

<script>
	$(document).ready(function() {

		$(".dialog-form").dialog({ autoOpen: false, modal: true, width: '800px', top: 10});

		$(".open-dialog").click(function() {
			var id = $(this).attr("id");
			$("#dialog-" + id).dialog('open');
		});
		$(".close-dialog").click(function() {
			var id = $(this).attr("id");
			$("#dialog-" + id).dialog('close');
		});

	});
</script>
