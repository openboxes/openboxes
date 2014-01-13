<span class="action-menu">
	<button name="actionButtonDropDown" class="action-btn" id="product-action">
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
	</button>
	<div class="actions">
		<div class="action-menu-item">
			<g:link controller="inventory" action="browse" params="['resetSearch':'true']">
				<img src="${resource(dir: 'images/icons', file: 'indent.gif')}"/>&nbsp;
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
			<g:link name="recordInventoryLink" controller="inventoryItem" action="showRecordInventory" params="['productInstance.id': productInstance?.id,'inventoryInstance.id':inventoryInstance?.id]">
				<img src="${resource(dir: 'images/icons/silk', file: 'book.png')}"/>&nbsp;
				<warehouse:message code="inventory.record.label"/>
			</g:link>
		</div>
		<g:isUserManager>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="inventoryAdjustedBtn"> <img
					src="${createLinkTo(dir:'images/icons/silk',file:'book_edit.png')}"
					alt="${warehouse.message(code: 'inventory.inventoryAdjusted.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.inventoryAdjusted.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="incomingTransferBtn"> <img
					src="${createLinkTo(dir:'images/icons/silk',file:'package_in.png')}"
					alt="${warehouse.message(code: 'inventory.incomingTransfer.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.incomingTransfer.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="outgoingTransferBtn"> <img
					src="${createLinkTo(dir:'images/icons/silk',file:'package_go.png')}"
					alt="${warehouse.message(code: 'inventory.outgoingTransfer.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.outgoingTransfer.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="addToShipmentBtn"> <img
					src="${resource(dir: 'images/icons/silk', file: 'lorry_add.png')}"
					alt="${warehouse.message(code: 'inventory.addToShipment.label') }" />
					&nbsp;<warehouse:message code="inventory.addToShipment.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<hr />
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="addToProductGroupBtn"> <img
					src="${createLinkTo(dir:'images/icons/silk',file:'link_add.png')}"
					alt="${warehouse.message(code: 'productGroup.addProducts.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="productGroup.addProducts.label" />
				</a>
			</div>
			<%-- 
			<div class="action-menu-item"> 
			
				<a href="#" id="linkProductToProductGroup" class="open-dialog">
					Link to product group
				</a>
				
			
			</div>
			--%>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="inventoryConsumedBtn"> <img
					src="${createLinkTo(dir:'images/icons/silk',file:'package_white.png')}"
					alt="${warehouse.message(code: 'inventory.inventoryConsumed.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.inventoryConsumed.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="inventoryDamagedBtn"> <img
					src="${createLinkTo(dir:'images/icons/silk',file:'exclamation.png')}"
					alt="${warehouse.message(code: 'inventory.inventoryDamaged.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.inventoryDamaged.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="inventoryExpiredBtn"> <img
					src="${createLinkTo(dir:'images/icons/silk',file:'clock_red.png')}"
					alt="${warehouse.message(code: 'inventory.inventoryExpired.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.inventoryExpired.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<hr />
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="markAsSupported"> <img
					src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}"
					alt="${warehouse.message(code: 'inventory.markAsSupported.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.markAsSupported.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="markAsNotSupported"> <img
					src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}"
					alt="${warehouse.message(code: 'inventory.markAsNotSupported.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.markAsNotSupported.label" />
				</a>
			</div>
		</g:isUserManager>		
		<g:isUserAdmin>		
			<g:if test="${productInstance.id}">
				<div class="action-menu-item">
					<g:link colection="product" action="delete" id="${productInstance.id}"
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
		</g:isUserAdmin>
		
	</div>
</span>
<g:form id="inventoryActionForm" name="inventoryActionForm" controller="inventory" action="createTransaction" method="POST">
	<g:hiddenField name="product.id" value="${productInstance?.id }"/>
</g:form>
<%--
<div class="dialog-form" id="dialog-linkProductToProductGroup">
	<g:form controller="product" action="saveGenericProduct" method="post">
		<table>
			<tbody>

				<tr class="prop">
					<td valign="middle" class="name"><label for="category"><warehouse:message
								code="productGroup.category.label" default="Category" /></label></td>
					<td valign="middle" class="value ${hasErrors(bean: product, field: 'category', 'errors')}">

						${productInstance?.category }							
					</td>
				</tr>
				<tr class="prop">
					<td valign="middle" class="name"><label for="description"><warehouse:message
								code="productGroup.name.label" default="Generic product" /></label>
					</td>
					<td valign="middle"
						class="value ${hasErrors(bean: productGroupInstance, field: 'description', 'errors')}">							
						<g:if test="${productGroups }">	
							<div>										
								<g:select name="id" from="${productGroups }" 
									optionKey="id" optionValue="description" value="${productGroupInstance?.id }" noSelection="['null':'']"/>
							</div>
						</g:if>
						<g:else>
							<div>
								<g:textField name="description" class="text" size="60" value="${productInstance?.name }"/>
							</div>
						</g:else>
							
						
					</td>
				</tr>
				<tr class="prop">
					<td class="top center" colspan="2">
						<g:submitButton name="create" class="button icon save"
							value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}" />
						<a href="javascript:void(-1);" class="close-dialog" id="linkProductToProductGroup">Cancel</a>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>
</div>
--%>
<script>
	$(document).ready(function() {		
		// Form Actions 
		$("#incomingTransferBtn").click(function(event) { 
			$("#inventoryActionForm").append($("<input>", {type: "hidden", name: "transactionType.id", "value": "8"})).submit();
		});
		$("#outgoingTransferBtn").click(function(event) { 
			$("#inventoryActionForm").append($("<input>", {type: "hidden", name: "transactionType.id", "value": "9"})).submit();
		});
		$("#inventoryAdjustedBtn").click(function(event) { 
			$("#inventoryActionForm").append($("<input>", {type: "hidden", name: "transactionType.id", "value": "7"})).submit();
		});
		$("#inventoryDamagedBtn").click(function(event) { 
			$("#inventoryActionForm").append($("<input>", {type: "hidden", name: "transactionType.id", "value": "5"})).submit();
		});
		$("#inventoryExpiredBtn").click(function(event) { 
			$("#inventoryActionForm").append($("<input>", {type: "hidden", name: "transactionType.id", "value": "4"})).submit();
		});
		$("#inventoryConsumedBtn").click(function(event) { 
			$("#inventoryActionForm").append($("<input>", {type: "hidden", name: "transactionType.id", "value": "2"})).submit();
		});
		$("#addToShipmentBtn").click(function(event) { 			
			$("#inventoryActionForm").attr("action", "${request.contextPath }/shipment/addToShipment").submit();
		});
		$("#addToProductGroupBtn").click(function(event) { 
			$("#inventoryActionForm").attr("action", "${request.contextPath }/productGroup/addToProductGroup").submit();
		});
		$("#transactionReportBtn").click(function(event) { 
			$("#inventoryActionForm").attr("action", "${request.contextPath }/report/showTransactionReport").submit();
		});
		$("#markAsSupported").click(function(event) { 
			$("#inventoryActionForm").attr("action", "${request.contextPath }/inventoryLevel/markAsSupported").submit();
		});
		$("#markAsNotSupported").click(function(event) { 
			$("#inventoryActionForm").attr("action", "${request.contextPath }/inventoryLevel/markAsNotSupported").submit();
		});
		$("#markAsNonInventoried").click(function(event) { 
			$("#inventoryActionForm").attr("action", "${request.contextPath }/inventoryLevel/markAsNonInventoried").submit();
		});

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