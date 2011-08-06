
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Confirm request receipt</title>
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
		<g:hasErrors bean="${requestCommand}">
			<div class="errors">
				<g:renderErrors bean="${requestCommand}" as="list" />
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
		<g:hasErrors bean="${request}">
			<div class="errors">
				<g:renderErrors bean="${request}" as="list" />
			</div>
		</g:hasErrors>
		
		<g:form action="fulfillRequest" method="post">
			<div class="dialog">
			
				<g:render template="progressBar" model="['state':'confirmFulfillment']"/>		
				<br clear='all'/>
				
				<fieldset>
					<g:render template="../request/summary" model="[requestInstance:request]"/>

					<table>
						<tbody>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='summary'>Summary</label>
								</td>
								<td valign='top'class='value'>
									You are about to create a new shipment of type  
									<b>${requestCommand?.shipmentType?.name}</b>
									
									being sent from 
									<b>${requestCommand?.request?.origin?.name?.encodeAsHTML()}</b>
									on 
									<b><format:date obj="${requestCommand?.shippedOn}"/></b>
									to be received by 
									<b>${requestCommand?.request?.destination?.name?.encodeAsHTML()}</b>
									on 
									<b><format:date obj="${requestCommand?.deliveredOn}"/></b>
								</td>
							</tr>
							<%-- 
							<tr class='prop'>
								<td valign='top' class='name'><label for='id'>Request Number:</label></td>
								<td valign='top' class='value'>
									<g:if test="${requestCommand?.request?.requestNumber }">
										${requestCommand?.request?.requestNumber }
									</g:if>
									<g:else>
										<span class="fade">New Request</span>
									</g:else>
								</td>
							</tr>
							
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'>Request from:</label></td>
								<td valign='top' class='value'>
									${requestCommand?.request?.origin?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination">Destination:</label></td>
								<td valign='top' class='value'>
									${requestCommand?.request?.destination?.name?.encodeAsHTML()}
								</td>
							</tr>
							
							
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='requestedBy'>Shipment type</label>
								</td>
								<td valign='top'class='value'>
									${requestCommand?.shipmentType?.name}
								</td>
							</tr>
							
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='shippedOn'>Shipped on</label>
								</td>
								<td valign='top'class='value'>									
									<format:date obj="${requestCommand?.shippedOn}"/>
								</td>
							</tr>								
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='deliveredOn'>Delivered on</label>
								</td>
								<td valign='top'class='value'>
									<format:date obj="${requestCommand?.deliveredOn}"/>
								</td>
							</tr>								
							--%>
							
							<tr class="prop">
	                            <td valign="top" class="name">
	                            	<label for='requestItems'><warehouse:message code="request.items.label" default="Items" /></label></td>
	                            <td valign="top" class="value">

									<g:if test="${requestItems }">
										<table id="requestItemsTable">
											<thead>
												<tr class="even">
													<th class="center" colspan="4">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'cart.png')}" alt="requested" style="vertical-align: middle"/>
														Items Requested
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
													<td class="center">Requested</td>										
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
												<g:each var="requestItem" in="${requestItems }" status="i">
													<g:if test="${requestItem?.quantityReceived > 0}">
														<tr class="">
															<td>
																<a name="requestItems${i }"></a>
																
																<g:hiddenField class="requestItemId" name="requestItems[${i }].requestItem.id" value="${requestItem?.requestItem?.id }"/>
																<g:hiddenField name="requestItems[${i }].primary" value="${requestItem?.primary }"/>
																<g:hiddenField name="requestItems[${i }].type" value="${requestItem?.type }"/>
																<g:hiddenField name="requestItems[${i }].description" value="${requestItem?.description }"/>
																<g:hiddenField name="requestItems[${i }].quantityRequested" value="${requestItem?.quantityRequested }"/>
															</td>
															<td>
																${requestItem?.type }
															</td>
															<td>
																${requestItem?.description }
															</td>
															<td class="center">
																${requestItem?.quantityRequested}
															</td>
															<%--
															<td class="center">
																 ${requestItem?.quantityRequested - requestItem?.requestItem?.quantityFulfilled()}
															</td>
															--%>
															<td class="center" style="border-left: 1px solid lightgrey;">															
																${requestItem?.quantityReceived }
															</td>
															<td>
																${requestItem?.productReceived?.name }
															</td>
															<td>
																${requestItem?.lotNumber } 
																<g:if test="${requestItem?.expirationDate }">
																	<span class="fade">(expires
																		<g:formatDate date="${requestItem?.expirationDate }" format="MMM yyyy"/>)
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
										<span class="fade">No items</span>
									</g:else>	
	                            </td>
	                        </tr>
						</tbody>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<span class="formButton"> 
							<g:submitButton name="back" value="Back"></g:submitButton> 
							<g:submitButton name="submit" value="Finish"></g:submitButton>
							<g:link action="fulfillRequest" event="cancel">Cancel</g:link>
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

				$("#requestItemsTable").alternateRowColors();
		    	
	    	});
	    </script>	
</body>
</html>