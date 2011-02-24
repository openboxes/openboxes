  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Enter Tracking Details</title>         
    </head>
    <body>
        <div class="body">
        
        	<g:if test="${message}">
				<div class="message">${message}</div>
			</g:if> 
			
			<g:hasErrors bean="${shipmentInstance}">
				<div class="errors"><g:renderErrors bean="${shipmentInstance}" as="list" /></div>
			</g:hasErrors> 
			
			<g:render template="flowHeader" model="['currentState':'Traveler']"/>
			
			<g:form action="createShipment" method="post">
				<g:hiddenField name="id" value="${shipmentInstance?.id}"/>
				<fieldset>
					<legend>Step 2&nbsp;Enter tracking details</legend>				
						
					<g:render template="../shipment/summary" />	
					
								
					
					<div class="dialog">
						<table>
		                    <tbody>
		                    
		                    <%-- 
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
									<td valign="top" class="name"><label><g:message code="shipment.type.label" default="Type" /></label></td>
									<td valign="top"
										class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentType', 'errors')}">
										<g:if test="${shipmentInstance?.shipmentType}">
											<g:hiddenField name="shipmentType.id" value="${shipmentInstance?.shipmentType?.id}" />
											${shipmentInstance?.shipmentType?.name }																	
										</g:if>
										<g:else>
											<g:select
												name="shipmentType.id"
												from="${org.pih.warehouse.shipping.ShipmentType.list()}"
												optionKey="id" optionValue="name" value="${shipmentInstance?.shipmentType?.id}" />								
										</g:else>
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='name'><label><g:message code="shipment.name.label" default="Name" /></label>
									</td>
									<td valign='top' class='value ${hasErrors(bean:shipmentInstance,field:'name','errors')}'>
										${shipmentInstance?.name?.encodeAsHTML()}
									</td>
								</tr>
								--%>
								  
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.traveler.label" default="Traveler" /></label></td>
									<td valign="top" style="width: 30%;">
										<g:autoSuggest id="carrier" name="carrier" jsonUrl="/warehouse/json/findPersonByName" 
											width="180" size="30"
											valueId="${shipmentInstance?.carrier?.id}" 
											valueName="${shipmentInstance?.carrier?.name}"/>		
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.freightForwarder.label" default="Freight Forwarder" /></label></td>
									<td valign="top" style="width: 30%;">
										&nbsp;	
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.recipient.label" default="Recipient" /></label></td>
									<td valign="top" style="width: 30%;">
										<g:autoSuggest id="recipient" name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
											width="180" size="30"
											valueId="${shipmentInstance?.recipient?.id}" 
											valueName="${shipmentInstance?.recipient?.name}"/>		
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.bol.label" default="BOL #" /></label></td>
									<td valign="top" style="width: 30%;">
										&nbsp;	
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.bol.label" default="AWB #" /></label></td>
									<td valign="top" style="width: 30%;">
										&nbsp;	
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.container.label" default="Container #"  /></label></td>
									<td valign="top" style="width: 30%;">
										&nbsp;	
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.seal.label" default="Seal #" /></label></td>
									<td valign="top" style="width: 30%;">
										&nbsp;	
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.totalValue.label" default="Flight #" /></label></td>
									<td valign="top"
										class=" ${hasErrors(bean: shipmentInstance, field: 'flightInformation', 'errors')}"
										nowrap="nowrap">
											<g:textField name="flightInformation" size="10" value="${shipmentInstance?.flightInformation}" /> 
											<span class="fade">(e.g. AA 2292)</span>
									</td>
								</tr>										
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.totalValue.label" default="Total value" /></label></td>
									<td valign="top"
										class=" ${hasErrors(bean: shipmentInstance, field: 'totalValue', 'errors')}"
										nowrap="nowrap">
											<g:textField name="totalValue" value="${formatNumber(format: '##,##0.00', number: shipmentInstance.totalValue)}" size="10"/> 
											<span class="fade">USD</span>
									</td>
								</tr>				
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.comments.label" default="Comments" /></label></td>
									<td valign="top" style="width: 30%;">
										&nbsp;	
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
