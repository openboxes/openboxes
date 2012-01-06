<div class="left">
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
					<span>
						<g:jqueryDatePicker id="transactionDate" name="transactionInstance.transactionDate"
								value="${command?.transactionInstance?.transactionDate}" format="MM/dd/yyyy"/>
					</span>								
				</td>
			</tr>	
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="default.to.label"/></label>
				</td>
				<td class="value">
					<span>
						<g:selectTransactionDestination name="transactionInstance.destination.id"
							value="${command?.transactionInstance?.destination?.id}" noSelection="['null': '']"/>

					</span>
				</td>
			</tr>
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="transaction.comment.label"/></label>
				</td>
				<td class="value">
					<span class="value">
						<g:textArea cols="80" rows="1" name="transactionInstance.comment" 
							value="${command?.transactionInstance?.comment }"></g:textArea>

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
													<span class="${onHandQuantity >0? '':'fade'}"><format:product product="${product }"/></span>
												</td>
												<td>
													<span class="${onHandQuantity >0? '':'fade'}">${inventoryItem?.lotNumber }</span>
												</td>
												<td>
													<span class="${onHandQuantity >0? '':'fade'}"><format:date obj="${inventoryItem?.expirationDate }"/></span>
												</td>
												<td>
													<span class="${onHandQuantity >0? '':'fade'}">${onHandQuantity }</span>
												</td>
												<td>
													<g:if test="${onHandQuantity > 0 }">
														<g:hiddenField name="transactionEntries[${status }].inventoryItem.id" value="${inventoryItem?.id }"/>									
														<g:if test="${command?.transactionInstance?.transactionEntries }">
															<g:textField name="transactionEntries[${status }].quantity"
																value="${command?.transactionInstance?.transactionEntries[status]?.quantity }" size="1" autocomplete="off" />
														</g:if>
														<g:else>
															<g:textField name="transactionEntries[${status }].quantity"
																value="" size="1" autocomplete="off" />
														</g:else>
														<g:set var="status" value="${status+1 }"/>										
														
													</g:if>
													<g:else>
														<input type="text" class="fade" disabled="disabled" value="N/A" size="1"/>
													</g:else>
												</td>
												<td class="center">
													<img class="delete middle" src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="${warehouse.message(code: 'delete.label') }"/>	
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
							<button type="submit" name="save">
								<warehouse:message code="default.button.save.label"/>
							</button>
							&nbsp;
							<g:link controller="inventory" action="browse">${warehouse.message(code: 'default.button.back.label')}</g:link>
						</div>
					</td>
				</tr>
			</tfoot>
		</table>				
	</g:form>
</div>
<script>
	$(document).ready(function() {
		//$('.tableScroll').tableScroll({height:300, width: 800});

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
