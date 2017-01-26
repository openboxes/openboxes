<div class="box">
    <h2><g:message code="inventory.outgoingTransfer.label"/></h2>
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
					<label><warehouse:message code="transaction.source.label"/></label>
				</td>
				<td class="value">
					${command?.location?.name}
				</td>
			</tr>	
			
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="transaction.destination.label"/></label>
				</td>
				<td class="value">
					<g:selectTransactionDestination name="destination.id" class="chzn-select-deselect"
						value="${command?.destination?.id}" noSelection="['null': '']"/>
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
					<%-- 
					<label><warehouse:message code="transaction.transactionEntries.label"/></label>
					--%>
				</td>
				<td style="padding: 0px;">
					<div>
						<table id="outgoingTransfer">
							<thead>
								<tr class="odd">
									<th><warehouse:message code="product.label"/></th>
									<th><warehouse:message code="product.lotNumber.label"/></th>
									<th><warehouse:message code="default.expires.label"/></th>
									<th><warehouse:message code="inventory.onHandQuantity.label"/></th>
									<th><warehouse:message code="default.qty.label"/></th>
									<th><warehouse:message code="default.actions.label"/></th>
								</tr>
							</thead>
							<tbody>
							
								<g:set var="status" value="${0 }"/>
								
								<g:unless test="${command?.productInventoryItems}">
									<tr>
										<td colspan="6" class="center">
											<!-- empty -->
										</td>
									</tr>
								</g:unless>
								<g:each var="product" in="${command?.productInventoryItems?.keySet() }">
									<%-- Hidden field used to keep track of the products that were selected --%>
									<g:hiddenField name="product.id" value="${product?.id }"/>
									
									<%-- Display one row for every inventory item --%>
									<g:each var="inventoryItem" in="${command?.productInventoryItems[product]?.sort { it.expirationDate } }">
										<g:set var="onHandQuantity" value="${command?.quantityMap[inventoryItem] ?: 0}"/>
										<g:if test="${onHandQuantity }">
											<tr class="row">
												<td>
													<span class="${onHandQuantity >0? '':'fade'}">
                                                        ${product?.productCode}
														<format:product product="${product }"/>
													</span>
												</td>
												<td>
													<span class="${onHandQuantity >0? '':'fade'} lotNumber">
														${inventoryItem?.lotNumber }
													</span>
												</td>
												<td>
													<span class="${onHandQuantity >0? '':'fade'}">
														<format:date obj="${inventoryItem?.expirationDate }"/>
													</span>
												</td>
												<td>
													<span class="${onHandQuantity >0? '':'fade'}">
														${onHandQuantity }
                                                        ${inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
													</span>
												</td>
												<td>
													<g:if test="${onHandQuantity > 0 }">
														<g:hiddenField name="transactionEntries[${status }].inventoryItem.id" value="${inventoryItem?.id }"/>									
														<g:if test="${command?.transactionEntries }">
															<g:textField name="transactionEntries[${status }].quantity"
																value="${command?.transactionEntries[status]?.quantity }" size="10" autocomplete="off"  class="text" />
                                                            ${inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }

                                                        </g:if>
														<g:else>
															<g:textField name="transactionEntries[${status}].quantity" class="text medium"
																value="" size="10" autocomplete="off" />
                                                            ${inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }

                                                        </g:else>
														<g:set var="status" value="${status+1}"/>
													</g:if>
													<g:else>
														<input type="text" class="fade" disabled="disabled" value="N/A" size="1"/>
													</g:else>
												</td>
												<td class="center">
													<img class="delete middle button" src="${resource(dir:'images/icons/silk',file:'delete.png')}"/>
												</td>
											</tr>
										</g:if>
									</g:each>
								</g:each>
							</tbody>
						</table>
					</div>	
				</td>
			</tr>		
			<tfoot>
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
			</tfoot>
		</table>				
	</g:form>
</div>
<script>
	$(document).ready(function() {
		alternateRowColors("#outgoingTransfer");

		/**
		 * Delete a row from the table.
		 */		
		$("img.delete").livequery('click', function(event) { 
			$(this).closest('tr').fadeTo(400, 0, function () { 
		        $(this).remove();
				renameRowFields($("#outgoingTransfer"));
				alternateRowColors("#outgoingTransfer");
		    });
		    return false;
		});			
				
	});
</script>
