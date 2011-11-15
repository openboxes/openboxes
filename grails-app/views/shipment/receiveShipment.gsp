
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="shipping.receiveShipment.label"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="shipping.receiveShipment.label"/></content>
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
												<label><warehouse:message code="shipping.recipient.label" /></label>
											</td>
											<td class="value" style="width: 30%;">
												<g:autoSuggest id="recipient" name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName"
													width="150"
													valueId="${receiptInstance?.recipient?.id}"
													valueName="${receiptInstance?.recipient?.email}"/>	
											</td>
										</tr>																						
										<tr class="prop">
											<td valign="top" class="name"><label><warehouse:message
												code="shipping.deliveredOn.label"/></label></td>
											<td valign="top"
												class=" ${hasErrors(bean: receiptInstance, field: 'actualDeliveryDate', 'errors')}"
												nowrap="nowrap">
													<g:jqueryDatePicker name="actualDeliveryDate"
														value="${receiptInstance?.actualDeliveryDate}" format="MM/dd/yyyy" />														
											</td>
										</tr>		
										
										<g:if test="${shipmentInstance?.destination.isWarehouse()}">					
											<tr class="prop">
												<td valign="top" class="name"><label><warehouse:message
													code="shipping.receiptItems.label" /></label></td>
												<td valign="top"
													class=" ${hasErrors(bean: receiptInstance, field: 'receiptItem', 'errors')}"
													nowrap="nowrap">
													
														<g:if test="${!receiptInstance.receiptItems}">
															<warehouse:message code="shipping.noItemsToReceive.label" />
														</g:if>			
														<g:else>										
															<table>
																<thead>
																	<tr>
																		<th colspan="2"></th>
																		<th colspan="2" style="text-align: center;"><warehouse:message code="default.quantity.label" /></th>
																		<th colspan="2"></th>
																	</tr>
																	<tr>
																		<th style="text-align: left;"><warehouse:message code="default.item.label" /></th>
																		<th style="text-align: center;"><warehouse:message code="default.lotSerialNo.label" /></th>
																		<th style="text-align: center;"><warehouse:message code="shipping.shipped.label" /></th>
																		<th style="text-align: center;"><warehouse:message code="shipping.received.label" /></th>
																		<th style="text-align: center;"><warehouse:message code="default.comment.label" /></th>
																	</tr>
																</thead>
																<tbody>
																	<g:each var="receiptItem" in="${receiptInstance.receiptItems}" status="i">															
																		<tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
																			<td style="text-align: left; vertical-align: middle;">
																				<g:hiddenField name="receiptItems[${i}].product.id" value="${receiptItem?.product?.id}"/>
																			<format:product product="${receiptItem?.product}"/>
																			</td>
																			<td style="text-align: center; vertical-align: middle;">
																				<g:hiddenField name="receiptItems[${i}].lotNumber" value="${receiptItem?.lotNumber}"/>
																				${receiptItem?.lotNumber}
																			</td>
																			<td style="text-align: center; vertical-align: middle;">
																				<g:hiddenField name="receiptItems[${i}].quantityShipped" value="${receiptItem?.quantityShipped}"/>																	
																				${receiptItem?.quantityShipped}
																			</td>
																			<td style="text-align: center; vertical-align: middle;">
																				<g:textField name="receiptItems[${i}].quantityReceived" value="${receiptItem?.quantityReceived}" size="3"/>
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
				                            <td valign="top" class="name"><label><warehouse:message code="default.comment.label" /></label></td>                            
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
														alt="save" /> <warehouse:message code="shipping.receiveShipment.label" /></button>
													<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}" class="negative">
														<img
															src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
															alt="Cancel" /> <warehouse:message code="default.button.cancel.label" /> </g:link>
												</div>				
											</td>
										</tr>
								</tbody>
							</table>
						</g:form>
					</fieldset>
				</td>		
			</tr>
		</table>	
			
	</div>
</body>
</html>
