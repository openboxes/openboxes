  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="shipping.enterShipmentDetails.label"/></title>         
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
			
	 		<g:if test="${addLocation}">
	 			<g:render template="addLocation" model="['locationInstance':locationInstance]"/>
	 		</g:if>
			
			
           	<g:form action="createShipment" method="post">
				<g:hiddenField name="id" value="${shipmentInstance?.id}"/>
				
					<g:render template="../shipment/summary" />	
					<g:render template="flowHeader" model="['currentState':'Details']"/>
					
               		<div class="dialog box">
		                <table>
		                    <tbody>
		                    	<g:if test="${!shipmentWorkflow?.isExcluded('shipmentType')}">
									<tr class="prop">
										<td valign="top" class="name"><label><warehouse:message code="default.type.label" /></label></td>
										<td valign="top"
											class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentType', 'errors')}">
											<g:if test="${shipmentInstance?.shipmentType}">
												<g:hiddenField name="shipmentType.id" value="${shipmentInstance?.shipmentType?.id}" />
												<format:metadata obj="${shipmentInstance?.shipmentType}"/>																	
											</g:if>
											<g:else>
												<g:select
													name="shipmentType.id"
													from="${org.pih.warehouse.shipping.ShipmentType.list()}"
													noSelection="['null':'']"
													optionKey="id" optionValue="${{format.metadata(obj:it)}}" 
													value="${shipmentInstance?.shipmentType?.id}" />
											</g:else>
										</td>
									</tr>
								</g:if>
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='name'><warehouse:message code="shipping.name.label"/></label>
									</td>
									<td valign='top' class='value ${hasErrors(bean:shipmentInstance,field:'name','errors')}'>
										<input type="text" name='name' value="${shipmentInstance?.name?.encodeAsHTML()}" size="40"/>
									</td>
								</tr>  
								<tr class="prop">
									<td valign="top" class="name"><label><warehouse:message code="default.origin.label" /></label></td>
									<td valign="top"
										class="value ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">								
										<g:selectShipmentOrigin name="origin.id" class="comboBox"
											optionKey="id" 
											value="${shipmentInstance?.origin?.id ? shipmentInstance?.origin?.id : params.type == 'OUTGOING' ? session.warehouse.id : ''}" 
											noSelection="['null':'']" />							
											
										<g:link action="createShipment" event="addLocation" params="[target:'origin']">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'building_add.png')}" alt="Add a location" class="middle"/>	
										</g:link>		
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label><warehouse:message code="default.destination.label" /></td>
									<td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'destination', 'errors')}">
										<g:selectShipmentDestination class="comboBox" name="destination.id" 
											optionKey="id" value="${shipmentInstance?.destination?.id ? shipmentInstance?.destination?.id : (params.type == 'INCOMING') ? session.warehouse.id : ''}" 
											noSelection="['null':'']" />	
										<g:link action="createShipment" event="addLocation" params="[target:'origin']">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'building_add.png')}" alt="Add a location" class="middle"/>	
										</g:link>		
									</td>
								</tr>
								
								<!--  
								<tr class="prop">
									<td valign="top" class="name"><label><warehouse:message code="shipment.loadingDate.label" default="Loading Date" /></td>
									<td>&nbsp;</td>
								</tr>
								-->
								
								<tr class="prop">
									<td valign="top" class="name"><label><warehouse:message code="shipping.expectedShippingDate.label"/></td>
									<td class="value ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}"> 
										<g:jqueryDatePicker id="expectedShippingDate" name="expectedShippingDate"
											value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy"/>							
									
									</td>
								</tr>
								<g:if test="${!shipmentWorkflow?.isExcluded('expectedDeliveryDate')}">
									<tr class="prop">
										<td valign="top" class="name"><label><warehouse:message code="shipping.expectedDeliveryDate.label"/></td>
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
						<button name="_eventId_back" disabled>&lsaquo; <warehouse:message code="default.button.back.label"/></button>	
						<button name="_eventId_next"><warehouse:message code="default.button.next.label"/> &rsaquo;</button> 
						<button name="_eventId_save"><warehouse:message code="default.button.saveAndExit.label"/></button>
						<button name="_eventId_cancel"><warehouse:message code="default.button.cancel.label"/></button>						
					</div>

            </g:form>
        </div>
    </body>
</html>
