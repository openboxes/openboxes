
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">
		Receive Shipment
	</content>
</head>

<body>
	<div class="body">
	
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${receiptInstance}">
			<div class="errors">
				<g:renderErrors bean="${receiptInstance}" as="list" />
			</div>
		</g:hasErrors>	

		<table>		
			<tr>
				<td width="75%">
					<fieldset>
						<g:render template="summary" />
						
							<g:form action="receiveShipment" method="POST">
								<g:hiddenField name="id" value="${shipmentInstance?.id}" />
								<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />								
								<table>
									<tbody>
										<tr class="prop">
											<td class="name"  style="width: 10%;">
												<label><g:message code="receipt.recipient.label" default="Recipient" /></label>
											</td>
											<td class="value" style="width: 30%;">
												<g:autoSuggest id="recipient" name="recipient" jsonUrl="/warehouse/json/findPersonByName"
													width="150"
													valueId="${receiptInstance?.recipient?.id}"
													valueName="${receiptInstance?.recipient?.email}"/>	
											</td>
										</tr>																						
										<tr class="prop">
											<td valign="top" class="name"><label><g:message
												code="receipt.actualDeliveryDate.label" default="Delivered On" /></label></td>
											<td valign="top"
												class=" ${hasErrors(bean: receiptInstance, field: 'actualDeliveryDate', 'errors')}"
												nowrap="nowrap">
													<g:jqueryDatePicker name="actualDeliveryDate"
														value="${receiptInstance?.actualDeliveryDate}" format="MM/dd/yyyy" />														
											</td>
										</tr>		
										
										<g:if test="${shipmentInstance?.destination.isWarehouse()}">					
											<tr class="prop">
												<td valign="top" class="name"><label><g:message
													code="receipt.receiptItems.label" default="Receipt Items" /></label></td>
												<td valign="top"
													class=" ${hasErrors(bean: receiptInstance, field: 'receiptItem', 'errors')}"
													nowrap="nowrap">
													
														<g:if test="${!receiptInstance.receiptItems}">
															There are no shipment items to receive.
														</g:if>			
														<g:else>										
															<table>
																<thead>
																	<tr>
																		<th colspan="2"></th>
																		<th colspan="2" style="text-align: center;">Quantity</th>
																		<th colspan="2"></th>
																	</tr>
																	<tr>
																		<th style="text-align: left;">Item</th>
																		<th style="text-align: center;">Lot / Serial No</th>
																		<th style="text-align: center;">Shipped</th>
																		<th style="text-align: center;">Received</th>
																		<th style="text-align: center;">Accepted?</th>
																		<th style="text-align: center;">Comment</th>
																	</tr>
																</thead>
																<tbody>
																	<g:each var="receiptItem" in="${receiptInstance.receiptItems}" status="i">															
																		<tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
																			<td style="text-align: left; vertical-align: middle;">
																				<g:hiddenField name="receiptItems[${i}].product.id" value="${receiptItem?.product?.id}"/>
																				${receiptItem?.product?.name}
																			</td>
																			<td style="text-align: center; vertical-align: middle;">
																				<g:hiddenField name="receiptItems[${i}].serialNumer" value="${receiptItem?.serialNumber}"/>
																				${receiptItem?.serialNumber}
																			</td>
																			<td style="text-align: center; vertical-align: middle;">
																				<g:hiddenField name="receiptItems[${i}].quantityDelivered" value="${receiptItem?.quantityDelivered}"/>																	
																				${receiptItem?.quantityDelivered}
																			</td>
																			<td style="text-align: center; vertical-align: middle;">
																				<g:textField name="receiptItems[${i}].quantityReceived" value="${receiptItem?.quantityReceived}" size="3"/>
																			</td>
																			<td style="text-align: center; vertical-align: middle;">
																				<g:select name="receiptItems[${i}].accepted" from="['true','false']" value="${receiptItem.accepted?receiptItem.accepted:'true'}" 
																				 noSelection="['null': '']" />																																			
																			</td>
																			<td style="text-align: center; vertical-align: middle;">
																				<g:textField name="receiptItems[${i}].comment" value="${receiptItem?.comment}" size="10"/>
																			</td>
																		</tr>												
																	</g:each>														
																</tbody>													
															</table>
														</g:else>
												</td>
											</tr>		
										</g:if>									
										
										
														
										<tr class="prop">
				                            <td valign="top" class="name"><label><g:message code="comment.comment.label" default="Comment" /></label></td>                            
				                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'comment', 'errors')}">
			                                    <g:textArea name="comment" cols="60" rows="3"/>
			                                </td>
				                        </tr>  	        
											
											
										<tr class="prop">
											<td valign="top" class="name"></td>
											<td valign="top" class="value">
												<div class="buttons">
													<button type="submit" class="positive"><img
														src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
														alt="save" /> Receive Shipment</button>
													<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}" class="negative">
														<img
															src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
															alt="Cancel" /> Cancel </g:link>
												</div>				
											</td>
										</tr>
								</tbody>
							</table>
						</g:form>
					</fieldset>
				</td>
				<td width="20%">
					<g:render template="sidebar" />						
				</td>				
			</tr>
		</table>	
			
	</div>
</body>
</html>
