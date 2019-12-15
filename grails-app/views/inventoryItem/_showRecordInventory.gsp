<div id="inventoryForm">
	<g:form action="saveRecordInventory" autocomplete="off">

		<g:hiddenField name="product.id" value="${commandInstance.product?.id}"/>
		<g:hiddenField name="inventory.id" value="${commandInstance?.inventory?.id}"/>

		<div class="dialog">
			<div class="box">
				<h2><g:message code="inventory.record.label" default="Record Stock"/></h2>
				<table>
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="product.label"/></label>
						</td>
						<td>
							<format:product product="${commandInstance?.product}"/>
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="location.label"/></label>
						</td>
						<td class="value">
							${session.warehouse.name }
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="default.createdBy.label"/></label>
						</td>
						<td class="value">
							${session.user.name }
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="transaction.transactionDate.label"/></label>
						</td>
						<td class="value">
							<g:datePicker name="transactionDate" value="${commandInstance?.transactionDate}" precision="minute" noSelection="['':'']"/>
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="default.comments.label"/></label>
						</td>
						<td class="value">
							<g:textArea name="comment" placeholder="" style="width:100%" rows="3"></g:textArea>
						</td>
					</tr>
				</table>
			</div>

			<div class="box">
				<h2><warehouse:message code="default.lineItems.label" default="Line Items"/></h2>
				<table id="inventoryItemsTable">
					<thead>
					<tr>
						<th><warehouse:message code="location.binLocation.label"/></th>
						<th><warehouse:message code="default.lotSerialNo.label"/></th>
						<th><warehouse:message code="default.expires.label"/></th>
						<th class="center"><warehouse:message code="inventory.oldQty.label"/></th>
						<th class="center"><warehouse:message code="inventory.newQty.label"/></th>
						<th class="center"><warehouse:message code="default.comments.label"/></th>
						<th class="center"><warehouse:message code="default.actions.label"/></th>
					</tr>
					</thead>
					<tbody>
                        <g:if test="${commandInstance.recordInventoryRows }">
                            <g:each var="recordInventoryRow" in="${commandInstance.recordInventoryRows}" status="status">
                                <g:set var="styleClass" value="${params?.inventoryItem?.id && recordInventoryRow?.id == params?.inventoryItem?.id ? 'selected-row' : ''}"/>
                                <tr class="${styleClass} ${status%2==0?'odd':'even'}">
                                    <td>
                                        <g:hiddenField
                                                name="recordInventoryRows[${status}].binLocation.id"
                                                value="${recordInventoryRow?.binLocation?.id}"/>

										<g:if test="${recordInventoryRow?.binLocation}">
											${recordInventoryRow?.binLocation?.name }
										</g:if>
										<g:else>
											<g:if test="${!recordInventoryRow?.binLocation?.parentLocation?.hasBinLocationSupport()}">
												<warehouse:message code="default.notSupported.label"/>
											</g:if>
											<g:else>
												<warehouse:message code="default.label"/>
											</g:else>
										</g:else>
                                    </td>
                                    <td>
                                        <g:hiddenField name="recordInventoryRows[${status}].id" value="${recordInventoryRow?.id }"/>
                                        <g:hiddenField name="recordInventoryRows[${status}].lotNumber" value="${recordInventoryRow?.lotNumber }"/>
										<g:if test="${recordInventoryRow?.lotNumber}">
											<div class="lotNumber">
												${recordInventoryRow?.lotNumber }
											</div>
										</g:if>
                                        <g:else>
											<span class="fade"><warehouse:message code="default.label"/></span>
										</g:else>
                                    </td>
                                    <td>
										<g:if test ="${!recordInventoryRow?.oldQuantity}">
											<g:set var="currentYear" value="${new Date()[Calendar.YEAR]}"/>
											<g:set var="minimumYear" value="${grailsApplication.config.openboxes.expirationDate.minValue[Calendar.YEAR]}"/>
											<g:datePicker name="recordInventoryRows[${status}].expirationDate" years="${minimumYear..currentYear + 20}"
														  noSelection="['': '']" precision="day"/>
										</g:if>
										<g:else>
											<g:hiddenField name="recordInventoryRows[${status}].expirationDate"
														   value="${formatDate(date: recordInventoryRow?.expirationDate, format: 'MM/dd/yyyy') }"/>
											<g:if test="${recordInventoryRow?.expirationDate}">
												<format:expirationDate obj="${recordInventoryRow?.expirationDate}"/>
											</g:if>
											<g:else>
												<div class="fade">${warehouse.message(code: 'default.never.label')}</div>
											</g:else>
										</g:else>
                                    </td>
                                    <td class="middle center">
                                        ${recordInventoryRow?.oldQuantity }
                                        <g:hiddenField name="recordInventoryRows[${status}].oldQuantity"
                                                       value="${recordInventoryRow?.oldQuantity }"/>
                                    </td>
                                    <td class="middle center">
                                        <g:textField id="newQuantity-${status}" type="number"
                                                     class="newQuantity text"
                                                     name="recordInventoryRows[${status }].newQuantity" size="8"
                                                     value="${recordInventoryRow?.newQuantity }" />

                                    </td>
									<td class="middle center">
										<g:textField id="comment-${status }" class="text"
													  name="recordInventoryRows[${status }].comment"
                                                      placeholder="${g.message(code:'transactionEntry.comment.message')}"
													  value="${recordInventoryRow?.comment }" style="width:100%;"/>
									</td>
                                    <td class="middle left">
                                    </td>
                                </tr>
                            </g:each>
                        </g:if>
                        <g:else>
                            <tr id="emptyRow">
                                <td colspan="5" class="center">
                                    <div class="fade empty center">
                                        <warehouse:message code="inventory.addNewInventoryItem.message"/>

                                    </div>
                                </td>
                            </tr>

                        </g:else>

					</tbody>
					<tfoot>
					<tr>
						<td colspan="7">
							<div class="left">
								<button id="addRow" class="button icon add" >
									<warehouse:message code="inventory.addInventoryItem.label"/>
								</button>
							</div>

						</td>
					</tr>
					<tr>
						<td colspan="7">
                                <div>
                                    <button name="save" type="submit" class="button icon approve primary" id="saveInventoryItem">
                                        <warehouse:message code="default.button.save.label"/>&nbsp;
                                    </button>
                                    <g:link controller="inventoryItem" action="showStockCard"
                                            params="['product.id':commandInstance.product?.id]" class="button icon remove danger">
                                        <warehouse:message code="default.button.discard.label" default="Discard"/>
                                    </g:link>

                                </div>
							</div>
						</td>
					</tr>
				</table>
			</div>


		</div>
	</g:form>
</div>

<script>



	// Keep track of the inventory items on the page
	var inventory = { InventoryItems: [] };

	// We need to do this in order to make sure the index for new items is correct
	<g:each var="row" in="${commandInstance?.recordInventoryRows?.findAll { it?.oldQuantity != 0 || it?.newQuantity != 0}}" status="status">
		var existingInventoryItem = {
				Id: '${row?.id}',
				Type: '',
				ProductId: '',
                BinLocation: '',
				UnitOfMeasure: '${commandInstance?.product?.unitOfMeasure?:"EA"}',
				LotNumber: '${row?.lotNumber?.replace("'", "\\'")}',
				ExpirationDate: '${row?.expirationDate?:warehouse.message(code: 'default.never.label')}',
				Qty: 0
			};
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
	};

	var buttonDownMouseHandler = function(event, data) {
		event.preventDefault();
		var accelerator = (event.ctrlKey) ? 100 : (event.shiftKey) ? 10 : 1;
		changeQuantity($(this).parent().parent().find('.newQuantity'), -1 * accelerator);
	};

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

		var inventoryItem = {Qty: 0, UnitOfMeasure: '${commandInstance?.product?.unitOfMeasure?:"EA"}'};
		// Add to the array of new inventory items
		inventory.InventoryItems.push(inventoryItem);

		$("#emptyRow").hide();

		// Add a new row to the table
		$("#newRowTemplate").tmpl(inventoryItem).appendTo('#inventoryItemsTable');

		$('#inventoryItemsTable tbody tr:last').find('.lotNumber').focus();
	}

    $(document).ready(function() {

        // FIXME all pages that extend the stock card layout need to hide the loading modal
        $(".loading").hide();

        // If there's already at least an existing row we don't want to create a blank row
        var rows = $("#inventoryItemsTable tbody tr");
        console.log(rows);
        if (inventory.InventoryItems.length < 1) {
            addRow();
        }

		$('#addRow').livequery(function() {
			$(this).click(function(event) {
				event.preventDefault();
				addRow();

			});
		});

		$("#selectedText").click(function(){
			$(this).select();
		});

		$(".newQuantity").click(function() {
		    $(this).select();
		});

		$(".binLocation").livequery(function() {
		    $(this).chosen({ allow_single_deselect:true, width: '100%' });
        });

		$('.lotNumberSuggest').livequery(function() {
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
				buttonImage: '${request.contextPath }/images/icons/silk/calendar.png'
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

		function getMonth(monthAbbr) {
			var monthNumbersByName = [];
			monthNumbersByName['Jan'] = 1;
			monthNumbersByName['Feb'] = 2;
			monthNumbersByName['Mar'] = 3;
			monthNumbersByName['Apr'] = 4;
			monthNumbersByName['May'] = 5;
			monthNumbersByName['Jun'] = 6;
			monthNumbersByName['Jul'] = 7;
			monthNumbersByName['Aug'] = 8;
			monthNumbersByName['Sep'] = 9;
			monthNumbersByName['Oct'] = 10;
			monthNumbersByName['Nov'] = 11;
			monthNumbersByName['Dec'] = 12;
			return monthNumbersByName[monthAbbr];
		}

		$(".expirationDate").livequery(function() {
			$(this).datepicker().keydown(function (e) {
				var code = e.keyCode || e.which;
				//   TAB: 9
				//  LEFT: 37
				//    UP: 38
				// RIGHT: 39
				//  DOWN: 40
				//console.log(code);


				// If key is not TAB
				if (code != '9') {
					// And arrow keys used "for performance on other keys"
					if (code == '37' || code == '38' || code == '39' || code == '40') {
					// Get current date
					var parts = $(this).val().split(" ");
					console.log(parts);
					var day = parts[0];
					var month = getMonth(parts[1])-1;
					var year = parts[2];
					var currentDate = new Date(year, month, day);
					// Show next/previous day/week
					switch (code) {
						// LEFT, -1 day
						case 37: currentDate.setDate(currentDate.getDate() - 1); break;
						// UP, -1 week
						case 38: currentDate.setDate(currentDate.getDate() - 7); break;
						// RIGHT, +1 day
						case 39: currentDate.setDate(currentDate.getDate() + 1); break;
						// DOWN, +1 week
						case 40: currentDate.setDate(currentDate.getDate() + 7); break;
					}
					// If result is ok then write it
					if (currentDate != null) {
						$(this).datepicker("setDate", currentDate); }
					} else {
						return false;
					} // If other keys pressed.. return false
				}
			});
		});
	});

</script>

<script id="newRowTemplate" type="x-jquery-tmpl">
<tr id="row-{{= getIndex()}}" class="{{= getClass()}}">

	<td>
		<g:selectBinLocation  id="binLocation-{{= getIndex()}}" class="binLocation" name="recordInventoryRows[{{= getIndex()}}].binLocation.id"
                             value="{{= BinLocation}}" noSelection="['':'']"/>

	</td>

	<td>
		<g:textField id="lotNumber-{{= getIndex()}}" class="lotNumber text" name="recordInventoryRows[{{= getIndex()}}].lotNumber" value="{{= LotNumber}}" size="25" />
	</td>
	<td class="nowrap">
        <style>
            .expirationDate {
                background-image: url('${request.contextPath }/images/icons/silk/calendar.png');
                background-repeat: no-repeat;
                background-position: center right;

            }
        </style>
        <g:set var="currentYear" value="${new Date()[Calendar.YEAR]}"/>
		<g:set var="minimumYear" value="${grailsApplication.config.openboxes.expirationDate.minValue[Calendar.YEAR]}"/>
        <g:datePicker name="recordInventoryRows[{{= getIndex()}}].expirationDate"
                      default="none" noSelection="['': '']" years="${minimumYear..currentYear + 20}"
                      precision="day"/>

    </td>
	<td style="text-align: center; vertical-align: middle;">
		{{= Qty}}
		<g:hiddenField id="oldQuantity-{{= getIndex()}}" class="oldQuantity"
			name="recordInventoryRows[{{= getIndex()}}].oldQuantity" value="{{= Qty}}"/>
	</td>
	<td style="text-align: center; vertical-align: middle;">
		<g:textField
        id="newQuantity-{{= getIndex()}}" type="number" class="newQuantity text"
        name="recordInventoryRows[{{= getIndex()}}].newQuantity"
        size="8" value="{{= Qty}}" />
	</td>
	<td class="center middle">
	    <g:textField id="comment-{{= getIndex()}}" class="text"
                     placeholder="${g.message(code:'transactionEntry.comment.message')}"
                     name="recordInventoryRows[{{= getIndex()}}].comment" style="width:100%;" value="" />
	</td>
	<td class="center">
		<button onclick="removeRow({{= getIndex()}});" class="button icon trash" tabIndex="-1">
			${warehouse.message(code: 'default.button.delete.label')}&nbsp;
		</button>
		<g:hiddenField name="recordInventoryRows[{{= getIndex()}}].id" value="{{= Id}}" size="1" />
	</td>

</tr>
</script>
