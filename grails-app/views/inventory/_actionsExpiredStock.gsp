
<div class="button-container">
	<div class="button-group">
		<a href="javascript:void(0);" class="button button-transaction"
		   id="outgoingTransferBtn"> <img
				src="${createLinkTo(dir:'images/icons/silk',file:'package_go.png')}"
				alt="${warehouse.message(code: 'inventory.outgoingTransfer.label') }"/> &nbsp;<g:message
				code="inventory.outgoingTransfer.label" />
		</a>

		<a href="javascript:void(0);" class="button button-transaction" id="inventoryExpiredBtn">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'hourglass.png')}" alt="${warehouse.message(code: 'inventory.inventoryExpired.label') }"/>
			&nbsp;<warehouse:message code="inventory.inventoryExpired.label"/>
		</a>

		<a href="javascript:void(0);" class="button button-transaction" id="inventoryConsumedBtn">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'pill.png')}" alt="${warehouse.message(code: 'inventory.inventoryConsumed.label') }" style="vertical-align: middle"/>
			&nbsp;<warehouse:message code="inventory.inventoryConsumed.label"/>
		</a>
	</div>

	<g:link params="[format:'csv',category:params.category]" controller="${controllerName}" action="${actionName}" class="button">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'disk.png')}" alt="${warehouse.message(code: 'default.button.download.label') }" style="vertical-align: middle"/>
		&nbsp; <g:message code="default.button.downloadAsCsv.label" default="Download as CSV"/></g:link>

</div>


<script>
	$(document).ready(function() {

		$(".button-transaction").click(function(event) {
		    var buttonId = $(this).attr("id")
		    var transactionTypeId;
		    switch (buttonId) {
				case "inventoryExpiredBtn":
				    transactionTypeId = 4;
					break;
                case "inventoryConsumedBtn":
                    transactionTypeId = 2;
                    break;
                case "outgoingTransferBtn":
                    transactionTypeId = 9;
                    break;
				default:
				    alert("Transaction type " + buttonId + " is not supported on this page");
				    break;
			}

            if ($("#inventoryActionForm input:checkbox:checked").length > 0) {
                $("#inventoryActionForm").append($("<input>", { type: "hidden", name: "transactionType.id", "value": transactionTypeId } )).submit();
			}
			else {
		        alert("${g.message(code:'inventory.selectAtLeastOneProduct.label')}");
			}
		});


    });
</script>
