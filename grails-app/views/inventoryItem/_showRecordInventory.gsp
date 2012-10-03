<div id="inventoryForm">		
	<g:form action="saveRecordInventory" autocomplete="off">
		
		<g:hiddenField name="productInstance.id" value="${commandInstance.productInstance?.id}"/>
		<g:hiddenField name="inventoryInstance.id" value="${commandInstance?.inventoryInstance?.id}"/>


		<div class="form-content box">
			
			<div class="middle left" style="padding: 10px 0 10px 0;">
				<label><warehouse:message code="inventory.inventoryDate.label"/></label>													
				<g:jqueryDatePicker 
					id="transactionDate" 
					name="transactionDate"
					value="${commandInstance?.transactionDate}" 
					format="MM/dd/yyyy"
					showTrigger="false" />						
					
					
							<button class="addAnother" >
								<img src="${createLinkTo(dir:'images/icons/silk', file:'add.png') }"/>
								<warehouse:message code="inventory.addInventoryItem.label"/>							
							</button>					
			</div>
			<table id="inventoryItemsTable">
				<thead>					
					<tr>	
						<th><warehouse:message code="default.lotSerialNo.label"/></th>
						<th><warehouse:message code="default.expires.label"/></th>
						<th class="center"><warehouse:message code="inventory.oldQty.label"/></th>
						<th class="center"><warehouse:message code="inventory.newQty.label"/></th>
						<th class="center"><warehouse:message code="default.actions.label"/></th>
					</tr>											
				</thead>									
				<tbody>												
					<g:set var="inventoryItems" value="${commandInstance?.recordInventoryRows.findAll{it.oldQuantity != 0 || it.newQuantity != 0}}"/>	
					<g:if test="${inventoryItems }">											
						<g:each var="recordInventoryRow" in="${inventoryItems?.sort { it.expirationDate }?.sort { it.lotNumber } }" status="status">
							<g:set var="styleClass" value="${params?.inventoryItem?.id && recordInventoryRow?.id == params?.inventoryItem?.id ? 'selected-row' : ''}"/>
							<tr class="${styleClass} ${status%2==0?'odd':'even'}">
								<td>
									<g:hiddenField name="recordInventoryRows[${status}].id" value="${recordInventoryRow?.id }"/>
									<g:hiddenField name="recordInventoryRows[${status}].lotNumber" value="${recordInventoryRow?.lotNumber }"/>
									${recordInventoryRow?.lotNumber?:'<span class="fade"><warehouse:message code="default.none.label"/></span>' }
								</td>
								<td>
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
									<g:textField id="newQuantity-${status }" class="newQuantity text center" name="recordInventoryRows[${status }].newQuantity" size="8" value="${recordInventoryRow?.newQuantity }" onFocus="this.select();" onClick="this.select();"/>
								</td>
								<td class="middle left">
								</td>
							</tr>
						</g:each>
					</g:if>
					<g:else>
						<tr id="emptyRow" class="odd">
							<td colspan="5" style="text-align: center;">
								<div class="fade">
									<warehouse:message code="inventory.addNewInventoryItem.message"/>
																
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

	function changeQuantity(textField, delta){ 
		textField.val(parseInt(textField.val()) + delta);
	}

	function removeRow(index) {
		$('#row-' + index).remove();
		var rows = $("#inventoryItemsTable tbody tr");
		if (rows.length <= 1) { 
			$("#emptyRow").show();
		}
	}

	function addRow() { 

		var inventoryItem = {Qty: 0};
		// Add to the array of new inventory items
		inventory.InventoryItems.push(inventoryItem);

		$("#emptyRow").hide();

		// Add a new row to the table
		$("#newRowTemplate").tmpl(inventoryItem).appendTo('#inventoryItemsTable');		

		$('#inventoryItemsTable tbody tr:last').find('.lotNumber').focus();
	}

	$(document).ready(function() {
		addRow();
		//$('.addAnother').focus();		
		$('.addAnother').livequery(function() { 
			$(this).click(function(event) { 
				event.preventDefault();
				addRow();
						
			});
		});

		$('.lotNumber').livequery(function() {

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
                change: function(event, ui) {
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
	<td>
		<g:textField id="lotNumber-{{= getIndex()}}" class="text" name="recordInventoryRows[{{= getIndex()}}].lotNumber" value="{{= LotNumber}}" size="25" /><br/>
	</td>
	<td>
		<style>
			.expirationDate { 
				background-image: url('${request.contextPath }/images/icons/silk/calendar.png');
				background-repeat: no-repeat;
				background-position: center right;
				
			}
		</style>
		<g:hiddenField id="expirationDate{{= getIndex()}}-hidden" name="recordInventoryRows[{{= getIndex()}}].expirationDate" value="{{= ExpirationDate}}"/>	
		<g:textField id="expirationDate{{= getIndex()}}" class="expirationDate date text" name="recordInventoryRows[{{= getIndex()}}].expirationDate-text" value="{{= ExpirationDate}}" size="10" />
		
	</td>
	<td style="text-align: center; vertical-align: middle;">
		{{= Qty}}
		<g:hiddenField id="oldQuantity-{{= getIndex()}}" class="oldQuantity" name="recordInventoryRows[{{= getIndex()}}].oldQuantity" value="{{= Qty}}"/>
	</td>	
	<td style="text-align: center; vertical-align: middle;">
		<g:textField  
			id="newQuantity-{{= getIndex()}}" class="newQuantity center text" name="recordInventoryRows[{{= getIndex()}}].newQuantity" size="8" value="{{= Qty}}" onFocus="this.select();" onClick="this.select();"/>

	</td>	
	<td class="center">
		<button onclick="removeRow({{= getIndex()}});">
			<img src="${createLinkTo(dir:'images/icons/silk', file:'delete.png')}"/>
			${warehouse.message(code: 'default.button.delete.label')}&nbsp;
		</button>
		<g:hiddenField name="recordInventoryRows[{{= getIndex()}}].id" value="{{= Id}}" size="1" />
	</td>

</tr>
</script>									
