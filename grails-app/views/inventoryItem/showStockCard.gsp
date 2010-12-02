
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
<div class="body"><g:if test="${flash.message}">
	<div class="message">
	${flash.message}
	</div>
</g:if> <g:hasErrors bean="${itemInstance}">
	<div class="errors"><g:renderErrors bean="${itemInstance}"
		as="list" /></div>
</g:hasErrors>

<div class="dialog">
<fieldset>


<h1><span class="fade"> ${productInstance?.productType?.name }
</span> &nbsp; ${productInstance?.name } ${productInstance?.dosageStrength } ${productInstance?.dosageUnit }

<%-- 
						&nbsp;
						<span style="font-size: 1em;">					
							<a href="${createLink(action: 'edit', id: itemInstance?.id, params: ['inventory.id':itemInstance?.inventory?.id]) }">edit</a>
						</span>
						--%></h1>
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
		<td width="75%">
		<fieldset><legend>Transactions</legend>
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
					<th>Actions</th>
				</tr>
			</thead>
			<tbody>
				<g:if test="${transactionEntryList}">
					<g:each var="transactionEntry" in="${transactionEntryList}"
						status="i">
						<tr class="${(i%2==0)?'odd':'even' }">
							<td><g:formatDate
								date="${transactionEntry?.transaction?.transactionDate}"
								format="MMM dd" /></td>
							<td>
							${transactionEntry?.transaction?.transactionType?.name }
							</td>
							<td>
							${transactionEntry?.transaction?.source?.name }
							</td>
														<td>${transactionEntry?.transaction?.destination?.name }</td>
														<td>${transactionEntry?.lotNumber}</td>
														<td style="text-align: right;">${transactionEntry?.quantity}</td>
														<td>
															<g:link controller="inventoryItem" action="deleteTransactionEntry" id="${transactionEntry?.id }" params="['inventoryItem.id':itemInstance?.id]">Remove</g:link>	
														</td>
													</tr>			
												</g:each>
											</g:if>			
											<g:else>
												<tr>
													<td colspan="7" style="text-align: center">
														<div class="fade">enter a transaction below</div>
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
													<th style="text-align: right">
														${inventoryItemList*.quantity?.sum() }
													</th>
													<th></th>
												</tr>
												
											</tfoot>
										</g:if>
									</table>
								</fieldset>
								
								<br/>
								<style>
									.selected { font-weight: bold; } 
								</style>
								<script>
									$(document).ready(function() {
										$("tr.transactionEntry").each(function() {		
											$this = $(this)
											$this.hide();
										});
										$("#initialLink").click(function() { 
											$('#transactionLinkBar a').removeClass('selected');											
											$(this).addClass('selected'); 
											$("tr.transactionEntry").each(function() {		
												$this = $(this)
												$this.hide();
											});
											$("#initial").toggle(); 
										});
										$("#transferLink").click(function() { 
											$('#transactionLinkBar a').removeClass('selected');											
											$(this).addClass('selected'); 
											$("tr.transactionEntry").each(function() {		
												$this = $(this)
												$this.hide();
											});
											$("#transfer").toggle(); 
										});
										$("#adjustmentLink").click(function() { 
											$('#transactionLinkBar a').removeClass('selected');											
											$(this).addClass('selected'); 
											$("tr.transactionEntry").each(function() {		
												$this = $(this)
												$this.hide();
											});
											$("#adjustment").toggle(); 

										});
										$("#consumptionLink").click(function() { 
											$('#transactionLinkBar a').removeClass('selected');											
											$(this).addClass('selected'); 
											$("tr.transactionEntry").each(function() {		
												$this = $(this)
												$this.hide();
											});
											$("#consumption").toggle(); 
										});
										$("#expirationLink").click(function() { 
											$('#transactionLinkBar a').removeClass('selected');											
											$(this).addClass('selected'); 
											$("tr.transactionEntry").each(function() {		
												$this = $(this)
												$this.hide();
											});
											$("#expiration").toggle(); 
										});
										$("#otherLink").click(function() { 											
											$('#transactionLinkBar a').removeClass('selected');											
											$(this).addClass('selected'); 
											$("tr.transactionEntry").each(function() {		
												$this = $(this)
												$this.hide();
											});
											$("#other").toggle(); 
										});
									});
								</script>

								<script>
									$(document).ready(function() {
										$("#transactionEntryButton").show();
										$("#transactionEntryForm").hide();
										
										$("#addTransactionEntryLink").click(function() { 
											$("#transactionEntryButton").hide(''); 
											$("#transactionEntryForm").show(''); 
										});
										$("#closeTransactionEntryLink").click(function() { 
											$("#transactionEntryButton").show(''); 
											$("#transactionEntryForm").hide(''); 
										});

									});
								</script>		
								
								<div id="transactionEntryButton" style="text-align: right;">
									<a href="#" id="addTransactionEntryLink">
										<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}"/>
										add new transaction</a>
								</div>																	
	
								<div id="transactionEntryForm">						
									<div style="text-align: right">			
										<a href="#" id="closeTransactionEntryLink">
										<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/>
										close</a>
									</div>									
									<fieldset>				
										<legend>New Transaction</legend>						
										<g:if test="${inventoryItemList }">
											<div id="transactionLinkBar">
												<a href="#" id="initialLink">
													<img src="${resource(dir: 'images/icons/silk', file: 'door_in.png')}"/>
													enter intial stock</a> &nbsp;<span class="fade">|</span>&nbsp;
												<a href="#" id="transferLink">
													<img src="${resource(dir: 'images/icons/silk', file: 'door_out.png')}"/>
													transfer stock</a> &nbsp;<span class="fade">|</span>&nbsp;
												<a href="#" id="adjustmentLink">
													<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}"/>
													adjust stock</a> &nbsp;<span class="fade">|</span>&nbsp;
												<a href="#" id="consumptionLink">
													<img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}"/>
													consume stock</a> &nbsp;<span class="fade">|</span>&nbsp; 
												<a href="#" id="expirationLink">
													<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/>
													expire stock</a> &nbsp;<span class="fade">|</span>&nbsp; 
												<a href="#" id="otherLink">
													other</a> 
											</div>
	
											<table id="transactionTable">
												<tr id="initial" class="transactionEntry">
													<td>												
														<g:form action="postTransactionEntry" autocomplete="off">
															<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
															<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
															<g:hiddenField name="product.id" value="${productInstance?.id}"/>
															<g:hiddenField name="createdBy.id" value="${session?.user?.id }"/>											
															<g:hiddenField name="transactionType.id" value="7"/>
															<g:hiddenField name="source.id" value="${session?.warehouse?.id }"/>											
															<g:hiddenField name="destination.id" value="${session?.warehouse?.id }"/>											
															<table>
																<tr>
																	<td nowrap>
																		Enter initial quantity  
																			<g:textField name="quantity" size="3" value=""/> units
																		units of
																		<select name="lotNumber">
																			<option value=""></option>
																			<g:each var="inventoryItem" in="${inventoryItemList}">
																				<option value="${inventoryItem?.lotNumber }">${inventoryItem?.lotNumber }</option>
																			</g:each>
																		</select>	
																		<g:jqueryDatePicker id="transactionDate" name="transactionDate" value="" format="MM/dd/yyyy"/>
																		
																	</td>
																	<td>
																		<input type=submit value="Submit">													
																	
																	</td>
															
															</table>
															<br/>
															<span class="fade">(use negative quantity to subtract stock)</span>
														</g:form>
													</td>
												</tr>
												<tr id="transfer" class="transactionEntry">
													<td>
													
														<g:form action="postTransactionEntry" autocomplete="off">
															<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
															<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
															<g:hiddenField name="product.id" value="${productInstance?.id}"/>
															<g:hiddenField name="createdBy.id" value="${session?.user?.id }"/>											
															<g:hiddenField name="transactionType.id" value="1"/>
													
															Transfer 
																<g:textField name="quantity" size="3" value=""/> 
															units of
															<select name="lotNumber">
																<option value=""></option>
																<g:each var="inventoryItem" in="${inventoryItemList}">
																	<option value="${inventoryItem?.lotNumber }">${inventoryItem?.lotNumber }</option>
																</g:each>
															</select>
															<br/>
															from 		
																<g:select name="source.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" 
																	optionKey="id" optionValue="name" value="" noSelection="['0': '']" />							
															to
																<g:select name="destination.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" 
																	optionKey="id" optionValue="name" value="" noSelection="['0': '']" />
		
															<g:jqueryDatePicker id="transactionDate-transfer" name="transactionDate" value="" format="MM/dd/yyyy"/>
															
															
															<input type=submit value="Submit">													
															<br/>
															<span class="fade">(use negative quantity to subtract stock)</span>
														</g:form>
													</td>
												</tr>
												<tr id="consumption" class="transactionEntry">
													<td>
														<g:form action="postTransactionEntry" autocomplete="off">
															<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
															<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
															<g:hiddenField name="product.id" value="${productInstance?.id}"/>
															<g:hiddenField name="createdBy.id" value="${session?.user?.id }"/>											
															<g:hiddenField name="transactionType.id" value="2"/>
															<g:hiddenField name="source.id" value="${session?.warehouse?.id }"/>											
															<g:hiddenField name="destination.id" value="${session?.warehouse?.id }"/>											
															Consume 
															<g:textField name="quantity" size="3" value=""/> units
															 units from 
															<select name="lotNumber">
																<option value=""></option>
																<g:each var="inventoryItem" in="${inventoryItemList}">
																	<option value="${inventoryItem?.lotNumber }">${inventoryItem?.lotNumber }</option>
																</g:each>
															</select>
															
															<g:jqueryDatePicker id="transactionDate-consumption" name="transactionDate" value="" format="MM/dd/yyyy"/>
															
															
															<input type=submit value="Submit">
															<br/>
															<span class="fade">(use negative quantity to subtract stock)</span>
														</g:form>
													</td>
												</tr>
												
												<tr id="adjustment" class="transactionEntry">
													<td>
														<g:form action="postTransactionEntry" autocomplete="off">
															<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
															<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
															<g:hiddenField name="product.id" value="${productInstance?.id}"/>
															<g:hiddenField name="createdBy.id" value="${session?.user?.id }"/>											
															<g:hiddenField name="transactionType.id" value="3"/>
															<g:hiddenField name="source.id" value="${session?.warehouse?.id }"/>											
															<g:hiddenField name="destination.id" value="${session?.warehouse?.id }"/>											
															Adjust inventory by adding 
																<g:textField name="quantity" size="3" value=""/> units
															to lot 
															<select name="lotNumber">
																<option value=""></option>
																<g:each var="inventoryItem" in="${inventoryItemList}">
																	<option value="${inventoryItem?.lotNumber }">${inventoryItem?.lotNumber }</option>
																</g:each>
															</select>
															<input type=submit value="Submit">
															
															<g:jqueryDatePicker id="transactionDate-adjustment" name="transactionDate" value="" format="MM/dd/yyyy"/>
															
															
															<br/>
															<span class="fade">(use negative quantity to subtract stock)</span>
														</g:form>
													</td>
												</tr>
												<tr id="expiration" class="transactionEntry">
													<td>
														<g:form action="postTransactionEntry" autocomplete="off">
															
															<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
															<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
															<g:hiddenField name="product.id" value="${productInstance?.id}"/>
															<g:hiddenField name="createdBy.id" value="${session?.user?.id }"/>											
															<g:hiddenField name="transactionType.id" value="4"/>
															<g:hiddenField name="source.id" value="${session?.warehouse?.id }"/>											
															<g:hiddenField name="destination.id" value="${session?.warehouse?.id }"/>											
															<g:hiddenField name="quantity" value="0"/>
															<div style="background-color: #efdfb7">
																Lot 
																<select name="lotNumber">
																	<option value=""></option>
																	<g:each var="inventoryItem" in="${inventoryItemList}">
																		<option value="${inventoryItem?.lotNumber }">${inventoryItem?.lotNumber }</option>
																	</g:each>
																</select>
																has expired. 																
																
																<input type=submit value="Submit">
																<br/>
																
																<g:jqueryDatePicker id="transactionDate-expiration" name="transactionDate" value="" format="MM/dd/yyyy"/>
															
																
																<span class="fade">(use negative quantity to subtract stock)</span>															
															</div>
														</g:form>											
													</td>
												</tr>
												
												<tr id="other" class="transactionEntry">
													<td>
														
														<g:form action="postTransactionEntry" autocomplete="off">
															<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
															<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
															<g:hiddenField name="product.id" value="${productInstance?.id}"/>
															<g:hiddenField name="createdBy.id" value="${session?.user?.id }"/>									
															
															<table bgcolor="#efdfb7" border=0 cellspacing=5 cellpadding=5>
																<tr>
																	<th>Date</th>
																	<th>Type</th>
																</tr>
																<tr>
																	<td>
																		<g:jqueryDatePicker id="transactionDate" name="transactionDate" value="" format="MM/dd/yyyy"/>
																	</td>
																	<td>
																		<g:select name="transactionType.id" from="${org.pih.warehouse.inventory.TransactionType.list()}" 
																			optionKey="id" optionValue="name" value=""  />
																	</td>
																</tr>
																<tr>
																	<th>Lot Number</th>
																	<th>Qty </th>	
																</tr>
																<tr>
																	<td align=center>
																		<g:textField name="lotNumber" size="10"/>
																		
																	</td>
																	<td align=center>
																		<g:textField name="quantity" size="3"/>
																	</td>							
																</tr>
																<tr>
																	<th>Source</th>
																	<th>Destination</th>
																</tr>
																<tr>
																	<td>
																		<g:select name="source.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" 
																			optionKey="id" optionValue="name" value="" noSelection="['0': '']" />							
																	</td>
																	<td>
																		<g:select name="destination.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" 
																			optionKey="id" optionValue="name" value="" noSelection="['0': '']" />
																	</td>
																</tr>
																<tr>
																	<td colspan="2" align=center>
																		<input type=submit value="Submit">
																	</td>
																</tr>
															</table>
														</g:form>
														<span class="fade">(use negative quantity to subtract stock)</span>											
													</td>
												</tr>
											</table>		
										</g:if>
										<g:else>
											You must add a lot before entering quantities.
										</g:else>								
									</fieldset>
								</div>
							</td>
						
							<td width="25%">
							
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
								
									
								<div id="inventoryLotReport" style="text-align: right;">										
									
								
								
									<fieldset>
										<legend>Quantity by Lots</legend>
										
										<div style="text-align: right">
											<a href="#" id="showInventoryLotFormLink">
												<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}"/>
												add new lot</a>
										</div>
										
										<table border="1" style="border:1px solid #f5f5f5">
											<thead>
												<tr>
													<th>Lot Number</th>
													<th>Expires</th>
													<th>Qty</th>
												</tr>											
											</thead>
											<tbody>
												<g:each var="itemInstance" in="${inventoryItemList }">				
													<tr>
														<td>${itemInstance?.lotNumber?:'<span class="fade">empty</span>' }</td>
														<td>
															<g:if test="${itemInstance?.inventoryLot?.expirationDate}">
																<g:formatDate date="${itemInstance?.inventoryLot?.expirationDate }" format="dd/MMM/yy" />
															</g:if>
															<g:else>
																<span class="fade">n/a</span>
															</g:else>
															
															
														</td>
														<td style="text-align: center;">${itemInstance?.quantity }</td>												
													</tr>
												</g:each>
											</tbody>
											<tfoot>
												<tr>
													<th></th>
													<th></th>
													<th style="text-align: center;">${inventoryItemList*.quantity.sum() }</th>
												</tr>
											</tfoot>
										</table>
									</fieldset>										
								</div>										


							
								<div id="inventoryLotForm">
									<g:form action="postInventoryLot" autocomplete="off">
										<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
										<g:hiddenField name="product.id" value="${productInstance?.id}"/>
										<g:hiddenField name="active" value="true"/>
										<g:hiddenField name="createdBy" value="${session?.user?.id }"/>									
										<g:hiddenField name="inventoryItemType" value="${org.pih.warehouse.inventory.InventoryItemType.NON_SERIALIZED}"/>
											
										<g:hasErrors bean="${inventoryLotInstance}">
								            <div class="errors">
								                <g:renderErrors bean="${inventoryLotInstance}" as="list" />
								            </div>
							            </g:hasErrors>									
																
										
										<fieldset>
											<legend>Quantity by Lots</legend>
											
											<div style="text-align: right">
												<a href="#" id="showInventoryLotReportLink">
													<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/>
													close</a>
											</div>
													
											
											<table border="1" style="border:1px solid #f5f5f5">
												<thead>
													<tr>
														<th>Lot Number</th>
														<th>Expires</th>
														<th>Actions</th>
													</tr>											
												</thead>
												<tbody>
													<g:each var="itemInstance" in="${inventoryItemList }" status="i">				
														<tr class="${(i%2 == 0)?'odd':'even' }">
															<td>${itemInstance?.lotNumber?:'<span class="fade">empty</span>' }</td>
															<td><g:formatDate date="${itemInstance?.inventoryLot?.expirationDate }" format="dd/MMM/yy" /></td>
															<td nowrap>
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
															<g:submitButton name="submit" value="Add"/>
														</td>
													</tr>
												</tbody>
											</table>
										</fieldset>
									</g:form>
								</div>
								<br/>
								
								
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
									<fieldset>				
										<legend>Warning Levels</legend>
										<div style="text-align: right">			
											<a href="#" id="configureWarningLevelsLink">
											<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}"/>
											configure</a>
										</div>
										<table>
											<tr class="prop">
												<td class="name"><label>Minimum Quantity </label></td>
												<td class="value">
													${itemInstance?.minQuantity?:0 }
												</td>
											</tr>
											<tr class="prop">
												<td class="name"><label>Reorder Quantity</label></td>
												<td class="value">
													${itemInstance?.reorderQuantity?:0 }
												</td>
											</tr>
											<tr class="prop">
												<td class="name"><label>Maximum Quantity</label></td>
												<td class="value">
													${itemInstance?.maxQuantity?:0}
												</td>
											</tr>
										</table>			
									</fieldset>
								</div>
								
								<div id="configureWarningLevels">
									<g:form action="update">
										<g:hiddenField name="id" value="${itemInstance?.id}"/>
										<g:hiddenField name="inventory.id" value="${itemInstance?.inventory?.id}"/>
										<fieldset>				
											<legend>Warning Levels</legend>
											<div style="text-align: right">			
												<a href="#" id="showWarningLevelsLink">
												<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/>
												close</a>
											</div>
											<table>
												<tr class="prop">
													<td class="name"><label>Minimum Quantity </label></td>
													<td class="value">
														<g:textField name="minQuantity" value="${itemInstance?.minQuantity }" size="3"/>
													</td>
												</tr>
												<tr class="prop">
													<td class="name"><label>Reorder Quantity</label></td>
													<td class="value">
														<g:textField name="reorderQuantity" value="${itemInstance?.reorderQuantity }" size="3"/>
													</td>
												</tr>
												<tr class="prop">
													<td class="name"><label>Maximum Quantity</label></td>
													<td class="value">
														<g:textField name="maxQuantity" value="${itemInstance?.maxQuantity }" size="3"/>
													</td>
												</tr>
												<tr class="prop">
													<td colspan="2">
														<div class="buttons" style="text-align: center;">
										                    <g:actionSubmit class="save" action="update" value="${message(code: 'default.button.save.label', default: 'Save')}" />
									                    </div>
													</td>
												</tr>
											</table>
										</fieldset>									
									</g:form>								
								</div>				
								
													
							</td>
						</tr>
					</table>
				
				</fieldset>
				
				
			</div>			
		</div>
	</body>
</html>
