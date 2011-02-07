<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'transaction.label', default: 'Transaction')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>    
        
        <style>
        	.dialog form label { position: absolute; display: inline; width: 140px; text-align: right;}
        	.dialog form .value { margin-left: 160px; }
        	.dialog form ul li { padding: 10px; } 
        	.dialog form { width: 100%; } 
        	.lotNumberTable tr td { padding: 2px; margin: 0px; }
        	.header th { background-color: #525D76; color: white; } 
        </style>
        
        <script>

			function initializeTypeSelector(select) { 
				var transactionType = select.val();
				console.log(transactionType == 1);
				//option:selected
				if (transactionType == 1) {
					console.log("show all");
					$("#source\\.id").closest("li").show();
					$("#destination\\.id").closest("li").show();
					$("#inventory\\.id").closest("li").hide();
				}
				else {
					console.log("hide all");
					$("#source\\.id").closest("li").hide();
					$("#destination\\.id").closest("li").hide();
					$("#inventory\\.id").closest("li").show();
				}						
			}

			function clearField(field) { 
		    	field.removeClass("fade"); 
		    	field.val(''); 
			}
			
	        var transaction = { TransactionEntries: [] };	

			<g:each var="entry" in="${transactionInstance.transactionEntries}" status="status">
				var transactionEntry = { Id: 0, Index: ${status}, ProductId: 0, ProductName: '', LotNumber: '', Description: '', ExpirationDate: '', Qty: 0, StyleClass: '' };
				transaction.TransactionEntries.push(transactionEntry);
			</g:each>
	        
	    	$(document).ready(function() {

	    		var cache = {}, lastXhr;
			    	
		    	// Initialize the transaction type selector
		    	initializeTypeSelector($("#transactionType\\.id"));
		    	
		    	// Initialize the product search autocomplete
				$('#productSearch').addClass("fade");


		    	
		    	/**
		    	 * On change of transaction type, initialize the fields 
		    	 * to display.
		    	 */
				$("#transactionType\\.id").change(function(){
					initializeTypeSelector($(this));					
				});
				

				/**
				 * When product search becomes focus, we remove fade, reset values
				 */
		    	$('#productSearch').focus(function() { 
			    	clearField($(this));
					$("#productId").val('');
					$("#productName").val('');			    				    	
			    });

			    
		    	//$("#productSearch").blur(function() { 
				//	$(this).addClass("fade");
			    //	$(this).val('Find another product to add'); 
			    //});		    	

		    	/**
		    	 * On click of addProduct button, adds a new transaction entry to table.
		    	 */
	    		$("#addProduct").click(function(event) { 
		    		event.preventDefault();
		    		$('#productSearch').addClass("fade");					
		    		$("#productSearch").val("Find another product to add");
		    		var productId = $('#productId').val();
					var productName = $('#productName').val();
					// If the product does not exist, show error 
					if (!productId) {
						alert("Please select a valid product");
					} 
					// Otherwise, add to the transaction entries array and display new row
					else { 
		    			var index = transaction.TransactionEntries.length;
		    			var transactionEntry = { Id: 0, Index: index++, ProductId: productId, ProductName: productName, 
		    	    			LotNumber: '', Description: '', ExpirationDate: '', Qty: 0, StyleClass: (index % 2) ? 'odd':'even' };

    	    			// Add to the array
		    			transaction.TransactionEntries.push(transactionEntry);

		    			// Display new row in the table 
		    			$("#transactionEntryRowTemplate").tmpl(transactionEntry).appendTo('#unsavedTransactionEntryTable');		    			
					}
					$("#productSearch").trigger("focus");
	    		});


	    		/**
	    		 * On click "addLotNumber", add new lot number row
	    		 */
	    		$(".addLotNumber").livequery(function() { 
	    			$(this).click(function(event) { 

		    			var productId = $(this).siblings().filter("#product\\.id");
	    				var index = transaction.TransactionEntries.length;
		    			var transactionEntry = { Id: 0, Index: index++, ProductId: productId.attr('value'), ProductName: '', 
		    	    			LotNumber: '', Description: '', ExpirationDate: '', Qty: 0, StyleClass: (index % 2) ? 'odd':'even' };
						transaction.TransactionEntries.push(transactionEntry);
		    			
						var lotNumberTableBody = $(this).parent().parent().parent();
						$("#lotNumberRowTemplate").tmpl(transactionEntry).appendTo(lotNumberTableBody);
						event.preventDefault();
	    			});
		    	});


	    		/**
	    		 * On deleteLotNumber click, remove lot number row
	    		 */
	    		$(".deleteLotNumber").livequery(function() { 
	    			$(this).click(function(event) { 
						console.log("remove lot number");
		    			$(this).closest("tr").remove();
	    			});
	    		});

	    		/**
	    		 * On click of deleteProduct button, remove entire product row.
	    		 */
	    		$(".deleteProduct").livequery(function(){
					$(this).click(function(event) {
						//event.preventDefault();
						$(this).closest("tr").parent().parent().closest("tr").remove();
					});
			    });


	    		/**
	    		 * Suppress the submit action when the ENTER key is pressed during a product search.
	    		 * In addition, we want to trigger a CLICK event on the addProduct button.
	    		 */
	    		$("#productSearch").keypress(function (event) {
	    			if (event.keyCode == 13) {
	    			    event.preventDefault();
	    			    $("#addProduct").trigger("click");
	    			}
	    		});   		
	    		
	    		/**
	    		 * Bind autocomplete widget to the product search field.
	    		 */
	    		$('#productSearch').autocomplete( {
		    		//selectFirst: true,
					minLength: 2,
					delay: 100,
    				source: function(request, response) {
        				
    					$.getJSON('/warehouse/json/findProductByName', request, function(data, status, xhr) {
    						var items = [];
    						$.each(data, function(i, item) { items.push(item); });
    						response(items);
    					});
    				

    					/* Use the following code to implement caching

    					var term = request.term;
    					if ( term in cache ) {
    						response( cache[ term ] );
    						return;
    					}

    					lastXhr = $.getJSON( "/warehouse/json/findProductByName", request, function( data, status, xhr ) {
    						var items = [];
    						$.each(data, function(i, item) { items.push(item); });
    						response(items);
    						        					
    						cache[ term ] = items;
    						if ( xhr === lastXhr ) {
    							response( items );
    						}
    					});
						*/
    				},
    				focus: function( event, ui ) {
    					$(this).val( ui.item.label );	// in order to prevent ID from being displayed in widget
    					return false;
    				},
    				select: function(event, ui) {
    					$(this).val( ui.item.label ); 	// in order to prevent ID from being displayed in widget
        				$("#productId").val(ui.item.value);
    					$("#productName").val(ui.item.valueText);
    					return false;
    				}
    				
	    		});

	    		
	    		/**
	    		 * Bind autocomplete widget to the all .lotNumber input fields.  
	    		 * The livequery feature allows us to bind the autocomplete 
	    		 * widget to all lot number fields (including those created after
	    	     * the onload event has completed.
	    		 */
				$(".lotNumber").livequery(function() {
					// For some reason, we have to get the product ID outside of the autocomplete.
					var productId = $(this).prev().prev().val();
					$(this).autocomplete( {
						delay: 100,
						minLength: 2,
	    				source: function(request, response) {

	    					var params = {};
	    					params['term'] = request.term;
	    					params['productId'] = productId;
	    					$.getJSON('/warehouse/json/findLotsByName', params, function(data) {
	    						var items = [];
	    						$.each(data, function(i, item) { items.push(item); });
	    						response(items);
	    					});
	    				},
	    				select: function(event, ui) {
							console.log("selected " + ui.item.lotNumber);
		    				$(this).parent().find('.hiddenLotNumber').val(ui.item.lotNumber);
		    				return false;
		    				//$(this).parent().siblings().find()
	    				},
	    				focus: function(event, ui) { 
							console.log("on focus " + ui.item.label);
	    					$(this).val( ui.item.label );	// in order to prevent ID from being displayed in widget
	    					return false;		    				
		    			},
	    				change: function(event, ui) { 
							// Allows user to enter a new lot number value
							var value = $(this).attr('value');
							console.log("changed value " + value);
							$(this).parent().find('.hiddenLotNumber').val(value);
							return false;
		    			}	    				
		    		});

		    		$(this).keypress(function (event) {
		    			if (event.keyCode == 13) {
		    			    event.preventDefault();
		    			}
		    		});   
		    				    		
		    		
					
				});
		    	
	    	});
        </script>
        <script id="transactionEntryRowTemplate" type="x-jquery-tmpl">
			<tr id="product{{= Index }}" class="product">
				<td colspan="3">
					<table id="lotNumberTable{{= Index }}" class="lotNumberTable" border="1" width="100%" style="border: 1px solid lightgrey;">
						<thead>
							<tr class="header">
								<th>Product</th>
								<th>Lot/Serial Number</th>
								<th>Qty</th>
								<th></th>
							</tr>
						</thead>
						<tbody class="lotNumberTableBody">							
							<tr class="{{= StyleClass}}">
								<td width="40%">
									{{= ProductName}}
								</td>
								<td width="25%">
									<g:hiddenField type="hidden" name="transactionEntries[{{= Index}}].product.id" class="hiddeProductId" value="{{= ProductId}}"/>
									<g:hiddenField type="hidden" name="transactionEntries[{{= Index}}].lotNumber" class="hiddenLotNumber" value=""/>
									<g:textField type="text" name="lotNumber" value="" class="lotNumber" size="15"/>
								</td>
								<td width="5%">
									<input type="text" class="quantity" name="transactionEntries[{{= Index}}].quantity" size="3"/>
								</td>
								<td width="20%" nowrap="nowrap">
									<input type="hidden" id="product.id" name="product.id" value="{{= ProductId}}"/>
									<button type="button" class="addLotNumber">
										<img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png')}"/>
									</button>	
									<button type="button" class="deleteProduct" value="{{= Index}}">
										<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bin.png')}"/>
									</button>
								</td>
							</tr>

						</tbody>
					</table>
				</td>
			</tr>
		</script>
        <script id="lotNumberRowTemplate" type="x-jquery-tmpl">
			<tr class="{{= StyleClass}}">
				<td>
				</td>
				<td>
					
					<g:hiddenField name="transactionEntries[{{= Index}}].product.id" class="hiddenProductId" value="{{= ProductId}}"/>
					<g:hiddenField name="transactionEntries[{{= Index}}].lotNumber" class="hiddenLotNumber" value=""/>
					<g:textField name="lotNumber" class="lotNumber" size="15"/>
				</td>
				<td>
					<g:textField class="quantity" name="transactionEntries[{{= Index}}].quantity" size="3"/>
				</td>
				<td>
					<button class="deleteLotNumber">
						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png')}"/>	
					</button>
				</td>
			</tr>
		</script>        
    </head>    

    <body>
        <div class="body">
			<div class="nav">
				<g:render template="nav"/>
			</div>
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    

			<div class="dialog">
			
				
				<g:form action="saveNewTransaction">
					<g:hiddenField name="id" value="${transactionInstance?.id}"/>
					
					<h2>Create New Transaction</h2>

					<ul>
						<li class="prop odd">											
							<label>ID</label>
							<span class="value">
								${transactionInstance.id }
	                       	</span>
						</li>
						<li class="prop odd">											
							<label>Transaction Type</label>
							<span class="value">
								<g:select name="transactionType.id" from="${transactionTypeList}" 
		                       		optionKey="id" optionValue="name" value="${transactionInstance.transactionType?.id}" noSelection="['null': '']" />
	                       	</span>
						</li>
						<li class="prop even">
							<label>Transaction Date</label>
							<span class="value">
								<g:jqueryDatePicker id="transactionDate" name="transactionDate"
										value="${transactionInstance?.transactionDate}" format="MM/dd/yyyy"/>
							</span>								
						</li>
						<li class="prop odd">
							<label>From</label>
							<span class="value">
								<g:select id="source.id" name="source.id" from="${warehouseInstanceList}" 
		                       		optionKey="id" optionValue="name" value="${transactionInstance?.source?.id}" noSelection="['null': '']" />
                       		</span>
						</li>
						<li>
							<label>To</label>
							<span class="value">
								<g:select id="destination.id" name="destination.id" from="${warehouseInstanceList}" 
		                       		optionKey="id" optionValue="name" value="${transactionInstance?.destination?.id}" noSelection="['null': '']" />
							</span>
						</li>
						<li class="prop odd">
							<label>Inventory</label>
							<span class="value">
								<g:hiddenField name="inventory.id" value="${warehouseInstance?.inventory?.id}"/>
								${warehouseInstance?.name }
							</span>								
						</li>
						
						
						<%-- 
						<g:if test="${transactionInstance?.id }">
							<li>
								<label>Confirmed</label>
								<span class="value">								
									<g:checkBox name="confirmed" value="${transactionInstance?.confirmed }"/>
								</span>
							</li>
							<li>
								<label>Confirmed by</label>
								<span class="value">
									<g:select name="confirmedBy.id" from="${org.pih.warehouse.core.User.list()}" 
			                       		optionKey="id" optionValue="name" value="${transactionInstance?.confirmedBy?.id}" noSelection="['null': '']" />									
			               		</span>
							</li>
							<li>
								<label>Confirmed on</label>
								<span class="value">
									<g:jqueryDatePicker id="dateConfirmed" name="dateConfirmed"
											value="${transactionInstance?.dateConfirmed}" format="MM/dd/yyyy"/>
								</span>
							</li>
						</g:if>
						--%>
					</ul>

						
						
					<h2>Add Transaction Entries</h2>
						
					<ul>
						<li class="prop odd">
							<label>Add another product</label>
							<div class="value">
								
								<input type="hidden" id="productId"/>
								<input type="hidden" id="productName"/>
								<input type="text" id="productSearch" value="Find another product to add"> &nbsp;
								<button id="addProduct" type="button">
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png')}"/>	
								</button>
							</div>								
						</li>
						
						<li class="prop even">
							
							<div  style="float: left; padding: 0px;  width: 50%">
								<h3>Saved Items</h3>
								<table id="transactionEntryTable" border="1" style="border: 1px solid lightgrey;">
									<g:if test="${transactionInstance?.transactionEntries }">
										<thead>
											<tr class="header">
												<th>ID</th>
												<th>Product</th>
												<th>ID / Serial / Exp Date</th>
												<th>Qty</th>
											</tr>
										</thead>
										<tbody>
											<g:set var="index" value="${0 }"/>
											<g:set var="transactionMap" value="${transactionInstance?.transactionEntries.groupBy { it.product.name } }"/>
											<g:each in="${transactionMap.keySet()}" var="key" >
												<g:set var="transactionEntries" value="${transactionMap.get(key) }"/>
												<g:each in="${transactionEntries }" var="transactionEntry" status="status">
													<tr class="${(status%2==0)?'odd':'even'}">
														<td>
															${transactionEntry?.id}
														
														</td>
														<td style="text-align: left;">
															<g:if test="${status == 0}">${transactionEntry?.product?.name }</g:if>
															<g:hiddenField name="transactionEntries[${index}].id" value="${transactionEntry?.id}"/>
															<g:hiddenField name="transactionEntries[${index}].product.id" value="${transactionEntry?.product?.id}"/>
														</td>										
														<td>
															<g:hiddenField name="transactionEntries[${index}].inventoryItem.id" value="${transactionEntry?.inventoryItem?.id}"/>
															<g:hiddenField name="transactionEntries[${index}].lotNumber" class="hiddenLotNumber" value="${transactionEntry?.inventoryItem?.lotNumber}"/>
															<g:textField type="text" name="lotNumber" class="lotNumber" value="${transactionEntry?.inventoryItem?.lotNumber}" size="15"/>															
														</td>		
														<td>
															<g:textField class="quantity" name="transactionEntries[${index}].quantity" value="${transactionEntry?.quantity }" size="3"/>
														</td>
													</tr>
													<g:set var="index" value="${index+1 }"/>
												</g:each>
											</g:each>
										</tbody>
									</g:if>
									<g:else>
										<thead>
											<tr>
												<td>
													<span class="fade">No saved transaction entries</span>
												</td>
											</tr>
										</thead>
									</g:else>
									
								</table>
							</div>
							
							<div style="float: left; padding: 0px; margin: 0px; width: 40%; padding-left: 10%;">
								<h3>Unsaved Items</h3>
								
								<table id="unsavedTransactionEntryTable">
									<!--  Empty to start -->								
								</table>																

								
							</div>							
							
							<br clear='all'/>
						</li>
						
					</ul>
					<ul>
						<li class="prop even">
							<div style="text-align: center;">
								<button type="submit" name="save">								
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'tick.png')}"/>&nbsp;Save
								</button>
								&nbsp;
								<g:link action="listTransactions">
				                    ${message(code: 'default.button.cancel.label', default: 'Cancel')}						
								</g:link>			
							</div>
						</li>
					</ul>
				</g:form>
			</div>
		</div>
    </body>
</html>
