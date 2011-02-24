

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

		
		<g:form action="createShipment">
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
								<td valign="top" class="name"><label>Instructions</label></td>
								<td valign="top" class="value">
									<p>By clicking <b>Send Shipment</b>, your ${shipmentInstance.shipmentType?.name} shipment will be marked as <b>Shipped</b> and 
									a notification email will be sent to the following people:</p>
							
									<br/>

									<table>	
										<tr>
											<th></th>
											<th>Recipient</th>
											<th>Notification</th>
										</tr>
										<tr class="prop">
											<td>
												<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
											</td>
											<td>${shipmentInstance?.carrier?.name }  &nbsp;<br/> <span class="fade">${shipmentInstance?.carrier?.email}</span></td>
											<td>Your ${shipmentInstance.shipmentType?.name} shipment is ready for pickup</td>
										</tr>
										<tr class="prop">
											<td>
												<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
											</td>
											<td>${shipmentInstance?.recipient?.name }  &nbsp;<br/> <span class="fade">${shipmentInstance?.recipient?.email}</span></td>
											<td>Your ${shipmentInstance.shipmentType?.name} shipment is ready to ship</td>
										</tr>
										
										<g:each var="recipient" in="${shipmentInstance.allShipmentItems.recipient.unique() }">
											<tr class="prop">
												<td>
													<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
												</td>
												<td>
													<g:if test="${recipient}">
														${recipient?.name } &nbsp;<br/><span class="fade">${recipient?.email}</span>
													</g:if>
													<g:else>
														<i>no recipient selected</i>
													</g:else>
												</td>
												<td>
													Your item(s) are ready to ship
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