  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="shipping.sendShipment.label"/></title>
         <style>
         	.top-border { border-top: 2px solid lightgrey; }
         	.right-border { border-right: 2px solid lightgrey; }
         </style>
    </head>
    <body>
        <div class="body">
        	<g:if test="${message}">
				<div class="message">${message}</div>
			</g:if> 
			<g:hasErrors bean="${command}">
				<div class="errors">
					<g:renderErrors bean="${command}" as="list" />
				</div>
			</g:hasErrors> 
			
			
			
			<g:form action="createShipment" method="post">
				<g:hiddenField name="id" value="${shipmentInstance?.id}"/>
				<g:hiddenField name="shipment.id" value="${shipmentInstance?.id}"/>
				
					<g:render template="../shipment/summary" />	
					<g:render template="flowHeader" model="['currentState':'Send']"/>
					<g:set var="shipmentItemsWithRecipient" value="${shipmentInstance.allShipmentItems.findAll { it.recipient } }"/>
					<g:set var="includeNotifications" value="${shipmentItemsWithRecipient || (!shipmentWorkflow?.isExcluded('carrier') && shipmentInstance?.carrier) || (!shipmentWorkflow?.isExcluded('recipient') && shipmentInstance?.recipient)}"/>
					
					<div class="dialog box">
						<table>
							<tbody>
								<tr class="prop">
									<td valign="top" class="name"><label><warehouse:message code="shipping.overview.label"/></label></td>
									<td valign="top" class="value">
										<p>
											<warehouse:message code="shipping.sendShipment.message" 
												args="[shipmentInstance?.origin?.name,shipmentInstance?.destination?.name]"/>
										</p>
										<hr/>
										<table>
											<tr>
												<td>
													<img src="${createLinkTo(dir:'images/icons/silk',file: 'lorry_go.png')}" style="vertical-align: middle"/>
													&nbsp;
													<warehouse:message code="shipping.shipmentWillBeMarkedAsShipped.message" 
													args="[shipmentInstance?.name]"/>
												</td>
											</tr>
											<g:if test="${shipmentInstance?.origin.isWarehouse()}">
												<tr>
													<td>
														<img src="${createLinkTo(dir:'images/icons/silk',file: 'delete.png')}" style="vertical-align: middle"/>
														&nbsp;
														<span id="itemsInShipmentWillBeDebited">
															<warehouse:message code="shipping.itemsInShipmentWillBeDebited.message" args="[shipmentInstance?.shipmentItems?.size(),shipmentInstance?.origin?.name]"/>
														</span>
														<span id="itemsInShipmentWillNotBeDebited" class="hidden">
															<warehouse:message code="shipping.itemsInShipmentWillNotBeDebited.message" args="[shipmentInstance?.shipmentItems?.size(),shipmentInstance?.origin?.name]"/>
														</span>
													</td>
												</tr>
											</g:if>
											<g:if test="${includeNotifications }">
												<tr>
													<td>
														<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
														&nbsp;
														<span id="notificationEmailsWillBeSentOut">
															<warehouse:message code="shipping.notificationEmailsWillBeSentOut.message"/>
														</span>
													</td>
												</tr>
											</g:if>
											<g:else>
												<tr>
													<td>
														<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
														&nbsp;
														<span id="notificationEmailsWillNotBeSentOut">
															<warehouse:message code="shipping.notificationEmailsWillNotBeSentOut.message"/>
														</span>
													</td>
												</tr>
											</g:else>
										</table>
									</td>								
								</tr>
								<tr class="prop">
									<td valign="top" class="name">
										<label>
											<warehouse:message code="shipping.expectedShippingDate.label" />:
										</label>
									</td>
									<td class="value">
										<format:date obj="${shipmentInstance?.expectedShippingDate}"
											format="dd/MMM/yyyy"/>
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name">
										<label>
											<warehouse:message code="shipping.actualShippingDate.label" />:
										</label>
									</td>
									<td valign="middle"
										class="value ${hasErrors(bean: shipmentInstance, field: 'actualShippingDate', 'errors')}"
										nowrap="nowrap">
										<g:jqueryDatePicker id="actualShippingDate" name="actualShippingDate"
											value="${command?.actualShippingDate}" format="MM/dd/yyyy"/>										
									</td>
								</tr>											
																	
								
								
								<%-- 								
								<tr class="prop">
									<td valign="top" class="name">
										<label>
											<warehouse:message code="default.status.label"/>
										</label>
									</td>
									<td valign="top" class="value">
										<span class="fade">
											<format:metadata obj="${org.pih.warehouse.shipping.ShipmentStatusCode.CREATED}"/>
										</span>
										&nbsp;
										<img src="${createLinkTo(dir:'images/icons/silk',file: 'arrow_right.png')}" style="vertical-align: middle"/>
										&nbsp;
										<span class="fade">
											<format:metadata obj="${shipmentInstance?.status?.code}"/>
										</span>
										&nbsp;
										<img src="${createLinkTo(dir:'images/icons/silk',file: 'arrow_right.png')}" style="vertical-align: middle"/>
										&nbsp;
										<span>
											<b><format:metadata obj="${org.pih.warehouse.shipping.ShipmentStatusCode.SHIPPED}"/></b>
										</span>
										&nbsp;
										<img src="${createLinkTo(dir:'images/icons/silk',file: 'arrow_right.png')}" style="vertical-align: middle"/>
										&nbsp;
										<span class="fade">
											<format:metadata obj="${org.pih.warehouse.shipping.ShipmentStatusCode.RECEIVED}"/>
										</span>
									</td>
								</tr>
								--%>
										
								<g:if test="${shipmentInstance?.origin.isWarehouse()}">
									<tr class="prop">
										<td valign="top" class="name">
											<label>
												<warehouse:message code="default.items.label"/>
											</label>
										</td>
										<td valign="top" class="value">
											<g:if test="${shipmentInstance.shipmentItems}">
												<%--
												<div>												
													<g:checkBox name="debitStockOnSend" value="${command ? command.debitStockOnSend : true}"/>
													&nbsp;
													<warehouse:message code="shipping.debitStockOnSend.message" args="[shipmentInstance?.origin?.name]"/>											
												</div>
												<br/>
												 --%>
												
												<div id="debitShipmentItems" class="${!command || command?.debitStockOnSend ? '' : 'hidden'  }">
													<warehouse:message code="shipping.willBeDebited.message" args="[shipmentInstance?.origin?.name]"/>
													
													<div style="overflow: auto; max-height: 300px; border: 1px solid lightgrey;">
														<table>
															<tr>
																<th><warehouse:message code="container.label"/></th>
																<th><warehouse:message code="product.label"/></th>
																<th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
																<th><warehouse:message code="inventoryItem.expirationDate.label"/></th>
																<th><warehouse:message code="default.quantity.label"/></th>
															</tr>
															<g:set var="previousContainer"/>
															<g:each var="shipmentItem" in="${shipmentInstance?.shipmentItems.sort() }" status="status">
																<g:set var="isSameAsPrevious" value="${shipmentItem?.container == previousContainer}"/>
																<tr class="${status % 2 ? 'even' : 'odd' } ${!isSameAsPrevious ? 'top-border':'' }">
																	<td class="right-border">																	
																		<g:if test="${!isSameAsPrevious }">
																			${shipmentItem?.container?.name }
																		</g:if>
																	</td>
																	<td>
																		<format:product product="${shipmentItem?.inventoryItem?.product}"/> 
																	</td>
																	<td>
																		<span class="lotNumber">${shipmentItem?.inventoryItem?.lotNumber }</span>																	
																	</td>
																	<td>
																		<span class="expirationDate">
																			<g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate }" format="MMMMM yyyy"/>
																		</span>																	
																	</td>
																	<td>
																		${shipmentItem?.quantity }
																		${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
																	</td>
																</tr>
																<g:set var="previousContainer" value="${shipmentItem?.container }"/>
															</g:each>
														</table>	
													</div>
												</div>
												<div id="noDebitShipmentItems" class="${!command || command?.debitStockOnSend ? 'hidden' : ''}">
													<warehouse:message code="shipping.willNotBeDebited.message" args="[shipmentInstance?.origin?.name]"/>
												</div>
											</g:if>
											
										</td>
									</tr>
								</g:if>

								<tr class="prop">
									<td valign="top" class="name">
										<label><warehouse:message code="shipping.notifications.label"/></label>
									</td>
									<td valign="top" class="value">
									
										<g:if test="${includeNotifications }">
										
										
											<warehouse:message code="shipping.notificationEmailsWillBeSentOut.message"/>
											<%-- 
											<div>												
												<g:checkBox name="debitStockOnSend" value="${command ? command.debitStockOnSend : true}"/>
												&nbsp;
												<warehouse:message code="shipping.debitStockOnSend.message" args="[shipmentInstance?.origin?.name]"/>											
											</div>
											<br/>
										
											<span>
												<g:checkBox name="notify" checked="true"/>
												<warehouse:message code="shipping.notifications.message"/>
											</span>
											--%>								
											<warehouse:message code="shipping.notifications.message"/>
											<table style="border: 1px solid lightgrey;" id="notifyRecipients">	
												<tr>
													<th></th>
													<th><warehouse:message code="default.role.label"/></th>
													<th><warehouse:message code="shipping.recipient.label"/></th>
												</tr>
												
												<g:if test="${!shipmentWorkflow?.isExcluded('carrier') && shipmentInstance?.carrier}">
													<tr class="prop odd">
														<td style="valign:center">
															<input type="checkbox" checked="true" name="emailRecipientId" value="${shipmentInstance?.carrier?.id}"/>
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/> 
														</td>
														<td>
															<warehouse:message code="shipping.traveler.label"/>
														</td>
														<td>
															${shipmentInstance?.carrier?.name }  &nbsp;
															<span class="fade">${shipmentInstance?.carrier?.email}</span>
														</td>
													</tr>
												</g:if>
												
												<g:if test="${!shipmentWorkflow?.isExcluded('recipient') && shipmentInstance?.recipient}">
													<tr class="prop even">
														<td>
															<input type="checkbox" checked="true" name="emailRecipientId" value="${shipmentInstance?.recipient?.id}"/>
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/> 
														</td>
														<td>
															<warehouse:message code="shipping.recipient.label"/>
														</td>
														<td>
															${shipmentInstance?.recipient?.name }  &nbsp; 
															<span class="fade">${shipmentInstance?.recipient?.email}</span>
														</td>
													</tr>
												</g:if>
												
												
												<g:if test="${shipmentItemsWithRecipient }">
													<g:set var="recipients" value="${shipmentItemsWithRecipient?.collect{it.recipient}.unique()}"/>
													<g:each var="recipient" in="${recipients}">
														
														<tr class="prop odd">
															<td>
																<input type="checkbox" checked="true" name="emailRecipientId" value="${recipient?.id}"/>
																<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/> 
															</td>
															<td>
																<warehouse:message code="shipping.recipient.label"/>
															</td>
															<td>							
																${recipient?.name } &nbsp;
																<span class="fade">${recipient?.email}</span>				
															</td>
														</tr>
													</g:each>
												</g:if>
											</table>			
										</g:if>		
										<g:else>
											<warehouse:message code="shipping.notificationEmailsWillNotBeSentOut.message"/>
										</g:else>
									</td>							
								</tr>
								<tr class="prop">
		                            <td valign="top" class="name">
		                            	<label>
		                            		<warehouse:message code="default.comments.label"/>
		                            	</label>
		                            </td>                            
		                            <td valign="top" class="value ${hasErrors(bean: command, field: 'comments', 'errors')}">
	                                    <g:textArea name="comments" value="${command?.comments }" cols="100" rows="5"/>
	                                </td>
		                        </tr>  	        
							</tbody>
							
	               		</table>
					</div>
					<div class="buttons">
						<button name="_eventId_back">&lsaquo; <warehouse:message code="default.button.back.label"/></button>	
						<button name="_eventId_next"><warehouse:message code="default.button.next.label"/> &rsaquo;</button> 
						<button name="_eventId_save"><warehouse:message code="default.button.saveAndExit.label"/></button>
						<button name="_eventId_cancel"><warehouse:message code="default.button.cancel.label"/></button>						
					</div>
				
			</g:form>
		</div>
		<script type="text/javascript">
			$(function() { 		
				$('#debitStockOnSend').click(function() {
				    $("#debitShipmentItems").toggle(this.checked);
				    $("#noDebitShipmentItems").toggle(!this.checked);
				    $("#itemsInShipmentWillBeDebited").toggle(this.checked);
				    $("#itemsInShipmentWillNotBeDebited").toggle(!this.checked);				    
				});
				$('#notify').click(function() {
				    $("#notifyRecipients").toggle(this.checked);
				    $("#notificationEmailsWillBeSentOut").toggle(this.checked);
				    $("#notificationEmailsWillNotBeSentOut").toggle(!this.checked);
				    
				});
			});
		</script>
		
	</body>
</html>
