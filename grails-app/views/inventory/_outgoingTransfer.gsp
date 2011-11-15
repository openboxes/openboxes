<div class="left">
	<g:form action="saveOutgoingTransfer">
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
					<span id="sourceSection" class="prop-multi">
						<label><warehouse:message code="default.from.label"/></label>
						<span>
							${command?.warehouseInstance?.name }						
							<%-- transfer out does not specify source id --%>
							<%-- 
							<g:if test="${command?.transactionInstance?.source }">
								${command?.transactionInstance?.source?.name }
								<g:hiddenField name="source.id" value="${transactionInstance?.source?.id }"/>
	                       	</g:if>
	                       	<g:else>
								<g:select name="source.id" from="${command?.locationList}" 
		                       		optionKey="id" optionValue="name" value="${command?.transactionInstance?.source?.id}" noSelection="['': '']" />
		           			</g:else>
							--%>
                     	</span>
                     </span>
                   	<span id="destinationSection" class="prop-multi">
						<label><warehouse:message code="default.to.label"/></label>
						<span>
							<%-- 
							<g:select name="destination.id" from="${command?.locationList}" 
	                       		optionKey="id" optionValue="name" value="${command?.transactionInstance?.destination?.id}" noSelection="['': '']" />
							--%>
							<g:selectTransactionDestination name="destination.id"
								value="${command?.transactionInstance?.destination?.id}" noSelection="['null': '']"/>

						</span>
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
					<%-- 
					<label><warehouse:message code="transaction.transactionEntries.label"/></label>
					--%>
				</td>
				<td style="padding: 0px;">
					<div>
						<table class="tableScroll">
							<thead>
								<tr class="odd">
									<th><warehouse:message code="product.label"/></th>
									<th><warehouse:message code="product.lotNumber.label"/></th>
									<th><warehouse:message code="default.expires.label"/></th>
									<th><warehouse:message code="inventory.onHandQuantity.label"/></th>
									<th><warehouse:message code="default.qty.label"/></th>
									<th></th>
								</tr>
							</thead>
							<tbody>
							
								<g:set var="status" value="${0 }"/>
								<g:each var="product" in="${command?.productInventoryItems.keySet() }">
									<%-- Hidden field used to keep track of the products that were selected --%>
									<g:hiddenField name="product.id" value="${product?.id }"/>
									
									<%-- Display one row for every inventory item --%>
									<g:each var="inventoryItem" in="${command?.productInventoryItems[product] }">
										
										<g:set var="onHandQuantity" value="${command?.quantityMap[inventoryItem] ?: 0}"/>
										<tr>
											<td>${product?.name }</td>
											<td>${inventoryItem?.lotNumber }</td>
											<td><format:date obj="${inventoryItem?.expirationDate }"/></td>
											<td>${onHandQuantity }</td>
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
													<input type="text" disabled="disabled" value="N/A" size="1"/>
												</g:else>
											</td>
											<td>
											
											</td>
										</tr>
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
											<td>
											
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
			<tfoot>
				<tr class="prop">
					<td colspan="7">
						<div style="text-align: center;">
							<button type="submit" name="save">								
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'tick.png')}"/>&nbsp;<warehouse:message code="default.button.save.label"/>&nbsp;
							</button>
						</div>
					</td>
				</tr>
			</tfoot>
		</table>				
	</g:form>
</div>
<script>
	$(document).ready(function() {
		$('.tableScroll').tableScroll({height:300, width: 800});
	});
</script>
