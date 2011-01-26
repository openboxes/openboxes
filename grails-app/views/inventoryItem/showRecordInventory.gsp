
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName" value="${message(code: 'inventory.label', default: 'inventory')}" />
<title><g:message code="inventory.record.label" args="[entityName]" default="Record Inventory"/></title>
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
	
	
		<div class="stockOptions" >
			<ul>
				<li>							
					<g:link controller="inventoryItem" 
						action="showStockCard" params="['product.id':commandInstance?.product?.id]">&lsaquo; Back to Stock Card</g:link>							
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
						

		<table >
			<tr>
				<td style="width: 250px;">
					<g:render template="productDetails" model="[productInstance:commandInstance?.product, inventoryLevelInstance: commandInstance?.inventoryLevel]"/>
				</td>
				<td>				
						
					<div>
							<div id="inventoryForm">
								<g:form action="saveRecordInventory" autocomplete="off">
									<g:hiddenField name="product.id" value="${commandInstance.product?.id}"/>
									<g:hiddenField name="inventory.id" value="${commandInstance?.inventory?.id}"/>
								<%--
									<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
									<g:hiddenField name="active" value="true"/>
									<g:hiddenField name="initialQuantity" value="0"/>							
									<g:hiddenField name="inventoryItemType" value="${org.pih.warehouse.inventory.InventoryItemType.NON_SERIALIZED}"/>
								 --%>	
									<div style="padding: 10px; text-align: left;">
										<label>Inventory date:</label>
										<g:jqueryDatePicker 
											id="transactionDate" 
											name="transactionDate" 
											value="${commandInstance?.transactionDate}" 
											format="MM/dd/yyyy"
											showTrigger="false" />
									</div>

									<script>
										$(document).ready(function() {
											$(".buttonUp").click(function(event, data) { 
												var status = this.id;
												var qtyFieldId = "#newQuantity-" + status + "";
												var value = parseInt($(qtyFieldId).val());
												
												$(qtyFieldId).val(value+1);
												event.preventDefault();
											});
											$(".buttonDown").click(function(event, data) { 
												var status = this.id;
												var qtyFieldId = "#newQuantity-" + status + "";
												var value = parseInt($(qtyFieldId).val());
												$(qtyFieldId).val(value-1);
												event.preventDefault();
											});
										});
									</script>
									
									<table id="recordInventoryTable" style="border: 1px solid lightgrey" border="1">
										<thead>
											<tr>
												<th>ID</th>
												<th>Lot/Serial #</th>
												<th>Expires</th>
												<th>Description</th>
												<th style="text-align:center;">Old Qty</th>
												<th style="text-align:center;">New Qty</th>
											</tr>											
										</thead>
										<tbody>
											<g:each var="recordInventoryRow" in="${commandInstance?.recordInventoryRows }" status="status">				
												<tr class="${(status%2==0)?'odd':'even' }">
													<td width="5%">
														${status+1 }
														<g:hiddenField name="recordInventoryRows[${status}].id" value="${recordInventoryRow?.id }"/>
													</td>
													<td width="15%">
														<%-- 
														<g:textField name="recordInventoryRows[${status}].lotNumber" size="10" value="${recordInventoryRow?.lotNumber }"/>
														--%>
														<g:hiddenField name="recordInventoryRows[${status}].lotNumber" value="${recordInventoryRow?.lotNumber }"/>
														${recordInventoryRow?.lotNumber }
													</td>
													<td width="10%">
														<%-- 
														<g:jqueryDatePicker id="expirationDate${status }" name="recordInventoryRows[${status}].expirationDate" 
															value="${recordInventoryRow?.expirationDate}" format="MM/dd/yyyy" showTrigger="false" />
														--%>
														<g:hiddenField name="recordInventoryRows[${status}].expirationDate" value="${formatDate(date: recordInventoryRow?.expirationDate, format: 'MM/dd/yyyy') }"/>
														<g:if test="${recordInventoryRow?.expirationDate}">
															<g:formatDate date="${recordInventoryRow?.expirationDate}" format="MMM dd yyyy"/>
														</g:if>
														<g:else>
															<span class="fade">never</span>
														</g:else>
													</td>
													<td width="45%">
														<%-- 
														<g:textField name="recordInventoryRows[${status}].description" size="25" value="${recordInventoryRow?.description }"/>
														--%>
														<g:hiddenField name="recordInventoryRows[${status}].description" value="${recordInventoryRow?.description }"/>
														${recordInventoryRow?.description }
													</td>
													<td width="10%" style="text-align: center; vertical-align: middle;">
														${recordInventoryRow?.oldQuantity }	
														<g:hiddenField name="recordInventoryRows[${status}].oldQuantity" value="${recordInventoryRow?.oldQuantity }"/>
													</td>	
													<td width="10%" style="text-align: center; vertical-align: middle;">
														<g:textField style="text-align: center;"  id="newQuantity-${status }" name="recordInventoryRows[${status }].newQuantity" size="3" value="${recordInventoryRow?.newQuantity }" onFocus="this.select();" onClick="this.select();"/>
													</td>	
													<td>
														<button id="${status }" class="buttonUp">
															<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_up.png') }"/>
														</button>
														<button id="${status }" class="buttonDown">
															<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_down.png') }"/>
														</button>
													</td>
												</tr>
											</g:each>
											<%-- 
											
											<tr>
												<td></td>
												<td> 
													<input type="text" id="lotNumber" name="lotNumber" size="5" />
												</td>
												<td> 
													<input type="text" id="lotNumberDate" name="lotNumberDate" size="5"/>
												</td>
												<td> 
													<input type="text" id="lotNumberDescription" name="lotNumberDescription" size="10" />
												</td>
												
											</tr>

											<tr>
												<td></td>
												<td colspan="4">
													Add another item:
													<g:lotNumberComboBox id="lotNumberWidget" name="lotNumberWidget"></g:lotNumberComboBox>
												</td>
											</tr>

											--%>
											
											<%-- 
											
											<g:set var="count" value="${commandInstance?.recordInventoryRows?.size() }"/>
											<tr class="${count%2==0?'odd':'even'}">
												<td>
													<span class="fade">${count+1 }</span>
												</td>
												<td class="${hasErrors(bean:user,field:'lotNumber', 'errors')}">
													<g:textField name="recordInventoryRows[${count }].lotNumber" size="10" value=""/>
												</td>
												<td class="${hasErrors(bean:user,field:'expirationDate', 'errors')}" nowrap>
													<g:jqueryDatePicker id="expirationDate" name="recordInventoryRows[${count }].expirationDate" 
														value="" format="MM/dd/yyyy" showTrigger="false" />
												</td>
												<td class="${hasErrors(bean:user,field:'description', 'errors')}">
													<g:textField name="recordInventoryRows[${count }].description" size="25" value=""/>
												</td>
												<td style="text-align: center;">
	
												</td>
												<td style="text-align: center;" class="${hasErrors(bean:user,field:'quantity', 'errors')}">
													<g:textField name="recordInventoryRows[${count }].quantity" size="3" value=""/>
												</td>
											</tr>
											--%>
										</tbody>										
									</table>
									<div style="text-align: center; border-top: 1px solid lightgrey; padding:10px;">
										<span class="buttons">
											<g:submitButton name="save" value="Save"/>
										</span>
										<g:link controller="inventoryItem" action="showStockCard" params="['product.id':commandInstance.product?.id]">Cancel</g:link>
									</div>												
								</g:form>
							</div>									
					</div>
				</td>
			</tr>
		</table>
	</div>			
</div>
</body>
</html>
