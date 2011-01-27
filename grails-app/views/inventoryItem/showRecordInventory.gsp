
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName" value="${message(code: 'inventory.label', default: 'inventory')}" />
<title><g:message code="inventory.record.label" args="[entityName]" default="Record Inventory"/></title>
<script>
	// Keep track of the inventory items on the page 
	var inventory = { InventoryItems: [] };	

	// We need to do this in order to make sure the index for new items is correct
	<g:each var="row" in="${commandInstance?.recordInventoryRows }" status="status">
		var existingInventoryItem = { Id: '${row.id}', Type: '', ProductId: '', LotNumber: '${row.lotNumber}', Description: '${row.description}', ExpirationDate: '${row.expirationDate?:never}', Qty: 0 };	
		inventory.InventoryItems.push(existingInventoryItem);
	</g:each>

	function getIndex() { 
		return $.inArray( this.data, inventory.InventoryItems );
	}
	
	function getClass() {
		return ($.inArray( this.data, inventory.InventoryItems ) % 2) ? "even" : "odd";
	}


	// Return a template selector that matches our  convention of <type>RowTemplate.
	function get_inventoryRowTemplateName(type) {
		//return '#' + type + 'RowTemplate';
		return '#rowTemplate';
	}

	/**
	 * When an item is selected, this is the callback we use to populate the table
	 */
	function onSelectCallback(event, ui) { 
		var inventoryItem = { 
				Id: ui.item.id, 
				Type: '', 
				ProductId: ui.item.productId,
				LotNumber: ui.item.lotNumber, 
				Description: ui.item.description, 
				ExpirationDate: ui.item.expirationDate, 
				Qty: 0 
			};
			
		addItemToTable(inventoryItem);
	}

	var buttonUpHandler = function(event, data) {
		event.preventDefault();
		changeQuantity(this.id, +1);	
	}

	var buttonDownHandler = function(event, data) { 
		event.preventDefault();
		changeQuantity(this.id, -1);	
	}
	
	/**
	 * Adds a new inventory item to the table.
	 */
	function addItemToTable(inventoryItem) { 
		// Add to the array
		inventory.InventoryItems.push(inventoryItem);
		
		// Determine which template should be used to render the new item.
		// Only necessary if we have multiple row types
		//var tmpl = get_inventoryRowTemplateName(inventoryItem.Type);
		
		// Render that template, using the new item as data,
		//  and append that new row to the end of the existing invoice table.
		//$(tmpl).tmpl(inventoryItem).appendTo('#inventoryItemsTable');		

		$("#rowTemplate").tmpl(inventoryItem).appendTo('#inventoryItemsTable');	

		// TODO This is a bit redundant since we are doing this in the , but I couldn't figure out how to get the 
		// buttons in the newly added row to 
		$('.newButtonUp').bind('click', buttonUpHandler);
		$('.newButtonDown').bind('click', buttonDownHandler);	
	}


	function changeQuantity(id, delta){ 
		var qtyFieldId = "#newQuantity-" + id + "";
		var value = parseInt($(qtyFieldId).val());											
		$(qtyFieldId).val(value + delta);
	}

	function populateInventoryItemsArray() { 
		// On page load, we get the other inventory items that were 
		// This doesn't work because when we submit the 
		/*
		$.get('http://localhost:8080/warehouse/inventoryItem/getInventoryItems', { 'product.id': '8' }, function(data) {
			$.each(data, function(index, value) { 
				var inventoryItem =  
					{ Id: value.id, Type: 'item', LotNumber: value.lotNumber, Description: value.description, 
						ExpirationDate: value.expirationDate, Qty: value.quantity }
				inventory.InventoryItems.push(inventoryItem);
				//addItemToTable(inventoryItem);
				
			});
		*/

	}
	
	
	$(document).ready(function() {
		// Bind the click event to the up buttons and call the change quantity function
		$(".buttonUp").click(buttonUpHandler);

		// Bind the click event to the down buttons and call the change quantity function
		$(".buttonDown").click(buttonDownHandler);
	});

</script>


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
		<table >
			<tr>
				<td style="width: 250px;">
				
					<div class="actionsMenu">
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
								
								<table id="inventoryItemsTable" style="border: 1px solid lightgrey" border="1">
									<thead>
										<tr>	
											<th>Index</th>
											<th>ID</th>
											<th>Lot/Serial #</th>
											<th>Expires</th>
											<th>Description</th>
											<th style="text-align:center;">Old Qty</th>
											<th style="text-align:center;">New Qty</th>
											<th></th>
										</tr>											
									</thead>
									<tbody>
										<g:each var="recordInventoryRow" in="${commandInstance?.recordInventoryRows }" status="status">				
											<tr class="${(status%2==0)?'odd':'even' }">
												<td width="5%">
													${status }												
												</td>
												<td width="5%" style="text-align: center;">
													${recordInventoryRow?.id }
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
												<td width="35%">
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
												<td width="10%" style="text-align: center;">
													<button id="${status }" class="buttonUp">
														<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_up.png') }"/>
													</button>
													<button id="${status }" class="buttonDown">
														<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_down.png') }"/>
													</button>
												</td>
											</tr>
										</g:each>
									</tbody>	
									<tfoot>
										<tr>
											<td>
											
											</td>
											<td>
											
											</td>
											<td colspan="3">
												<div id="#lotNumberWidget" style="padding: 0px; text-align: left;">									
													<style>
														#lotNumberWidget { font-size: 1.2em; } 
														#lotNumberWidget-suggest { 
															font-size: 1.2em;  
															background-image: url('/warehouse/images/icons/silk/magnifier.png');
															background-repeat: no-repeat;
															background-position: center left;
															padding: 5px;
															padding-left: 20px;
															width: 350px;
														}
													</style>
													<g:lotNumberComboBox id="lotNumberWidget" onSelectCallback="onSelectCallback" name="lotNumberWidget" display="inline"></g:lotNumberComboBox>
												</div>
											</td>
											<td></td>
											<td></td>
											<td></td>
										</tr>
									</tfoot>
								</table>

								<div style="text-align: center; border-top: 1px solid lightgrey; padding:10px;">
									<span class="buttons">
										<g:submitButton name="save" value="Save"/>
									</span>
									<g:link controller="inventoryItem" action="showStockCard" params="['product.id':commandInstance.product?.id]">Cancel</g:link>
								</div>												
							</g:form>
						</div>
						<div class="result"></div>
					</div>
				</td>
			</tr>
		</table>
	</div>			
</div>



<script id="rowTemplate" type="x-jquery-tmpl">
<tr class="{{= getClass()}}">
	<td width="5%" style="text-align: center;">
		{{= getIndex()}}
	</td>
	<td width="5%" style="text-align: center;">	
		{{= Id}}
		<g:hiddenField name="recordInventoryRows[{{= getIndex()}}].id" value="{{= Id}}" size="1" />
	</td>		
	<td width="15%">
		<g:hiddenField name="recordInventoryRows[{{= getIndex()}}].lotNumber" value="{{= LotNumber}}"/>
		{{= LotNumber}}
	</td>
	<td width="10%">
		<g:hiddenField name="recordInventoryRows[{{= getIndex()}}].expirationDate" value="{{= ExpirationDate}}"/>
		{{= ExpirationDate}}
	</td>
	<td width="35%">
		<g:hiddenField name="recordInventoryRows[{{= getIndex()}}].description" value="{{= Description}}"/>
		{{= Description}}
	</td>
	<td width="10%" style="text-align: center; vertical-align: middle;">
		{{= Qty}}
		<g:hiddenField name="recordInventoryRows[{{= getIndex()}}].oldQuantity" value="{{= Qty}}"/>
	</td>	
	<td width="10%" style="text-align: center; vertical-align: middle;">
		<g:textField style="text-align: center;"  
			id="newQuantity-{{= getIndex()}}" name="recordInventoryRows[{{= getIndex()}}].newQuantity" size="3" value="{{= Qty}}" onFocus="this.select();" onClick="this.select();"/>
	</td>	
	<td width="10%" style="text-align: center;">
		<button id="{{= getIndex()}}" class="newButtonUp">
			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_up.png') }"/>
		</button>
		<button id="{{= getIndex()}}" class="newButtonDown">
			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_down.png') }"/>
		</button>
	</td>
</tr>
</script>									


<script id="inventoryTableTemplate" type="x-jquery-tmpl">
	<table class="inventoryItems" border="1">	
		{{each InventoryItems}}
			{{tmpl($value) get_inventoryRowTemplateName(Type)}}
		{{/each}}
	</table>
</script>


</body>
</html>

											

								<%--
								<table id="newItemsTable" border="1" style="border: 1px solid lightgrey;">
									<thead>
										<tr class="even">
											<th>ID</th>
											<th>Lot/Serial #</th>
											<th>Expires</th>
											<th>Description</th>
											<th style="text-align:center;">Old Qty</th>
											<th style="text-align:center;">New Qty</th>
											<th></th>
										</tr>											
									</thead>
									<tbody>
										<tr class="odd" style="display: none;">
											<td width="10%">{which}</td>
											<td width="15%">
												<!-- These are the elements that are updated -->
												<input id="lotNumberWidget-id" type="hidden" name="lotNumber" value=""/>
												<span id="lotNumberWidget-name"></span>
											</td>
											<td width="10%">												
												<span id="lotNumberWidget-date">Date:</span>
											</td>
											<td width="35%">
												<span id="lotNumberWidget-description">Description: </span>
											</td>
											<td width="10%">
												0
											</td>
											<td width="10%">
												<input type="text" id="lotNumberWidget-quantity" name="quantity" value=""/>
											</td>
											<td width="10%">
											</td>
										</tr>
									</tbody>
										<!-- 
										
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
										 --> 
									</tbody>										
								</table>
								--%>
