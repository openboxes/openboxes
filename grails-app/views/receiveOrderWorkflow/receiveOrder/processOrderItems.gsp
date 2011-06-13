<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Add order items</title>

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
		<div class="dialog">
			<fieldset>
				<g:render template="../order/header" model="[orderInstance:order]"/>
				<g:render template="progressBar" model="['state':'processOrderItems']"/>		
				<g:form action="receiveOrder" autocomplete="false">
					<table>
						<tr>
							<td >
								<div style="margin: 10px">
									<p>There are ${(orderCommand?.orderItems) ? orderCommand?.orderItems?.size() : 0 } items in this order.</p>
								</div>							
						
							
								<div style="min-height: 175px">
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
														Items Ordered
													</th>
													<th class="center" align="center" colspan="4" style="border-left: 1px solid lightgrey;">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="received" style="vertical-align: middle"/>
														Items Received
													</th>
												</tr>
												<tr class="even">
													<td>Type</td>
													<td>Description</td>
													<td class="center">Ordered</td>										
													<td class="center">Remaining</td>	
													<td style="border-left: 1px solid lightgrey;">Received</td>										
													<td width="250px">Product</td>										
													<td width="100px">Lot Number</td>		
													
												</tr>
											</thead>									
											<tbody>
											
												<g:set var="i" value="${0 }"/>
												<g:each var="entrymap" in="${orderItems?.groupBy { it?.orderItem } }">
													<g:each var="orderItem" in="${entrymap.value}">
												
														<tr class="${(orderItem?.primary)?"black-top":""} orderItem">
															<td>
																<g:hiddenField class="orderItemId" name="orderItems[${i }].orderItem.id" value="${orderItem?.orderItem?.id }"/>
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
																<g:if test="${orderItem?.primary }">${orderItem?.quantityOrdered - orderItem?.orderItem?.quantityFulfilled()}</g:if>
															</td>
															<td class="center" style="border-left: 1px solid lightgrey;">
																
																<g:if test="${!orderItem?.orderItem?.isComplete() }">
																	<input type="text" name='orderItems[${i }].quantityReceived' value="${orderItem?.quantityReceived }" size="5" class="center updateable" />
																</g:if>
															</td>
															<td>
																<g:if test="${!orderItem?.orderItem?.isComplete() }">
																	<div class="ui-widget">
																		<g:select class="combobox updateable productId" name="orderItems[${i }].productReceived.id" from="${org.pih.warehouse.product.Product.list().sort{it.name}}" 
																			optionKey="id" value="${orderItem?.productReceived?.id }" noSelection="['':'']"/>
																	</div>	
																</g:if>
															</td>
															<td>
																<g:if test="${!orderItem?.orderItem?.isComplete() }">
																	<g:textField name="orderItems[${i }].lotNumber" value="${orderItem?.lotNumber }" size="10" class="updateable"/>
																</g:if>
															</td>
															<td nowrap="true">
																<g:if test="${!orderItem?.orderItem?.isComplete() }">
																	<g:datePicker name="orderItems[${i }].expirationDate" precision="month" default="none" noSelection="['':'']"
																		years="${(1900 + (new Date().year))..(1900+ (new Date() + (50 * 365)).year)}" value="${orderItem?.expirationDate }" />					
																</g:if>
															</td>															
															<td>
																<g:if test="${!orderItem?.orderItem?.isComplete() }">
																	<span class="buttons" style="padding: 0px;">
																		<input type="image" src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="add" class="btnAdd" style="vertical-align: middle"/>
																	</span>
																</g:if>
																<%-- 
																<g:link controller="order" action="addOrderShipment" id="${orderCommand?.order?.id }" params="[index: i]" class="checkable" fragment="orderItems${i }">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="add" style="vertical-align: middle"/>
																</g:link>
																&nbsp;
																<g:if test="${!orderItem?.primary }">
																	<g:link controller="order" action="removeOrderShipment" id="${orderItem?.orderItem?.id }" params="[index: i]" class="checkable" fragment="orderItems${i }">
																		<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="tag" style="vertical-align: middle"/>
																	</g:link>
																</g:if> 
																--%>
															</td>
														</tr>
														<%-- 
														<g:each in="${orderItem?.orderItem?.shipmentItems()}" var="shipmentItem">
															<tr class="shipmentItem">
																<td colspan="5">
																</td>
																<td class="center" style="border-left: 1px solid lightgrey;">
																
																	${shipmentItem?.quantity }
																</td>
																<td>
																	${shipmentItem?.product?.name }
																</td>
																<td class="center">
																	${shipmentItem?.lotNumber }
																</td>
																<td>																
																</td>		
															</tr>												
														</g:each>
														--%>
														<g:set var="i" value="${i+1 }"/>
													</g:each>
												</g:each>
												
											</tbody>
										</table>
									</g:if>
									<g:else>
										<span class="fade">No items</span>
									</g:else>									
								</div>

							</td>						
						</tr>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<g:submitButton name="back" value="Back"></g:submitButton>
						<g:submitButton name="next" value="Next"></g:submitButton>
						<%-- 
						<g:submitButton name="finish" value="Save & Exit"></g:submitButton>								
						--%>
						<g:link action="receiveOrder" event="cancel">Cancel</g:link>
					</div>
				</g:form>
				

			</fieldset>
		</div>

	</div>
	<g:comboBox />
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
		var productId = currentRow.find(".productId");
		var orderItemId = currentRow.find(".orderItemId");
 		var item = { Id: '0', Index: index, ProductId: productId.val(), LotNumber: "", ExpirationDate: "", 
 		  				OrderItemId: orderItemId.val(), Template: '#new-item-template' };
		currentRow.after($(item.Template).tmpl(item));	
		$("#orderItemsTable").alternateRowColors();

		//var productSelect = $("#productReceived-" + index);
		//console.log(productSelect);
		//selectCombo(productSelect, productId );
		
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
	<script id="new-item-template" type="x-jquery-tmpl">						
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
					<div class="ui-widget">
						<g:select class="combobox updateable" id="productReceived-{{= Index}}" name="orderItems[{{= Index }}].productReceived.id" value="{{= ProductId }}" from="${org.pih.warehouse.product.Product.list().sort{it.name}}" 
							optionKey="id" noSelection="['':'']"/>
					</div>	
				</td>
				<td>
					<g:textField name="orderItems[{{= Index }}].lotNumber" value="{{= LotNumber}}" size="10" class="updateable"/>
				</td>
				<td nowrap="true">
					<g:datePicker name="orderItems[{{= Index }}].expirationDate" precision="month" default="none" value="" noSelection="['':'']"
						years="${(1900 + (new Date().year))..(1900+ (new Date() + (50 * 365)).year)}"/>					
				</td>															
				<td>
					<span class="buttons" style="padding: 0px;">
						<input type="image" src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}" alt="delete" class="btnDel" style="vertical-align: middle"/>
					</span>
				</td>
			</tr>
		</script>    	    
	
</body>
</html>