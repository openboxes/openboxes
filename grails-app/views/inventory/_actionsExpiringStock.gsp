<div class="button-container">

	<g:link controller="inventory" action="browse" class="button">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="${warehouse.message(code: 'inventory.browse.label') }" style="vertical-align: middle"/>
		&nbsp;<warehouse:message code="inventory.browse.label"/>
	</g:link>

	<div class="button-group">

		<a href="javascript:void(0);" class="button" id="inventoryExpiredBtn">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'hourglass.png')}" alt="${warehouse.message(code: 'inventory.inventoryExpired.label') }" style="vertical-align: middle"/>
			&nbsp;<warehouse:message code="inventory.inventoryExpired.label"/>
		</a>

		<a href="javascript:void(0);" class="button" id="inventoryConsumedBtn">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'cup.png')}" alt="${warehouse.message(code: 'inventory.inventoryConsumed.label') }" style="vertical-align: middle"/>
			&nbsp;<warehouse:message code="inventory.inventoryConsumed.label"/>
		</a>

		<a href="javascript:void(0);" class="button" id="outgoingTransferBtn">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="${warehouse.message(code: 'inventory.outgoingTransfer.label') }" style="vertical-align: middle"/>
			&nbsp;<warehouse:message code="inventory.outgoingTransfer.label"/>
		</a>

	</div>

	<g:link params="[format:'csv',threshold:params.threshold,category:params.category]" controller="${controllerName}" action="${actionName}"
			class="button">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'disk.png')}" />
		&nbsp;<g:message code="default.button.download.label"/></g:link>

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