

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipmentType.label', default: 'Shipment Type')}" />
	<title><g:message code="shipment.create.label" default="Create Suitcase Shipment" /></title>        
	<content tag="pageTitle"><g:message code="shipment.create.label" default="Create Suitcase Shipment" /></content>
	<style>
	</style>
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
		
		<g:render template="flowHeader"/>						
								
				
		<g:form action="suitcase">
			<g:hiddenField name="id" value="${shipmentInstance?.id}" />
			<g:hiddenField name="version" value="${shipmentInstance?.version}" />
	
	
	
			<fieldset>
				<legend>Step 5.&nbsp;Send Shipment</legend>
			
				<div class="dialog">				
					<table>
						<tbody>
							
							<%-- 
							<tr class="prop">
								<td valign="top" class="name" style="width: 10%;"><label><g:message
									code="shipment.shipper.label" default="Shipping Method" /></label>
								</td>
								<td valign="top" style="width: 30%; line-height: 1.5em">			
									<g:if test="${shipmentInstance?.shipmentMethod?.shipperService}">								
										<g:autoSuggest id="shipperService" name="shipmentMethod.shipperService" 
											jsonUrl="/warehouse/json/findShipperServiceByName" 
											valueId="${shipmentInstance?.shipmentMethod?.shipperService?.id}" 
											valueName="${shipmentInstance?.shipmentMethod?.shipper?.name} ${shipmentInstance?.shipmentMethod?.shipperService?.name}"/>												
									</g:if>
									<g:else>
										<g:autoSuggest id="shipperService" name="shipmentMethod.shipperService" 
											jsonUrl="/warehouse/json/findShipperServiceByName"/>																							
									</g:else>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name" style="width: 10%;"><label><g:message
									code="shipment.shipper.label" default="Tracking Number" /></label>
								</td>
								<td valign="top" style="width: 30%;">											
									<g:textField name="shipmentMethod.trackingNumber" 
										value="${shipmentInstance?.shipmentMethod?.trackingNumber}" style="width: 200px" />										
								</td>
							</tr>
							 --%>												
							<%-- 
							<tr class="prop">
								<td class="name"  style="width: 10%;">
									<label><g:message code="shipment.destination.label" default="Recipient" /></label>
								</td>
								<td class="value" style="width: 30%;">		
									<g:autoSuggest id="recipient" name="recipient" jsonUrl="/warehouse/json/findPersonByName"
										width="180" size="30"
										valueId="${shipmentInstance?.recipient?.id}"
										valueName="${shipmentInstance?.recipient?.name}"/>

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
							--%>			
							<tr class="prop">
								<td valign="top" class="name"><label>Name</label></td>
								<td valign="top" class="value">
									${shipmentInstance?.name }
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label>Notifications</label></td>
								<td valign="top" class="value">
									By clicking <b>Finish</b>, your suitcase will be marked as <b>Shipped</b> and 
									a notification email will be sent to the following people:
									<br/><br/>
									<table>	
										<g:each var="itemInstance" in="${shipmentInstance.allShipmentItems }">
											<tr>
												<td>
													<img src="${createLinkTo(dir:'images/icons/silk',file: 'email.png')}" style="vertical-align: middle"/>
													&nbsp; 
													<g:if test="${itemInstance?.recipient}">
														${itemInstance?.recipient?.name } <span class="">(${itemInstance?.recipient?.email})</span>
													</g:if>
													<g:else>
														<i>no recipient selected</i>
													</g:else>
													<br/>
													<div style="padding-left: 50px;">
														<b>${itemInstance?.product?.name }</b> (${itemInstance?.quantity}) 
													</div>
												</td>
											</tr>												
										</g:each>
									</table>
								</td>							
							</tr>
							<tr class="prop">
	                            <td valign="top" class="name"><label><g:message code="comment.comment.label" default="Additional Comments" /></label></td>                            
	                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'comment', 'errors')}">
                                    <g:textArea name="comment" cols="30" rows="2"/>
                                </td>
	                        </tr>  	        
						</tbody>
					</table>										
				</div>
				<div class="buttons">
				    <g:submitButton name="back" value="Back"></g:submitButton>
				    <g:submitButton name="finish" value="Finish"></g:submitButton>
			    </div>

			</fieldset>
		</g:form>		
	</div>
</body>
</html>