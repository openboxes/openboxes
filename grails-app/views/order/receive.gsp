
<%@ page import="org.pih.warehouse.order.Order" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'order.label', default: 'Order')}" />
        <title><g:message code="default.receive.label" default="Receive {0}" args="[entityName]" /></title>
       
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
	            		<g:render template="header" model="[orderInstance:orderCommand?.order]"/>
		                <table>
		                    <tbody>
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='id'>Status</label>
									</td>
									<td valign='top'class='value'>
										<g:hiddenField name="order.id" value="${orderCommand?.order?.id }"/>
										${ (orderCommand?.order?.isComplete()) ? "Complete" : "Pending" }
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
														<td>Qty Ordered</td>										
														<td>Qty Fulfilled</td>										
														<td style="border-left: 1px solid lightgrey;">Qty Recv'd</td>										
														<td>Product Recv'd</td>										
														<td>Lot Number</td>										
														<td>Actions</td>										
													</tr>
												</thead>									
												<tbody>
													<g:each var="orderItem" in="${orderCommand?.orderItems.sort { it?.orderItem?.product?.name} }" status="i">
														<tr class="${(orderItem?.primary)?"black-top":""}">
															<td>
																<a name="orderItems${i }"></a>
																${orderItem?.orderItem?.id }
																<g:hiddenField name="orderItems[${i }].orderItem.id" value="${orderItem?.orderItem?.id }"/>
																<g:hiddenField name="orderItems[${i }].primary" value="${orderItem?.primary }"/>
															</td>
															<td class="center">
																<g:if test="${orderItem?.primary }">${orderItem?.type }</g:if>
																<g:hiddenField name="orderItems[${i }].type" value="${orderItem?.type }"/>
															</td>
															<td>
																<g:if test="${orderItem?.primary }">${orderItem?.description }</g:if>
																<g:hiddenField name="orderItems[${i }].description" value="${orderItem?.description }"/>
															</td>
															<td class="center">
																<g:if test="${orderItem?.primary }">${orderItem?.quantityOrdered}</g:if>
																<g:hiddenField name="orderItems[${i }].quantityOrdered" value="${orderItem?.quantityOrdered }"/>
															</td>
															<td class="center">
																<g:if test="${orderItem?.primary }">${orderItem?.orderItem?.quantityFulfilled()}</g:if>
															</td>
															<td style="border-left: 1px solid lightgrey;">
																<input type="text" name='orderItems[${i }].quantityReceived' value="${orderItem?.quantityReceived }" size="5" class="center updateable" />
															</td>
															<td>
																<div class="ui-widget">
																	<g:select class="combobox updateable" name="orderItems[${i }].productReceived.id" from="${org.pih.warehouse.product.Product.list().sort{it.name}}" 
																		optionKey="id" value="${orderItem?.productReceived?.id }" noSelection="['':'']"/>
																</div>	
															</td>
															<td>
																<g:textField name="orderItems[${i }].lotNumber" value="${orderItem?.lotNumber }" size="10" class="updateable"/>
															</td>
															<td>
																<g:link controller="order" action="addOrderShipment" id="${orderCommand?.order?.id }" params="[index: i]" class="checkable" fragment="orderItems${i }">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="add" style="vertical-align: middle"/>
																</g:link>
																&nbsp;
																<g:if test="${!orderItem?.primary }">
																	<g:link controller="order" action="removeOrderShipment" id="${orderItem?.orderItem?.id }" params="[index: i]" class="checkable" fragment="orderItems${i }">
																		<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="tag" style="vertical-align: middle"/>
																	</g:link>
																</g:if> 
															</td>
															
														</tr>
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
											<g:actionSubmit action="saveOrderShipment" value="Save & Continue"/> 
											<g:actionSubmit action="saveOrderShipmentAndExit" value="Save & Exit"/>			                        	
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
	    
      
	    
    </body>
</html>
