
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
	<g:hasErrors bean="${itemInstance}">
		<div class="errors"><g:renderErrors bean="${itemInstance}" as="list" /></div>
	</g:hasErrors>
	<div class="dialog">		
	
	<%-- 
			<g:if test="${productInstance?.category }">
				<div style="padding: 15px;">
					<a href="${createLink(controller: 'inventory', action: 'browse', params: ['categoryId' : productInstance?.category?.id])}">
					</a>
					<g:render template="../inventory/breadcrumb" model="${['categoryInstance':productInstance?.category]}"/> 
					
				</div>
			</g:if>			
	--%>	
<%-- 
						&nbsp;
						<span style="font-size: 1em;">					
							<a href="${createLink(action: 'edit', id: itemInstance?.id, params: ['inventory.id':itemInstance?.inventory?.id]) }">edit</a>
						</span>
						--%>
<%-- 
					<table>
						<tr>
							<td>								
								<ul>
									<li>Min Quantity&nbsp;<label>${itemInstance?.minQuantity }</label> </li>
									<li>Reorder Quantity&nbsp;<label>${itemInstance?.reorderQuantity }</label></li>
									<li>Max Quantity&nbsp;<label>${itemInstance?.maxQuantity }</label></li>
								
								</ul>
							</td>														
							<td>
								<g:if test="${itemInstance?.warnings }">
									<div class="error">	
										<ul>										
											<g:each var="warning" in="${itemInstance?.warnings }">
												<li>
													<g:if test="${warning?.contains('error') || warning?.contains('alert')}">
														<img src="${resource(dir: 'images/icons/silk', file: 'exclamation.png') }" />
													</g:if>
													<g:elseif test="${warning?.contains('warning')}">
														<img src="${resource(dir: 'images/icons/silk', file: 'error.png') }" />
													</g:elseif>
													<g:elseif test="${warning?.contains('info')}">
														<img src="${resource(dir: 'images/icons/silk', file: 'information.png') }" />
													</g:elseif>														
													&nbsp;${message(code: warning)}								
												</li>							
											</g:each>
										</ul>						
									</div>
								</g:if>
								<g:else>
									No Alerts
								</g:else>									
							</td>
							<td style="text-align: right;">
								<!-- -->
							</td>
						</tr>
					</table>
					--%>



							<table>
								<tr>
								
								
								
									<td style="width: 300px">
										<script>
											$(document).ready(function() {
												$("#inventoryLotReport").show();
												$("#inventoryLotForm").hide();
												
												$("#showInventoryLotFormLink").click(function() { 
													$("#inventoryLotReport").hide(''); 
													$("#inventoryLotForm").show(''); 
												});
												$("#showInventoryLotReportLink").click(function() { 
													$("#inventoryLotReport").show(''); 
													$("#inventoryLotForm").hide(''); 
												});
		
											});
										</script>
										
										<style>
											span.name {   } 
											span.value { font-size: 1.0em; } 
											span.name:after { content: ": " }
											th { color: lightgrey; } 
										</style>
										<fieldset>
											<legend><span class="fade">Product Details</span></legend>
											<table>
												<tr class="">	
													<td style="text-align: left;">
														<span class="name">Description</span>
													</td>
													<td>
														<span class="value">${productInstance?.name }</span>
													</td>
												</tr>
												<tr class="">	
													<td style="text-align: left;">
														<span class="name">Product Code</span>
													</td>
													<td>
														<span class="value">${productInstance?.productCode?:'<span class="fade">none</span>' }</span>
													</td>
												</tr>
												
												<tr class="">	
													<td style="text-align: left;">
														<span class="name">Cold Chain</span>
													</td>
													<td>
														<span class="value">${productInstance?.coldChain?'Yes':'No' }</span>
													</td>
												</tr>
												<tr class="">	
													<td style="text-align: left;">
														<span class="name">Category</span>
													</td>
													<td>
														<g:if test="${productInstance?.category?.parentCategory }">
															<span class="value">${productInstance?.category?.parentCategory?.name } &rsaquo;</span>
														</g:if>
														<span class="value">${productInstance?.category?.name }
													</td>
												</tr>
												<g:each var="productAttribute" in="${productAttributes}">
												<tr>
													<td style="text-align: left;">
														<span class="name">${productAttribute?.name }</span>
													</td>
													<td>
														<span class="value">${productAttribute.value }</span>
													</td>
												</tr>													
												</g:each>
												<tr>
													<td colspan="3"><hr/></td>
												</tr>
												<tr class="">
													<td style="text-align: left;">
														<span class="name">Minimum Quantity</span>
													</td>
													<td>
														<span class="value">${inventoryLevelInstance?.minQuantity?:'<span class="fade">Not Configured</span>' }</span>
													</td>
												</tr>
												<tr class="">
													<td style="text-align: left;">
														<span class="name">Reorder Quantity</span>
													</td>
													<td>
														<span class="value">${inventoryLevelInstance?.reorderQuantity?:'<span class="fade">Not Configured</span>' }</span>
													</td>
												</tr>										
												<tr class="">
													<td></td>
													<td>
														<div style="text-align: left;">			
															<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
															<a href="#" id="configureWarningLevelsLink">
															Configure</a>
														</div>													
													</td>
												</tr>
											</table>
											
											
											
											
											<%-- 								
											<script>
												$(document).ready(function() {
													$("#showWarningLevels").show();
													$("#configureWarningLevels").hide();
													$("#configureWarningLevelsLink").click(function() { 
														$("#showWarningLevels").hide(); 
														$("#configureWarningLevels").show(); 
													});
													$("#showWarningLevelsLink").click(function() { 
														$("#showWarningLevels").show(); 
														$("#configureWarningLevels").hide(); 
													});
			
												});
											</script>								

											<div id="showWarningLevels">
												<table>
													<tr class="prop">
														<td style="text-align: right"><label>Minimum Quantity </label></td>
														<td class="value">
															${inventoryLevelInstance?.minQuantity?:'Not Configured' }
														</td>
													</tr>
													<tr class="prop">
														<td style="text-align: right"><label>Reorder Quantity</label></td>
														<td class="value">
															${inventoryLevelInstance?.reorderQuantity?:'Not Configured' }
														</td>
													</tr>
												</table>	
												<div style="text-align: right">			
													<a href="#" id="configureWarningLevelsLink">
													<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
													configure</a>
												</div>
											</div>
										
											<div id="configureWarningLevels">
												<g:form>
													<g:hiddenField name="id" value="${inventoryLevelInstance?.id}"/>
													<g:hiddenField name="product.id" value="${productInstance?.id}"/>
													<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
														<table>
															<tr class="prop">
																<td class="name"><label>Minimum Quantity </label></td>
																<td class="value">
																	<g:textField name="minQuantity" value="${inventoryLevelInstance?.minQuantity }" size="3"/>
																</td>
															</tr>
															<tr class="prop">
																<td class="name"><label>Reorder Quantity</label></td>
																<td class="value">
																	<g:textField name="reorderQuantity" value="${inventoryLevelInstance?.reorderQuantity }" size="3"/>
																</td>
															</tr>
														</table>
														<div class="buttonBar" style="text-align: center;">
										                    <g:actionSubmit class="save" action="saveInventoryLevel" value="${message(code: 'default.button.save.label', default: 'Save')}" />
										                    &nbsp;
															<a href="#" id="showWarningLevelsLink">
																<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/> Cancel</a>
									                    </div>
												</g:form>								
											</div>		
											--%>		
										</fieldset>
									</td>
									<td>										
										
										<div style="text-align: right">
											<label>Actions:</label> 
											<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
											<g:link controller="inventory" action="enterStock" params="['product.id':productInstance?.id]">
												Record inventory</g:link>
										</div>
									
									
										<fieldset>
											<legend>Current Stock</legend>
										
											<div id="inventoryLotReport" style="text-align: right;">										
												
												<table border="1" style="border:1px solid #f5f5f5">
													<thead>
														<tr>
															<th>Lot Number</th>
															<th>Expires</th>
															<th>Description</th>
															<th>Qty</th>
															<th>Actions</th>
														</tr>											
													</thead>
													<tbody>
														<g:if test="${!inventoryItemList}">
															<tr class="odd">
																<td colspan="5" style="text-align: center">
																	<span class="fade">No current stock </span>
																</td>
															</tr>
														</g:if>
													
														<g:each var="itemInstance" in="${inventoryItemList }" status="status">				
															<tr class="${(status%2==0)?'odd':'even' }">
																<td>${itemInstance?.lotNumber?:'<span class="fade">EMPTY</span>' }</td>
																<td>
																	<g:if test="${itemInstance?.inventoryLot?.expirationDate}">
																		<g:formatDate date="${itemInstance?.inventoryLot?.expirationDate }" format="dd/MMM/yy" />
																	</g:if>
																	<g:else>
																		<span class="fade">n/a</span>
																	</g:else>
																</td>
																<td>${itemInstance?.product?.name }</td>
																<td style="text-align: center;">${itemInstance?.quantity }</td>												
																<td></td>
															</tr>
														</g:each>
													</tbody>
													<tfoot>
														<tr>
															<th>Total</th>
															<th></th>
															<th style="text-align: center;">${inventoryItemList*.quantity.sum() }</th>
														</tr>
													</tfoot>
												</table>										
											</div>										
											<div id="inventoryLotForm">
												<g:form action="saveInventoryLot" autocomplete="off">
													<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
													<g:hiddenField name="product.id" value="${productInstance?.id}"/>
													<g:hiddenField name="active" value="true"/>
													<g:hiddenField name="createdBy" value="${session?.user?.id }"/>		
													<g:hiddenField name="initialQuantity" value="0"/>							
													<g:hiddenField name="inventoryItemType" value="${org.pih.warehouse.inventory.InventoryItemType.NON_SERIALIZED}"/>
														
													<g:hasErrors bean="${inventoryLotInstance}">
											            <div class="errors">
											                <g:renderErrors bean="${inventoryLotInstance}" as="list" />
											            </div>
										            </g:hasErrors>									
														
														<table border="1" style="border:1px solid #f5f5f5">
															<thead>
																<tr>
																	<th>Lot Number</th>
																	<th>Expires</th>
																	<th>&nbsp;</th>
																</tr>											
															</thead>
															<tbody>
																<g:each var="itemInstance" in="${inventoryItemList }" status="i">				
																	<tr class="${(i%2 == 0)?'odd':'even' }">
																		<td>${itemInstance?.lotNumber?:'<span class="fade">EMPTY</span>' }</td>
																		<td><g:formatDate date="${itemInstance?.inventoryLot?.expirationDate }" format="dd/MMM/yy" /></td>
																		<td style="text-align:center;">
																			<g:link controller="inventoryItem" action="deleteInventoryItem" id="${itemInstance?.id }" params="['inventory.id':inventoryInstance?.id]">
																				<img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}"/>
																			</g:link>
																		</td>
																	</tr>
																</g:each>
																
																<tr>
																	<td>
																		<g:textField name="lotNumber" size="10"/>
																	</td>
																	<td nowrap>
																		<g:jqueryDatePicker id="expirationDate" name="expirationDate" value="" format="MM/dd/yyyy"
																			showTrigger="false" />
																	</td>
																	<td>
																	</td>
																</tr>
															</tbody>
														</table>
														<div class="buttonBar" >
															<g:submitButton name="save" value="Save"/>
															&nbsp;
															<a href="#" id="showInventoryLotReportLink">
																<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/> Cancel</a>
														</div>												
														
														
												</g:form>
											</div>									
										</fieldset>
										
										
										<br/>
										
										<fieldset>
											<legend><span class="fade">Transaction Log</span></legend>
											
											<%-- 
											<div id="transactionEntryButton" style="text-align: right;">
												<a href="#" id="addTransactionEntryLink">
													<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
													edit stock card</a>
											</div>
											--%>												
											<table border="1" style="border: 1px solid #f5f5f5">
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
														<th>&nbsp;</th>
													</tr>
												</thead>
												<tbody>
													<g:if test="${transactionEntryList}">
														<g:each var="transactionEntry" in="${transactionEntryList}" status="status">
															<tr class="${(status%2==0)?'odd':'even' }">
																<td><g:formatDate
																	date="${transactionEntry?.transaction?.transactionDate}" format="MMM dd" />
																</td>
																<td>
																	${transactionEntry?.transaction?.transactionType?.name }
																</td>
																<td>
																	${transactionEntry?.transaction?.source?.name }
																</td>
																<td>${transactionEntry?.transaction?.destination?.name }</td>
																<td>${transactionEntry?.lotNumber?:'<span class="fade">EMPTY</span>'}</td>
																<td style="text-align: center;">${transactionEntry?.quantity}</td>
																<td>
																	<g:link controller="inventoryItem" action="deleteTransactionEntry" 
																		id="${transactionEntry?.id }" params="['inventoryItem.id':itemInstance?.id]">
																		<img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}"/>
																	</g:link>	
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
													<tfoot>
														<tr>
															<th colspan="5">
																Total												 
															</th>
															<th style="text-align: center">
																${inventoryItemList*.quantity?.sum() }
															</th>
															<th></th>
														</tr>
														
													</tfoot>
												</g:if>
											</table>
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
