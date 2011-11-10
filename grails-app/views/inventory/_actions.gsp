<span class="action-menu">
	<button class="action-btn">
		<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>							
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
	</button>
	<div class="actions">
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="outgoingTransferBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="${warehouse.message(code: 'inventory.outgoingTransfer.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.outgoingTransfer.label"/>
			</a>
		</div>	
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="incomingTransferBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="${warehouse.message(code: 'inventory.incomingTransfer.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.incomingTransfer.label"/>
			</a>
		</div>
		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="inventoryAdjustmentBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'box.png')}" alt="${warehouse.message(code: 'inventory.inventoryAdjustment.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.inventoryAdjustment.label"/>
			</a>
		</div>	
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="inventoryDamagedBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}" alt="${warehouse.message(code: 'inventory.inventoryDamaged.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.inventoryDamaged.label"/>
			</a>
		</div>	
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" id="addToShipmentBtn">
				<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" alt="${warehouse.message(code: 'inventory.addToShipment.label') }"/>
				&nbsp;<warehouse:message code="inventory.addToShipment.label"/>
			</a>
		</div>
		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">														
			<g:link controller="inventory" action="listAllTransactions">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="${warehouse.message(code: 'inventory.listTransactions.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.listTransactions.label"/>
			</g:link>
		</div>		
		<%-- 
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="addToConsumptionBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'cup.png')}" alt="${warehouse.message(code: 'inventory.addToConsumptionTransaction.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.addToConsumption.label"/>
			</a>
		</div>	
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="addToInventoryBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'box.png')}" alt="${warehouse.message(code: 'inventory.addToInventoryTransaction.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.addToInventory.label"/>
			</a>
		</div>	
		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" id="addToShipmentBtn">
				<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" alt="${warehouse.message(code: 'inventory.addToShipment.label') }"/>
				&nbsp;<warehouse:message code="inventory.addToShipment.label"/>
			</a>
		</div>
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="addToTransactionBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="${warehouse.message(code: 'inventory.addToTransaction') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.addToTransaction.label"/>
			</a>
		</div>
		--%>
	</div>
</span>

<script>
	$(document).ready(function() {

		// Form Actions 
		$("#incomingTransferBtn").click(function(event) { 
			var transactionType = $("<input>").attr("type", "hidden").attr("name", "transactionType.id").val("8");
			$('#inventoryActionForm').append($(transactionType));
			$("#inventoryActionForm").attr("action", "${request.contextPath }/inventory/createTransaction");
			$("#inventoryActionForm").submit();
		});
		$("#outgoingTransferBtn").click(function(event) { 
			var transactionType = $("<input>").attr("type", "hidden").attr("name", "transactionType.id").val("9");
			$('#inventoryActionForm').append($(transactionType));
			$("#inventoryActionForm").attr("action", "${request.contextPath }/inventory/createTransaction");
			$("#inventoryActionForm").submit();
		});
		$("#inventoryAdjustmentBtn").click(function(event) { 
			var transactionType = $("<input>").attr("type", "hidden").attr("name", "transactionType.id").val("7");
			$('#inventoryActionForm').append($(transactionType));
			$("#inventoryActionForm").attr("action", "${request.contextPath }/inventory/createTransaction");
			$("#inventoryActionForm").submit();
		});
		$("#inventoryDamagedBtn").click(function(event) { 
			var transactionType = $("<input>").attr("type", "hidden").attr("name", "transactionType.id").val("5");
			$('#inventoryActionForm').append($(transactionType));
			$("#inventoryActionForm").attr("action", "${request.contextPath }/inventory/createTransaction");
			$("#inventoryActionForm").submit();
		});
		$("#inventoryExpiredBtn").click(function(event) { 
			var transactionType = $("<input>").attr("type", "hidden").attr("name", "transactionType.id").val("4");
			$('#inventoryActionForm').append($(transactionType));
			$("#inventoryActionForm").attr("action", "${request.contextPath }/inventory/createTransaction");
			$("#inventoryActionForm").submit();
		});
		$("#addToShipmentBtn").click(function(event) { 
			//var action = $("<input>").attr("type", "hidden").attr("name", "actionButton").val("addToShipment");
			//$('#inventoryActionForm').append($(action));
			$("#inventoryActionForm").attr("action", "${request.contextPath }/shipment/addToShipment");
			$("#inventoryActionForm").submit();					
		});

		<%--
		$("#addToConsumptionBtn").click(function(event) { 
			//var action = $("<input>").attr("type", "hidden").attr("name", "actionButton").val("addToTransaction");
			//$('#inventoryActionForm').append($(action));
			$("#inventoryActionForm").attr("action", "${request.contextPath }/inventory/createTransaction");
			$("#inventoryActionForm").submit();
		});
		$("#addToInventoryBtn").click(function(event) { 
			//var action = $("<input>").attr("type", "hidden").attr("name", "actionButton").val("addToTransaction");
			//$('#inventoryActionForm').append($(action));
			$("#inventoryActionForm").attr("action", "${request.contextPath }/inventory/createTransaction");
			$("#inventoryActionForm").submit();
		});
		$("#addToShipmentBtn").click(function(event) { 
			//var action = $("<input>").attr("type", "hidden").attr("name", "actionButton").val("addToShipment");
			//$('#inventoryActionForm').append($(action));
			$("#inventoryActionForm").attr("action", "${request.contextPath }/shipment/addToShipment");
			$("#inventoryActionForm").submit();					
		});
		$("#addToTransactionBtn").click(function(event) { 
			//var action = $("<input>").attr("type", "hidden").attr("name", "actionButton").val("addToTransaction");
			//$('#inventoryActionForm').append($(action));
			$("#inventoryActionForm").attr("action", "${request.contextPath }/inventory/createTransaction");
			$("#inventoryActionForm").submit();
		});
		
		
		--%>

	});
</script>