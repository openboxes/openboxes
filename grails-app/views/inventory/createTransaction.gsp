<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'transaction.label', default: 'Transaction')}" />
        <title>
	        <g:if test="${transactionInstance?.id }">
		        <g:message code="default.edit.label" args="[entityName]" />  
	    	</g:if>
	    	<g:else>
		        <g:message code="default.create.label" args="[entityName]" />    
			</g:else>    	    
		</title>
        <style>
        	.dialog form label { position: absolute; display: inline; width: 140px; text-align: right;}
        	.dialog form .value { margin-left: 160px; }
        	.dialog form ul li { padding: 10px; } 
        	.dialog form { width: 100%; } 
        	.header th { background-color: #525D76; color: white; } 
        </style>
        
        <script>
        	function paintStripes(obj) { 
            	obj.not(":hidden").removeClass("even");
            	obj.not(":hidden").removeClass("odd");            	
				obj.not(":hidden").filter(":even").addClass("even");
				obj.not(":hidden").filter(":odd").addClass("odd");
            }
			function initializeTypeSelector(select) { 
				var transactionType = select.val();
				//option:selected
				if (transactionType == 1) {
					$("#source\\.id").closest("li").show();
					$("#destination\\.id").closest("li").show();
					$("#inventory\\.id").closest("li").hide();
				}
				else {
					$("#source\\.id").closest("li").hide();
					$("#destination\\.id").closest("li").hide();
					$("#inventory\\.id").closest("li").show();
				}						
				var stripedList = $(".striped li").not(":hidden");
								
				paintStripes($(".striped li"));
				$("#transaction-entries-li").addClass("even");
				
			}

			function clearField(field) { 
		    	field.removeClass("fade"); 
		    	field.val(''); 
			}

			/**
			 * Initialize the transaction entry array
			 */
	        var transaction = { TransactionEntries: [] };
	        <g:if test="${transactionInstance?.transactionEntries }">
	        	<g:set var="index" value="${0 }"/>
		        <g:set var="transactionMap" value="${transactionInstance?.transactionEntries?.groupBy { it?.product?.name } }"/>
				<g:each in="${transactionMap?.keySet()}" var="key" >
					<g:set var="transactionEntries" value="${transactionMap?.get(key) }"/>
					<g:each in="${transactionEntries }" var="transactionEntry" status="status">	        
						transaction.TransactionEntries.push({ 	Id: '${transactionEntry?.id}', 
																Index: ${index++}, 
																Template: '${status==0?'#transaction-entry-template':'#lot-number-template'}',
																ProductId: '${transactionEntry?.product?.id}', 
																ProductName: '${transactionEntry?.product?.name}', 
																LotNumber: '${transactionEntry?.inventoryItem?.lotNumber}', 
																Description: '${transactionEntry?.inventoryItem?.description}', 
																ExpirationDate: '', Qty: '${transactionEntry?.quantity}', 
																StyleClass: '' });
					</g:each>
				</g:each>
			</g:if>

			/**
			 * On load event 
			 */	        
	    	$(document).ready(function() {
				// Initialize table with the transaction entries from the server 
				if (transaction.TransactionEntries.length > 0) { 
					$.each(transaction.TransactionEntries, function(index, value) { 
						if (value.ProductId) { 
							$(value.Template).tmpl(value).appendTo('#transaction-entries-table > tbody');								
						}
					});
					$("#transaction-entries-table > tbody tr.empty").hide();	
					paintStripes($("#transaction-entries-table > tbody tr"));  
				} 					    	

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
		    		$("#productSearch").val("Add another product ...");
		    		var productId = $('#productId').val();
					var productName = $('#productName').val();
					// If the product does not exist, show error 
					if (!productId) {
						alert("Please select a valid product");
					} 
					// Otherwise, add to the transaction entries array and display new row
					else { 
		    			var index = transaction.TransactionEntries.length;
		    			var transactionEntry = { Id: '(new)', Index: index++, ProductId: productId, ProductName: productName, 
		    	    			LotNumber: '', Description: '', ExpirationDate: '', Qty: 0, StyleClass: '' };

    	    			// Add to the array
		    			transaction.TransactionEntries.push(transactionEntry);

		    			// Display new row in the table 
		    			//$("#transaction-entries-table > thead").hide();
		    			$("#transaction-entry-template").tmpl(transactionEntry).appendTo('#transaction-entries-table > tbody');		
		    			$("#transaction-entries-table > tbody tr.empty").hide();    
		    			paintStripes($("#transaction-entries-table > tbody tr"));    
		    						
					}
					$("#productSearch").trigger("focus");
	    		});


	    		/**
	    		 * On click "addLotNumber", add new lot number row
	    		 */
	    		$(".add-lot-number").livequery(function() { 
	    			$(this).click(function(event) { 
	    				var productName = $(this).siblings().filter("#productName");
		    			var productId = $(this).siblings().filter("#product\\.id");
	    				var index = transaction.TransactionEntries.length;
		    			var transactionEntry = { 
			    			Id: '(new)', 
			    			Index: index++, 
			    			ProductId: productId.val(), 
			    			ProductName: productName.val(), 
	    	    			LotNumber: '', 
	    	    			Description: '', 
	    	    			ExpirationDate: '', 
	    	    			Qty: 0, 
	    	    			StyleClass: '' 
			    	    };
						transaction.TransactionEntries.push(transactionEntry);
		    			
						//var lotNumberTableBody = $(this).parent().parent().parent();
						//$("#lot-number-template").tmpl(transactionEntry).appendTo(lotNumberTableBody);
						var tr = $(this).closest("tr");
						var lotNumberTr = $("#lot-number-template").tmpl(transactionEntry);
						lotNumberTr.insertAfter(tr);
						paintStripes($("#transaction-entries-table > tbody tr"));    
						event.preventDefault();

						
	    			});
		    	});


	    		/**
	    		 * On delete-lot-number click, remove lot number row
	    		 */
	    		$(".delete-lot-number").livequery(function() { 
	    			$(this).click(function(event) { 
		    			$(this).closest("tr").remove();
		    			paintStripes($("#transaction-entries-table > tbody tr"));   
	    			});
	    		});

	    		/**
	    		 * On click of delete-product button, remove entire product row.
	    		 */
	    		$(".delete-product").livequery(function(){
					$(this).click(function(event) {
						//event.preventDefault();
						var rowClass = "tr.product-" + $(this).val();
						$("#transaction-entries-table > tbody").find(rowClass).remove();
						paintStripes($("#transaction-entries-table > tbody tr"));   
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
		    				$(this).parent().find('.hiddenLotNumber').val(ui.item.lotNumber);
		    				return false;
		    				//$(this).parent().siblings().find()
	    				},
	    				focus: function(event, ui) { 
	    					$(this).val( ui.item.label );	// in order to prevent ID from being displayed in widget
	    					return false;		    				
		    			},
	    				change: function(event, ui) { 
							// Allows user to enter a new lot number value
							var value = $(this).attr('value');
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
        

        
        <script id="transaction-entry-empty-template" type="x-jquery-tmpl">
			<tr>
				<td colspan="5">No products</td>
			</tr>
        </script>
        <script id="transaction-entry-template" type="x-jquery-tmpl">						
			<tr id="product-lot-row-{{= Index }}" class="{{= StyleClass}} product-{{= ProductId}}">
				<td>
					<span class="fade">{{= Id }}</span>
				</td>
				<td>
					{{= ProductName}} 
					&nbsp; <a href="${createLink(controller: 'inventoryItem', action: 'showStockCard')}/{{= ProductId}}" target="_blank">Open Stock Card &rsaquo;</a>
				</td>
				<td>
					<g:hiddenField name="transactionEntries[{{= Index}}].product.id" class="hiddenProductId" value="{{= ProductId}}"/>
					<g:hiddenField name="transactionEntries[{{= Index}}].lotNumber" class="hiddenLotNumber" value="{{= LotNumber}}"/>
					<g:textField name="lotNumber" value="" class="lotNumber" size="15" value="{{= LotNumber}}"/>
				</td>
				<td>

				</td>
				<td>
					<g:textField class="quantity" name="transactionEntries[{{= Index}}].quantity" value="{{= Qty}}" size="3"/>
				</td>
				<td nowrap="nowrap">
					<g:hiddenField id="product.id" name="product.id" value="{{= ProductId}}"/>
					<g:hiddenField id="productName" name="productName" value="{{= ProductName}}"/>
					<button type="button" class="add-lot-number">
						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png')}"/>
					</button>	
					<button type="button" class="delete-product" value="{{= ProductId}}">
						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bin.png')}"/>
					</button>
				</td>
			</tr>        
		</script>
        <script id="lot-number-template" type="x-jquery-tmpl">
			<tr id="product-lot-row-{{= Index }}" class="{{= StyleClass}} product-{{= ProductId}}">
				<td>
					<span class="fade">{{= Id }}</span>
				</td>
				<td>
					<span class="fade">{{= ProductName}} </span>
				</td>
				<td>
					<g:hiddenField name="transactionEntries[{{= Index}}].product.id" class="hiddenProductId" value="{{= ProductId}}"/>
					<g:hiddenField name="transactionEntries[{{= Index}}].lotNumber" class="hiddenLotNumber" value="{{= LotNumber}}"/>
					<g:textField name="lotNumber" class="lotNumber" size="15" value="{{= LotNumber}}"/>
				</td>
				<td>

				</td>
				<td>
					<g:textField class="quantity" name="transactionEntries[{{= Index}}].quantity" value="{{= Qty}}" size="3"/>
				</td>
				<td>
					<button class="delete-lot-number">
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

			<div class="dialog" >
			
				
				<g:form action="saveNewTransaction">
					<g:hiddenField name="id" value="${transactionInstance?.id}"/>
					
					<ul class="striped">
						<li id="transaction-type-li" class="prop">											
							<label>Transaction Type</label>
							<span class="value">
								<g:select name="transactionType.id" from="${transactionTypeList}" 
		                       		optionKey="id" optionValue="name" value="${transactionInstance.transactionType?.id}" noSelection="['null': '']" />
	                       	</span>
						</li>
						<li id="transaction-date-li" class="prop transactionDate">
							<label>Transaction Date</label>
							<span class="value">
								<g:jqueryDatePicker id="transactionDate" name="transactionDate"
										value="${transactionInstance?.transactionDate}" format="MM/dd/yyyy"/>
							</span>								
						</li>
						<li id="from-li"  lass="prop from">
							<label>From</label>
							<span class="value">
								<g:select id="source.id" name="source.id" from="${warehouseInstanceList}" 
		                       		optionKey="id" optionValue="name" value="${transactionInstance?.source?.id}" noSelection="['null': '']" />
                       		</span>
						</li>
						<li id="to-li" class="prop to">
							<label>To</label>
							<span class="value">
								<g:select id="destination.id" name="destination.id" from="${warehouseInstanceList}" 
		                       		optionKey="id" optionValue="name" value="${transactionInstance?.destination?.id}" noSelection="['null': '']" />
							</span>
						</li>
						<li id="inventory-li" class="prop inventory">
							<label>Inventory</label>
							<span class="value">
								<g:hiddenField name="inventory.id" value="${warehouseInstance?.inventory?.id}"/>
								${warehouseInstance?.name }
							</span>								
						</li>
						<li id="transaction-entries-li" class="prop entries">
							<div style="width: 100%;" >
								
								<table id="transaction-entries-table" border="0" style="border: 0px solid lightgrey; background-color: white; display: inline;">
									<thead>
										<tr class="header">
											<th>ID</th>
											<th>Product</th>
											<th>Lot / Serial Number</th>
											<th>Expires</th>
											<th>Qty</th>
											<th>Actions</th>
										</tr>
									</thead>
									<tbody>
										<%--
										
										<g:if test="${transactionInstance?.transactionEntries }">
										
											<g:set var="index" value="${0 }"/>
											<g:set var="transactionMap" value="${transactionInstance?.transactionEntries.groupBy { it.product.name } }"/>
											<g:each in="${transactionMap.keySet()}" var="key" >
												<g:set var="transactionEntries" value="${transactionMap.get(key) }"/>
												<g:each in="${transactionEntries }" var="transactionEntry" status="status">
													<tr class="${(index%2==0)?'odd':'even'}">
														<td width="5%">
															${transactionEntry?.id}
														
														</td>
														<td width="35%" style="text-align: left;">
															<g:if test="${status == 0}">
																${transactionEntry?.product?.name }
																&nbsp;
																<g:link controller="inventoryItem" action="showStockCard" id="${transactionEntry?.product?.id }">Show stock card</g:link> 
																
															</g:if>
															<g:hiddenField name="transactionEntries[${index}].id" value="${transactionEntry?.id}"/>
															<g:hiddenField name="transactionEntries[${index}].product.id" value="${transactionEntry?.product?.id}"/>
														</td>										
														<td width="25%">
															<g:hiddenField name="transactionEntries[${index}].inventoryItem.id" value="${transactionEntry?.inventoryItem?.id}"/>
															<g:hiddenField name="transactionEntries[${index}].lotNumber" class="hiddenLotNumber" value="${transactionEntry?.inventoryItem?.lotNumber}"/>
															<g:textField type="text" name="lotNumber" class="lotNumber" value="${transactionEntry?.inventoryItem?.lotNumber}" size="15"/>															
														</td>		
														<td width="5%">
															<g:textField class="quantity" name="transactionEntries[${index}].quantity" value="${transactionEntry?.quantity }" size="3"/>
														</td>
														<td width="10%"></td>
													</tr>
													<g:set var="index" value="${index+1 }"/>
												</g:each>
											</g:each>
										</g:if>
										
										 --%>
									</tbody>
													
													
									<tfoot>
										<tr>
											<td colspan="6">
												<g:hiddenField id="productId" name="productId"/>
												<g:hiddenField id="productName" name="productName"/>
												<g:textField  id="productSearch" name="productSearch" value="Add another product ..." size="30"/> &nbsp;
												<button id="addProduct" type="button">
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png')}"/>	
												</button>							
											
											</td>
										</tr>
									</tfoot>				
								</table>
							</div>							
						</li>						
					</ul>
					
					<ul>
						<li class="prop even">
							<div style="text-align: center;">
								<button type="submit" name="save">								
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'tick.png')}"/>&nbsp;Save
								</button>
								&nbsp;
								
								<g:if test="${transactionInstance?.id }">
									<g:link action="listTransactions">
					                    ${message(code: 'default.button.cancel.label', default: '&lsaquo; Back to transactions')}						
									</g:link>			
								</g:if>
								<g:else>
									<g:link action="listTransactions">
					                    ${message(code: 'default.button.cancel.label', default: 'Cancel')}						
									</g:link>								
								</g:else>
							</div>
						</li>
					</ul>
				</g:form>
			</div>
		</div>
    </body>
</html>
