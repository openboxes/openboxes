<div class="action-menu">
	<button class="action-btn">
		<img
			src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"
			style="vertical-align: middle" />
	</button>
	<div class="actions">
		<g:supports activityCode="${org.pih.warehouse.core.ActivityCode.ADJUST_INVENTORY}">
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "inventory", action: "createInventory")}">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'book_edit.png')}"/>&nbsp;
				<g:message code="inventory.adjustStock.label" />
				</a>
			</div>
		</g:supports>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "inventory", action: "createInboundTransfer")}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'package_in.png')}"/>&nbsp;
				<g:message code="inventory.incomingTransfer.label" />
			</a>
		</div>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "inventory", action: "createOutboundTransfer")}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'package_go.png')}"/>&nbsp;
			<g:message code="inventory.outgoingTransfer.label" />
			</a>
		</div>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "inventory", action: "createConsumed")}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'package_white.png')}"/>&nbsp;
			<g:message code="inventory.inventoryConsumed.label" />
			</a>
		</div>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "inventory", action: "createDamaged")}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'exclamation.png')}"/>&nbsp;
			<g:message code="inventory.inventoryDamaged.label" />
			</a>
		</div>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "inventory", action: "createDamaged")}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'hourglass.png')}"/>&nbsp;
			<g:message code="inventory.inventoryExpired.label" />
			</a>
		</div>
		<div class="action-menu-item">
			<hr />
		</div>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "shipment", action: "addToShipment")}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry_add.png')}"/>&nbsp;
			<g:message code="inventory.addToShipment.label" />
			</a>
		</div>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "productGroup", action: "addToProductGroup")}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'link_add.png')}"/>&nbsp;
			<g:message code="productGroup.addProducts.label" />
			</a>
		</div>
		<div class="action-menu-item">
			<hr />
		</div>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "inventoryLevel", action: "markAsSupported")}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}"/>
				&nbsp;<g:message code="inventory.markAsSupported.label" />
			</a>
		</div>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "inventoryLevel", action: "markAsNotSupported")}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}"/>
				&nbsp;<g:message code="inventory.markAsNotSupported.label" />
			</a>
		</div>
		<div class="action-menu-item">
			<hr />
		</div>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "product", action: "exportProducts")}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}"/>
				&nbsp;<g:message code="product.exportAsCsv.label" />
			</a>
		</div>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" data-action="${g.createLink(controller: "product", action: "deleteProducts")}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}"/>
				&nbsp;<g:message code="default.delete.label" args="[g.message(code:'products.label')]" />
			</a>
		</div>
	</div>
</div>


<script>
	$(document).ready(function() {
		$(".actionButton").click(function(event) { 
			var numChecked = $("input.checkbox:checked").length;
			if (numChecked <= 0) { 
				alert("${warehouse.message(code: 'inventory.selectAtLeastOneProduct.label')}");
				event.stopImmediatePropagation();
			}
			var form = $("#inventoryBrowserForm");
			form.attr("action", $(this).data("action"));
			form.submit();
		});
	});
</script>
