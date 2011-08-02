  
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

			
           	<g:form action="createShipment" method="post">
				<g:hiddenField name="id" value="${shipmentInstance?.id}"/>
				<fieldset>
					<g:render template="../shipment/summary" />	
					<g:render template="flowHeader" model="['currentState':'Details']"/>

					
					
               		<div class="dialog">
		                <table>
		                    <tbody>
		                    	<g:if test="${!shipmentWorkflow?.isExcluded('shipmentType')}">
									<tr class="prop">
										<td valign="top" class="name"><label><warehouse:message code="shipment.type.label" default="Type" /></label></td>
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
										<label for='name'><warehouse:message code="shipment.name.label" default="Name" /></label>
									</td>
									<td valign='top' class='value ${hasErrors(bean:shipmentInstance,field:'name','errors')}'>
										<input type="text" name='name' value="${shipmentInstance?.name?.encodeAsHTML()}" size="40"/>
									</td>
								</tr>  
								<tr class="prop">
									<td valign="top" class="name"><label><warehouse:message code="shipment.origin.label" default="Origin" /></label></td>
									<td valign="top"
										class="value ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">								
										<g:select name="origin.id" from="${org.pih.warehouse.core.Location.list().sort()}" optionKey="id" value="${shipmentInstance?.origin?.id ? shipmentInstance?.origin?.id : session.warehouse.id}" style="width: 180px" />							
										<br/>
										<g:link controller="location" action="edit" target="_blank"><span class="small">Add a New Location</span></g:link>							
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label><warehouse:message code="shipment.destination.label" default="Destination" /></td>
									<td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'destination', 'errors')}">
										<g:select name="destination.id" from="${org.pih.warehouse.core.Location.list().sort()}" optionKey="id" value="${shipmentInstance?.destination?.id}" style="width: 180px" />	
									</td>
								</tr>
								
								<!--  
								<tr class="prop">
									<td valign="top" class="name"><label><warehouse:message code="shipment.loadingDate.label" default="Loading Date" /></td>
									<td>&nbsp;</td>
								</tr>
								-->
								
								<tr class="prop">
									<td valign="top" class="name"><label><warehouse:message code="shipment.expectedShippingDate.label" default="Expected to ship on" /></td>
									<td class="value ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}"> 
										<g:jqueryDatePicker id="expectedShippingDate" name="expectedShippingDate"
											value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy"/>							
									
									</td>
								</tr>
								<g:if test="${!shipmentWorkflow?.isExcluded('expectedDeliveryDate')}">
									<tr class="prop">
										<td valign="top" class="name"><label><warehouse:message code="shipment.expectedDeliveryDate.label" default="Expected to arrive on" /></td>
										<td class="value ${hasErrors(bean: shipmentInstance, field: 'expectedDeliveryDate', 'errors')}"> 
											<g:jqueryDatePicker id="expectedDeliveryDate" name="expectedDeliveryDate"
												value="${shipmentInstance?.expectedDeliveryDate}" format="MM/dd/yyyy"/>
										</td>
									</tr>
								</g:if>
		                    </tbody>
		               </table>
					</div>
					<div class="">
						<table>
							<tr>
								<td width="100%" style="text-align: right;">
									<button type="submit" name="_eventId_back" disabled>&lsaquo; Back</button>	
									<button type="submit" name="_eventId_next">Next &rsaquo;</button> 
									<button type="submit" name="_eventId_save">Save & Exit</button>
									<button type="submit" name="_eventId_cancel">Cancel</button>						
								</td>
							</tr>
						</table>
					</div>
				</fieldset>
            </g:form>
        </div>
    </body>
</html>
