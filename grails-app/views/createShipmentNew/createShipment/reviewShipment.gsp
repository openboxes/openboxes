                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Review Shipment</title>         
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
           
									
			<g:render template="flowHeader" model="['currentState':'Review']"/>
           
			<g:form action="createShipment" method="post" >
           		<fieldset>
           			<legend>Step 4. Review shipment</legend>
           		
           			<g:render template="../shipment/summary" />
           		
           			
					<div class="dialog">     
						<table>
							<tbody>
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.name.label" default="Name" /></label>
									</td>
									<td colspan="3" valign="top"
										class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
										${shipmentInstance?.name}
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
								<tr class="prop">
									<td valign="middle" class="name"><label><g:message
										code="shipment.shipmentType.label" default="Type" /></label></td>
									<td valign="middle" class="value" nowrap="nowrap">
										<g:hiddenField name="shipmentType.id" value="${shipmentInstance?.shipmentType?.id}"/>
										${shipmentInstance?.shipmentType?.name}								
		
									</td>
								</tr>											
								<tr class="prop">
									<td valign="top" class="name"><label>Origin</label></td>
									<td valign="top" class="value">
										${shipmentInstance?.origin?.name}
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label>Destination</label></td>
									<td valign="top" class="value">
										${shipmentInstance?.destination?.name}
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.traveler.label" default="Traveler" /></label></td>
									<td valign="top" style="width: 30%;" class="value">
										${shipmentInstance?.carrier?.name}
											
									</td>
								</tr>	
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.freightForwarder.label" default="Freight forwarder" /></label></td>
									<td>&nbsp;</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.recipient.label" default="Recipient" /></label></td>
									<td>&nbsp;</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.loadingDate.label" default="Loading date" /></label></td>
									<td>&nbsp;</td>
								</tr>			
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.expectedShippingDate.label" default="Expected shipping date" /></label></td>
									<td valign="top"
										class="value ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}"
										nowrap="nowrap">
											<g:formatDate date="${shipmentInstance?.expectedShippingDate}" format="MMM dd, yyyy"/>
									</td>
								</tr>		
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.expectedShippingDate.label" default="Expected arrival date" /></label></td>
									<td valign="top"
										class="value ${hasErrors(bean: shipmentInstance, field: 'expectedDeliveryDate', 'errors')}"
										nowrap="nowrap">
											<g:formatDate date="${shipmentInstance?.expectedDeliveryDate}" format="MMM dd, yyyy"/>
									</td>
								</tr>					
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.billOfLadingNumber.label" default="BOL #" /></label></td>
									<td>&nbsp;</td>
								</tr>
								
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.airWaybillNumber.label" default="AWB #" /></label></td>
									<td>&nbsp;</td>
								</tr>
								
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.containerNumber.label" default="Container #" /></label></td>
									<td>&nbsp;</td>
								</tr>
								
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.sealNumber.label" default="Seal #" /></label></td>
									<td>&nbsp;</td>
								</tr>
								
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.flightNumber.label" default="Flight #" /></label></td>
									<td>&nbsp;</td>
								</tr>
								
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.totalValue.label" default="Total Value (USD)" /></label></td>
									<td valign="top"
										class="value ${hasErrors(bean: shipmentInstance, field: 'totalValue', 'errors')}"
										nowrap="nowrap">
										<g:if test="${shipmentInstance?.totalValue}">
											USD $<g:formatNumber number="${shipmentInstance?.totalValue}" format="###,##0.00"/>
										</g:if>
									</td>
								</tr>		
								
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.comment.label" default="Comments" /></label></td>
									<td>&nbsp;</td>
								</tr>
															
								<tr class="prop">
									<td class="name"><label>Suitcase Letter</label></td>
									<td class="value">
										<img src="${createLinkTo(dir: 'images/icons/silk', file: 'email_open.png') }" style="vertical-align:middle"> &nbsp;
										<g:link action="suitcase" event="reviewLetter">view letter</g:link>					
									</td>
								</tr>
								
								<tr class="prop">
									<td colspan="2">
										<g:render template="itemList" model="['shipmentInstance':shipmentInstance]" />											
									</td>
								</tr>
							</tbody>
						</table>					
					</div>
					
					<div class="buttons">
						<table>
							<tr>
								<td width="45%" style="text-align: right;">
									<g:submitButton name="back" value="Back"></g:submitButton> 
									<g:submitButton name="next" value="Next"></g:submitButton> 
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
