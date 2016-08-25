<div class="action-menu">
	<button class="action-btn">
		<img
			src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"
			style="vertical-align: middle" />
	</button>
	<div class="actions left">
		<g:isUserManager>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="inventoryAdjustedBtn"> <img
					src="${resource(dir:'images/icons/silk',file:'book_edit.png')}"
					alt="${warehouse.message(code: 'inventory.inventoryAdjusted.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.inventoryAdjusted.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="incomingTransferBtn"> <img
					src="${resource(dir:'images/icons/silk',file:'package_in.png')}"
					alt="${warehouse.message(code: 'inventory.incomingTransfer.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.incomingTransfer.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="outgoingTransferBtn"> <img
					src="${resource(dir:'images/icons/silk',file:'package_go.png')}"
					alt="${warehouse.message(code: 'inventory.outgoingTransfer.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.outgoingTransfer.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<hr />
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
				<a href="javascript:void(0);" class="actionButton"
					id="addToProductGroupBtn"> <img
					src="${resource(dir:'images/icons/silk',file:'link_add.png')}"
					alt="${warehouse.message(code: 'productGroup.addProducts.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="productGroup.addProducts.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<hr />
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="inventoryConsumedBtn"> <img
					src="${resource(dir:'images/icons/silk',file:'package_white.png')}"
					alt="${warehouse.message(code: 'inventory.inventoryConsumed.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.inventoryConsumed.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="inventoryDamagedBtn"> <img
					src="${resource(dir:'images/icons/silk',file:'exclamation.png')}"
					alt="${warehouse.message(code: 'inventory.inventoryDamaged.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.inventoryDamaged.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="inventoryExpiredBtn"> <img
					src="${resource(dir:'images/icons/silk',file:'clock_red.png')}"
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
					src="${resource(dir:'images/icons/silk',file:'accept.png')}"
					alt="${warehouse.message(code: 'inventory.markAsSupported.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.markAsSupported.label" />
				</a>
			</div>
			<%-- 
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="markAsNonInventoried"> <img
					src="${resource(dir:'images/icons/silk',file:'flag_orange.png')}"
					alt="${warehouse.message(code: 'inventory.markAsNonInventoried.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.markAsNonInventoried.label" />
				</a>
			</div>
			--%>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="markAsNotSupported"> <img
					src="${resource(dir:'images/icons/silk',file:'decline.png')}"
					alt="${warehouse.message(code: 'inventory.markAsNotSupported.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="inventory.markAsNotSupported.label" />
				</a>
			</div>
			<div class="action-menu-item">
				<hr />
			</div>
			<div class="action-menu-item">
				<a href="javascript:void(0);" class="actionButton"
					id="exportProductsBtn"> <img
					src="${resource(dir:'images/icons/silk',file:'table_save.png')}"
					alt="${warehouse.message(code: 'product.exportAsCsv.label') }"
					style="vertical-align: middle" /> &nbsp;<warehouse:message
						code="product.exportAsCsv.label" />
				</a>
			</div>
            <div class="action-menu-item">
                <a href="javascript:void(0);" class="actionButton"
                   id="deleteProductsBtn"> <img
                        src="${resource(dir:'images/icons/silk',file:'table_delete.png')}"
                        alt="${warehouse.message(code: 'products.delete.label', default: 'Delete selected products') }"
                        style="vertical-align: middle" /> &nbsp;<warehouse:message code="products.button.delete.label" default="Delete selected products" />
                </a>
            </div>


		</g:isUserManager>
		<%--  Need to set defaults for the Transaction Report to generate a report.
			This might cause problems with the other actions, so keep that in mind. 
			<div class="action-menu-item">					
				<g:hiddenField name="category.id" value="${commandInstance?.categoryInstance?.id?:quickCategories[0]?.id}"/>
				<g:hiddenField name="location.id" value="${session?.warehouse?.id}"/>
															
				<a href="javascript:void(0);" class="actionButton" id="transactionReportBtn">
					<img src="${resource(dir:'images/icons/silk',file:'report.png')}" alt="${warehouse.message(code: 'report.showTransactionReport.label') }" style="vertical-align: middle"/>
					&nbsp;<warehouse:message code="report.showTransactionReport.label"/>
				</a>
			</div>		
		--%>
		<div class="action-menu-item">
			<g:link controller="inventory" action="listAllTransactions">
				<img
					src="${resource(dir:'images/icons/silk',file:'application_view_list.png')}"
					alt="${warehouse.message(code: 'inventory.listTransactions.label') }"
					style="vertical-align: middle" />
				&nbsp;<warehouse:message code="inventory.listTransactions.label" />
			</g:link>
		</div>
	</div>
</div>


<script>
	$(document).ready(function() {
		$(".actionButton").click(function(event) { 
			//var anyChecked = $('.checkbox').attr('checked');
			var numChecked = $("input.checkbox:checked").length;
			if (numChecked <= 0) { 
				alert("${warehouse.message(code: 'inventory.selectAtLeastOneProduct.label')}");
				event.stopImmediatePropagation();
			}
			return false;			
		});
		
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
		$("#exportProductsBtn").click(function(event) { 
			$("#inventoryActionForm").attr("action", "${request.contextPath }/product/exportProducts").submit();
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
        $("#deleteProductsBtn").click(function(event) {
            $("#inventoryActionForm").attr("action", "${request.contextPath }/product/deleteProducts").submit();
        });

	});
</script>
