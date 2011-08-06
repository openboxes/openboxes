<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Add request items</title>

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
		<g:hasErrors bean="${requestListCommand}">
			<div class="errors">
				<g:renderErrors bean="${requestListCommand}" as="list" />
			</div>
		</g:hasErrors>
				
		<div class="dialog">
			<fieldset>
				<g:render template="../request/summary" model="[requestInstance:requestInstance]"/>				
				<g:render template="progressBar" model="['state':'pickRequestItems']"/>		
				<g:form action="fulfillRequest" autocomplete="false">
					<table>
						<tr>
							<td style="padding: 0; margin: 0;">														
								<div>
									<g:if test="${requestItems }">
										<table id="requestItemsTable" border="0">
											<thead>
												<tr class="odd prop">
													<td class="center" colspan="3">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'cart.png')}" alt="requested" style="vertical-align: middle"/>
														Items Requested
													</td>
													<td class="center" colspan="4" style="border-left: 1px solid lightgrey;">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="received" style="vertical-align: middle"/>
														Items Fulfilled
													</td>
												</tr>
												<tr class="even">
													<td class="center">Requested</td>										
													<td>Description</td>
													<td>Actions</td>
													<%--<td class="center">Remaining</td> --%>	
													<td class="center" style="border-left: 1px solid lightgrey;">Fulfilled</td>										
													<td>Product</td>										
													<td>Lot Number</td>
													<td>Expires</td>
												</tr>
											</thead>									
											<tbody>
												<g:each var="entrymap" in="${requestItems?.groupBy { it?.requestItem } }" status="i">
													<g:set var="fulfillItemsMap" value="${requestCommand?.fulfillItems?.groupBy { it.requestItem }}"/>
													<g:each var="requestItem" in="${entrymap.value}">
														<tr class="${(requestItem?.primary)?"black-top-off":""} ${i%2?'even':'odd'} requestItem">
															<td class="center">															
																<g:if test="${requestItem?.primary }">${requestItem?.quantityRequested}</g:if>
															</td>
															<td>
																<g:hiddenField name="requestItems[${i }].requestItem.id" class="requestItemId" value="${requestItem?.requestItem?.id }"/>
																<g:hiddenField name="requestItems[${i }].primary" value="${requestItem?.primary }"/>
																<g:hiddenField name="requestItems[${i }].type" value="${requestItem?.type }"/>
																<g:hiddenField name="requestItems[${i }].description" value="${requestItem?.description }"/>
																<g:hiddenField name="requestItems[${i }].quantityRequested" value="${requestItem?.quantityRequested }"/>
																<g:if test="${requestItem?.primary }">
																	<g:if test="${requestItem.requestItem.product }">
																		<g:link controller="inventoryItem" action="showStockCard" id="${requestItem?.requestItem?.product?.id }">
																			${requestItem?.description }
																		</g:link>
																	</g:if>
																	<g:else>
																		${requestItem?.description }
																	</g:else>
																</g:if>
																<%-- 
																<g:if test="${requestItem?.primary }"><span class="fade">${requestItem?.type }</span></g:if>
																--%>
															</td>
															<td>
																<g:link action="fulfillRequest" event="showDialog" params="['requestItem.id':requestItem?.requestItem?.id]">
																	<img src="${resource(dir: 'images/icons/silk', file: 'accept.png') }"/>
																	<warehouse:message code="request.fulfillItem.label"/>
																</g:link>
																<%-- 
																<button name="_eventId_fulfillItem">
																	<img src="${resource(dir: 'images/icons/silk', file: 'accept.png') }"/>
																	<warehouse:message code="request.fulfillItem.label"/>
																</button>
																--%>															
																<%-- 
																<button id="request-item-id${requestItem?.requestItem?.id }" class="fulfill-btn">
																	<img src="${resource(dir: 'images/icons/silk', file: 'accept.png') }"/>
																	Choose &nbsp;
																</button>
																--%>
															</td>
															<%-- 
															<td class="center">
																<g:if test="${requestItem?.primary }">
																	${requestItem?.quantityRequested - requestItem?.requestItem?.quantityFulfilled()}
																</g:if>
															</td>
															--%>
															<td colspan="4" class="center" style="padding: 0px; border-left: 1px solid lightgrey;">
																
																<g:set var="fulfillmentItems" value="${fulfillItemsMap[entrymap.key] }"/>
																<g:if test="${fulfillmentItems }">																
																	<table>
																		<g:each var="fulfillmentItem" in="${fulfillmentItems }" status="j">
																			<tr class="${j%2?'even':'odd'}">
																				<td class="center">
																					${fulfillmentItem?.quantityReceived }
																				</td>
																				<td style="width: 250px;" >
																					${fulfillmentItem?.productReceived?.name }
																					<span class="fade">
																						${fulfillmentItem?.productReceived?.manufacturer }
																					</span>
																				</td>
																				<td style="width: 100px;">
																					${fulfillmentItem?.lotNumber }
																				</td>
																				<td style="width: 100px;">
																					${fulfillmentItem?.expirationDate }
																				</td>
																			</tr>
																		</g:each>
																	</table>
																</g:if>
															</td>
														</tr>
													</g:each>
												</g:each>
											</tbody>
										</table>
									</g:if>
								</div>
							</td>		
						</tr>
						<tr class="prop">
							<td>
								<div class="buttons" style="border-top: 0px solid lightgrey;">
									<g:submitButton name="back" value="Back"></g:submitButton>
									<g:submitButton name="next" value="Next"></g:submitButton>
									<g:link action="fulfillRequest" event="cancel">Cancel</g:link>
								</div>
							</td>
						</tr>
						
					</table>
				</g:form>
			</fieldset>
		</div>
		<%-- 
		<div id="dialog">
			<div id="dialog-form">
				<!-- populated dynamically -->			
			</div>
		</div>
		--%>			
		
		
		<g:if test="${showDialog}">
			<g:render template="fulfillItem" model="['requestItem':requestItem]"/>
		</g:if>


	</div>	  
	
</body>
</html>