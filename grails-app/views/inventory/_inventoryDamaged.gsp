<div class="box">
    <h2><warehouse:message code="inventory.damaged.label"/></h2>

    <g:form action="saveDebitTransaction">
		<g:hiddenField name="transactionInstance.id" value="${command?.transactionInstance?.id}"/>
		<g:hiddenField name="transactionInstance.inventory.id" value="${command?.warehouseInstance?.inventory?.id}"/>
		<g:hiddenField name="transactionInstance.transactionType.id" value="${command?.transactionInstance?.transactionType?.id }"/>
		<table>
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
						<g:textArea cols="120" rows="5" name="transactionInstance.comment"
							value="${command?.transactionInstance?.comment }"></g:textArea>
					</span>
				</td>
			</tr>
			<tr class="prop">
				<td style="padding: 0px;" colspan="2">
					<div>
						<table id="inventoryDamagedTable">
							<thead>
								<tr class="odd">
									<th><warehouse:message code="product.label"/></th>
                                    <th><warehouse:message code="product.unitOfMeasure.label"/></th>
									<th><warehouse:message code="product.lotNumber.label"/></th>
									<th><warehouse:message code="default.expires.label"/></th>
									<th><warehouse:message code="inventory.onHandQuantity.label"/></th>
									<th><warehouse:message code="inventory.damaged.label"/></th>
									<th><warehouse:message code="default.actions.label"/></th>
								</tr>
							</thead>
							<tbody>
								<g:set var="status" value="${0 }"/>
								<g:each var="product" in="${command?.productInventoryItems.keySet() }">
									<%-- Hidden field used to keep track of the products that were selected --%>
									<g:hiddenField name="product.id" value="${product?.id }"/>

									<%-- Display one row for every inventory item --%>
									<g:each var="inventoryItem" in="${command?.productInventoryItems[product]?.sort { it.expirationDate } }">

										<g:set var="onHandQuantity" value="${command?.quantityMap[inventoryItem] ?: 0}"/>
										<g:if test="${onHandQuantity > 0 }">
											<tr>
												<td>
                                                    ${product?.productCode}
                                                    <format:product product="${product }"/>
												</td>
                                                <td>
                                                    ${product?.unitOfMeasure }
                                                </td>
												<td>
													${inventoryItem?.lotNumber }
												</td>
												<td>
													<format:date obj="${inventoryItem?.expirationDate }" format="d MMM yyyy"/>
												</td>
												<td>
													${command?.quantityMap[inventoryItem]}
												</td>
												<td>
													<g:hiddenField name="transactionEntries[${status }].inventoryItem.id" value="${inventoryItem?.id }"/>
													<g:if test="${command?.transactionInstance?.transactionEntries }">
														<g:textField name="transactionEntries[${status }].quantity" class="text"
															value="${command?.transactionInstance?.transactionEntries[status]?.quantity }" size="10" autocomplete="off" />
													</g:if>
													<g:else>
														<g:textField name="transactionEntries[${status }].quantity" class="text medium"
															value="${command?.quantityMap[inventoryItem] }" size="10" autocomplete="off" />
													</g:else>
												</td>
												<td>
													<img class="delete middle" src="${resource(dir:'images/icons/silk',file:'delete.png')}" alt="${warehouse.message(code: 'delete.label') }"/>
												</td>

											</tr>
											<g:set var="status" value="${status+1 }"/>
										</g:if>
									</g:each>


								</g:each>
							</tbody>
						</table>
					</div>
				</td>
			</tr>
			<tr class="prop">
				<td colspan="7">
					<div class="center">
						<button type="submit" name="save" class="button icon approve">
							<warehouse:message code="default.button.save.label"/>&nbsp;
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
		alternateRowColors("#inventoryDamagedTable");

		/**
		 * Delete a row from the table.
		 */
		$("img.delete").livequery('click', function(event) {
			$(this).closest('tr').fadeTo(400, 0, function () {
		        $(this).remove();
				renameRowFields($("#inventoryDamagedTable"));
				alternateRowColors("#inventoryDamagedTable");
		    });
		    return false;
		});
	});
</script>



