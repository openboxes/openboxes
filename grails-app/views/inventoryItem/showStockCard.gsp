
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<g:set var="entityName"
			value="${message(code: 'stockCard.label', default: 'Stock Card')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="body">
	
			<div class="nav">
				<g:render template="../inventory/nav"/>
			</div>
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if> 
			<g:hasErrors bean="${commandInstance}">
				<div class="errors"><g:renderErrors bean="${commandInstance}" as="list" /></div>
			</g:hasErrors>
			
			
			<div class="dialog">		
			
				<div style="float: left; margin-left: 15px;">	
					<span>		
						<img src="${resource(dir: 'images/icons/silk', file: 'table_refresh.png')}"/>
						<g:link controller="inventory" action="browse" >
							Back to Inventory
						</g:link>
					</span>
				</div>
				
				<div style="float: right; margin-right: 15px;">					
					
					<label>Actions:</label>
					<span style="margin-right: 15px;">
						<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
						<g:link controller="inventoryItem" action="showRecordInventory" params="['product.id':commandInstance?.productInstance?.id,'inventory.id':commandInstance?.inventoryInstance?.id]">
							Record inventory
						</g:link>
					</span>
					<%-- 
					<img src="${resource(dir: 'images/icons/silk', file: 'magnifier.png')}"/>
					<g:link controller="inventoryItem" action="showTransactions" params="['product.id':productInstance?.id,'inventory.id':inventoryInstance?.id]">
						Show changes
					</g:link>
					--%>
					<span>
						<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}"/>
						<g:link controller="inventoryItem" action="createInventoryItem" params="['product.id':commandInstance?.productInstance?.id,'inventory.id':commandInstance?.inventoryInstance?.id]">
							Add item to inventory
						</g:link>
					</span>	
				</div>
				
				
				<table>
					<tr>
						<td style="width: 250px;">
							<g:render template="productDetails" model="[productInstance:commandInstance?.productInstance]"/>											
						</td>
						<td>			
						
							<div> 					
								<fieldset>	
									<legend class="fade">Current Stock</legend>																
									<div id="inventoryView" style="text-align: right;">										
										<table border="0" style="border:1px solid #f5f5f5" width="100%">
											<thead>
												<tr class="odd">
													<th>ID</th>
													<th>Description</th>
													<th>Lot Number</th>
													<th>Expires</th>
													<th style="text-align: center;" >Qty</th>
												</tr>											
											</thead>
											<tbody>
												<g:if test="${!commandInstance?.inventoryItemList}">
													<tr class="even">
														<td colspan="4">
															No current stock 
														</td>
													</tr>
												</g:if>
											
												<g:each var="itemInstance" in="${commandInstance?.inventoryItemList }" status="status">				
													<tr class="${(status%2==0)?'even':'odd' }">
														<td class="fade">${itemInstance?.id}</td>
														<td>${itemInstance?.description}</td>
														<td>${itemInstance?.lotNumber?:'<span class="fade">EMPTY</span>' }</td>
														<td>
															<g:if test="${itemInstance?.expirationDate}">
																<g:formatDate date="${itemInstance?.expirationDate }" format="dd/MMM/yy" />
															</g:if>
															<g:else>
																<span class="fade">never</span>
															</g:else>
														</td>
														<td style="text-align: center;">
															<g:set var="transactionEntries" value="${commandInstance?.transactionEntriesByInventoryItem?.get(itemInstance)}"/>
															
															${transactionEntries?transactionEntries*.quantity.sum():0}	
														</td>
													</tr>
												</g:each>
											</tbody>
											<tfoot>
												<tr style="font-size: 1.5em; line-height: 2em;">
													<th colspan="4" style="text-align: right;"></th>
													<th class="large" style="text-align: center;">
														${(commandInstance?.transactionEntryList)?commandInstance?.transactionEntryList*.quantity.sum():0 }</th>
												</tr>
											</tfoot>
										</table>										
									</div>			
								</fieldset>
							</div>
							<br/>						
							<div>							
								<fieldset>
									<legend><span class="fade">Transaction Log</span></legend>
									<div style="padding: 10px;">
										<g:form method="GET" action="showStockCard">
											<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id }"/>
											<label>Filter transactions: </label>
											<span>
												<g:jqueryDatePicker 
													id="startDate" 
													name="startDate" 
													value="${commandInstance?.startDate }" 
													format="MM/dd/yyyy"
													showTrigger="false" />
											</span>
											<span> - </span>
											<span> 
												<g:jqueryDatePicker 
													id="endDate" 
													name="endDate" 
													value="${commandInstance?.endDate }" 
													format="MM/dd/yyyy"
													showTrigger="false" />
											</span>
											<span>
												<g:select name="transactionType.id" 
													from="${org.pih.warehouse.inventory.TransactionType.list()}" 
													optionKey="id" optionValue="name" value="${commandInstance?.transactionType?.id }" 
													noSelection="['0': '-- All types --']" /> 
											</span>
											<span class="buttons">
												<g:submitButton name="filter" value="Go"/>
											</span>
										</g:form>
									</div>		
									<script>
										jQuery(document).ready(function() {
											jQuery("tr.transactionEntry").hide();

											jQuery(".toggleDetails").click(function() {
												jQuery("tr.transaction" + this.id).toggle();								
											});
										});	
									</script>
									<table width="100%">
										<thead>
											<tr class="odd">
												<th>
													${message(code: 'transaction.id.label', default: 'ID')}
												</th>
												<th>
													${message(code: 'transaction.transactionDate.label', default: 'Date')}
												</th>
												<th>
													${message(code: 'transaction.transactionType.label', default: 'Type')}
												</th>
												<th>
													${message(code: 'transaction.transactionEntries.label', default: 'Entries')}
												</th>
												<th>
													${message(code: 'transaction.quantityChange.label', default: 'Qty +/-')}
												</th>
											</tr>
										</thead>
										<tbody>			
											<g:if test="${!commandInstance?.transactionEntryMap }">
												<tr>
													<td colspan="5">												
														No transaction entries
													</td>
												</tr>
											</g:if>
										
																			
											<g:each var="transaction" in="${commandInstance?.transactionEntryMap.keySet().sort {it.transactionDate}.reverse() }" status="status">
												<tr id="${transacton?.id }" class="transaction ${(status%2==0)?'even':'odd' }">
													<td class="fade">
														${transaction?.id }
													</td>
													<td>
														<g:formatDate
															date="${transaction?.transactionDate}" format="MMM dd" />
	
													</td>
													<td>	
														${transaction?.transactionType?.name }
													</td>
													<td>
														There were ${transaction?.transactionEntries?.size() } inventory item(s) involved in this transaction.														
														<a href="#" id="${transaction?.id }" class="toggleDetails">Show details &rsaquo;</a>
													</td>
													<td>
														<g:set var="quantityChange" value="${transaction?.transactionEntries*.quantity?.sum() }"/>
														<g:if test="${quantityChange>0}">
															+${quantityChange }
														</g:if>
														<g:else>
															${quantityChange }
														</g:else>
													</td>
												</tr>
												<g:each var="transactionEntry" in="${commandInstance?.transactionEntryMap.get(transaction) }">
													<tr id="transactionEntry?.id" class="transactionEntry transaction${transaction?.id } ${(status%2==0)?'even':'odd' }">
														<td>
														</td>
														<td>
														</td>
														<td>
														</td>												
														<td>
															<span class="fade">
																${transactionEntry?.inventoryItem?.description }
																${transactionEntry?.lotNumber }</span>
														</td>
														<td>
															<span class="fade">
															${transactionEntry?.quantity }</span>
														</td>
														
													</tr>
												</g:each>
											</g:each>
										</tbody>
									</table>
								</fieldset>
							</div>
																	
						</td>
					</tr>
				</table>
			</div>			
		</div>
	</body>
</html>
