
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
			
			
			<div class="dialog" style="min-height: 880px">		
			
				<div class="actionsMenu" style="float: left; margin-left: 15px;">	
					<span>		
						<g:link controller="inventory" action="browse" >&lsaquo; Back to Inventory</g:link>
					</span>
				</div>
				
				<div class="actionsMenu" style="float: right; margin-right: 15px;">					
					<ul>
						<li>
							<label>Actions:</label>
						</li>
						<li>
							<g:link controller="inventoryItem" action="showRecordInventory" params="['product.id':commandInstance?.productInstance?.id,'inventory.id':commandInstance?.inventoryInstance?.id]">
								<button class="positive">
									<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
									Record inventory
								</button>
							</g:link>
						</li>
						<li>
							<g:link class="new button" controller="inventory" action="createTransaction">
								<button class="positive">
									<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}"/>
									Add new transaction
								</button>
							</g:link>
						</li>	
					</ul>					
				</div>
				
				<br clear="all">
				<table>
					<tr>
						<td style="width: 250px;">
							<g:render template="productDetails" 
								model="[productInstance:commandInstance?.productInstance, inventoryInstance:commandInstance?.inventoryInstance, inventoryLevelInstance: commandInstance?.inventoryLevelInstance, totalQuantity: commandInstance?.totalQuantity]"/>											
						</td>
						<td>			
						
							<div style="min-height: 200px;"> 					
								<fieldset>	
									<legend class="fade">In Stock</legend>	
									<div id="inventoryView" style="text-align: right;">										
										<table border="0" style="border:1px solid #f5f5f5;" >
											<thead>
												<tr class="even">
													<th>Lot Number</th>
													<th>Expires</th>
													<th>Description</th>
													<th style="text-align: center;" >Qty</th>
													<th style="text-align: right;">Actions</th>
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
												<g:set var="count" value="${0 }"/>
												<g:each var="itemInstance" in="${commandInstance?.inventoryItemList }" status="status">	
													<g:if test="${commandInstance.quantityByInventoryItemMap.get(itemInstance)!=0}">			
														<tr class="${(count++%2==0)?'odd':'even' } prop">
															<td>
																
																${itemInstance?.lotNumber?:'<span class="fade">EMPTY</span>' }
																<g:link action="show" controller="inventoryItem" id="${itemInstance?.id }">
																</g:link>
															</td>
															<td>
																<g:if test="${itemInstance?.expirationDate}">
																	<g:formatDate date="${itemInstance?.expirationDate }" format="dd/MMM/yy" />
																</g:if>
																<g:else>
																	<span class="fade">never</span>
																</g:else>
															</td>
															<td>
																${(itemInstance?.description)?:'<span class="fade">None</span>'}
															</td>
															<td style="text-align: center;">
																<g:set var="itemQuantity" value="${commandInstance.quantityByInventoryItemMap.get(itemInstance) }"/>
																<g:set var="styleClass" value=""/>
																<g:if test="${itemQuantity<0}">
																	<g:set var="styleClass" value="color: red;"/>																	
																</g:if>
																<span style="${styleClass}">${itemQuantity }</span>
															</td>	
															
															<td style="text-align: right;" width="100px">
																<g:link controller="inventoryItem" action="showRecordInventory" params="['product.id':commandInstance?.productInstance?.id,'inventory.id':commandInstance?.inventoryInstance?.id, 'inventoryItem.id':itemInstance?.id]">
																	<button class="positive">
																		<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
																		Adjust
																	</button>
																</g:link>
															</td>													
														</tr>
													</g:if>
												</g:each>
											</tbody>
											<tfoot>
												<tr class="prop" style="height: 3em;">
													<td colspan="3" style="text-align: left; font-size: 1.5em; vertical-align: middle;">
														Net total
													</td>
													<td style="text-align: center; font-size: 1.5em; vertical-align: middle">
														${commandInstance.totalQuantity }</td>
												</tr>
											</tfoot>
										</table>										
									</div>			
								</fieldset>
							</div>
						</td>
					</tr>
					<tr>
						<td></td>
						<td>
							<div>							
								<fieldset>
									<legend><span class="fade">Recent Transactions</span></legend>
									
									
									<script>
										jQuery(document).ready(function() {
											jQuery(".toggleDetails").click(function(event) {
												//event.preventDefault();
											});
											/*
											jQuery(".toggleDetails").mouseover(function(event) {
												console.log(this.id);
												jQuery("#transactionEntries" + this.id).toggle('slow');								
											});
											jQuery(".toggleDetails").mouseout(function(event) {
												console.log(this.id);
												jQuery("#transactionEntries" + this.id).toggle('slow');								
											});
											*/

											jQuery(".toggleDetails").hoverIntent({
												over: function(event) {
													jQuery("#transactionEntries" + this.id).slideDown('fast');													
												},
												timeout: 500,
												out: function(event) {
													jQuery("#transactionEntries" + this.id).slideUp('fast');								
												}
											});	
										});	
									</script>
									<div>
										<g:form method="GET" action="showStockCard">
											<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id }"/>
									
											<table style="inline-block;">
												<thead>
													<tr class="even prop">
														<th>
															${message(code: 'transaction.transactionType.label', default: 'Details')}
														</th>													
														<th>
															${message(code: 'transaction.transactionType.label', default: 'Type')}
														</th>
														<th>
															${message(code: 'transaction.transactionDate.label', default: 'Date')}
														</th>
														<th style="text-align: center">
															${message(code: 'transaction.quantityChange.label', default: 'Qty In/Out')}
														</th>
													</tr>
													<tr class="odd prop">
														<td>
															<span class="fade">
																${message(code: 'transaction.filterTransactions.label', default: 'Filter transactions')}
															</span>
														</td>
														<td>
															<g:select name="transactionType.id" 
																from="${org.pih.warehouse.inventory.TransactionType.list()}" 
																optionKey="id" optionValue="name" value="${commandInstance?.transactionType?.id }" 
																noSelection="['0': '-- All types --']" /> 
																
														</td>													
														<td>
															<g:jqueryDatePicker 
																id="startDate" 
																name="startDate" 
																value="${commandInstance?.startDate }" 
																format="MM/dd/yyyy"
																showTrigger="false" />
															to
															<g:jqueryDatePicker 
																id="endDate" 
																name="endDate" 
																value="${commandInstance?.endDate }" 
																format="MM/dd/yyyy"
																showTrigger="false" />
															&nbsp;
															<button  class="positive" name="filter">Search</button>
														</td>
														<td></td>
													</tr>												
												</thead>
												<tbody>			
													<g:if test="${!commandInstance?.transactionLogMap }">
														<tr>
															<td colspan="4">												
																No transaction entries
															</td>
														</tr>
													</g:if>
													<g:else>
														<g:set var="totalQuantityChange" value="${0 }"/>							
														<g:each var="transaction" in="${commandInstance?.transactionLogMap?.keySet().sort {it.transactionDate}.reverse() }" status="status">
															<tr class="transaction ${(status%2==0)?'even':'odd' } prop">
																<td>	
																	<a href="${createLink(controller: 'inventory', action:'showTransaction', id: transaction.id, params: ['product.id', 'test'])}" style="display: inline-block; padding: 2px;" href="#" id="${transaction?.id }" class="toggleDetails">
																		<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>
																		Show details &rsaquo;
																	</a>
																	<span id="transactionEntries${transaction?.id}" style="text-align: left; padding: 10px; margin: 10px; display: none; position:absolute; padding: 10px; background-color: white; border: 1px dashed black">
																		<label>Entries for Transaction #${transaction.id }</label>
																		<table width="100%">
																			<tr> 
																				<th>Description</th>
																				<th>Serial/Lot Number</th>
																				<th style="text-align: center;">Qty Change</th>
																			</tr>
																			<g:each var="transactionEntry" in="${commandInstance?.transactionLogMap?.get(transaction) }" status="status2">
																				<tr class="${(status2%2==0)?'odd':'even' }">
																					<td>
																						${transactionEntry?.inventoryItem?.product?.name }
																						<g:if test="${transactionEntry?.inventoryItem?.description }">
																							&rsaquo;
																						</g:if>
																						${transactionEntry?.inventoryItem?.description }
																					</td>
																					<td>
																						${transactionEntry?.lotNumber }
																					</td>
																					<td style="text-align: center;">
																						<g:if test="${transactionEntry.quantity<0}">
																							<g:set var="styleClass" value="color: red;"/>																	
																						</g:if>
																						<span style="${styleClass}">${transactionEntry.quantity }</span> 
																						
																					</td>
																				</tr>
																			</g:each>
																			<tfoot>
																				<tr style="border-top: 1px solid lightgrey;">
																					<th colspan="2">
																					</th>
																					<th style="text-align: center;">
	
																						<g:set var="quantityChange" value="${transaction?.transactionEntries.findAll{it.product == commandInstance?.productInstance}.quantity?.sum() }"/>
																						<g:set var="totalQuantityChange" value="${totalQuantityChange + quantityChange}"/>
																																						<g:set var="styleClass" value="color:black;"/>														
																						<g:if test="${quantityChange<0}">
																							<g:set var="styleClass" value="color: red;"/>																	
																						</g:if>
																						<span style="${styleClass}">${quantityChange }</span> 
																					</th>
																				</tr>
																			</tfoot>
																		</table>
																	</span>															
																</td>
																<td>
																	${transaction?.transactionType?.name }&nbsp;
																
																</td>
																<td>
																	<span class="fade">
																		<g:formatDate
																			date="${transaction?.transactionDate}" format="MMM dd" />																
																	</span>
																	&nbsp;
																	<g:prettyDateFormat date="${transaction?.dateCreated}" /> 
																</td>
																<td style="text-align: center">
																	
																	<g:set var="styleClass" value="color:black;"/>														
																	<g:if test="${quantityChange<0}">
																		<g:set var="styleClass" value="color: red;"/>																	
																	</g:if>
																	<span style="${styleClass}">${quantityChange }</span> 
																</td>
															</tr>
														</g:each>
														<tr>
															<th></th>
															<th></th>
															<th></th>
															<th class="large" style="text-align: center;">
																<g:if test="${totalQuantityChange>0}">${totalQuantityChange}</g:if>
																<g:else>${totalQuantityChange}</g:else>														
															</th>
														</tr>
													</g:else>
												</tbody>
											</table>
										</g:form>
										
									</div>
								</fieldset>
							</div>
						</td>
					</tr>
				</table>
			</div>			
		</div>
	</body>
</html>
