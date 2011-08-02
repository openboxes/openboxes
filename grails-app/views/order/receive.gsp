
<%@ page import="org.pih.warehouse.order.Order" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'order.label', default: 'Order')}" />
        <title><warehouse:message code="default.receive.label" default="Receive {0}" args="[entityName]" /></title>
       
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
			<g:hasErrors bean="${orderCommand}">
	            <div class="errors">
	                <g:renderErrors bean="${orderCommand}" as="list" />
	            </div>
            </g:hasErrors>            
			<g:hasErrors bean="${orderCommand.shipment}">
	            <div class="errors">
	                <g:renderErrors bean="${orderCommand.shipment}" as="list" />
	            </div>
            </g:hasErrors>            
            
            <div class="dialog">
            
            	<g:form autocomplete="off">
            		
	            	<fieldset>
	            		<g:render template="summary" model="[orderInstance:orderCommand?.order]"/>
		                <table>
		                    <tbody>
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='id'>Status</label>
									</td>
									<td valign='top'class='value'>
										<g:hiddenField name="order.id" value="${orderCommand?.order?.id }"/>
										${ (orderCommand?.order?.isCompletelyReceived()) ? "Complete" : "Pending" }
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='orderedBy'>Shipment type</label>
									</td>
									<td valign='top'class='value'>
										<g:select class="combobox updateable" name="shipmentType.id" from="${org.pih.warehouse.shipping.ShipmentType.list()}" 
											optionKey="id" optionValue="name" value="${orderCommand?.shipmentType?.id }" noSelection="['':'']" />
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='orderedBy'>Receipient</label>
									</td>
									<td valign='top'class='value'>
										<div class="ui-widget">
											<g:select class="combobox updateable" name="recipient.id" from="${org.pih.warehouse.core.Person.list()}" 
												optionKey="id" optionValue="name" value="${orderCommand?.recipient?.id }" noSelection="['':'']" />
										</div>									
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='shippedOn'>Shipped on</label>
									</td>
									<td valign='top'class='value'>									
										<g:jqueryDatePicker 
											id="shippedOn" 
											name="shippedOn" 
											class="updateable"
											value="${orderCommand?.shippedOn }" 
											format="MM/dd/yyyy"
											showTrigger="false" />
									</td>
								</tr>								
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='deliveredOn'>Delivered on</label>
									</td>
									<td valign='top'class='value'>
										<g:jqueryDatePicker 
											id="deliveredOn" 
											name="deliveredOn" 
											class="updateable"
											value="${orderCommand?.deliveredOn }" 
											format="MM/dd/yyyy"
											showTrigger="false" />
									</td>
								</tr>								
		                        <tr class="prop">
		                            <td valign="top" colspan="2">
										<g:if test="${orderCommand?.orderItems }">
											<table id="orderItemsTable">
												<thead>
													<tr class="even">
														<th class="center" align="center" colspan="5">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'cart.png')}" alt="ordered" style="vertical-align: middle"/>
															Items Ordered
														</th>
														<th class="center" align="center" colspan="4" style="border-left: 1px solid lightgrey;">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="received" style="vertical-align: middle"/>
															Items Received
														</th>
													</tr>
													<tr class="even">
														<td></td>
														<td>Type</td>
														<td>Description</td>
														<td class="center">Ordered</td>										
														<td class="center">Remaining</td>	
														<td style="border-left: 1px solid lightgrey;">Received</td>										
														<td>Product</td>										
														<td>Lot Number</td>		
														<%-- 								
														<td>Actions</td>										
														--%>
													</tr>
												</thead>									
												<tbody>
													<g:each var="orderItem" in="${orderCommand?.orderItems }" status="i">
												
														<tr class="${(orderItem?.primary)?"black-top":""}">
															<td>
																<a name="orderItems${i }"></a>
																${i }
																<g:hiddenField class="orderItemId" name="orderItems[${i }].orderItem.id" value="${orderItem?.orderItem?.id }"/>
																<g:hiddenField name="orderItems[${i }].primary" value="${orderItem?.primary }"/>
																<g:hiddenField name="orderItems[${i }].type" value="${orderItem?.type }"/>
																<g:hiddenField name="orderItems[${i }].description" value="${orderItem?.description }"/>
																<g:hiddenField name="orderItems[${i }].quantityOrdered" value="${orderItem?.quantityOrdered }"/>
															</td>
															<td>
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
																
																<g:if test="${!orderItem?.orderItem?.isCompletelyFulfilled() }">
																	<input type="text" name='orderItems[${i }].quantityReceived' value="${orderItem?.quantityReceived }" size="5" class="center updateable" />
																</g:if>
															</td>
															<td>
																<g:if test="${!orderItem?.orderItem?.isCompletelyFulfilled() }">
																	<div class="ui-widget">
																		<g:select class="combobox updateable productId" name="orderItems[${i }].productReceived.id" from="${org.pih.warehouse.product.Product.list().sort{it.name}}" 
																			optionKey="id" value="${orderItem?.productReceived?.id }" noSelection="['':'']"/>
																	</div>	
																</g:if>
															</td>
															<td>
																<g:if test="${!orderItem?.orderItem?.isCompletelyFulfilled() }">
																	<g:textField name="orderItems[${i }].lotNumber" value="${orderItem?.lotNumber }" size="10" class="updateable"/>
																</g:if>
															</td>
															<td>
																<div class="buttons">
																	<input type="image" src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="add" class="btnAdd" style="vertical-align: middle"/>
																</div>
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
														<g:each in="${orderItem?.orderItem?.shipmentItems()}" var="shipmentItem">
															<tr >
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

															
													</g:each>
													
													
												</tbody>
											</table>
										</g:if>
										<g:else>
											<span class="fade">No items</span>
										</g:else>
		                            </td>
		                        </tr>
		                        <tr class="prop">
			                        <td colspan="2">
			                        	<div class="buttons">
											<g:actionSubmit action="saveOrderShipment" value="Save"/> 
			                        	</div>
			                        </td>
		                        </tr>
		                        
		                    </tbody>
		                </table>
	               </fieldset>
				</g:form>
            </div>
        </div>
        <g:comboBox />
		<script>
			var changed = false;
			var currentIndex = $("#orderItemsTable tbody tr").length;
		
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
				var index = ++currentIndex;
				var currentRow = $(this).parent().parent().parent();
				var productId = currentRow.find(".productId");
				var orderItemId = currentRow.find(".orderItemId");
    			var item = { Id: '0', Index: index, ProductId: productId.val(), OrderItemId: orderItemId.val(), Template: '#new-item-template' };
				//$(item.Template).tmpl(item).appendTo('#orderItemsTable > tbody');	
				var newRow = $(item.Template).tmpl(item);
				currentRow.after(newRow);	
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
		<script id="new-item-template" type="x-jquery-tmpl">						
			<tr class="">
				<td>
					<a name="orderItems{{= Index }}"></a>
					{{= Index }}
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
				<td class="center">
				</td>
				<td class="center">
				</td>
				<td class="center" style="border-left: 1px solid lightgrey;">
					<input type="text" name='orderItems[{{= Index }}].quantityReceived' value="" size="5" class="center updateable" />
				</td>
				<td>
					<div class="ui-widget">
						<g:select class="combobox updateable" name="orderItems[{{= Index }}].productReceived.id" value="{{= ProductId }}" from="${org.pih.warehouse.product.Product.list().sort{it.name}}" 
							optionKey="id" value="" noSelection="['':'']"/>
					</div>	
				</td>
				<td>
					<g:textField name="orderItems[{{= Index }}].lotNumber" value="${orderItem?.lotNumber }" size="10" class="updateable"/>
				</td>
				<td>
					<div class="buttons">
						<input type="image" src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}" alt="delete" class="btnDel" style="vertical-align: middle"/>
					</div>
				</td>
			</tr>
		</script>    	    

    </body>
</html>
