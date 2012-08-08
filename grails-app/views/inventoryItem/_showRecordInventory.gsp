<div id="inventoryForm">		
	<g:form action="saveRecordInventory" autocomplete="off">
		
		<g:hiddenField name="productInstance.id" value="${commandInstance.productInstance?.id}"/>
		<g:hiddenField name="inventoryInstance.id" value="${commandInstance?.inventoryInstance?.id}"/>

			
		<div class="middle" style="padding: 5px;">
			<label><warehouse:message code="inventory.dateOfInventory.label"/></label>													
			<g:jqueryDatePicker 
				id="transactionDate" 
				name="transactionDate"
				value="${commandInstance?.transactionDate}" 
				format="MM/dd/yyyy"
				showTrigger="false" />						
		</div>
		<div class="form-content" style="border: 1px solid lightgrey;">
			<table id="inventoryItemsTable">
				<thead>					
					<tr class="even">	
						<th width="30%"><warehouse:message code="default.lotSerialNo.label"/></th>
						<th width="20%"><warehouse:message code="default.expires.label"/></th>
						<th class="center" width="10%"><warehouse:message code="inventory.oldQty.label"/></th>
						<th class="center" width="10%"><warehouse:message code="inventory.newQty.label"/></th>
						<th class="left" width="30%"><warehouse:message code="default.actions.label"/></th>
					</tr>											
				</thead>									
				<tbody>												
					<g:set var="inventoryItems" value="${commandInstance?.recordInventoryRows.findAll{it.oldQuantity != 0 || it.newQuantity != 0}}"/>	
					<g:if test="${inventoryItems }">											
						<g:each var="recordInventoryRow" in="${inventoryItems?.sort { it.expirationDate }?.sort { it.lotNumber } }" status="status">
							<g:set var="styleClass" value="${params?.inventoryItem?.id && recordInventoryRow?.id == params?.inventoryItem?.id ? 'selected-row' : ''}"/>
							<tr class="${styleClass} ${status%2==0?'odd':'even'}">
								<td>
									<%-- 
									<g:textField name="recordInventoryRows[${status}].lotNumber" size="10" value="${recordInventoryRow?.lotNumber }"/>
									--%>
									<g:hiddenField name="recordInventoryRows[${status}].id" value="${recordInventoryRow?.id }"/>
									<g:hiddenField name="recordInventoryRows[${status}].lotNumber" value="${recordInventoryRow?.lotNumber }"/>
									${recordInventoryRow?.lotNumber?:'<span class="fade"><warehouse:message code="default.none.label"/></span>' }
								</td>
								<td>
									<%-- 
									<g:jqueryDatePicker id="expirationDate${status }" name="recordInventoryRows[${status}].expirationDate" 
										value="${recordInventoryRow?.expirationDate}" format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" showTrigger="false" />
									--%>
									<g:hiddenField name="recordInventoryRows[${status}].expirationDate" value="${formatDate(date: recordInventoryRow?.expirationDate, format: 'MM/dd/yyyy') }"/>
									<g:if test="${recordInventoryRow?.expirationDate}">
										<format:expirationDate obj="${recordInventoryRow?.expirationDate}"/>
									</g:if>
									<g:else>
										<span class="fade">${warehouse.message(code: 'default.never.label')}</span>
									</g:else>
								</td>
								<td class="middle center">
									${recordInventoryRow?.oldQuantity }	
									<g:hiddenField name="recordInventoryRows[${status}].oldQuantity" value="${recordInventoryRow?.oldQuantity }"/>
								</td>	
								<td class="middle center">
									<g:textField id="newQuantity-${status }" class="newQuantity text right" name="recordInventoryRows[${status }].newQuantity" size="5" value="${recordInventoryRow?.newQuantity }" onFocus="this.select();" onClick="this.select();"/>
								</td>
								<td class="middle left">
									<img class="addAnother" src="${createLinkTo(dir:'images/icons/silk', file:'add.png') }"/>								
								</td>
							</tr>
						</g:each>
					</g:if>
					<g:else>
						<tr id="emptyRow">
							<td colspan="5" style="text-align: center;">
								<div class="fade">
									<warehouse:message code="inventory.addNewInventoryItem.message"/>
									<img class="addAnother" src="${createLinkTo(dir:'images/icons/silk', file:'add.png') }"/>								
								</div>
							</td>
						</tr>
						
					</g:else>
					
				</tbody>
			</table>
		</div>												
		<div class="center buttons">		
			<button name="save" type="submit" class="positive">
				<img src="${createLinkTo(dir:'images/icons/silk', file:'accept.png') }"/>&nbsp;<warehouse:message code="default.button.save.label"/>&nbsp;
			</button>
			&nbsp;
			<g:link controller="inventoryItem" action="showStockCard" 
				params="['product.id':commandInstance.productInstance?.id]" class="negative"><warehouse:message code="default.button.cancel.label"/></g:link>
		
		</div>
		
	</g:form>
</div>

<script>
	// Keep track of the inventory items on the page 
	var inventory = { InventoryItems: [] };	

	// We need to do this in order to make sure the index for new items is correct
	<g:each var="row" in="${commandInstance?.recordInventoryRows}" status="status">
		var existingInventoryItem = { Id: '${row.id}', Type: '', ProductId: '', LotNumber: '${row.lotNumber}', ExpirationDate: '${row.expirationDate?:warehouse.message(code: 'default.never.label')}', Qty: 0 };	
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

	 
	function addNewInventoryItem(id, type, productId, lotNumber, expDate, qty) { 
		var inventoryItem = 
			{ Id: id, Type: 'new', ProductId: productId,LotNumber: lotNumber, ExpirationDate: expDate, Qty: 0 };


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
		$.get('http://localhost:8080${request.contextPath }/inventoryItem/getInventoryItems', { 'product.id': '8' }, function(data) {
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
		var rows = $("#inventoryItemsTable tbody tr");
		if (rows.length <= 1) { 
			$("#emptyRow").show();
		}
	}

	$(document).ready(function() {

		//$('.addAnother').focus();		
		$('.addAnother').livequery(function() { 
			$(this).click(function(event) { 
				event.preventDefault();
				var inventoryItem = {Qty: 0};
				// Add to the array of new inventory items
				inventory.InventoryItems.push(inventoryItem);
	
				$("#emptyRow").hide();
	
				// Add a new row to the table
				$("#newRowTemplate").tmpl(inventoryItem).appendTo('#inventoryItemsTable');		
				//$("#inventoryItemsTable tbody tr:even").addClass("even");
				//$("#inventoryItemsTable tbody tr:odd").addClass("odd");
	
				
				$('#inventoryItemsTable tbody tr:last').find('.lotNumber').focus();
						
			});
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
					$.getJSON('${request.contextPath }/json/findLotsByName', req, function(data) {
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

					lotNumber.val(ui.item.value);
					lotNumberValue.val(ui.item.value);

					// Set the ID
					id.val(ui.item.id);
					// Set the lot number and disable the field
					
					if (ui.item.expirationDate) {
						expirationDate.val(ui.item.expirationDate);						
					}
					else { 
						expirationDate.val('${warehouse.message(code: 'default.never.label')}');
					}
				}							  
			});				
		});
		$('.expirationDate').livequery(function() {
			var altFieldId = "#" + $(this).attr('id') + "-hidden"; 
			$(this).datepicker({
				altField: altFieldId,
				altFormat: 'mm/dd/yy',
				dateFormat: 'dd M yy',
				buttonImageOnly: true, 
				changeMonth: true,
				changeYear: true,
				buttonImage: '${request.contextPath }/images/icons/silk/calendar.png',
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

<script id="newRowTemplate" type="x-jquery-tmpl">
<tr id="row-{{= getIndex()}}" class="{{= getClass()}}">	
	<td width="15%">
		<g:textField id="lotNumber-{{= getIndex()}}" class="text" name="recordInventoryRows[{{= getIndex()}}].lotNumber" value="{{= LotNumber}}" size="25" /><br/>
		<%--
		<g:textField id="lotNumber-{{= getIndex()}}" class="lotNumber" name="lotNumber-{{= getIndex()}}" value="{{= LotNumber}}" size="15"  /><br/>
		<g:hiddenField id="lotNumberValue-{{= getIndex()}}" class="lotNumberValue" name="recordInventoryRows[{{= getIndex()}}].lotNumber" value="{{= LotNumber}}" size="10"  />
		--%>
	</td>
	<td width="10%">
		<style>
			.expirationDate { 
				background-image: url('${request.contextPath }/images/icons/silk/calendar.png');
				background-repeat: no-repeat;
				background-position: center left;
				padding-left: 20px;
			}
		</style>
		<g:hiddenField id="expirationDate{{= getIndex()}}-hidden" name="recordInventoryRows[{{= getIndex()}}].expirationDate" value="{{= ExpirationDate}}"/>	
		<g:textField id="expirationDate{{= getIndex()}}" class="expirationDate" name="recordInventoryRows[{{= getIndex()}}].expirationDate-text" value="{{= ExpirationDate}}" size="10" />
		
	</td>
	<td width="10%" style="text-align: center; vertical-align: middle;">
		{{= Qty}}
		<g:hiddenField id="oldQuantity-{{= getIndex()}}" class="oldQuantity" name="recordInventoryRows[{{= getIndex()}}].oldQuantity" value="{{= Qty}}"/>
	</td>	
	<td width="10%" style="text-align: center; vertical-align: middle;">
		<g:textField  
			id="newQuantity-{{= getIndex()}}" class="newQuantity right text" name="recordInventoryRows[{{= getIndex()}}].newQuantity" size="5" value="{{= Qty}}" onFocus="this.select();" onClick="this.select();"/>

	</td>	
	<td width="5%" class="left">
		<%--
			<button id="buttonUp-{{= getIndex()}}" class="buttonUp">
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_up.png') }"/>
			</button>
			<button id="buttonDown-{{= getIndex()}}" class="buttonDown">
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_arrow_down.png') }"/>
			</button>
		--%>
		<img class="addAnother" src="${createLinkTo(dir:'images/icons/silk', file:'add.png') }"/>&nbsp;
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
	<%--
	<td width="35%">
		<g:hiddenField name="recordInventoryRows[{{= getIndex()}}].description" value="{{= Description}}"/>
		{{= Description}}
	</td>
	--%>
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


