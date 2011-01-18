
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName"
	value="${message(code: 'transactions.label', default: 'Transactions')}" />
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
	<g:hasErrors bean="${itemInstance}">
		<div class="errors"><g:renderErrors bean="${itemInstance}" as="list" /></div>
	</g:hasErrors>
	<div class="dialog">		
							<table>
								
							
							
								<tr>
									<td style="width: 250px;">
										<g:render template="productDetails" model="[productInstance:productInstance]"/>											
									</td>
									<td>										
										<div class="stockOptions">
											<ul>
												<li>							
													<img src="${resource(dir: 'images/icons/silk', file: 'table_refresh.png')}"/>
													<g:link controller="inventoryItem" 
														action="showStockCard" params="['product.id':productInstance?.id]">Back to Stock Card</g:link>							
												</li>
												<%-- 
												<li>
													<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
													<g:link controller="inventoryItem" action="showRecordInventory" params="['product.id':productInstance?.id,'inventory.id':inventoryInstance?.id]">
														Record stock
													</g:link>
												</li>
												<li>
													<img src="${resource(dir: 'images/icons/silk', file: 'magnifier.png')}"/>
													<g:link controller="inventoryItem" action="showTransactions" params="['product.id':productInstance?.id,'inventory.id':inventoryInstance?.id]">
														Show changes
													</g:link>
												</li>
												<li>
													<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}"/>
													<g:link controller="inventoryItem" action="create" params="['product.id':productInstance?.id,'inventory.id':inventoryInstance?.id]">
														Add item
													</g:link>
												</li>
												--%>
											</ul>
										</div>											
										
										<fieldset>
											<legend><span class="fade">Recent Transactions</span></legend>
											
											<table width="100%">
												<thead>
													<tr>
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
													<g:each var="transaction" in="${transactionEntryMap.keySet().sort {it.transactionDate}.reverse() }" status="status">
														<tr class="${(status%2==0)?'odd':'even' }">
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
																${transaction?.transactionEntries?.size() } item(s) modified
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
													</g:each>
												</tbody>
											</table>
											
																		
											
											<%-- 
											<div id="transactionEntryButton" style="text-align: right;">
												<a href="#" id="addTransactionEntryLink">
													<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
													edit stock card</a>
											</div>
											--%>				
											<%-- 
											<table border="0">
												<thead>
													<tr>
														<th>
															${message(code: 'transaction.transactionDate.label', default: 'Date')}
														</th>
														<th>
															${message(code: 'transaction.transactionType.label', default: 'Type')}
														</th>
														<th>
															${message(code: 'transaction.source.label', default: 'Source')}
														</th>
														<th>
															${message(code: 'inventory.destination.label', default: 'Destination')}
														</th>
														<th>
															${message(code: 'inventoryItem.lotNumber.label', default: 'Lot Number')}
														</th>
														<th>
															${message(code: 'inventory.quantity.label', default: 'Qty')}
														</th>
													</tr>
												</thead>
												<tbody>
													<g:if test="${transactionEntryList}">
														<g:each var="transactionEntry" in="${transactionEntryList}" status="status">
															<tr class="${(status%2==0)?'odd':'even' }">
																<td>
																	<g:formatDate
																		date="${transactionEntry?.transaction?.transactionDate}" format="MMM dd" />
																</td>
																<td>
																	${transactionEntry?.transaction?.transactionType?.name }
																</td>
																<td>
																	${transactionEntry?.transaction?.source?.name }
																</td>
																<td>
																	${transactionEntry?.transaction?.destination?.name }
																</td>
																<td>
																	${transactionEntry?.lotNumber?:'<span class="fade">EMPTY</span>'}
																</td>
																<td style="text-align: center;">	
																	${transactionEntry?.quantity}
																</td>
															</tr>			
														</g:each>
													</g:if>			
													<g:else>
														<tr class="odd">
															<td colspan="7" style="text-align: center">
																<div class="fade">No stock movements</div>
															</td>
														</tr>
													</g:else>				
												</tbody>
												<g:if test="${inventoryItemList }">
													<tfoot >
														<tr >
															<th colspan="5">
																Total												 
															</th>
															<th style="text-align: center">
																${inventoryItemList*.quantity?.sum() }
															</th>
														</tr>
														
													</tfoot>
												</g:if>
											</table>
											--%>
											
											
										</fieldset>
								
								
								
								<script>
									$(document).ready(function() {
										$("#transactionEntryButton").show();
										$("#transactionEntryForm").hide();
										
										$("#addTransactionEntryLink").click(function() { 
											$("#transactionEntryButton").hide(''); 
											$("#transactionEntryForm").show(''); 
										});
										$(".closeTransactionEntryLink").click(function() { 
											$("#transactionEntryButton").show(''); 
											$("#transactionEntryForm").hide(''); 
										});

									});
								</script>		
								
																								
	
								<div id="transactionEntryForm">						
									<style>
										.selected { font-weight: bold; } 
									</style>
									<script>
										$(document).ready(function() {
											$("div.transactionEntry").each(function() {		
												$this = $(this)
												$this.hide();
											});
											$("#initialLink").click(function() { 
												$('#transactionLinkBar a').removeClass('selected');											
												$(this).addClass('selected'); 
												$("div.transactionEntry").each(function() {		
													$this = $(this)
													$this.hide();
												});
												$("#initial").toggle(); 
											});
											$("#transferInLink").click(function() { 
												$('#transactionLinkBar a').removeClass('selected');											
												$(this).addClass('selected'); 
												$("div.transactionEntry").each(function() {		
													$this = $(this)
													$this.hide();
												});
												$("#transferIn").toggle(); 
											});
											$("#transferOutLink").click(function() { 
												$('#transactionLinkBar a').removeClass('selected');											
												$(this).addClass('selected'); 
												$("div.transactionEntry").each(function() {		
													$this = $(this)
													$this.hide();
												});
												$("#transferOut").toggle(); 
											});
											$("#adjustmentLink").click(function() { 
												$('#transactionLinkBar a').removeClass('selected');											
												$(this).addClass('selected'); 
												$("div.transactionEntry").each(function() {		
													$this = $(this)
													$this.hide();
												});
												$("#adjustment").toggle(); 
	
											});
											$("#consumptionLink").click(function() { 
												$('#transactionLinkBar a').removeClass('selected');											
												$(this).addClass('selected'); 
												$("div.transactionEntry").each(function() {		
													$this = $(this)
													$this.hide();
												});
												$("#consumption").toggle(); 
											});
											$("#expirationLink").click(function() { 
												$('#transactionLinkBar a').removeClass('selected');											
												$(this).addClass('selected'); 
												$("div.transactionEntry").each(function() {		
													$this = $(this)
													$this.hide();
												});
												$("#expiration").toggle(); 
											});
											$("#otherLink").click(function() { 											
												$('#transactionLinkBar a').removeClass('selected');											
												$(this).addClass('selected'); 
												$("div.transactionEntry").each(function() {		
													$this = $(this)
													$this.hide();
												});
												$("#other").toggle(); 
											});
										});
									</script>
										<div id="transactionEntryForm">						
											<g:if test="${inventoryItemList }">
												<div id="transactionLinkBar" style="text-align: right;">
													<a href="#" id="initialLink"><img src="${resource(dir: 'images/icons/silk', file: 'basket.png')}"/>
														Intial Quantity</a> 
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
													<a href="#" id="transferInLink"><img src="${resource(dir: 'images/icons/silk', file: 'basket_put.png')}"/>
														Transfer In</a> 
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
													<a href="#" id="transferOutLink"><img src="${resource(dir: 'images/icons/silk', file: 'basket_remove.png')}"/>
														Transfer Out</a> 
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
													<a href="#" id="adjustmentLink"><img src="${resource(dir: 'images/icons/silk', file: 'basket_edit.png')}"/>
														Adjustment</a> 
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
													<a href="#" id="consumptionLink"><img src="${resource(dir: 'images/icons/silk', file: 'basket_delete.png')}"/>
														Consumption</a> 
												</div>


											
												<div id="initial" class="transactionEntry">
													<g:form autocomplete="off">
														<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
														<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
														<g:hiddenField name="product.id" value="${productInstance?.id}"/>
														<g:hiddenField name="createdBy.id" value="${session?.user?.id }"/>											
														<g:hiddenField name="transactionType.id" value="7"/>
														<g:hiddenField name="source.id" value="${session?.warehouse?.id }"/>											
														<g:hiddenField name="destination.id" value="${session?.warehouse?.id }"/>											
														<fieldset>
															<legend>Initial Quantity</legend>
															<table border="0" cellspacing="5" cellpadding="5">
																<tr class="prop">
																	<td class="name"><label>Date</label></td>
																	<td class="value">
																		<g:jqueryDatePicker id="transactionDate0" name="transactionDate" value="" format="MM/dd/yyyy"/>
																	</td>
																</tr>
																<tr class="prop">
																	<td class="name"><label>Quantity</label></td>
																	<td class="value"><g:textField name="quantity" size="3" value=""/></td>
																</tr>
																<tr class="prop">
																	<td class="name"><label>Lot Number</label></td>
																	<td class="value">
																		<select name="lotNumber">
																			<g:each var="inventoryItem" in="${inventoryItemList}">
																				<option value="${inventoryItem?.lotNumber }">${inventoryItem?.lotNumber?:"EMPTY" }</option>
																			</g:each>
																		</select>	
																	</td>
																</tr>
															</table>
															<div class="buttonBar">
																<g:actionSubmit action="saveTransactionEntry" value="Save"/>		
																&nbsp;
																<a href="#" class="closeTransactionEntryLink"><img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/> Cancel</a>
																										
															</div>
		
															<span class="fade">(use negative quantity to subtract stock)</span>
														</fieldset>
													</g:form>
												</div>
												<div id="transferIn" class="transactionEntry">
													<g:form autocomplete="off">
														<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
														<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
														<g:hiddenField name="product.id" value="${productInstance?.id}"/>
														<g:hiddenField name="createdBy.id" value="${session?.user?.id }"/>											
														<g:hiddenField name="transactionType.id" value="1"/>
														<g:hiddenField name="destination.id" value="${session?.warehouse?.id }"/>											
														
														<fieldset>
															<legend>Transfer In</legend>
															<table border="0" cellspacing="5" cellpadding="5">
																<tr class="prop">
																	<td class="name"><label>Transfer From</label></td>
																	<td class="value">
																		<g:select name="source.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" 
																			optionKey="id" optionValue="name" value="" noSelection="['0': '']" />							
																	</td>
																</tr>
																<tr class="prop">
																	<td class="name"><label>Date</label></td>
																	<td class="value">
																		<g:jqueryDatePicker id="transactionDate1" name="transactionDate" value="" format="MM/dd/yyyy"/>
																	</td>
																</tr>		
																<tr class="prop">
																	<td class="name"><label>Quantity</label></td>
																	<td class="value"><g:textField name="quantity" size="3" value=""/></td>
																</tr>
																<tr class="prop">
																	<td class="name"><label>Lot Number</label></td>
																	<td class="value">
																		<select name="lotNumber">
																			<g:each var="inventoryItem" in="${inventoryItemList}">
																				<option value="${inventoryItem?.lotNumber }">${inventoryItem?.lotNumber?:"EMPTY" }</option>
																			</g:each>
																		</select>	
																	</td>
																</tr>
															</table>	
															<div class="buttonBar">
																<g:actionSubmit action="saveTransactionEntry" value="Save"/>																
																&nbsp;
																<a href="#" class="closeTransactionEntryLink"><img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/> Cancel</a>
																
															</div>
		
															<span class="fade">(use negative quantity to subtract stock)</span>
														</fieldset>
													</g:form>
												</div>
												<div id="transferOut" class="transactionEntry">
													<g:form autocomplete="off">
														<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
														<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
														<g:hiddenField name="product.id" value="${productInstance?.id}"/>
														<g:hiddenField name="createdBy.id" value="${session?.user?.id }"/>											
														<g:hiddenField name="transactionType.id" value="1"/>
														<g:hiddenField name="source.id" value="${session?.warehouse?.id }"/>											
														<fieldset>
															<legend>Transfer Out</legend>
															<table border="0" cellspacing="5" cellpadding="5">
																<tr class="prop">
																	<td class="name"><label>Transfer To</label></td>
																	<td class="value">
																		<g:select name="destination.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" 
																			optionKey="id" optionValue="name" value="" noSelection="['0': '']" />							
																	</td>
																</tr>
																<tr class="prop">
																	<td class="name"><label>Date</label></td>
																	<td class="value">
																		<g:jqueryDatePicker id="transactionDate2" name="transactionDate" value="" format="MM/dd/yyyy"/>
																	</td>
																</tr>		
																<tr class="prop">
																	<td class="name"><label>Quantity</label></td>
																	<td class="value"><g:textField name="quantity" size="3" value=""/></td>
																</tr>
																<tr class="prop">
																	<td class="name"><label>Lot Number</label></td>
																	<td class="value">
																		<select name="lotNumber">
																			<g:each var="inventoryItem" in="${inventoryItemList}">
																				<option value="${inventoryItem?.lotNumber }">${inventoryItem?.lotNumber?:"EMPTY" }</option>
																			</g:each>
																		</select>	
																	</td>
																</tr>
															</table>	
															<div class="buttonBar">
																<g:actionSubmit action="saveTransactionEntry" value="Save"/>												
																&nbsp;
																<a href="#" class="closeTransactionEntryLink"><img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/> Cancel</a>
															</div>
		
															<span class="fade">(use negative quantity to subtract stock)</span>
														</fieldset>
													</g:form>
												</div>
																								
												<div id="consumption" class="transactionEntry">
													<g:form autocomplete="off">
														<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
														<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
														<g:hiddenField name="product.id" value="${productInstance?.id}"/>
														<g:hiddenField name="createdBy.id" value="${session?.user?.id }"/>											
														<g:hiddenField name="transactionType.id" value="2"/>
														<g:hiddenField name="source.id" value="${session?.warehouse?.id }"/>											
														<g:hiddenField name="destination.id" value="${session?.warehouse?.id }"/>											
		
														<fieldset>
															<legend>Consumption</legend>
														
															<table border="0" cellspacing="5" cellpadding="5">
																<tr class="prop">
																	<td class="name"><label>Date</label></td>
																	<td class="value">
																		<g:jqueryDatePicker id="transactionDate3" name="transactionDate" value="" format="MM/dd/yyyy"/>
																	</td>
																</tr>		
																<tr class="prop">
																	<td class="name"><label>Quantity</label></td>
																	<td class="value">
																		<g:textField name="quantity" size="3" value=""/>
																	</td>
																</tr>
																<tr class="prop">
																	<td class="name"><label>Lot Number</label></td>
																	<td class="value">
																		<select name="lotNumber">
																			<g:each var="inventoryItem" in="${inventoryItemList}">
																				<option value="${inventoryItem?.lotNumber }">${inventoryItem?.lotNumber?:"EMPTY" }</option>
																			</g:each>
																		</select>	
																	</td>
																</tr>
															</table>	
															<div class="buttonBar">
																<g:actionSubmit action="saveTransactionEntry" value="Save"/>												
																&nbsp;
																<a href="#" class="closeTransactionEntryLink"><img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/> Cancel</a>
															</div>
		
															<span class="fade">(use negative quantity to subtract stock)</span>
														</fieldset>
													</g:form>
												</div>
												
												<div id="adjustment" class="transactionEntry">
													<g:form autocomplete="off">
														<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
														<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
														<g:hiddenField name="product.id" value="${productInstance?.id}"/>
														<g:hiddenField name="createdBy.id" value="${session?.user?.id }"/>											
														<g:hiddenField name="transactionType.id" value="3"/>
														<g:hiddenField name="source.id" value="${session?.warehouse?.id }"/>											
														<g:hiddenField name="destination.id" value="${session?.warehouse?.id }"/>											
														
														
														<fieldset>
															<legend>Adjustment</legend>
														
															<table border="0" cellspacing="5" cellpadding="5">
																<tr class="prop">
																	<td class="name"><label>Date</label></td>
																	<td class="value">
																		<g:jqueryDatePicker id="transactionDate4" name="transactionDate" value="" format="MM/dd/yyyy"/>
																	</td>
																</tr>		
																<tr class="prop">
																	<td class="name"><label>Quantity</label></td>
																	<td class="value"><g:textField name="quantity" size="3" value=""/></td>
																</tr>
																<tr class="prop">
																	<td class="name"><label>Lot Number</label></td>
																	<td class="value">
																		<select name="lotNumber">
																			<g:each var="inventoryItem" in="${inventoryItemList}">
																				<option value="${inventoryItem?.lotNumber }">${inventoryItem?.lotNumber?:"EMPTY" }</option>
																			</g:each>
																		</select>	
																	</td>
																</tr>
															</table>	
															<div class="buttonBar">
																<g:actionSubmit action="saveTransactionEntry" value="Save"/>	
																&nbsp;
																<a href="#" class="closeTransactionEntryLink"><img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/> Cancel</a>
															</div>
		
															<span class="fade">(use negative quantity to subtract stock)</span>
														</fieldset>																		
													</g:form>
												</div>
												
											</g:if>
											<g:else>
												You must create at least one lot before entering quantities.
											</g:else>	
										</div>
									</div>
								</fieldset>
							</td>

						</tr>
					</table>
			</div>			
		</div>
	</body>
</html>
