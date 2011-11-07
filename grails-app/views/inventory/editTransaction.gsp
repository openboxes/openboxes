<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        <title>
	        <g:if test="${transactionInstance?.id }">
		        <warehouse:message code="default.edit.label" args="[entityName.toLowerCase()]" />  
	    	</g:if>
	    	<g:else>
		        <warehouse:message code="default.add.label" args="[entityName.toLowerCase()]" />    
			</g:else>    	    
		</title>
        <style>
        	.dialog form label { position: absolute; display: inline; width: 140px; text-align: right;}
        	.dialog form .value { margin-left: 160px; }
        	.dialog form ul li { padding: 10px; } 
        	.dialog form { width: 100%; } 
        	.header th { background-color: #525D76; color: white; } 
        </style>
    </head>    
    <body>
        <div class="body">

            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    

			<div class="dialog" >
				<fieldset>
					<table style="height: 100%;">
						<tr>
							<td>			
								<div class="summary">
									<!-- Action menu -->
									<span class="action-menu">
										<button class="action-btn">
											<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle;"/>
											<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
										</button>
										<div class="actions">
											<g:if test="${params?.product?.id }">
												<div class="action-menu-item">
													<g:link controller="inventoryItem" action="showStockCard" params="['product.id':params?.product?.id]">
														<img src="${createLinkTo(dir: 'images/icons/silk', file: 'arrow_left.png')}"/>
														${warehouse.message(code: 'transaction.backToStockCard.label', default: 'Back to Stock Card')}
													</g:link>		
												</div>	
											</g:if>
											<div class="action-menu-item">
												<g:link controller="inventory" action="browse">
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'application_view_list.png')}"/>
													${warehouse.message(code: 'transaction.backToInventory.label')}
												</g:link>			
											</div>
											<%-- 				
											<div class="action-menu-item">
												<g:link controller="inventory" action="listTransactions">
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'arrow_up.png')}"/>
													${warehouse.message(code: 'transaction.backToTransactions.label')}
												</g:link>			
											</div>
											--%>
											<g:if test="${transactionInstance?.id }">
												<div class="action-menu-item">
													<hr/>
												</div>							
												<div class="action-menu-item">											
													<g:link controller="inventory" action="deleteTransaction" id="${transactionInstance?.id }" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
								    					<img src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}" alt="Delete" />
														&nbsp;${warehouse.message(code: 'transaction.delete.label')}&nbsp;
													</g:link>							
												</div>
											</g:if>
										</div>
									</span>
								</div>					
							</td>
						</tr>
						<tr>
							<td style="padding: 0px;">
								<div class="left" >
									<g:form action="saveTransaction">
										<g:hiddenField name="id" value="${transactionInstance?.id}"/>
										<g:hiddenField name="inventory.id" value="${warehouseInstance?.inventory?.id}"/>
										<table>
											<tr class="prop">
												<td>
													<label><warehouse:message code="transaction.transactionNumber.label"/></label>
												</td>
												<td>
													<span class="value">
														<g:if test="${transactionInstance?.id }">
															${transactionInstance?.transactionNumber() }
														</g:if>
														<g:else>
															<span class="fade">(new transaction)</span>
														</g:else>
													</span>
												</td>
											</tr>
											<tr class="prop">
												<td>
													<label><warehouse:message code="transaction.status.label"/></label>
												</td>												
												<td>
													<span class="value">
														<g:if test="${transactionInstance?.id }">
															<warehouse:message code="enum.TransactionStatus.COMPLETE"/>
														</g:if>
														<g:else>
															<warehouse:message code="enum.TransactionStatus.PENDING"/>
														</g:else>
													</span>
												</td>
											</tr>
											<tr class="prop">
												<td>
													<label><warehouse:message code="transaction.date.label"/></label>
												</td>
												<td>
													<span class="value">
														<g:jqueryDatePicker id="transactionDate" name="transactionDate"
																value="${transactionInstance?.transactionDate}" format="MM/dd/yyyy"/>
													</span>								
												</td>
											</tr>											
											<tr class="prop">
												<td>
													<label><warehouse:message code="transaction.type.label"/></label>
												</td>
												<td>
													<span class="value">
														<g:select id="transactionTypeSelector" name="transactionType.id" from="${transactionTypeList}" 
								                       		optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${transactionInstance.transactionType?.id}" noSelection="['': '']" />
							                       	</span>
													<span id="sourceSection">
														<label><warehouse:message code="default.from.label"/></label>
														<span class="value">
															<g:select id="sourceId" name="source.id" from="${locationInstanceList}" 
									                       		optionKey="id" optionValue="name" value="${transactionInstance?.source?.id}" noSelection="['': '']" />
							                       		</span>
							                       	</span>
							                       	<span id="destinationSection">
														<label><warehouse:message code="default.to.label"/></label>
														<span class="value">
															<g:select id="destinationId" name="destination.id" from="${locationInstanceList}" 
									                       		optionKey="id" optionValue="name" value="${transactionInstance?.destination?.id}" noSelection="['': '']" />
														</span>
													</span>
												</td>
											</tr>
											<tr class="prop">
												<td>
													<label><warehouse:message code="transaction.comment.label"/></label>
												</td>
												<td>
													<span class="value">
														<g:textArea cols="60" rows="5" name="comment" value="${transactionInstance?.comment }"></g:textArea>

													</span>								
												</td>
											</tr>				


											<tr class="prop">
												<td colspan="2" style="padding: 0px;">
													<div style="height:300px; overflow:auto;">
														<table id="transaction-entries-table" border="0" style="margin: 0; padding: 0; border: 0px solid lightgrey; background-color: white;">
															<thead>
																<tr class="odd">
																	<th style="width: 60%"><warehouse:message code="product.label"/></th>
																	<th nowrap="true"><warehouse:message code="product.lotNumber.label"/></th>
																	<th nowrap="true"><warehouse:message code="default.expires.label"/></th>
																	<th><warehouse:message code="inventory.onHandQuantity.label"/></th>
																	<th><warehouse:message code="default.qty.label"/></th>
																	<th><warehouse:message code="default.actions.label"/></th>
																</tr>
															</thead>
															<tbody>
																<tr class="empty">
																	<td colspan="7" style="text-align: center; display:none;" id="noItemsRow">
																		<span class="fade"><warehouse:message code="transaction.noItems.message"/></span>
																	</td>
																</tr>
							                                    <tr id="itemRowTemplate" style="display:none;">
							                                    	<td>
							                                    		 <g:hiddenField class="entryIdField" name="transactionEntryId" value=""/>
							                                    		 <g:hiddenField class="entryDeleteField" name="deleteEntry" value="false"/>
							                                    		 <g:hiddenField class="productIdField" name="productId" value=""/>
							                                    		 <g:hiddenField class="inventoryItemIdField" name="inventoryItemId" value=""/> 
							                                    		 <span class="productNameLabel"></span>
							                                    	</td>
							                                    	<td style="white-space:nowrap;">
							                                    		<span class="lotNumberFieldSection" style="display:none;">
							                                    			<g:textField class="lotNumber lotNumberField" name="lotNumber" size="15" value=""/>
							                                    		</span>
							                                    		<span class="lotNumberLabel"></span>
							                                    	</td>
							                                    	<td style="white-space:nowrap;">
							                                    		<span class="expirationFieldSection" style="display:none;">
							                                    			<g:datePicker class="expirationDateField" name="expirationDate" precision="month" default="none" noSelection="['':'']"/>
							                                    		</span>
							                                    		<span class="expirationLabel"></span>
							                                    	</td>
							                                    	<td style="white-space:nowrap;" class="onHandQtyLabel"></td>
							                                    	<td style="white-space:nowrap;"><g:textField class="quantityField" name="quantity" value="" size="1"/></td>
							                                    	<td style="white-space:nowrap;" class="actionsCell">
							                                    		<button type="button" class="rowDeleteButton">
							                                    			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png')}"/>
							                                    		</button>
							                                    	</td>
							                                    </tr>
															</tbody>
														</table>
													</div>	
												</td>
											</tr>		
											<tr class="prop">
												<td colspan="7">
													<div style="text-align: center;">
														<button type="submit" name="save">								
															<img src="${createLinkTo(dir: 'images/icons/silk', file: 'tick.png')}"/>&nbsp;<warehouse:message code="default.button.save.label"/>&nbsp;
														</button>
													</div>
												</td>
											</tr>
										</table>				
									</g:form>
								</div>
							</td>
							
							
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
		
		<script type="text/javascript">
                             
			var nextIndex = 0;
			
			function addEntryRow(entry, newLot) {
				var row = $("#itemRowTemplate").clone(true).show();
				$(row).attr("id", "itemRow"+nextIndex).addClass("displayedItemRow");
				$(row).find(".entryIdField").val(entry.EntryId);
				$(row).find(".productIdField").val(entry.ProductId);
				$(row).find(".inventoryItemIdField").val(entry.InventoryItemId);
				$(row).find(".lotNumberField").val(entry.LotNumber);
				$(row).find(".quantityField").val(entry.Qty);
				$(row).find(".productNameLabel").html(entry.ProductName);
				if (newLot) {
					$(row).find(".lotNumberFieldSection").show();
					$(row).find(".lotNumberLabel").hide();
					$(row).find(".expirationFieldSection").show();
					$(row).find(".expirationLabel").hide();
				}
				else {
					$(row).find(".lotNumberLabel").html(entry.LotNumber);
					$(row).find(".expirationLabel").html(entry.ExpirationMonth + ' ' + entry.ExpirationYear);
				}
				$(row).find(".onHandQtyLabel").html(entry.OnHandQty);
				$(row).find(".rowDeleteButton").attr("id", "deleteButton"+nextIndex).click(function(event) {
					var $row = $(this).parent().parent();
					$row.removeClass("displayedItemRow");
					if (entry.EntryId) {
						$row.find(".entryDeleteField").val('true');
						$row.hide();
					}
					else {
						$row.remove();
					}
					paintStripes();
				});
				$('#itemRowTemplate').parent().append(row);
				paintStripes();
				nextIndex++;												
			}

			function addNewItem(productId) { 
				$.getJSON('/warehouse/json/findProduct', {id: productId}, function(data) {
	    			var entry = { 
	    				EntryId: '', 
	    				ProductId: data.product.id, 
	    				ProductName: data.product.name, 
	    				LotNumber: '', 
	    				ExpirationMonth: '', 
	    				ExpirationYear: '', 
	    				Qty: '', 
	    				OnHandQty: 'N/A' 
	    			};
	    			addEntryRow(entry, true);
				}); 
			}

			function addExistingItem(productId, lotNumber, onHandQty) { 
				$.getJSON('/warehouse/json/findInventoryItem', {productId: productId, lotNumber: lotNumber}, function(data) {
					var expMonth = '';
					var expYear = '';
					if (data.inventoryItem.expirationDate) {
						var d = new Date(data.inventoryItem.expirationDate);
						expMonth = monthNamesShort[d.getMonth()-1];
						expYear = d.getFullYear();
					}
	    			var entry = { 
	    				EntryId: '',
	    				ProductId: data.product.id,
	    				InventoryItemId: data.inventoryItem.id, 
	    				ProductName: data.product.name, 
	    				LotNumber: data.inventoryItem.lotNumber, 
	    				ExpirationMonth: expMonth,
	    				ExpirationYear: expYear,
	    				Qty: '', 
	    				OnHandQty: onHandQty
	    			};
	    			addEntryRow(entry, false);
				}); 				
			}

			function paintStripes() {
				var nextClass = "even";
				$(".displayedItemRow").each(function () {
					$(this).addClass(nextClass);
					nextClass = nextClass == "even" ? "odd" : "even";
				});
			}
			
			function selectCombo(comboBoxElem, value) {
				if (comboBoxElem != null) {
					if (comboBoxElem.options) { 
						for (var i = 0; i < comboBoxElem.options.length; i++) {
				        	if (comboBoxElem.options[i].value == value &&
				                comboBoxElem.options[i].value != "") { //empty string is for "noSelection handling as "" == 0 in js
				                comboBoxElem.options[i].selected = true;
				                break
				        	}
						}
					}
				}
			}		
            
			function changeTransactionType(select) { 
				var transactionType = select.val();
				$("#sourceSection").hide();
				$("#destinationSection").hide();
				if (transactionType == '${org.pih.warehouse.core.Constants.TRANSFER_IN_TRANSACTION_TYPE_ID}') {
					$("#sourceSection").show();
				}
				else if (transactionType == '${org.pih.warehouse.core.Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID}') {
					$("#destinationSection").show();
				}	
				paintStripes();				
			}
			
			function clear(field) { 
		    	field.removeClass("fade"); 
		    	field.val(''); 
			}							
		</script>

		<script type="text/javascript">
	    	$(document).ready(function() {

				/* ------------------------------	Initialization ------------------------------ */

		    	// Initialize the transaction type selector
		    	changeTransactionType($("#transactionTypeSelector"));
		    	
		    	// Initialize the product search autocomplete
				$('#productSearch').addClass("fade");
				
				// Load the initial entries
				<g:if test="${transactionInstance?.transactionEntries}">
			        <g:set var="transactionMap" value="${transactionInstance?.transactionEntries?.groupBy { it?.inventoryItem?.product?.name } }"/>
					<g:each in="${transactionMap.sort()?.keySet()}" var="key" >
						<g:set var="transactionEntries" value="${transactionMap?.get(key) }"/>
						<g:each in="${transactionEntries.sort { it.inventoryItem.expirationDate } }" var="transactionEntry" status="status">	        
							var entry = { 
								EntryId: '${transactionEntry?.id}', 
								ProductId: '${transactionEntry?.inventoryItem?.product?.id}', 
								ProductName: '${format.product(product:transactionEntry?.inventoryItem?.product)}', 
								InventoryItemId: '${transactionEntry?.inventoryItem?.id}', 
								LotNumber: '${transactionEntry?.inventoryItem?.lotNumber}',
								ExpirationMonth: '<g:formatDate date="${transactionEntry?.inventoryItem?.expirationDate}" format="MMM"/>', 
								ExpirationYear: '<g:formatDate date="${transactionEntry?.inventoryItem?.expirationDate}" format="yyyy"/>',
								OnHandQty: '${quantityMap[transactionEntry?.inventoryItem]}',
								Qty: '${transactionEntry?.quantity}'
							};
							addEntryRow(entry, false);
						</g:each>
					</g:each>
				</g:if>
		    	
		    	
				/* --------------------	Transaction Type Switcher Handler	----------------------- */
		    			    	
		    	/**
		    	 * On change of transaction type, initialize the fields 
		    	 * to display.
		    	 */
				$("#transactionTypeSelector").change(function(){
					changeTransactionType($(this));					
				});

				/* --------------------	Product Search Handler	----------------------- */

				/**
				 * When product search becomes focus, we remove fade, reset values
				 */
		    	$('#productSearch').focus(function() { 
			    	clear($(this));		    				    	
			    });

	    		/**
	    		 * Suppress the submit action when the ENTER key is pressed during a product search.
	    		 */
	    		$("#productSearch").keypress(function (event) {
	    			if (event.keyCode == 13) {
	    			    event.preventDefault();
	    			}
	    		});   		
	    		
	    		/**
	    		 * Bind autocomplete widget to the product search field.
	    		 */
	    		$('#productSearch').autocomplete( {
					minLength: 2,
					delay: 500,
    				source: function(request, response) {
    					var currentWarehouseId = $("#currentWarehouseId").val();
    					$.getJSON('/warehouse/json/findProductByName', { term: request.term, warehouseId: currentWarehouseId }, function(data, status, xhr) {
    						var items = [];
    						$.each(data, function(i, item) { items.push(item); });
    						response(items);
    					});
    				},
    				focus: function( event, ui ) {
    					$(this).val( ui.item.label );	// in order to prevent ID from being displayed in widget
    					return false;
    				},
    				select: function(event, ui) {
    					$('#productSearch').blur();
    					
    					// in order to prevent ID from being displayed in widget
    					$(this).val( ui.item.label ); 	
        				
        				// Show product details
        				$('#product-details').show();
    		    		$("#hiddenProductId").val(ui.item.product.id);
        				$("#hiddenProductName").val(ui.item.product.name);
        				$("#product-details-id").html(ui.item.product.id);
    					$("#product-details-name").html(ui.item.product.name);
        				$("#product-details-manufacturer").html(ui.item.product.manufacturer);

    					// Reset product search
    					$('#productSearch').addClass("fade");					
    		    		$('#productSearch').val("Search products ...");    					

    		    		// Show product display DIV
						$('#product-details').slideDown("fast");
    		    		//$("#addProduct").focus();


    		    		// Clear table and add inventory items     		    		
    		    		$('#product-details-lotNumbers > tbody tr').remove();
    		    		
    		    		if (ui.item.inventoryItems) { 
        		    		$('#product-details-lotNumbers').show();
        		    		$.each(ui.item.inventoryItems, function(index, value) { 
        		    			if (value) {
		    		    			var lotNumber = value.lotNumber || "<span class='fade'><warehouse:message code="default.empty.label"/></span>";
		    		    			var expirationDate = value.expirationDate;
		    		    				    		    			
		    		    			var existingLotNumber = 
			    		    			"<tr>" + 
			    		    			"<td><button onClick=\"addExistingItem('" + ui.item.product.id + "','" + value.lotNumber + "','" + value.quantity + "');\"><img src=\"${resource(dir: 'images/icons/silk', file: 'add.png')}\" style=\"vertical-align: middle;\"/></button></td>" + 
			    		    			"<td>" + lotNumber + "</td>" + 
		    		    				"<td>" + value.expirationDate + "</td>" + 
		    		    				"<td>" + value.quantity + "</td>" + 
		    		    				"<td></td>" + 
		    		    				"</tr>";
			    		    		
									$("#product-details-lotNumbers > tbody:last").append($(existingLotNumber));
								}	    		
							});

    		    			var newLotNumber = 
	    		    			"<tr class=\"prop\">" + 
	    		    			"<td><button onClick=\"addNewItem('" + ui.item.product.id + "');\"><img src=\"${resource(dir: 'images/icons/silk', file: 'add.png')}\" style=\"vertical-align: middle;\"/></button></td>" + 
	    		    			"<td colspan=\"3\"><warehouse:message code="transaction.addNewLotSerialNumber.label"/></td>" + 
    		    				"<td></td>" + 
    		    				"</tr>";
    		    				
    		    			// Add lot number row
							$("#product-details-lotNumbers > tbody:first").append($(newLotNumber));	        		    		
	    		    		
							
    		    		}
    		    		else { 
        		    		$('#product-details-lotNumbers').hide();
        		    	}
    		    		
    					return false;
    				}
    				
	    		}); 
	    	});
        </script>
    </body>
</html>
