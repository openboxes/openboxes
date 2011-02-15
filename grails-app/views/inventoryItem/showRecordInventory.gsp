
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
	<g:each var="row" in="${commandInstance?.recordInventoryRows.findAll{ it.newQuantity > 0 } }" status="status">
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
		return '#' + type + 'RowTemplate';
		//return '#rowTemplate';
	}

	/**
	 * When an item is selected, this is the callback we use to populate the table
	function onSelectCallback(event, ui) { 
		var inventoryItem = { 
				Id: ui.item.id, 
				Type: 'existing', 
				ProductId: ui.item.productId,
				LotNumber: ui.item.lotNumber, 
				Description: ui.item.description, 
				ExpirationDate: ui.item.expirationDate, 
				Qty: 0 
			};

		// Indicates whether we display the lotnumber, etc as read-only or not
		inventoryItem.Type = (ui.item.exists) ? 'existing' : 'new';
			
		addItemToTable(inventoryItem);
	}
	 */

	function onSelectCallback(id, event, ui) { 
		alert(id);
	}

	 
	function addNewInventoryItem(id, type, productId, lotNumber, description, expDate, qty) { 
		var inventoryItem = 
			{ Id: id, Type: 'new', ProductId: productId,LotNumber: lotNumber, Description: description, ExpirationDate: expDate, Qty: 0 };


	}
	
	var buttonUpMouseHandler = function(event, data) {
		event.preventDefault();		
		var accelerator = (event.ctrlKey) ? 100 : (event.shiftKey) ? 10 : 1;
		changeQuantity($(this).parent().parent().find('.newQuantity'), +1 * accelerator);	
	}

	var buttonDownMouseHandler = function(event, data) { 
		event.preventDefault();
		var accelerator = (event.ctrlKey) ? 100 : (event.shiftKey) ? 10 : 1;
		changeQuantity($(this).parent().parent().find('.newQuantity'), -1 * accelerator);	
	}
	
	/**
	 * Adds a new inventory item to the table.
	 */
	function addItemToTable(inventoryItem) { 
		// Add to the array
		inventory.InventoryItems.push(inventoryItem);		
		// Determine which template should be used to render the new item.
		// Only necessary if we have multiple row types
		var tmpl = get_inventoryRowTemplateName(inventoryItem.Type);
		
		// Render that template, using the new item as data,
		//  and append that new row to the end of the existing invoice table.
		$(tmpl).tmpl(inventoryItem).appendTo('#inventoryItemsTable');		
		//$("#rowTemplate").tmpl(inventoryItem).appendTo('#inventoryItemsTable');	

		// TODO This is a bit redundant since we are doing this in the , but I couldn't figure out how to get the 
		// buttons in the newly added row to 
		//$('.newButtonUp').bind('click', buttonUpHandler);
		//$('.newButtonDown').bind('click', buttonDownHandler);	
	}


	function changeQuantity(textField, delta){ 
		textField.val(parseInt(textField.val()) + delta);
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

	function removeRow(index) { 
		$('#row-' + index).remove();
	}

	$(document).ready(function() {

		$('#addAnother').focus();		
		$('#addAnother').click(function(event) { 
			event.preventDefault();
			var inventoryItem = {Qty: 0};

			// Add to the array of new inventory items
			inventory.InventoryItems.push(inventoryItem);

			// Add a new row to the table
			$("#newRowTemplate").tmpl(inventoryItem).appendTo('#inventoryItemsTable');		

			//$("#inventoryItemsTable tbody tr:even").addClass("even");
			//$("#inventoryItemsTable tbody tr:odd").addClass("odd");

			
			$('#inventoryItemsTable tbody tr:last').find('.lotNumber').focus();
					
		});


		//$('.newQuantity').livequery(function() { 
		//	$(this).blur(function() { $('#addAnother').focus();	});
		//});
		

		$('.lotNumber').livequery(function() {
			//$(this).addClass("fade");
			//$(this).val("Enter serial number, lot number, or barcode");
			//$(this).click(function() { $(this).val(""); });
			//$(this).focus(function() { $(this).val(""); });
			
			$(this).autocomplete( {
				source: function(req, add){
					$.getJSON('/warehouse/json/findLotsByName', req, function(data) {
						var items = [];
						$.each(data, function(i, item) {
							items.push(item);
						});
						add(items);
					});
				},
		        focus: function(event, ui) {	
			        //alert("focus");
					//$('#lotNumberWidget-suggest').val("");
		        },	
		        change: function(event, ui) { 
					//$('#lotNumberWidget-id').val(0);
					var lotNumber = $(this).parent().parent().find('.lotNumber');
					var lotNumberValue = $(this).parent().parent().find('.lotNumberValue');
					lotNumberValue.val(lotNumber.val());				
					
		        },
		        close: function(even, ui) { 
				},
				select: function(event, ui) {

					var id = $(this).parent().parent().find('.id');
					var lotNumber = $(this).parent().parent().find('.lotNumber');
					var lotNumberValue = $(this).parent().parent().find('.lotNumberValue');
					var quantity = $(this).parent().parent().find('.newQuantity');
					var expirationDate = $(this).parent().parent().find('.expirationDate');
					var description = $(this).parent().parent().find('.description');


					lotNumber.val(ui.item.value);
					lotNumberValue.val(ui.item.value);

					if (description.val() == '') { 
						quantity.focus();
						quantity.select();					
					}
					else { 
						description.focus();
					}
					 
						
					//alert("selected " + ui.item)
					/*
					$('#lotNumberWidget-id').val(ui.item.value);
					$('#lotNumberWidget-span').html(ui.item.valueText);
					$('#lotNumberWidget-name').html(ui.item.lotNumber);
					$('#lotNumberWidget-description').html(ui.item.description);
					if (ui.item.expirationDate=='') {
						$('#lotNumberWidget-date').html(ui.item.expirationDate);
					}
					else { 
						$('#lotNumberWidget-date').html('<span class="fade">never</span>');								
					}
					$('#lotNumberDescription').val(ui.item.description);
					$('#lotNumberDate').val(ui.item.expirationDate);
					$('#lotNumberWidget-suggest').focus();
					*/

					
					// Set the lot number and disable the field
					//$(this).attr('disabled', 'disabled');
					
					// Set the ID
					
					id.val(ui.item.id);
					// Set the lot number and disable the field
					
					if (ui.item.expirationDate) {
						expirationDate.val(ui.item.expirationDate);						
					}
					else { 
						expirationDate.val('never');
					}
					//expirationDate.attr('disabled', 'disabled');
					
					// Set the description and disable the field (unless it's null)						
					
					description.val(ui.item.description);
					//description.addClass("fade");
					//if (description.val()) { 
					//	description.attr('disabled', 'disabled');
					//}
					
				
				}							  
			});				
		});
		$('.expirationDate').livequery(function() {
			var altFieldId = "#" + $(this).attr('id') + "-hidden"; 
			console.log(altFieldId);
			$(this).datepicker({
				altField: altFieldId,
				altFormat: 'mm/dd/yy',
				dateFormat: 'dd M yy',
				buttonImageOnly: true, 
				buttonImage: '/warehouse/images/icons/silk/calendar.png',
			});								
		});

		$('.description').livequery(function() { 
			$(this).autocomplete( {
				source: function(req, add){
					$.getJSON('/warehouse/json/findDescriptionByName', req, function(data) {
						var items = [];
						$.each(data, function(i, item) {
							items.push(item);
						});
						add(items);
					});
				},
				select: function(event, ui) {

					$(this).parent().parent().find('.description').val(ui.item.value);
					$(this).parent().parent().find('.descriptionValue').val(ui.item.value);
					
				}
			});
		});

		// Bind the click event to the up buttons and call the change quantity function
		$(".buttonUp").livequery(function() { 
			$(this).click(buttonUpMouseHandler);
		});

		// Bind the click event to the down buttons and call the change quantity function
		$(".buttonDown").livequery(function() { 
			$(this).click(buttonDownMouseHandler);
		});

		
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
		<table>
			<tr>
				<td style="width: 250px;">
					<div class="actionsMenu">
						<ul>
							<li>							
								<g:link controller="inventoryItem" 
									action="showStockCard" params="['product.id':commandInstance?.product?.id]">&lsaquo; Back to stock card</g:link>							
							</li>
						</ul>
					</div>	
					<br/>				
					<g:render template="productDetails" model="[productInstance:commandInstance?.product, inventoryInstance: inventoryInstance, inventoryLevelInstance: inventoryLevelInstance, totalQuantity: totalQuantity]"/>
				</td>
				<td>				
						
					<div class="actionsMenu">
						<ul>
							<li>								
						
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
					<div id="inventoryForm">		
						<g:form action="saveRecordInventory" autocomplete="off">
							<style>
								.form-content { 
									border: 0px solid black;
									min-height: 100%;
									height: auto !important;
									height: 100%;
									margin: 0 auto -4em;
									padding: 10px;
								}
								.form-footer {
									text-align:right; 
									height: 4em;
									clear: both;
								}	
								fieldset { 
									min-height: 500px;
								}
							</style>
						
							<fieldset>
								<legend>Record Current Inventory</legend>
								
								<g:hiddenField name="product.id" value="${commandInstance.product?.id}"/>
								<g:hiddenField name="inventory.id" value="${commandInstance?.inventory?.id}"/>
								<%--
									<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
									<g:hiddenField name="active" value="true"/>
									<g:hiddenField name="initialQuantity" value="0"/>							
									<g:hiddenField name="inventoryItemType" value="${org.pih.warehouse.inventory.InventoryItemType.NON_SERIALIZED}"/>
								 --%>	
								<div style="padding: 10px">
									<label>Inventory date:</label>
									<g:jqueryDatePicker 
										id="transactionDate" 
										name="transactionDate" 
										value="${commandInstance?.transactionDate}" 
										format="MM/dd/yyyy"
										showTrigger="false" />						
								</div>
													
								<div class="form-content">
									<table id="inventoryItemsTable" style="border: 0px solid lightgrey" border="1">
										<thead>
											<tr>	
												<th>Lot/Serial #</th>
												<th>Expires</th>
												<th>Description</th>
												<th style="text-align:center;">Old Qty</th>
												<th style="text-align:center;">New Qty</th>
												<th>Actions</th>
											</tr>											
										</thead>									
										<tbody>
											<style>
												.selected-row { background-color: #ffffe0; } 
											</style>
											<g:each var="recordInventoryRow" in="${commandInstance?.recordInventoryRows.findAll{it.newQuantity > 0} }" status="status">
												<g:set var="styleClass" value="${params?.inventoryItem?.id && recordInventoryRow?.id == Integer.valueOf(params?.inventoryItem?.id) ? 'selected-row' : ''}"/>
												
												<tr class="${(status%2==0)?'odd':'even' } ${styleClass}">
													<td width="15%">
														<%-- 
														<g:textField name="recordInventoryRows[${status}].lotNumber" size="10" value="${recordInventoryRow?.lotNumber }"/>
														--%>
														<g:hiddenField name="recordInventoryRows[${status}].id" value="${recordInventoryRow?.id }"/>
														<g:hiddenField name="recordInventoryRows[${status}].lotNumber" value="${recordInventoryRow?.lotNumber }"/>
														${recordInventoryRow?.lotNumber?:'<span class="fade">EMPTY</span>' }
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
													<td width="25%">
														<%-- 
														<g:textField name="recordInventoryRows[${status}].description" size="25" value="${recordInventoryRow?.description }"/>
														--%>
														<g:hiddenField name="recordInventoryRows[${status}].description" value="${recordInventoryRow?.description }"/>
														${(recordInventoryRow?.description)?:'<span class="fade">None</span>'}
													</td>
													<td width="10%" style="text-align: center; vertical-align: middle;">
														${recordInventoryRow?.oldQuantity }	
														<g:hiddenField name="recordInventoryRows[${status}].oldQuantity" value="${recordInventoryRow?.oldQuantity }"/>
													</td>	
													<td width="10%" style="text-align: center; vertical-align: middle;">
														<g:textField style="text-align: center;" id="newQuantity-${status }" class="newQuantity" name="recordInventoryRows[${status }].newQuantity" size="3" value="${recordInventoryRow?.newQuantity }" onFocus="this.select();" onClick="this.select();"/>
													</td>
													<td width="15%" style="text-align: left">
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
												<td colspan="6">
													<div style="float: left;">
														<button id="addAnother" type="button" class="positive">
															<img src="${createLinkTo(dir:'images/icons/silk', file:'add.png') }"/>&nbsp;Add Item
														</button>
													</div>
													<div style="float: right;">
														<g:link controller="inventoryItem" action="showStockCard" 
															params="['product.id':commandInstance.product?.id]" class="negative">Cancel</g:link>
														&nbsp;
														<button name="save" type="submit" class="positive">
															<img src="${createLinkTo(dir:'images/icons/silk', file:'tick.png') }"/>&nbsp;Save 
														</button>
													</div>
												</td>
											</tr>
										</tfoot>
									</table>
								</div>												
							</fieldset>						
						</g:form>
					</div>
				</td>
			</tr>
		</table>
	</div>			
</div>

<script id="newRowTemplate" type="x-jquery-tmpl">
<tr id="row-{{= getIndex()}}" class="{{= getClass()}}">	
	<td width="15%">
		<style>
			#lotNumber-{{= getIndex()}} { 
				background-image: url('/warehouse/images/icons/silk/magnifier.png');
				background-repeat: no-repeat;
				background-position: center left;
				padding: 5px;
				padding-left: 20px;
			}
		</style>
		<g:textField id="lotNumber-{{= getIndex()}}" class="lotNumber" name="lotNumber-{{= getIndex()}}" value="{{= LotNumber}}" size="15"  /><br/>
		<g:hiddenField id="lotNumberValue-{{= getIndex()}}" class="lotNumberValue" name="recordInventoryRows[{{= getIndex()}}].lotNumber" value="{{= LotNumber}}" size="10"  />
	</td>
	<td width="10%">
		<style>
			#expirationDate{{= getIndex()}} { 
				background-image: url('/warehouse/images/icons/silk/calendar.png');
				background-repeat: no-repeat;
				background-position: center left;
				padding: 5px;
				padding-left: 20px;
			}
		</style>
		<g:hiddenField id="expirationDate{{= getIndex()}}-hidden" name="recordInventoryRows[{{= getIndex()}}].expirationDate" value="{{= ExpirationDate}}"/>	
		<g:textField id="expirationDate{{= getIndex()}}" class="expirationDate" name="recordInventoryRows[{{= getIndex()}}].expirationDate-text" value="{{= ExpirationDate}}" size="10" />
		
	</td>
	<td width="25%">		
		<style>
			#description-{{= getIndex()}} { 
				background-image: url('/warehouse/images/icons/silk/magnifier.png');
				background-repeat: no-repeat;
				background-position: center left;
				padding: 5px;
				padding-left: 20px;
			}
		</style>
		<g:textField id="description-{{= getIndex()}}" class="description" name="recordInventoryRows[{{= getIndex()}}].description" value="{{= Description}}" size="18" />		
	</td>
	<td width="10%" style="text-align: center; vertical-align: middle;">
		{{= Qty}}
		<g:hiddenField id="oldQuantity-{{= getIndex()}}" class="oldQuantity" name="recordInventoryRows[{{= getIndex()}}].oldQuantity" value="{{= Qty}}"/>
	</td>	
	<td width="10%" style="text-align: center; vertical-align: middle;">
		<g:textField style="text-align: center;"  
			id="newQuantity-{{= getIndex()}}" class="newQuantity" name="recordInventoryRows[{{= getIndex()}}].newQuantity" size="3" value="{{= Qty}}" onFocus="this.select();" onClick="this.select();"/>

	</td>	
	<td width="5%" style="text-align: left;">
		<button id="buttonUp-{{= getIndex()}}" class="buttonUp">
			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_up.png') }"/>
		</button>
		<button id="buttonDown-{{= getIndex()}}" class="buttonDown">
			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_down.png') }"/>
		</button>
		<a href="#" onclick="removeRow({{= getIndex()}});"><img src="${createLinkTo(dir:'images/icons/silk', file:'delete.png')}"/></a>
		<g:hiddenField name="recordInventoryRows[{{= getIndex()}}].id" value="{{= Id}}" size="1" />
	</td>

</tr>
</script>									

<script id="existingRowTemplate" type="x-jquery-tmpl">
<tr id="row{{= getIndex()}}" class="{{= getClass()}}">
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
	<td width="10%" style="text-align: left;">
		<button id="{{= getIndex()}}" class="buttonUp">
			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_up.png') }"/>
		</button>
		<button id="{{= getIndex()}}" class="buttonDown">
			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_down.png') }"/>
		</button>
	</td>
	<td width="5%" style="text-align: center;">
		{{= getIndex()}}
		<g:hiddenField name="recordInventoryRows[{{= getIndex()}}].id" value="{{= Id}}" size="1" />
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

											

								