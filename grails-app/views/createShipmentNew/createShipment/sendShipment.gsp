

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipmentType.label', default: 'Shipment Type')}" />
	<title>Send Shipment</title>        
</head>

<body>    
	<div class="body">
		
		<g:if test="${message}">
			<div class="message">${message}</div>
		</g:if>
		
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>	

		<g:render template="flowHeader" model="['currentState':'Ship']"/>	

		
		<g:form action="createShipment" method="post">
			<g:hiddenField name="id" value="${shipmentInstance?.id}" />
			<g:hiddenField name="version" value="${shipmentInstance?.version}" />

	
			<fieldset>
				<legend>Step 5.&nbsp;Send Shipment</legend>
				
				<g:render template="../shipment/summary" />
			
				<div class="dialog">				
					<table>
						<tbody>
							
							<tr class="prop">
								<td valign="top" class="name"><label>Name</label></td>
								<td valign="top" class="value">
									${shipmentInstance?.name }
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.name.label" default="Shipment Number" /></label>
								</td>
								<td colspan="3" valign="top"
									class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
									<span style="line-height: 1.5em">${shipmentInstance?.shipmentNumber}</span>
								</td>
							</tr>
							
							<g:if test="${!shipmentInstance.hasShipped()}">
								<tr class="prop">
									
									<td valign="top" class="name"><label>Overview</label></td>
									<td valign="top" class="value">
										<p>By clicking <b>Send Shipment</b> below, you are autorizing that the  
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
												<td>Notification emails will be sent to you, the traveler, and all recepients</td>
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
											<tr class="prop odd">
												<td>
													<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
												</td>
												<td>Traveler</td>
												<td>${shipmentInstance?.carrier?.name }  &nbsp;<br/> <span class="fade">${shipmentInstance?.carrier?.email}</span></td>
												<td>Your ${shipmentInstance.shipmentType?.name} shipment is ready for pickup</td>
											</tr>
											<tr class="prop even">
												<td>
													<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
												</td>
												<td>Owner</td>
												<td>${shipmentInstance?.recipient?.name }  &nbsp;<br/> <span class="fade">${shipmentInstance?.recipient?.email}</span></td>
												<td>Your ${shipmentInstance.shipmentType?.name} shipment is ready to ship</td>
											</tr>
											
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
							</g:if>
							<g:else>
								<tr class="prop">
									<td valign="top" class="name">&nbsp;
									</td>
									<td>
										This shipment shipped on <g:formatDate date="${shipmentInstance?.actualShippingDate}" format="MMM dd, yyyy"/>.
									</td>
								</tr>
							</g:else>
						</tbody>
					</table>										
				</div>
				<div class="buttons">
					<table>
							<tr>
								<td width="45%" style="text-align: right;">
									<g:submitButton name="back" value="Back"></g:submitButton>	
									<g:if test="${!shipmentInstance.hasShipped()}">
										<g:submitButton name="send" value="Send Shipment"></g:submitButton> 
									</g:if>
								</td>
								<td width="10%">&nbsp;</td>
								<td width="45%" style="text-align: left;">
									<g:submitButton name="save" value="Save and Exit"></g:submitButton>
									<g:submitButton name="cancel" value="Cancel"></g:submitButton>						
								</td>
							</tr>
					</table>
			    </div>
			</fieldset>
		</g:form>		
	</div>
</body>
</html>