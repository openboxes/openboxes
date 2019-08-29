
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="shipping.sendShipment.label" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">
		<warehouse:message code="shipping.sendShipment.label" />
	</content>
</head>

<body>
	<div class="body">
	
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>	
		<g:hasErrors bean="${transactionInstance}">
			<div class="errors">
				<g:renderErrors bean="${transactionInstance}" as="list" />
			</div>
		</g:hasErrors>	

		<div class="dialog">
			<fieldset>
				<g:render template="summary" />

				<g:form action="sendShipment" method="POST">
					<g:hiddenField name="id" value="${shipmentInstance?.id}" />
					<table>
						<tbody>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label>
										<warehouse:message code="shipping.shippingDate.label" />
									</label>
								</td>
								<td valign="top"
									class=" ${hasErrors(bean: shipmentInstance, field: 'actualShippingDate', 'errors')}"
									nowrap="nowrap">
										<g:jqueryDatePicker name="actualShippingDate"
									value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy"/>
								</td>
							</tr>											
							<tr class="prop">
	                            <td valign="top" class="name">
	                            	<label>
	                            		<warehouse:message code="shipping.note.label"/>
	                            	</label>
	                            </td>                            
	                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'comment', 'errors')}">
                                    <g:textArea name="comment" cols="100" rows="5"/>
                                </td>
	                        </tr>  	        
						
					
							<tr class="prop">
								
								<td valign="top" class="name"><label><warehouse:message code="shipping.overview.label"/></label></td>
								<td valign="top" class="value">
									<p>
										<warehouse:message code="shipping.sendShipment.message" 
											args="[shipmentInstance?.origin?.name,shipmentInstance?.destination?.name]"/>
									</p>
									<ul class="prop">
										<li>
											<img src="${createLinkTo(dir:'images/icons/silk',file: 'lorry_go.png')}" style="vertical-align: middle"/>
											&nbsp;
											<warehouse:message code="shipping.shipmentWillBeMarkedAsShipped.message" 
											args="[shipmentInstance?.name]"/>
										</li>
										<g:if test="${shipmentInstance?.origin.isWarehouse()}">
											<li>
												<img src="${createLinkTo(dir:'images/icons/silk',file: 'delete.png')}" style="vertical-align: middle"/>
												&nbsp;
												<warehouse:message code="shipping.itemsInShipmentWillBeDebited.message" args="[shipmentInstance?.shipmentItems?.size(),shipmentInstance?.origin?.name]"/>
											</li>
										</g:if>
										<li>
											<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
											&nbsp;
											<warehouse:message code="shipping.notificationEmailsWillBeSentOut.message"/>
										</li>
									</ul>
								</td>								
							</tr>
							
							
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
												
							<g:if test="${shipmentInstance?.origin.isWarehouse()}">
								<tr class="prop">
									<td valign="top" class="name">
										<label>
											<warehouse:message code="default.items.label"/>
										</label>
									</td>
									<td valign="top" class="value">
										<warehouse:message code="shipping.willBeDebited.message" args="[shipmentInstance?.origin?.name]"/>											
										<g:if test="${shipmentInstance.shipmentItems.sort()}">
											<table style="display: inline" id="debitShipmentItems">
												<tr>
													<th></th>
													<th></th>
													<th><warehouse:message code="default.item.label"/></th>
													<th><warehouse:message code="default.quantity.label"/></th>
												</tr>
												<g:each var="item" in="${shipmentInstance?.shipmentItems }" status="status">
													<tr class="${status % 2 ? 'even' : 'odd' }">
														<td>
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'delete.png')}" style="vertical-align: middle"/>
														</td>
														<td>
															${item?.container?.name }
														</td>
														<td>
															<format:product product="${item?.product}"/> ${item?.lotNumber }
														</td>
														<td>
															${item?.quantity }
														</td>
													</tr>
												</g:each>
											</table>	
										</g:if>
										<g:else>
											<warehouse:message code="shipping.noItemsToShip.message"/>
										</g:else>
									</td>
								</tr>
							</g:if>
					
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="shipping.notifications.label"/></label></td>
								<td valign="top" class="value">								
									<warehouse:message code="shipping.notifications.message"/>
									<table style="display: inline" id="notifyRecipients">	
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
										<g:set var="recipients" value="${shipmentInstance.allShipmentItems.recipient }"/>
										<g:if test="${recipients }">
											<g:each var="recipient" in="${shipmentInstance.allShipmentItems.recipient }">
												<g:if test="${recipient?.id != shipmentInstance?.recipient?.id}">
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
												</g:if>						
											</g:each>
										</g:if>
									</table>
								</td>							
							</tr>
				
									
							<tr class="prop">
								<td valign="top" class="name"></td>
								<td valign="top" class="value">
									
									<button type="submit" class="positive"><img
										src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}"
										alt="save" /> <warehouse:message code="shipping.sendShipment.label"/></button>
									&nbsp;
									<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}" class="negative">
										<warehouse:message code="default.button.cancel.label"/> </g:link>
								</td>
							</tr>
						</tbody>
					</table>
				</g:form>
			</fieldset>
		</div>
	</div>
	<script type="text/javascript">
		$(function() { 		
			$('#debit').click(function() {
			    $("#debitShipmentItems").toggle(this.checked);
			});
			$('#notify').click(function() {
			    $("#notifyRecipients").toggle(this.checked);
			});
		});
	</script>
</body>
</html>
