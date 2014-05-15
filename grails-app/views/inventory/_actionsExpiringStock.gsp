<div class="action-menu" style="padding: 1px;">
	<button class="action-btn">
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
	</button>
	<div class="actions">
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="inventoryExpiredBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'hourglass.png')}" alt="${warehouse.message(code: 'inventory.inventoryExpired.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.inventoryExpired.label"/>
			</a>
		</div>	
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="inventoryConsumedBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'cup.png')}" alt="${warehouse.message(code: 'inventory.inventoryConsumed.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.inventoryConsumed.label"/>
			</a>
		</div>	
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="outgoingTransferBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="${warehouse.message(code: 'inventory.outgoingTransfer.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.outgoingTransfer.label"/>
			</a>
		</div>	
		
		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">														
			<g:link controller="inventory" action="browse">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="${warehouse.message(code: 'inventory.browse.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.browse.label"/>
			</g:link>
		</div>		
	</div>
</div>

<script>
	$(document).ready(function() {
		
		// Form Actions 
		$("#outgoingTransferBtn").click(function(event) { 
			$("#inventoryActionForm").append($("<input>", {type: "hidden", name: "transactionType.id", "value": "9"})).submit();
		});
		$("#inventoryExpiredBtn").click(function(event) { 
			$("#inventoryActionForm").append($("<input>", {type: "hidden", name: "transactionType.id", "value": "4"})).submit();
		});
		$("#inventoryConsumedBtn").click(function(event) { 
			$("#inventoryActionForm").append($("<input>", {type: "hidden", name: "transactionType.id", "value": "2"})).submit();
		});

	});
</script>