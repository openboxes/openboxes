<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Add request items</title>

</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>	
	
		<g:hasErrors bean="${requestCommand}">
			<div class="errors">
				<g:renderErrors bean="${requestCommand}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${requestListCommand}">
			<div class="errors">
				<g:renderErrors bean="${requestListCommand}" as="list" />
			</div>
		</g:hasErrors>
				
		<div class="dialog">
			<fieldset>
				<g:render template="../request/summary" model="[requestInstance:requestInstance]"/>
				<g:render template="progressBar" model="['state':'pickRequestItems']"/>		
				<g:form action="fulfillRequest" autocomplete="false">
					<table>
						<tr>
							<td style="padding: 0; margin: 0;">														
								<div>
									<g:if test="${requestItems }">
										<table id="requestItemsTable" border="0">
											<thead>
												<tr class="even">
													<td class="center" align="center" colspan="3">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'cart.png')}" alt="requested" style="vertical-align: middle"/>
														Items Requested
													</td>
													<td class="center" align="center" colspan="4" style="border-left: 1px solid lightgrey;">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="received" style="vertical-align: middle"/>
														Items Fulfilled
													</td>
												</tr>
												<tr class="even">
													<td>Type</td>
													<td>Description</td>
													<td class="center">Requested</td>										
													<%--<td class="center">Remaining</td> --%>	
													<td class="center" style="border-left: 1px solid lightgrey;">Fulfilled</td>										
													<td style="width: 250px">Product</td>										
													<td style="width: 100px;">Lot Number</td>
													<td style="width: 100px;">Expires</td>
												</tr>
											</thead>									
											<tbody>
												<g:each var="entrymap" in="${requestItems?.groupBy { it?.requestItem } }" status="i">
													<g:set var="fulfillItemsMap" value="${requestCommand?.fulfillItems?.groupBy { it.requestItem }}"/>
													<g:each var="requestItem" in="${entrymap.value}">
														<tr class="${(requestItem?.primary)?"black-top":""} ${i%2?'even':'odd'} requestItem">
															<td>
																<g:hiddenField name="requestItems[${i }].requestItem.id" class="requestItemId" value="${requestItem?.requestItem?.id }"/>
																<g:hiddenField name="requestItems[${i }].primary" value="${requestItem?.primary }"/>
																<g:hiddenField name="requestItems[${i }].type" value="${requestItem?.type }"/>
																<g:hiddenField name="requestItems[${i }].description" value="${requestItem?.description }"/>
																<g:hiddenField name="requestItems[${i }].quantityRequested" value="${requestItem?.quantityRequested }"/>
																<g:if test="${requestItem?.primary }">${requestItem?.type }</g:if>
															</td>
															<td>
																<g:if test="${requestItem?.primary }">
																	<g:if test="${requestItem.requestItem.product }">
																		<g:link controller="inventoryItem" action="showStockCard" id="${requestItem?.requestItem?.product?.id }">
																			${requestItem?.description }
																		</g:link>
																	</g:if>
																	<g:else>
																		${requestItem?.description }
																	</g:else>
																</g:if>
															</td>
															<td class="center">															
																<g:if test="${requestItem?.primary }">${requestItem?.quantityRequested}</g:if>
															</td>
															<%-- 
															<td class="center">
																<g:if test="${requestItem?.primary }">
																	${requestItem?.quantityRequested - requestItem?.requestItem?.quantityFulfilled()}
																</g:if>
															</td>
															--%>
															<td colspan="4" class="center" style="padding: 0px; border-left: 1px solid lightgrey;">
																
																<g:set var="fulfillmentItems" value="${fulfillItemsMap[entrymap.key] }"/>
																<g:if test="${fulfillmentItems }">																
																	<table>
																		<g:each var="fulfillmentItem" in="${fulfillmentItems }" status="j">
																			<tr class="${j%2?'even':'odd'}">
																				<td class="center">
																					${fulfillmentItem?.quantityReceived }
																				</td>
																				<td style="width: 250px;" >
																					${fulfillmentItem?.productReceived?.name }
																					<span class="fade">
																						${fulfillmentItem?.productReceived?.manufacturer }
																					</span>
																				</td>
																				<td style="width: 100px;">
																					${fulfillmentItem?.lotNumber }
																				</td>
																				<td style="width: 100px;">
																					${fulfillmentItem?.expirationDate }
																				</td>
																			</tr>
																		</g:each>
																	</table>
																</g:if>
																<div style="padding: 10px;">
																
																	<a href="javascript:void(0);" id="request-item-id${requestItem?.requestItem?.id }" class="fulfill-item-btn">fulfill item</a>
																</div>
															</td>
														</tr>
													</g:each>
												</g:each>
											</tbody>
										</table>
									</g:if>
								</div>
							</td>		
						</tr>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
					
						<a class="fulfill-item-btn">Fulfill item</a>
					
						<g:submitButton name="back" value="Back"></g:submitButton>
						<g:submitButton name="next" value="Next"></g:submitButton>
						<%-- 
						<g:submitButton name="finish" value="Save & Exit"></g:submitButton>								
						--%>
						<g:link action="fulfillRequest" event="cancel">Cancel</g:link>
					</div>
				</g:form>
			</fieldset>
		</div>

		<div id="dialog">
			<div id="dialog-form"></div>
			
		
		
			<%-- 
			<hr/>
			<g:form action="fulfillRequest" autocomplete="false">
				<table>
					<tr class="prop">	
						<td>
							<label>${warehouse.message(code: 'request.requestItem.label', default: 'Requested Item') }</label>
						</td>		
						<td>
							<g:select name="requestItem.id" from="${requestItems }" 
								optionKey="${{it?.requestItem?.id}}" optionValue="${{it?.quantityRequested + ' x ' + it?.requestItem?.description}}" 
								/>
							
						</td>
					</tr>					
					<tr class="prop">	
						<td>
							<label>${warehouse.message(code: 'requestItem.quantityFulfilled.label', default: 'Qty Fulfilled') }</label>
						</td>		
						<td>
							<input type="text" name='quantityReceived' 
								value="${requestItem?.quantityReceived }" size="5" class="center updateable" />
						</td>
					</tr>					
					<tr class="prop">								
						<td>
							<label>${warehouse.message(code: 'requestItem.productFulfilled.label', default: 'Product') }</label>
						</td>		
						<td>
							<g:autoSuggest name="productReceived" jsonUrl="/warehouse/json/findProductByName" width="200" valueId="${requestItem?.productReceived?.id }" valueName="${requestItem?.productReceived?.name }"/>	
						</td>
					</tr>
					<tr class="prop">			
						<td>
							<label>${warehouse.message(code: 'requestItem.lotNumber.label', default: 'Lot or Serial Number') }</label>
						</td>		
						<td>
							<g:textField name="lotNumbers" value="${requestItem?.lotNumber }" size="10" class="updateable"/>
						</td>
					</tr>
					<tr class="prop">			
						<td>
							<label>${warehouse.message(code: 'requestItem.expirationDate.label', default: 'Expiration Date') }</label>
						</td>		
						<td >
							<g:datePicker name="expirationDate" precision="month" default="none" noSelection="['':'']"
								years="${(1900 + (new Date().year))..(1900+ (new Date() + (50 * 365)).year)}" value="${requestItem?.expirationDate }" />					
						</td>															
					</tr>
					<tr>
						<td>
						
						</td>
						<td>
							<g:submitButton name="fulfillRequestItem" value="Fulfill"></g:submitButton>
						
						</td>
					</tr>
				</table>
			</g:form>
			--%>
		</div>			



	</div>
	
	
	
	<script>
		var changed = false;
		var currentIndex = $("#requestItemsTable tbody tr.requestItem").length;
	
	
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
		$(".btnDel").livequery(function(){
			$(this).click(function(event) {					
				//removeItem($(this).val());
				event.preventDefault();
				$(this).parent().parent().parent().remove();
				$("#requestItemsTable").alternateRowColors();
			});
	    });			
		    
		$(".btnAdd").click(function(event) {
			event.preventDefault();
			//console.log($(this));
			var index = currentIndex++;
			var currentRow = $(this).parent().parent().parent();
			var value = currentRow.find(".value");
			var valueText = currentRow.find(".autocomplete");
			var requestItemId = currentRow.find(".requestItemId");
	 		var item = { Id: '0', Index: index, ProductId: value.val(), ProductName: valueText.val(), LotNumber: "", ExpirationDate: "", 
	 		  				RequestItemId: requestItemId.val(), Template: '#new-item-template' };
			currentRow.after($(item.Template).tmpl(item));	
			$("#requestItemsTable").alternateRowColors();
			//var productSelect = $("#productReceived-" + index);
			//console.log(productSelect);
			//selectCombo(productSelect, productId );
			
		});
	
		$(document).ready(function() {
			/*
			jQuery.fn.alternateRowColors = function() {
				$('tbody tr:odd', this).removeClass('odd').addClass('even');
				$('tbody tr:even', this).removeClass('even').addClass('odd');
				return this;
			};				
			*/
			//$("#requestItemsTable").alternateRowColors();
	    	$(".updateable").change(function() { 
				changed = true
			});
	
			
			$(".checkable").click(function() { 
				if (changed) {
					alert("Please reset or save your changes first.")
					return false;
				}
			});
	   	});
	</script>  	  
	<script language="javascript">
		$(document).ready(function() {		
			$("a.fulfill-item-btn").click(function(event) {
				//$("#fulfill-item-dialog").dialog('open');
				$('#dialog').dialog({
					autoOpen: true, width: 800, height: 500, modal: true
				});	

				var requestItemId = $(this).attr('id').replace('request-item-id', '')
				$("#dialog-form").load("/warehouse/request/fulfillItem?id=" + requestItemId);


				//var link = $(this);
				//var dialog = jQuery('#transaction-details').load(link.attr('href')).dialog("open");										        
		        //event.preventDefault();
																	
			});					
			$(".autocomplete").livequery(function() {
				$(this).autocomplete({
					width: '200px',
					minLength: 1,
					dataType: 'json',
					highlight: true,
					scroll: true,
					autoFill: true,
					autoFocus: true,
					//define callback to format results
					source: function(req, add){
						$.getJSON('/warehouse/json/findProductByName', req, function(data) {
							var items = [];
							$.each(data, function(i, item) {
								items.push(item);
							});
							add(items);
						});
					  },
					focus: function(event, ui) {
						return false;
					},
					change: function(event, ui) {
						if (!ui.item) { 
							$(this).prev().val("");
							$(this).val("");
						}						
						return false;
					},
					select: function(event, ui) {						
						if (ui.item) { 
							$(this).val(ui.item.valueText);
							$(this).prev().val(ui.item.value);
						}
						return false;
					}
				});
			});
		});
	</script>	
	  
	
</body>
</html>