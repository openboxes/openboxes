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
				<g:render template="../order/summary" model="[orderInstance:order, currentState:'processOrderItems']"/>
				<div class="box">
					<h2>${warehouse.message(code: 'order.wizard.receiveItems.label', default: 'Receive order items')}</h2>
					<hr/>
					<table>
						<tr class="">
							<td style="padding: 0px;">
								<div>
									<g:hiddenField name="order.id" value="${orderCommand?.order?.id }"/>
									<g:hiddenField name="shipmentType.id" value="${orderCommand?.shipmentType?.id }"/>
									<g:hiddenField name="recipient.id" value="${orderCommand?.recipient?.id }"/>
									<g:hiddenField name="shippedOn" value="${formatDate(format:'MM/dd/yyyy', date: orderCommand?.shippedOn )}"/>
									<g:hiddenField name="deliveredOn" value="${formatDate(format:'MM/dd/yyyy', date: orderCommand?.deliveredOn )}"/>
								
									<g:if test="${orderItems }">
										<table id="orderItemsTable">
											<thead>
												<tr class="even">
													<th class="center" align="center" colspan="6">
														<h2>
															<img src="${createLinkTo(dir:'images/icons/silk',file:'cart.png')}" alt="ordered" style="vertical-align: middle"/>
															<warehouse:message code="order.itemsOrdered.label"/>
														</h2>
													</th>
													<th class="center" align="center" colspan="5" style="border-left: 1px solid lightgrey;">
														<h2>
															<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="received" style="vertical-align: middle"/>
															<warehouse:message code="order.itemsReceived.label"/>
														</h2>
													</th>
												</tr>
												<tr class="even">
													<th><warehouse:message code="default.type.label"/></th>
													<th><warehouse:message code="product.productCode.label"/></th>
													<th><warehouse:message code="product.name.label"/></th>
													<th><warehouse:message code="product.uom.label"/></th>
													<th class="center"><warehouse:message code="order.ordered.label"/></th>
													<th class="center"><warehouse:message code="order.remaining.label"/></th>
													<th class="center" style="border-left: 1px solid lightgrey;"><warehouse:message code="order.received.label"/></th>
													<th><warehouse:message code="product.label"/></th>
													<th><warehouse:message code="product.lotNumber.label"/></th>
													<th><warehouse:message code="default.expires.label"/></th>
													<th></th>
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
																<g:if test="${orderItem?.primary }">${orderItem?.orderItem?.product?.productCode }</g:if>
															</td>
															<td>
																<g:if test="${orderItem?.primary }">${orderItem?.orderItem?.product.name?:orderItem?.description }</g:if>
															</td>
															<td>
																<g:if test="${orderItem?.primary }">${orderItem?.orderItem?.product.unitOfMeasure?:"each" }</g:if>
															</td>
															<td class="center">
																<g:if test="${orderItem?.primary }">${orderItem?.quantityOrdered}</g:if>
															</td>
															<td class="center">
																<g:if test="${orderItem?.primary }">
																	${orderItem?.quantityOrdered - orderItem?.orderItem?.quantityFulfilled()}
																</g:if>
															</td>
															<g:if test="${!orderItem?.orderItem?.isCompletelyFulfilled() }">
																<td class="center" style="border-left: 1px solid lightgrey;">

																	<input type="text" name='orderItems[${i }].quantityReceived' value="${orderItem?.quantityReceived }" size="5"
																		   class="center updateable text" />
																</td>


																<td>
																	<g:autoSuggest id="productReceived-${i }" name="orderItems[${i }].productReceived"
																				   jsonUrl="${request.contextPath }/json/findProductByName" styleClass="text"
																				   width="200" valueId="${orderItem?.productReceived?.id }"
																				   valueName="${format.product(product:orderItem?.productReceived)}"/>
																</td>
																<td>
																	<g:textField name="orderItems[${i }].lotNumber" value="${orderItem?.lotNumber }" size="20" class="text updateable"/>
																</td>
																<td nowrap="true">
																	<g:datePicker name="orderItems[${i }].expirationDate" precision="day" default="none" class="chzn-select" noSelection="['':'']"
																		years="${(1900 + (new Date().year))..(1900+ (new Date() + (50 * 365)).year)}" value="${orderItem?.expirationDate }" />
																</td>
																<td>
																	<span class="buttons" style="padding: 0px;">
																		<button alt="Add another item" class="btnAdd button icon add"
																				style="vertical-align: middle">${warehouse.message(code:'default.splitItem.label', default:'Split item')}</button>
																	</span>
																</td>
															</g:if>
															<g:else>
																<td colspan="5" style="border-left: 1px solid lightgrey" class="center fade">
																	${warehouse.message(code:'order.orderItemHasBeenReceived.message', default:'This order item has already been received')}
																</td>
															</g:else>
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
						<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}" class="button"></g:submitButton>
						<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}" class="button"></g:submitButton>
					</div>
				</div>
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


	$(document).ready(function() {

		$(".btnDel").livequery(function(){
			$(this).click(function(event) {
				event.preventDefault();
				$(this).parent().parent().parent().remove();
				$("#orderItemsTable").alternateRowColors();
			});
		});

		$(".btnAdd").click(function(event) {
			event.preventDefault();
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
				<td>
				</td>
				<td>
				</td>
				<td>
				</td>
				<td>
				</td>
				<td class="center" style="border-left: 1px solid lightgrey;">
					<input type="text" name='orderItems[{{= Index }}].quantityReceived' value="" size="5" class="center updateable text" />
				</td>
				<td>
					<input id="productReceived{{= Index}}.id" class="value" type="hidden" name="orderItems[{{= Index }}].productReceived.id" value="{{= ProductId}}"/>
					<input id="productReceived{{= Index}}.text" class="autocomplete text" type="text" name="productReceived{{= Index}}.text" value="{{= ProductName}}" style="width: 200px;">
				</td>
				<td>
					<g:textField name="orderItems[{{= Index }}].lotNumber" value="{{= LotNumber}}" size="10" class="updateable text"/>
				</td>
				<td nowrap="true">
					<g:datePicker name="orderItems[{{= Index }}].expirationDate" precision="day" default="none" value="" noSelection="['':'']"
						years="${(1900 + (new Date().year))..(1900+ (new Date() + (50 * 365)).year)}"/>					
				</td>															
				<td>
					<span class="buttons" style="padding: 0px;">
						<button alt="Delete item" class="btnDel button icon trash">${warehouse.message(code:'default.deleteItem.label', default:'Delete item')}</button>
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
