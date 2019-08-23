

<g:form action="saveDebitTransaction">
	<div class="box">
        <h2><warehouse:message code="inventory.outgoingTransfer.label"/></h2>

		<g:hiddenField name="transactionInstance.id" value="${command?.transactionInstance?.id}"/>
		<g:hiddenField name="transactionInstance.inventory.id" value="${command?.warehouseInstance?.inventory?.id}"/>
		<g:hiddenField name="transactionInstance.transactionType.id" value="${command?.transactionInstance?.transactionType?.id }"/>							
		<table>
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="transaction.type.label"/></label>
				</td>
				<td class="value">
					<format:metadata obj="${command?.transactionInstance?.transactionType}"/>
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
					<label><warehouse:message code="transaction.source.label"/></label>
				</td>
				<td class="value">
					<span>
						${command?.warehouseInstance?.name}
					</span>								
				</td>
			</tr>	
			
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="transaction.destination.label"/></label>
				</td>
				<td class="value">
					<span>
						<g:selectTransactionDestination name="transactionInstance.destination.id" class="chzn-select-deselect"
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
						<g:textArea cols="120" rows="5" name="transactionInstance.comment" style="width:100%"
							value="${command?.transactionInstance?.comment }"></g:textArea>

					</span>								
				</td>
			</tr>
			<tr class="prop">

				<td class="name">
					<label><g:message code="transaction.transactionEntries.label"/></label>
				</td>
				<td style="padding: 0px;">
					<table id="outgoingTransfer">
						<thead>
						<tr class="odd">
							<th><warehouse:message code="product.label"/></th>
							<th><warehouse:message code="location.binLocation.label"/></th>
							<th><warehouse:message code="product.lotNumber.label"/></th>
							<th><warehouse:message code="default.expires.label"/></th>
							<th><warehouse:message code="default.unitOfMeasure.label"/></th>
							<th><warehouse:message code="inventory.onHandQuantity.label"/></th>
							<th><warehouse:message code="default.qty.label"/></th>
							<th><warehouse:message code="default.actions.label"/></th>
						</tr>
						</thead>
						<tbody>

						<g:set var="status" value="${0 }"/>

						<g:each var="entry" in="${command?.binLocations }">
						<%-- Used in case we need to render this view again on a validation error --%>
							<g:hiddenField name="product.id" value="${entry?.product?.id }"/>
							<g:set var="onHandQuantity" value="${command?.quantityMap[entry?.inventoryItem] ?: 0}"/>
							<g:if test="${true }">
								<tr class="row">
									<td>
										<span class="${onHandQuantity >0? '':'fade'}">
											${entry?.product?.productCode}
											<format:product product="${entry?.product }"/>
										</span>
									</td>
									<td>
										<span class="${onHandQuantity >0? '':'fade'}">
											${entry?.binLocation?.name }
										</span>
									</td>
									<td>
										<span class="${onHandQuantity >0? '':'fade'} lotNumber">
											${entry?.inventoryItem?.lotNumber }
										</span>
									</td>
									<td>
										<span class="${onHandQuantity >0? '':'fade'}">
											<format:date obj="${entry?.inventoryItem?.expirationDate }"/>
										</span>
									</td>
									<td>
										<span>
											${entry?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
										</span>
									</td>
									<td>
										<span class="${onHandQuantity >0? '':'fade'}">
											${onHandQuantity }
										</span>
									</td>
									<td>
										<g:if test="${onHandQuantity > 0 }">
											<g:hiddenField name="transactionEntries[${status }].binLocation.id" value="${entry?.binLocation?.id}"/>
											<g:hiddenField name="transactionEntries[${status }].inventoryItem.id" value="${entry?.inventoryItem?.id }"/>
											<g:if test="${command?.transactionInstance?.transactionEntries }">
												<g:textField name="transactionEntries[${status }].quantity"
															 value="${command?.transactionInstance?.transactionEntries[status]?.quantity }" size="10" autocomplete="off"  class="text" />
											</g:if>
											<g:else>
												<g:textField name="transactionEntries[${status }].quantity" class="text"
															 value="${0}" size="10" autocomplete="off" />
											</g:else>
											<g:set var="status" value="${status+1 }"/>

										</g:if>
										<g:else>
											<input type="text" class="text fade" disabled="disabled" value="N/A" size="1"/>
										</g:else>
									</td>
									<td class="center">
										<img class="delete" src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}"/>
									</td>
								</tr>
							</g:if>
						</g:each>
						<g:unless test="${command?.binLocations}">
							<tr>
								<td colspan="8" class="center empty fade">
									<g:message code="default.empty.label"/>
								</td>
							</tr>
						</g:unless>
						</tbody>
					</table>
				</td>
			</tr>
		</table>
	</div>


	<div class="buttons center">
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
