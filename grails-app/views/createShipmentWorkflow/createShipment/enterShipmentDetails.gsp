  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Enter Shipment Details</title>         
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

			<g:render template="flowHeader" model="['currentState':'Details']"/>
			
           	<g:form action="createShipment" method="post">
				<g:hiddenField name="id" value="${shipmentInstance?.id}"/>
				<fieldset>
					<legend>Step 1&nbsp; Enter shipment details</legend>
					
					<g:render template="../shipment/summary" />	
					
               		<div class="dialog">
		                <table>
		                    <tbody>
		                    	<g:if test="${!shipmentWorkflow?.isExcluded('shipmentType')}">
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
								</g:if>
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='name'><g:message code="shipment.name.label" default="Name" /></label>
									</td>
									<td valign='top' class='value ${hasErrors(bean:shipmentInstance,field:'name','errors')}'>
										<input type="text" name='name' value="${shipmentInstance?.name?.encodeAsHTML()}" size="40"/>
									</td>
								</tr>  
								<tr class="prop">
									<td valign="top" class="name"><label><g:message code="shipment.origin.label" default="Origin" /></label></td>
									<td valign="top"
										class="value ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">								
										<g:select name="origin.id" from="${org.pih.warehouse.core.Location.list().sort()}" optionKey="id" value="${shipmentInstance?.origin?.id ? shipmentInstance?.origin?.id : session.warehouse.id}" style="width: 180px" />							
										<br/>
										<g:link controller="location" action="create" target="_blank"><span class="small">Add a New Location</span></g:link>							
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label><g:message code="shipment.destination.label" default="Destination" /></td>
									<td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'destination', 'errors')}">
										<g:select name="destination.id" from="${org.pih.warehouse.core.Location.list().sort()}" optionKey="id" value="${shipmentInstance?.destination?.id}" style="width: 180px" />	
									</td>
								</tr>
								
								<!--  
								<tr class="prop">
									<td valign="top" class="name"><label><g:message code="shipment.loadingDate.label" default="Loading Date" /></td>
									<td>&nbsp;</td>
								</tr>
								-->
								
								<tr class="prop">
									<td valign="top" class="name"><label><g:message code="shipment.expectedShippingDate.label" default="Expected to ship on" /></td>
									<td class="value ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}"> 
										<g:jqueryDatePicker id="expectedShippingDate" name="expectedShippingDate"
											value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy"/>							
									
									</td>
								</tr>
								<g:if test="${!shipmentWorkflow?.isExcluded('expectedDeliveryDate')}">
								<tr class="prop">
									<td valign="top" class="name"><label><g:message code="shipment.expectedDeliveryDate.label" default="Expected to arrive on" /></td>
									<td class="value ${hasErrors(bean: shipmentInstance, field: 'expectedDeliveryDate', 'errors')}"> 
										<g:jqueryDatePicker id="expectedDeliveryDate" name="expectedDeliveryDate"
											value="${shipmentInstance?.expectedDeliveryDate}" format="MM/dd/yyyy"/>
									</td>
								</tr>
								</g:if>
		                    </tbody>
		               </table>
					</div>
					<div class="buttons">
						<table>
							<tr>
								<td width="45%" style="text-align: right;">
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
