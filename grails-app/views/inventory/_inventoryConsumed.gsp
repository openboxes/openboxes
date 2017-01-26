<div class="box">
    <h2><warehouse:message code="inventory.consumption.label"/> <g:message code="transaction.new.label"/></h2>
	<g:form action="saveDebitTransaction">
		<g:hiddenField name="id" value="${command?.id}"/>
		<g:hiddenField name="inventory.id" value="${command?.inventory?.id}"/>
		<g:hiddenField name="transactionType.id" value="${command?.transactionType?.id }"/>
		<table>
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="transaction.type.label"/></label>
				</td>
				<td class="value">
					${format.metadata(obj: command?.transactionType)}
				</td>
			</tr>
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="transaction.date.label"/></label>
				</td>
				<td class="value">
                    <g:datePicker name="transactionDate" value="${command?.transactionDate}" precision="minute" noSelection="['':'']"/>
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
					<label><warehouse:message code="transaction.comment.label"/></label>
				</td>
				<td class="value">
					<span class="value">
						<g:textArea cols="120" rows="5" name="comment"
							value="${command?.comment }"></g:textArea>
					</span>								
				</td>
			</tr>				
			<tr class="prop">
                <td class="name">

                </td>
				<td class="value" style="padding: 0px;">
					<div>
						<table id="inventoryConsumedTable">
							<thead>
								<tr class="odd">
									<th><warehouse:message code="product.label"/></th>
                                    <th><warehouse:message code="product.unitOfMeasure.label"/></th>
									<th><warehouse:message code="product.lotNumber.label"/></th>
									<th><warehouse:message code="default.expires.label"/></th>
									<th><warehouse:message code="inventory.onHandQuantity.label"/></th>
									<th><warehouse:message code="inventory.consumed.label"/></th>
									<th><warehouse:message code="default.actions.label"/></th>
								</tr>
							</thead>
							<tbody>
								<g:set var="status" value="${0 }"/>
								<g:unless test="${command?.productInventoryItems}">
									<tr>
										<td colspan="6" class="center empty">
                                            <warehouse:message code="inventory.noItemsCurrentlyInStock.message" args="[format.product(product:command?.productInstance)]"/>

										</td>
									</tr>
								</g:unless>
								<g:each var="product" in="${command?.productInventoryItems?.keySet() }">
									<%-- Hidden field used to keep track of the products that were selected --%>
									<g:hiddenField name="product.id" value="${product?.id }"/>
									
									<%-- Display one row for every inventory item --%>
									<g:each var="inventoryItem" in="${command?.productInventoryItems[product]?.sort { it.expirationDate } }">
										<g:set var="onHandQuantity" value="${command?.quantityMap[inventoryItem] ?: 0}"/>									

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
                                                    ${onHandQuantity}
                                                </td>
												<td>
                                                    <g:hiddenField name="transactionEntries[${status }].inventoryItem.id" value="${inventoryItem?.id }"/>
                                                    <g:if test="${onHandQuantity > 0}">
                                                        <g:if test="${command?.transactionEntries }">
                                                            <g:textField name="transactionEntries[${status }].quantity"
                                                                value="${command?.transactionEntries[status]?.quantity }" size="10" autocomplete="off" />
                                                        </g:if>
                                                        <g:else>
                                                            <g:textField name="transactionEntries[${status }].quantity" class="text" size="10"
                                                                value="${command?.quantityMap[inventoryItem] }" autocomplete="off" />
                                                        </g:else>
                                                    </g:if>
                                                    <g:else>
                                                        0
                                                    </g:else>
												</td>
												<td>
													<img class="delete middle" src="${resource(dir:'images/icons/silk',file:'delete.png')}" alt="${warehouse.message(code: 'delete.label') }"/>
												</td>
											</tr>
											<g:set var="status" value="${status+1 }"/>										
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
		
		/**
		 * Delete a row from the table.
		 */		
		$("img.delete").livequery('click', function(event) { 
			$(this).closest('tr').fadeTo(400, 0, function () { 
		        $(this).remove();
		        renameRowFields($("#inventoryConsumedTable"));
		        alternateRowColors("#inventoryConsumedTable");				
		    });
		    return false;
		});			
	});
</script>


