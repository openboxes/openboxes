<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.addOrderItems.label"/></title>

</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>	
	
		<g:hasErrors bean="${orderCommand}">
			<div class="errors">
				<g:renderErrors bean="${orderCommand}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${orderListCommand}">
			<div class="errors">
				<g:renderErrors bean="${orderListCommand}" as="list" />
			</div>
		</g:hasErrors>
				
		<g:form action="receiveOrder" autocomplete="off">
			<div class="dialog">
				<fieldset>
					<g:render template="../order/summary" model="[orderInstance:order]"/>
					<g:render template="progressBar" model="['state':'processOrderItems']"/>		
					<table>
						<tr>
							<td style="padding: 0px;">
								<div>
									<g:hiddenField name="order.id" value="${orderCommand?.order?.id }"/>
									<g:hiddenField name="shipmentType.id" value="${orderCommand?.shipmentType?.id }"/>
									<g:hiddenField name="recipient.id" value="${orderCommand?.recipient?.id }"/>
									<g:hiddenField name="shippedOn" value="${formatDate(format:'MM/dd/yyyy', date: orderCommand?.shippedOn )}"/>
									<g:hiddenField name="deliveredOn" value="${formatDate(format:'MM/dd/yyyy', date: orderCommand?.deliveredOn )}"/>
								
									<g:if test="${orderItems }">
										<table id="orderItemsTable" border="0">
											<thead>
												<tr class="even">
													<th class="center" align="center" colspan="4">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'cart.png')}" alt="ordered" style="vertical-align: middle"/>
														<warehouse:message code="order.itemsOrdered.label"/>
													</th>
													<th class="center" align="center" colspan="4" style="border-left: 1px solid lightgrey;">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="received" style="vertical-align: middle"/>
														<warehouse:message code="order.itemsReceived.label"/>
													</th>
												</tr>
												<tr class="even">
													<td><warehouse:message code="default.type.label"/></td>
													<td><warehouse:message code="default.description.label"/></td>
													<td class="center"><warehouse:message code="order.ordered.label"/></td>										
													<td class="center"><warehouse:message code="order.remaining.label"/></td>	
													<td style="border-left: 1px solid lightgrey;"><warehouse:message code="order.received.label"/></td>										
													<td width="250px"><warehouse:message code="product.label"/></td>										
													<td width="100px"><warehouse:message code="product.lotNumber.label"/></td>		
													<td><warehouse:message code="default.expires.label"/></td>
												</tr>
											</thead>									
											<tbody>
											
												<g:set var="i" value="${0 }"/>
												<g:each var="entrymap" in="${orderItems?.groupBy { it?.orderItem } }">
													<g:each var="orderItem" in="${entrymap.value}">
												
														<tr class="${(orderItem?.primary)?"black-top":""} orderItem">
															<td>
																<g:hiddenField name="orderItems[${i }].orderItem.id" class="orderItemId" value="${orderItem?.orderItem?.id }"/>
																<g:hiddenField name="orderItems[${i }].primary" value="${orderItem?.primary }"/>
																<g:hiddenField name="orderItems[${i }].type" value="${orderItem?.type }"/>
																<g:hiddenField name="orderItems[${i }].description" value="${orderItem?.description }"/>
																<g:hiddenField name="orderItems[${i }].quantityOrdered" value="${orderItem?.quantityOrdered }"/>
																<g:if test="${orderItem?.primary }">${orderItem?.type }</g:if>
															</td>
															<td>
																<g:if test="${orderItem?.primary }">${orderItem?.description }</g:if>
															</td>
															<td class="center">
																<g:if test="${orderItem?.primary }">${orderItem?.quantityOrdered}</g:if>
															</td>
															<td class="center">
																<g:if test="${orderItem?.primary }">
																	${orderItem?.quantityOrdered - orderItem?.orderItem?.quantityFulfilled()}
																</g:if>
															</td>
															<td class="center" style="border-left: 1px solid lightgrey;">
																
																<g:if test="${!orderItem?.orderItem?.isCompletelyFulfilled() }">
																	<input type="text" name='orderItems[${i }].quantityReceived' value="${orderItem?.quantityReceived }" size="5" class="center updateable" />
																</g:if>
															</td>
															<td>
																<g:if test="${!orderItem?.orderItem?.isCompletelyFulfilled() }">
																	<g:autoSuggest id="productReceived-${i }" name="orderItems[${i }].productReceived" jsonUrl="${request.contextPath }/json/findProductByName" width="200" valueId="${orderItem?.productReceived?.id }" valueName="${format.product(product:orderItem?.productReceived)}"/>	
																</g:if>
															</td>
															<td>
																<g:if test="${!orderItem?.orderItem?.isCompletelyFulfilled() }">
																	<g:textField name="orderItems[${i }].lotNumber" value="${orderItem?.lotNumber }" size="10" class="updateable"/>
																</g:if>
															</td>
															<td nowrap="true">
																<g:if test="${!orderItem?.orderItem?.isCompletelyFulfilled() }">
																	<g:datePicker name="orderItems[${i }].expirationDate" precision="day" default="none" noSelection="['':'']"
																		years="${(1900 + (new Date().year))..(1900+ (new Date() + (50 * 365)).year)}" value="${orderItem?.expirationDate }" />					
																</g:if>
															</td>															
															<td>
																<g:if test="${!orderItem?.orderItem?.isCompletelyFulfilled() }">
																	<span class="buttons" style="padding: 0px;">
																		<input type="image" src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="add" class="btnAdd" style="vertical-align: middle"/>
																	</span>
																</g:if>
															</td>
														</tr>
														<g:set var="i" value="${i+1 }"/>
													</g:each>
												</g:each>
												
											</tbody>
													

										</table>
									</g:if>
									<g:else>
										<span class="fade"><warehouse:message code="default.noItems.label"/></span>
									</g:else>									
								</div>

							</td>						
						</tr>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}"></g:submitButton>
						<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton>
						<%-- 
						<g:submitButton name="finish" value="${warehouse.message(code:'default.button.saveAndExit.label')}"></g:submitButton>								
						--%>
						<g:link action="receiveOrder" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
					</div>
				</fieldset>
			</div>
		</g:form>
	</div>
	<script>
	var changed = false;
	var currentIndex = $("#orderItemsTable tbody tr.orderItem").length;

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
			$("#orderItemsTable").alternateRowColors();
		});
    });			
	    
	$(".btnAdd").click(function(event) {
		event.preventDefault();
		//console.log($(this));
		var index = currentIndex++;
		var currentRow = $(this).parent().parent().parent();
		var value = currentRow.find(".value");
		var valueText = currentRow.find(".autocomplete");
		var orderItemId = currentRow.find(".orderItemId");
 		var item = { Id: '0', Index: index, ProductId: value.val(), ProductName: valueText.val(), LotNumber: "", ExpirationDate: "", 
 		  				OrderItemId: orderItemId.val(), Template: '#new-item-template' };
		currentRow.after($(item.Template).tmpl(item));	
		$("#orderItemsTable").alternateRowColors();
	});

	$(document).ready(function() {
		jQuery.fn.alternateRowColors = function() {
			$('tbody tr:odd', this).removeClass('odd').addClass('even');
			$('tbody tr:even', this).removeClass('even').addClass('odd');
			return this;
		};				

		$("#orderItemsTable").alternateRowColors();
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
	<script id="new-item-template" type="text/x-jquery-tmpl">						
			<tr class="orderItem">
				<td>
					<a name="orderItems{{= Index }}"></a>
					<g:hiddenField name="orderItems[{{= Index }}].orderItem.id" value="{{= OrderItemId }}"/>
					<g:hiddenField name="orderItems[{{= Index }}].primary" value=""/>
					<g:hiddenField name="orderItems[{{= Index }}].type" value=""/>
					<g:hiddenField name="orderItems[{{= Index }}].description" value=""/>
					<g:hiddenField name="orderItems[{{= Index }}].quantityOrdered" value=""/>
				</td>
				<td>
				</td>
				<td class="center">
				</td>
				<td class="center">
				</td>
				<td class="center" style="border-left: 1px solid lightgrey;">
					<input type="text" name='orderItems[{{= Index }}].quantityReceived' value="" size="5" class="center updateable" />
				</td>
				<td>
					<input id="productReceived{{= Index}}.id" class="value" type="hidden" name="orderItems[{{= Index }}].productReceived.id" value="{{= ProductId}}"/>
					<input id="productReceived{{= Index}}.text" class="autocomplete" type="text" name="productReceived{{= Index}}.text" value="{{= ProductName}}" style="width: 200px;">
				</td>
				<td>
					<g:textField name="orderItems[{{= Index }}].lotNumber" value="{{= LotNumber}}" size="10" class="updateable"/>
				</td>
				<td nowrap="true">
					<g:datePicker name="orderItems[{{= Index }}].expirationDate" precision="day" default="none" value="" noSelection="['':'']"
						years="${(1900 + (new Date().year))..(1900+ (new Date() + (50 * 365)).year)}"/>					
				</td>															
				<td>
					<span class="buttons" style="padding: 0px;">
						<input type="image" src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}" alt="delete" class="btnDel" style="vertical-align: middle"/>
					</span>
				</td>
			</tr>
			
	</script>    	  
	<script language="javascript">
		$(document).ready(function() {
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
						$.getJSON('${request.contextPath }/json/findProductByName', req, function(data) {
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
							$(this).prev().val("null");
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