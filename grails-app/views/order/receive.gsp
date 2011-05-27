
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
            <div class="dialog">
            
            	<g:form action="saveOrderShipment">
            		<g:hiddenField name="id" value="${orderCommand?.order?.id }"/>
            	
	            	<fieldset>
	            		<g:render template="header" model="[orderInstance:orderCommand?.order]"/>
		                <table>
		                    <tbody>
		                    	
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='orderedBy'>Receipient</label>
									</td>
									<td valign='top'class='value'>
										<div class="ui-widget">
											<g:select class="combobox" name="recipient.id" from="${org.pih.warehouse.core.Person.list()}" 
												optionKey="id" optionValue="name" value="${orderCommand?.recipient?.id }" noSelection="['':'']" />
										</div>									
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
											value="${orderCommand?.deliveredOn }" 
											format="MM/dd/yyyy"
											size="8"
											showTrigger="false" />
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='orderedBy'>Shipment type</label>
									</td>
									<td valign='top'class='value'>
										<g:select class="combobox" name="shipment.shipmentType.id" from="${org.pih.warehouse.shipping.ShipmentType.list()}" 
											optionKey="id" optionValue="name" value="${orderCommand?.shipment?.shipmentType?.id }" noSelection="['':'']" />
									</td>
								</tr>
		                        <tr class="prop">
		                            <td valign="top" colspan="2">
										<g:if test="${orderCommand?.orderItems }">
											<table>
												<thead>
													<tr class="odd">
														<th>Type</th>
														<th>Description</th>
														<th>Qty Ordered</th>										
														<th>Product Recv'd</th>										
														<th>Lot Number</th>										
														<th>Qty Recv'd</th>										
													</tr>
												</thead>									
												<tbody>
													<g:each var="orderItem" in="${orderCommand?.orderItems}" status="i">
														<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
															<td>
																${orderItem?.type }
																<g:hiddenField name="orderItems[${i }].type" value="${orderItem?.type }"/>
															</td>
															<td>
																${orderItem?.description }
																<g:hiddenField name="orderItems[${i }].description" value="${orderItem?.description }"/>
															</td>
															<td>
																${orderItem?.quantityOrdered}
																<g:hiddenField name="orderItems[${i }].quantityOrdered" value="${orderItem?.quantityOrdered }"/>
															</td>
															<td>
																<div class="ui-widget">
																	<g:select class="combobox" name="orderItems[${i }].productReceived.id" from="${org.pih.warehouse.product.Product.list().sort{it.name}}" 
																		optionKey="id" value="${orderItem?.productReceived?.id }" noSelection="['':'']" />
																</div>	
															</td>
															<td>
																<g:textField name="orderItems[${i }].lotNumber" value="${orderItem?.lotNumber }"/>
															</td>
															<td>
																<input type="text" name='orderItems[${i }].quantityReceived' value="${orderItem?.quantityReceived }" size="5" />
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
		                        <tr>
			                        <td colspan="2">
			                        	<div class="buttons">
			                        		<g:submitButton name="save" value="Save"/>
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
    </body>
</html>
