
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">
		Edit Shipment
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


		<table>		
			<tr>
				<td width="75%">
					<fieldset>
						<g:render template="summary" />

						
							<g:form action="sendShipment" method="POST">
								<g:hiddenField name="id" value="${shipmentInstance?.id}" />
								<table>
									<tbody>
							
									<tr class="prop">
										
										<td valign="top" class="name"><label>Overview</label></td>
										<td valign="top" class="value">
											<p>By clicking <b>Send Shipment</b> below, you are authorizing that the  
											following inventory items will be sent from <b>${shipmentInstance?.origin?.name }</b>
											to <b>${shipmentInstance?.destination?.name }</b>.  Upon submission, the following
											actions will take place:
											</p>
											<table style="display: inline">
												<tr>
													<td>
														<img src="${createLinkTo(dir:'images/icons/silk',file: 'lorry_go.png')}" style="vertical-align: middle"/>
													</td>
													<td>Shipment <b>${shipmentInstance?.name}</b> will transition from <b>${shipmentInstance?.mostRecentStatus}</b> to <b>Shipped</b></td>
												<tr>
													<td>
														<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
													</td>
													<td>Notification emails will be sent out</td>
												</tr>
												<tr>
													<td>
														<img src="${createLinkTo(dir:'images/icons/silk',file: 'delete.png')}" style="vertical-align: middle"/>
													</td>
													<td><b>${shipmentInstance?.shipmentItems?.size() } items</b> in the shipment will be debited from ${shipmentInstance?.origin?.name }</td>
												</tr>
											</table>
										</td>								
									</tr>
							
									<tr class="prop">
										<td valign="top" class="name"><label>Items</label></td>
										<td valign="top" class="value">
											The following items will be debited from <b>${shipmentInstance?.origin?.name }</b>.
											<br/>
											<g:if test="${shipmentInstance.shipmentItems}">
												<table style="display: inline">
													<tr>
														<th></th>
														<th>Item</th>
														<th>Quantity</th>
													</tr>
													<g:each var="item" in="${shipmentInstance?.shipmentItems }" status="status">
														<tr class="${status % 2 ? 'even' : 'odd' }">
															<td>
																<img src="${createLinkTo(dir:'images/icons/silk',file: 'delete.png')}" style="vertical-align: middle"/>
															</td>
															<td>
																${item?.product?.name } ${item?.lotNumber }
															</td>
															<td>
																-${item?.quantity }
															</td>
														</tr>
													</g:each>
												</table>	
											</g:if>
											<g:else>
												There are no shipment items to be shipped.
											</g:else>
										</td>
									</tr>
							
									<tr class="prop">
										<td valign="top" class="name"><label>Notifications</label></td>
										<td valign="top" class="value">
											<p>The following people will receive email notifications:</p>								
											<table style="display: inline">	
												<tr>
													<th></th>
													<th>Role</th>
													<th>Recipient</th>
													<th>Notification</th>
												</tr>
												
												<g:if test="${!shipmentWorkflow?.isExcluded('carrier')}">
													<tr class="prop odd">
														<td>
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
														</td>
														<td>Traveler</td>
														<td>${shipmentInstance?.carrier?.name }  &nbsp;<br/> <span class="fade">${shipmentInstance?.carrier?.email}</span></td>
														<td>Your ${shipmentInstance.shipmentType?.name} shipment is ready for pickup</td>
													</tr>
												</g:if>
												
												<g:if test="${!shipmentWorkflow?.isExcluded('recipient')}">
													<tr class="prop even">
														<td>
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
														</td>
														<td>Owner</td>
														<td>${shipmentInstance?.recipient?.name }  &nbsp;<br/> <span class="fade">${shipmentInstance?.recipient?.email}</span></td>
														<td>Your ${shipmentInstance.shipmentType?.name} shipment is ready to ship</td>
													</tr>
												</g:if>
												
												<g:each var="recipient" in="${shipmentInstance.allShipmentItems.recipient.unique() }">
													<tr class="prop odd">
														<td>
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
														</td>
														<td>Recipient</td>
														<td>
															<g:if test="${recipient}">
																${recipient?.name } &nbsp;<br/><span class="fade">${recipient?.email}</span>
															</g:if>
															<g:else>
																<i>no recipient selected</i>
															</g:else>
														</td>
														<td>
															Your item(s) are about to ship
														</td>
													</tr>						
												</g:each>
											</table>
										</td>							
									</tr>
						
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.actualShippingDate.label" default="Shipping date" /></label></td>
										<td valign="top"
											class=" ${hasErrors(bean: shipmentInstance, field: 'actualShippingDate', 'errors')}"
											nowrap="nowrap">
												<g:jqueryDatePicker name="actualShippingDate"
											value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy"/>
										</td>
									</tr>											
									<tr class="prop">
			                            <td valign="top" class="name"><label><g:message code="note.label" default="Note" /></label></td>                            
			                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'comment', 'errors')}">
		                                    <g:textArea name="comment" cols="60" rows="5"/>
		                                </td>
			                        </tr>  	        
											
										<tr class="prop">
											<td valign="top" class="name"></td>
											<td valign="top" class="value">
												<div class="buttons">
													<button type="submit" class="positive"><img
														src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
														alt="save" /> Send Shipment</button>
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
