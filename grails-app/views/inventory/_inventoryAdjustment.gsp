
<g:form action="saveAdjustmentTransaction">
    <div class="box">
        <h2><g:message code="inventory.adjustStock.label"/></h2>
        <g:hiddenField name="transactionInstance.id" value="${command?.transactionInstance?.id}"/>
        <g:hiddenField name="transactionInstance.inventory.id" value="${command?.warehouseInstance?.inventory?.id}"/>
        <g:hiddenField name="transactionInstance.transactionType.id" value="${command?.transactionInstance?.transactionType?.id }"/>
        <table>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="transaction.transactionType.label"/></label>
                </td>
                <td class="value">
                    <format:metadata obj="${command?.transactionInstance?.transactionType?.name}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="transaction.inventory.label"/></label>
                </td>
                <td class="value">
                    ${session.warehouse.name}
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="transaction.date.label"/></label>
                </td>
                <td class="value">
                    <g:datePicker name="transactionInstance.transactionDate" value="${command?.transactionInstance?.transactionDate}" precision="minute" noSelection="['':'']"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="transaction.comment.label"/></label>
                </td>
                <td class="value">
                    <span class="value">
                        <g:textArea cols="120" rows="2" name="transactionInstance.comment" value="${command?.transactionInstance?.comment }" style="width:100%"></g:textArea>

                    </span>
                </td>
            </tr>
        </table>
    </div>
    <div class="box">
        <h2><g:message code="transaction.transactionEntries.label"/></h2>
        <table id="adjustStockTable">
            <thead>
            <tr class="odd">
                <th><g:message code="product.label"/></th>
                <th><g:message code="location.binLocation.label"/></th>
                <th><g:message code="inventoryItem.lotNumber.label"/></th>
                <th><g:message code="inventoryItem.expirationDate.label"/></th>
                <th><g:message code="inventory.onHandQuantity.label"/></th>
                <th><g:message code="inventory.newQuantity.label"/></th>
                <th><g:message code="default.quantityDiff.label" default="Quantity Diff"/></th>
                <th><g:message code="default.actions.label"/></th>
                <th><g:message code="default.reasonCode.label" default="Reason Code"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each var="entry" in="${command?.binLocations }" status="status">
                <g:hiddenField name="product.id" value="${entry?.product?.id }"/>
                <g:hiddenField name="transactionEntries[${status }].binLocation.id" value="${entry?.binLocation?.id }" />
                <g:hiddenField name="transactionEntries[${status }].inventoryItem.id" value="${entry?.inventoryItem?.id }"/>
                <tr class="row ${status%2==0?'odd':'even'}">
                    <td class="middle">
                        <format:product product="${entry?.product }"/>
                    </td>
                    <td class="middle">
                        <g:if test="${entry?.binLocation}">
                            ${entry?.binLocation?.name }
                        </g:if>
                        <g:else>
                            <g:message code="default.label"/>
                        </g:else>
                    </td>
                    <td class="middle">
                        ${entry?.inventoryItem?.lotNumber }
                    </td>
                    <td class="middle">
                        <format:date obj="${entry?.inventoryItem?.expirationDate }" format="d MMM yyyy"/>
                    </td>
                    <td class="middle">
                        ${entry?.quantity?:0}
                    </td>
                    <td class="middle">
                        <g:hiddenField id="oldQuantity-${status}"
                                       name="transactionEntries[${status }].oldQuantity"
                                       value="${entry?.quantity?:0 }" />
                        <g:textField id="newQuantity-${status}"
                                     data-id="${status}"
                                     name="transactionEntries[${status }].newQuantity"
                                     value="${entry?.quantity?:0 }"
                                     autocomplete="off" size="10"
                                     style="text-align: right"
                                     class="text newQuantity" />
                    </td>
                    <td class="middle">
                        <g:textField id="adjustedQuantity-${status}"
                                     class="text" size="10"
                                     readonly="readonly"
                                     name="transactionEntries[${status }].quantity" value="${0}"/>
                    </td>
                    <td class="middle">
                        <div class="button-group">
                            <img data-id="${status}" class="add"
                                 src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}"
                                 alt="${g.message(code: 'default.button.increment.label') }"/>
                            <img data-id="${status}" class="minus"
                                 src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}"
                                 alt="${g.message(code: 'default.button.decrement.label') }"/>
                            <img data-id="${status}" class="reset"
                                 src="${createLinkTo(dir:'images/icons/silk',file:'arrow_undo.png')}"
                                 alt="${g.message(code: 'default.button.reset.label') }"/>
                        </div>
                    </td>
                    <td class="middle">
                        <span class="chzn-container" style="width:100px;">
                            <g:selectInventoryAdjustmentReasonCode
                                    data-id="${status}"
                                    name="transactionEntries[${status }].reasonCode"
                                    noSelection="['':'']"
                                    class="reason-code"/>
                        </div>
                        <g:textField name="transactionEntries[${status }].comment"
                                     id="comments-${status}"
                                     data-id="${status}"
                                     placeholder="${g.message(code:'default.comments.label', default: 'Comments')}"
                                     value="" autocomplete="off" class="text comments" size="30" />
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>


    <div class="center buttons">
        <button type="submit" name="save" class="button icon approve">
            <warehouse:message code="default.button.save.label"/>
        </button>
        &nbsp;
        <g:link controller="inventory" action="browse" class="button icon trash">
            ${warehouse.message(code: 'default.button.cancel.label')}
        </g:link>

    </div>
</g:form>


<script>
	$(document).ready(function() {

		alternateRowColors("#adjustStockTable");

		/**
		 * Delete a row from the table.
		 */		
		$(".delete").livequery('click', function(event) {
			$(this).closest('tr').fadeTo(400, 0, function () { 
		        $(this).remove();
				renameRowFields($("#adjustStockTable"));
				alternateRowColors("#adjustStockTable");
		    });
		    return false;
		});

        $(".add").livequery('click', function(event) {
            event.preventDefault();
            changeQuantity($(this).data("id"), +1);
        });

        $(".minus").livequery('click', function(event) {
            event.preventDefault();
            changeQuantity($(this).data("id"), -1);
        });

        $(".reset").livequery('click', function(event) {
            event.preventDefault();
            resetQuantity($(this).data("id"));
            $("#")
        });

        $(".newQuantity").livequery("input", function(event){
            updateDiffQuantity($(this).data("id"))
        });
    });

	function resetQuantity(id) {
        var oldQuantity = parseInt($("#oldQuantity-" + id).val(), 10);
        $("#newQuantity-" + id).val(oldQuantity);
	    $("#adjustedQuantity-" + id).val(0);
    }

    function updateDiffQuantity(id) {
	    var oldQuantity = parseInt($("#oldQuantity-" + id).val(), 10);
        var currentQuantity = parseInt($("#newQuantity-" + id).val(), 10);
        var adjustedQuantity = currentQuantity - oldQuantity;
	    $("#adjustedQuantity-" + id).val(adjustedQuantity);
    }

	function changeQuantity(id, delta) {
	    var oldQuantity = parseInt($("#oldQuantity-" + id).val(), 10);
        var currentQuantity = parseInt($("#newQuantity-" + id).val(), 10);
        $("#newQuantity-" + id).val(currentQuantity+delta);
        updateDiffQuantity(id);
	}

</script>

