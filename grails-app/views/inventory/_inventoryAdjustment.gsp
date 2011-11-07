<div class="left">
	<g:form action="saveInventoryAdjustment">
		<g:hiddenField name="id" value="${command?.transactionInstance?.id}"/>
		<g:hiddenField name="inventory.id" value="${command?.warehouseInstance?.inventory?.id}"/>
		<table>
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="transaction.status.label"/></label>
				</td>
				<td class="value">
					<span>
						<g:if test="${command?.transactionInstance?.id }">
							<warehouse:message code="enum.TransactionStatus.COMPLETE"/>
						</g:if>
						<g:else>
							<warehouse:message code="enum.TransactionStatus.PENDING"/>
						</g:else>
					</span>
				</td>
			</tr>
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="transaction.type.label"/></label>
				</td>
				<td class="value">
					<span >
						<g:if test="${command?.transactionInstance?.transactionType }">
							${format.metadata(obj:command?.transactionInstance?.transactionType)}
							<g:hiddenField name="transactionType.id" value="${command?.transactionInstance?.transactionType?.id }"/>
							
                       	</g:if>
                       	<g:else>
							<g:select id="transactionTypeSelector" name="transactionType.id" from="${command?.transactionTypeList}" 
	                       		optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${command?.transactionInstance.transactionType?.id}" noSelection="['': '']" />
						</g:else>
					</span>
				</td>
			</tr>
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="transaction.date.label"/></label>
				</td>
				<td class="value">
					<span>
						<g:jqueryDatePicker id="transactionDate" name="transactionDate"
								value="${command?.transactionInstance?.transactionDate}" format="MM/dd/yyyy"/>
					</span>								
				</td>
			</tr>	
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="transaction.comment.label"/></label>
				</td>
				<td class="value">
					<span class="value">
						<g:textArea cols="60" rows="3" name="comment" value="${command?.transactionInstance?.comment }"></g:textArea>

					</span>								
				</td>
			</tr>				
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="transaction.transactionEntries.label"/></label>
				</td>
				<td style="padding: 0px;">
					<div style="height:300px; overflow:auto;">
						<table>
							<thead>
								<tr class="odd">
									<th><warehouse:message code="product.label"/></th>
									<th><warehouse:message code="product.lotNumber.label"/></th>
									<th><warehouse:message code="default.expires.label"/></th>
									<th><warehouse:message code="inventory.onHandQuantity.label"/></th>
									<th><warehouse:message code="default.qty.label"/></th>
									
								</tr>
							</thead>
							<tbody>
							
								<g:set var="status" value="${0 }"/>
								<g:each var="product" in="${command?.productInventoryItems.keySet() }">
									<%-- Hidden field used to keep track of the products that were selected --%>
									<g:hiddenField name="product.id" value="${product?.id }"/>
									
									<%-- Display one row for every inventory item --%>
									<g:each var="inventoryItem" in="${command?.productInventoryItems[product] }">
										
										<g:hiddenField name="transactionEntries[${status }].inventoryItem.id" value="${inventoryItem?.id }"/>
										<tr>
											<td>${product?.name }</td>
											<td>${inventoryItem?.lotNumber }</td>
											<td>${inventoryItem?.expirationDate }</td>
											<td>${command?.quantityMap[inventoryItem]}</td>
											<td>
												<g:if test="${command?.transactionInstance?.transactionEntries }">
													<g:textField name="transactionEntries[${status }].quantity"
														value="${command?.transactionInstance?.transactionEntries[status]?.quantity }" size="1" autocomplete="off" />
												</g:if>
												<g:else>
													<g:textField name="transactionEntries[${status }].quantity"
														value="${command?.quantityMap[inventoryItem] }" size="1" autocomplete="off" />
												</g:else>
											</td>
										</tr>
										<g:set var="status" value="${status+1 }"/>										
									</g:each>
									<g:unless test="${command?.productInventoryItems[product] }">
										<tr>
											<td>
												${product?.name }
											</td>
											<td>
												
											</td>
											<td>
											
											</td>
											<td>
												0
											</td>
											<td>
												<input type="text" disabled="disabled" value="N/A" size="1"/>
											</td>
										</tr>
									</g:unless>
									
								</g:each>
							</tbody>
							<%-- 
							<tbody>
								<g:each var="transactionEntry" in="${transactionInstance?.transactionEntries}" status="i">
									<g:hiddenField name="transactionEntries[${i }].inventoryItem.id" value="${transactionEntry?.inventoryItem?.id }"/>
									<tr>
										<td>${transactionEntry?.inventoryItem?.product }</td>
										<td>${transactionEntry?.inventoryItem?.lotNumber }</td>
										<td>${transactionEntry?.inventoryItem?.expirationDate }</td>
										<td>${quantityMap[transactionEntry?.inventoryItem] }</td>
										<td><g:textField name="transactionEntries[${i }].quantity"
												value="${transactionEntry.quantity }" size="1" />
										</td>
									</tr>
								</g:each>
								<g:unless test="${!transactionInstance?.transactionEntries }">
									<tr class="empty">
										<td colspan="5" style="text-align: center; display:none;" id="noItemsRow">
											<span class="fade"><warehouse:message code="transaction.noItems.message"/></span>
										</td>
									</tr>
								</g:unless>
							</tbody>
							--%>
						</table>
					</div>	
				</td>
			</tr>		
			<tr class="prop">
				<td colspan="7">
					<div style="text-align: center;">
						<button type="submit" name="save">								
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'tick.png')}"/>&nbsp;<warehouse:message code="default.button.save.label"/>&nbsp;
						</button>
					</div>
				</td>
			</tr>
		</table>				
	</g:form>
</div>


