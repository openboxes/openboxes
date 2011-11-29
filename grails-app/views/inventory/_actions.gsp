<span class="action-menu">
	<button class="action-btn">
		<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>							
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
	</button>
	<div class="actions">
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="inventoryAdjustedBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'box.png')}" alt="${warehouse.message(code: 'inventory.inventoryAdjusted.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.inventoryAdjusted.label"/>
			</a>
		</div>	
		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="outgoingTransferBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="${warehouse.message(code: 'inventory.outgoingTransfer.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.outgoingTransfer.label"/>
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
			<a href="javascript:void(0);" class="actionButton" id="incomingTransferBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="${warehouse.message(code: 'inventory.incomingTransfer.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.incomingTransfer.label"/>
			</a>
		</div>
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="inventoryConsumedBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'cup.png')}" alt="${warehouse.message(code: 'inventory.inventoryConsumed.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.inventoryConsumed.label"/>
			</a>
		</div>	
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="inventoryDamagedBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}" alt="${warehouse.message(code: 'inventory.inventoryDamaged.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.inventoryDamaged.label"/>
			</a>
		</div>	
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="inventoryExpiredBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'hourglass.png')}" alt="${warehouse.message(code: 'inventory.inventoryExpired.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.inventoryExpired.label"/>
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
	</div>
</span>

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
		$("#addToShipmentBtn").click(function(event) { 
			$("#inventoryActionForm").attr("action", "${request.contextPath }/shipment/addToShipment").submit();
		});

	});
</script>