
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Confirm order receipt</title>
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
		
		<g:form action="receiveOrder" method="post">
			<div class="dialog">
			
			
				<fieldset>
					<g:render template="../order/header" model="[orderInstance:order]"/>
					<g:render template="progressBar" model="['state':'confirmOrderReceipt']"/>		


					<table>
						<tbody>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='summary'>Summary</label>
								</td>
								<td valign='top'class='value'>
									You are about to create a new shipment of type  
									<b>${orderCommand?.shipmentType?.name}</b>
									
									being sent from 
									<b>${orderCommand?.order?.origin?.name?.encodeAsHTML()}</b>
									on 
									<b><format:date obj="${orderCommand?.shippedOn}"/></b>
									to be received by 
									<b>${orderCommand?.order?.destination?.name?.encodeAsHTML()}</b>
									on 
									<b><format:date obj="${orderCommand?.deliveredOn}"/></b>
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
	                            	<label for='orderItems'><g:message code="order.items.label" default="Items" /></label></td>
	                            <td valign="top" class="value">

									<g:if test="${orderItems }">
										<table id="orderItemsTable">
											<thead>
												<tr class="even">
													<th class="center" colspan="4">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'cart.png')}" alt="ordered" style="vertical-align: middle"/>
														Items Ordered
													</th>
													<th class="center" colspan="4" style="border-left: 1px solid lightgrey;">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="received" style="vertical-align: middle"/>
														Items Received
													</th>
												</tr>
												<tr class="even">
													<td></td>
													<td>Type</td>
													<td>Description</td>
													<td class="center">Ordered</td>										
													<%-- <td class="center">Remaining</td>--%>	
													<td style="border-left: 1px solid lightgrey;">Received</td>										
													<td>Product Received</td>										
													<td>Lot Number</td>		
													<%-- 								
													<td>Actions</td>										
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
																<span class="fade">(expires
																<g:formatDate date="${orderItem?.expirationDate }" format="MMM yyyy"/>)</span>
															</td>
														</tr>
													</g:if>
												</g:each>
											</tbody>
										</table>
									</g:if>
									<g:else>
										<span class="fade">No items</span>
									</g:else>	
	                            </td>
	                        </tr>
						</tbody>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<span class="formButton"> 
							<g:submitButton name="back" value="Back"></g:submitButton> 
							<g:submitButton name="finish" value="Finish"></g:submitButton>
							<g:link action="receiveOrder" event="cancel">Cancel</g:link>
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