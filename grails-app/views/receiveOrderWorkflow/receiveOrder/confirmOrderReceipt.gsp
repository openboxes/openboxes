
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.confirmOrderReceipt.label"/></title>
<style>
</style>
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
		<g:hasErrors bean="${shipment}">
			<div class="errors">
				<g:renderErrors bean="${shipment}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${receipt}">
			<div class="errors">
				<g:renderErrors bean="${receipt}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${order}">
			<div class="errors">
				<g:renderErrors bean="${order}" as="list" />
			</div>
		</g:hasErrors>
		
		<g:form action="receiveOrder" method="post">
			<div class="dialog">
			
			
				<fieldset>
					<g:render template="../order/summary" model="[orderInstance:order]"/>
					<g:render template="progressBar" model="['state':'confirmOrderReceipt']"/>		


					<table>
						<tbody>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='summary'><warehouse:message code="default.summary.label"/></label>
								</td>
								<td valign='top'class='value'>
									<warehouse:message code="order.youAreAboutToCreateANewShipment.message" 
										args="[format.metadata(obj:orderCommand?.shipmentType), orderCommand?.order?.origin?.name?.encodeAsHTML(),
										orderCommand?.order?.destination?.name?.encodeAsHTML(),format.date(obj:orderCommand?.deliveredOn)]"/>
								</td>
							</tr>
							<%-- 
							<tr class='prop'>
								<td valign='top' class='name'><label for='id'>Order Number:</label></td>
								<td valign='top' class='value'>
									<g:if test="${orderCommand?.order?.orderNumber }">
										${orderCommand?.order?.orderNumber }
									</g:if>
									<g:else>
										<span class="fade">New Order</span>
									</g:else>
								</td>
							</tr>
							
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'>Order from:</label></td>
								<td valign='top' class='value'>
									${orderCommand?.order?.origin?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination">Destination:</label></td>
								<td valign='top' class='value'>
									${orderCommand?.order?.destination?.name?.encodeAsHTML()}
								</td>
							</tr>
							
							
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='orderedBy'>Shipment type</label>
								</td>
								<td valign='top'class='value'>
									${orderCommand?.shipmentType?.name}
								</td>
							</tr>
							
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='shippedOn'>Shipped on</label>
								</td>
								<td valign='top'class='value'>									
									<format:date obj="${orderCommand?.shippedOn}"/>
								</td>
							</tr>								
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='deliveredOn'>Delivered on</label>
								</td>
								<td valign='top'class='value'>
									<format:date obj="${orderCommand?.deliveredOn}"/>
								</td>
							</tr>								
							--%>
							
							<tr class="prop">
	                            <td valign="top" class="name">
	                            	<label for='orderItems'><warehouse:message code="order.items.label" default="Items" /></label></td>
	                            <td valign="top" class="value" style="padding: 0px;">

									<g:if test="${orderItems }">
										<table id="orderItemsTable">
											<thead>
												<tr class="even">
													<th class="center" colspan="4">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'cart.png')}" alt="ordered" style="vertical-align: middle"/>
														<warehouse:message code="order.itemsOrdered.label"/>
													</th>
													<th class="center" colspan="4" style="border-left: 1px solid lightgrey;">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="received" style="vertical-align: middle"/>
														<warehouse:message code="order.itemsReceived.label"/>
													</th>
												</tr>
												<tr class="even">
													<td></td>
													<td><warehouse:message code="default.type.label"/></td>
													<td><warehouse:message code="default.description.label"/></td>
													<td class="center"><warehouse:message code="order.ordered.label"/></td>										
													<%-- <td class="center"><warehouse:message code="order.remaining.label"/></td>--%>	
													<td style="border-left: 1px solid lightgrey;"><warehouse:message code="order.received.label"/></td>										
													<td><warehouse:message code="order.productReceived.label"/></td>										
													<td><warehouse:message code="product.lotNumber.label"/></td>		
													<%-- 								
													<td><warehouse:message code="default.actions.label"/></td>										
													--%>
												</tr>
											</thead>									
											<tbody>
												<g:each var="orderItem" in="${orderItems }" status="i">
													<g:if test="${orderItem?.quantityReceived > 0}">
														<tr class="">
															<td>
																<a name="orderItems${i }"></a>
																
																<g:hiddenField class="orderItemId" name="orderItems[${i }].orderItem.id" value="${orderItem?.orderItem?.id }"/>
																<g:hiddenField name="orderItems[${i }].primary" value="${orderItem?.primary }"/>
																<g:hiddenField name="orderItems[${i }].type" value="${orderItem?.type }"/>
																<g:hiddenField name="orderItems[${i }].description" value="${orderItem?.description }"/>
																<g:hiddenField name="orderItems[${i }].quantityOrdered" value="${orderItem?.quantityOrdered }"/>
															</td>
															<td>
																${orderItem?.type }
															</td>
															<td>
																${orderItem?.description }
															</td>
															<td class="center">
																${orderItem?.quantityOrdered}
															</td>
															<%--
															<td class="center">
																 ${orderItem?.quantityOrdered - orderItem?.orderItem?.quantityFulfilled()}
															</td>
															--%>
															<td class="center" style="border-left: 1px solid lightgrey;">															
																${orderItem?.quantityReceived }
															</td>
															<td>
																${orderItem?.productReceived?.name }
															</td>
															<td>
																${orderItem?.lotNumber } 
																<g:if test="${orderItem?.expirationDate }">
																	<span class="fade">(expires
																		<g:formatDate date="${orderItem?.expirationDate }" format="MMM yyyy"/>)
																	</span>
																</g:if>
															</td>
														</tr>
													</g:if>
												</g:each>
											</tbody>
										</table>
									</g:if>
									<g:else>
										<span class="fade"><warehouse:message code="order.noItems.label"/></span>
									</g:else>	
	                            </td>
	                        </tr>
						</tbody>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<span class="formButton"> 
							<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}"></g:submitButton> 
							<g:submitButton name="submit" value="${warehouse.message(code:'default.button.finish.label')}"></g:submitButton>
							<g:link action="receiveOrder" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
						</span>
					</div>
				</fieldset>
			</div>				
				
				
		</g:form>
	</div>
	
		<script>
			$(document).ready(function() {
				jQuery.fn.alternateRowColors = function() {
					$('tbody tr:odd', this).removeClass('odd').addClass('even');
					$('tbody tr:even', this).removeClass('even').addClass('odd');
					return this;
				};				

				$("#orderItemsTable").alternateRowColors();
		    	
	    	});
	    </script>	
</body>
</html>