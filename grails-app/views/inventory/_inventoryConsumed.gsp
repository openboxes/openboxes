<div class="box">
    <h2>
        <g:message code="default.create.label" args="[g.message(code: 'transaction.label')]"/>
    </h2>
	<g:form action="saveDebitTransaction">
		<g:hiddenField name="transactionInstance.id" value="${command?.transactionInstance?.id}"/>
		<g:hiddenField name="transactionInstance.inventory.id" value="${command?.warehouseInstance?.inventory?.id}"/>
		<table>
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
					<label><warehouse:message code="default.date.label"/></label>
				</td>
				<td class="value">
                    <g:datePicker name="transactionInstance.transactionDate" value="${command?.transactionInstance?.transactionDate}" precision="minute" noSelection="['':'']"/>

                </td>
			</tr>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="transaction.transactionType.label"/></label>
                </td>
                <td class="value">
                    <g:selectTransactionType name="transactionInstance.transactionType.id"
                                             value="${command?.transactionInstance?.transactionType?.id}"
                                             noSelection="['':'']"
                                             transactionCode="${org.pih.warehouse.inventory.TransactionCode.DEBIT}"
                                             class="chzn-select-deselect"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label class="fade"><warehouse:message code="transaction.destination.label"/></label>
                </td>
                <td class="value">
                    <g:selectTransactionDestination name="destination.id" noSelection="['':'']" class="chzn-select-deselect"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="transaction.createdBy.label"/></label>
                </td>
                <td class="value">
                    <g:selectUser name="createdBy.id" value="${session?.user?.id}" class="chzn-select-deselect"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label class="fade"><warehouse:message code="transaction.comment.label"/></label>
                </td>
                <td class="value">
                    <span class="value">
                        <g:textArea cols="120" rows="2" name="transactionInstance.comment"
                                    value="${command?.transactionInstance?.comment }" style="width:100%"></g:textArea>

                    </span>
                </td>
            </tr>
			<tr class="prop">
				<td class="name">
					<label><g:message code="transaction.transactionEntries.label"/></label>
				</td>
				<td style="padding: 0px;">
					<div class="list">
						<table id="inventoryConsumedTable">
							<thead>
								<tr class="odd">
									<th><warehouse:message code="product.productCode.label"/></th>
                                    <th><warehouse:message code="product.label"/></th>
									<th><warehouse:message code="location.binLocation.label"/></th>
									<th><warehouse:message code="product.lotNumber.label"/></th>
									<th><warehouse:message code="default.expires.label"/></th>
									<th><warehouse:message code="default.quantityOnHand.label"/></th>
									<th><g:message code="default.quantityToDebit.label"/></th>
									<th class="center middle">

                                        <img data-id="all" class="plus action" src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" title="${g.message(code: 'default.button.increment.label') }"/>
                                        &nbsp;
                                        <img data-id="all" class="minus action" src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" title="${g.message(code: 'default.button.decrement.label') }"/>
                                        &nbsp;
                                        <img data-id="all" class="reset action" src="${createLinkTo(dir:'images/icons/silk',file:'reload.png')}" title="${g.message(code: 'default.button.reset.label', default: 'Reset') }"/>
                                        &nbsp;
                                        <img data-id="all" class="max action" src="${createLinkTo(dir:'images/icons/silk',file:'asterisk_orange.png')}" title="${g.message(code: 'default.button.all.label', default: 'All') }"/>

                                    </th>
								</tr>
							</thead>
							<tbody>
								<g:unless test="${command?.binLocations}">
									<tr>
										<td colspan="10" class="center empty">
                                            <warehouse:message code="inventory.noItemsCurrentlyInStock.message" />
										</td>
									</tr>
								</g:unless>
								<g:each var="entry" in="${command?.binLocations }" status="status">
									<%-- Hidden field used to keep track of the products that were selected --%>
                                    <g:hiddenField name="product.id" value="${entry?.product?.id }"/>
                                    <g:hiddenField name="transactionEntries[${status }].binLocation.id" value="${entry?.binLocation?.id }" />
                                    <g:hiddenField name="transactionEntries[${status }].inventoryItem.id" value="${entry?.inventoryItem?.id }"/>

                                    <%-- Display one row for every bin location / inventory item --%>
									<g:set var="onHandQuantity" value="${entry?.quantity ?: 0}"/>
									<tr>
                                        <td>
                                            ${entry?.product?.productCode}
                                        </td>
										<td>
											<format:product product="${entry?.product }"/>
										</td>
										<td>
											${entry?.binLocation?.name}
										</td>

										<td>
											${entry?.inventoryItem?.lotNumber }
										</td>
										<td>
                                            <g:expirationDate date="${entry?.inventoryItem?.expirationDate}" format="d MMM yyyy"/>
										</td>
										<td>
											${onHandQuantity?:0} ${entry?.product?.unitOfMeasure }
										</td>
										<td>
                                            <g:hiddenField id="oldQuantity-${status}" name="transactionEntries[${status }].oldQuantity" value="${entry?.quantity?:0 }" />
											<g:if test="${onHandQuantity > 0}">
												<g:if test="${command?.transactionInstance?.transactionEntries }">
													<g:textField id="newQuantity-${status}" name="transactionEntries[${status }].quantity"
														value="${command?.transactionInstance?.transactionEntries[status]?.quantity }" size="10" autocomplete="off" class="text"/>
												</g:if>
												<g:else>
													<g:textField id="newQuantity-${status}" name="transactionEntries[${status }].quantity" class="text" size="10"
														value="${0 }" autocomplete="off" />
												</g:else>
											</g:if>
											<g:else>
												0
											</g:else>
                                        </td>
                                        <td class="center middle">
                                            <img data-id="${status}" class="plus action" src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" title="${g.message(code: 'default.button.increment.label') }"/>
                                            &nbsp;
                                            <img data-id="${status}" class="minus action" src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" title="${g.message(code: 'default.button.decrement.label') }"/>
                                            &nbsp;
                                            <img data-id="${status}" class="reset action" src="${createLinkTo(dir:'images/icons/silk',file:'reload.png')}" title="${g.message(code: 'default.button.reset.label', default: 'Reset') }"/>
                                            &nbsp;
                                            <img data-id="${status}" class="max action" src="${createLinkTo(dir:'images/icons/silk',file:'asterisk_orange.png')}" title="${g.message(code: 'default.button.all.label', default: 'All') }"/>
										</td>
									</tr>

								</g:each>
							</tbody>

						</table>
					</div>
				</td>
			</tr>
			<tr class="prop">
				<td></td>
				<td>
					<div class="left">
						<button type="submit" name="save" class="button icon approve">
							<warehouse:message code="default.button.save.label"/>
						</button>
						&nbsp;
						<g:link controller="inventory" action="browse" class="button icon trash">
							${warehouse.message(code: 'default.button.cancel.label')}
						</g:link>
					</div>
				</td>
			</tr>
		</table>
	</g:form>
</div>


<script>

	$(document).ready(function() {
		alternateRowColors("#inventoryConsumedTable");

        $(".plus").click(function(event) {
            event.preventDefault();
            var id = $(this).data("id");
            if (id == "all") {
                $(".plus").each(function(index, element) {
                    changeQuantity($(this).data("id"), +1);
                });
            }
            else {
                changeQuantity($(this).data("id"), +1);
            }
        });

        $(".minus").click(function(event) {
            event.preventDefault();
            var id = $(this).data("id");
            if (id == "all") {
                $(".plus").each(function(index, element) {
                    changeQuantity($(this).data("id"), -1);
                });
            }
            else {
                changeQuantity($(this).data("id"), -1);
            }
        });

        $(".max").click(function(event) {
            event.preventDefault();
            var id = $(this).data("id");
            if (id == "all") {
                $(".plus").each(function(index, element) {
                    reloadQuantity($(this).data("id"));
                });
            }
            else {
                reloadQuantity($(this).data("id"));
            }
        });

        $(".reset").click(function(event) {
            event.preventDefault();
            var id = $(this).data("id");
            if (id == "all") {
                $(".plus").each(function(index, element) {
                    resetQuantity($(this).data("id"));
                });
            }
            else {
                resetQuantity($(this).data("id"));
            }
        });

		/**
		 * Delete a row from the table.
		 */
		$(".delete").click(function(event) {
			$(this).closest('tr').fadeTo(400, 0, function () {
		        $(this).remove();
		        renameRowFields($("#inventoryConsumedTable"));
		        alternateRowColors("#inventoryConsumedTable");
		    });
		    return false;
		});
	});

    function reloadQuantity(id) {
        var oldQuantity = parseInt($("#oldQuantity-" + id).val(), 10);
        var quantityField = $("#newQuantity-" + id);
        quantityField.val(oldQuantity);
        animate(quantityField);
    }

    function resetQuantity(id) {
        var quantityField = $("#newQuantity-" + id);
        quantityField.val(0);
        animate(quantityField);
    }

    function changeQuantity(id, delta) {
        var quantityField = $("#newQuantity-" + id);
        var currentQuantity = parseInt(quantityField.val(), 10);
        animate(quantityField);
        quantityField.val(currentQuantity+delta);
    }

    function animate(field) {
        field.fadeTo(100, 0.3, function() { $(this).fadeTo(500, 1.0); });
    }




</script>


