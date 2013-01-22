  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="shipping.enterTrackingDetails.label"/></title>         
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
			
	 		<g:if test="${addPerson}">
	 			<g:render template="addPerson" model="['personInstance':personInstance]"/>
	 		</g:if>
	 		<g:if test="${addShipper}">
	 			<g:render template="addShipper" model="['shipperInstance':shipperInstance]"/>
	 		</g:if>
			
			
			<g:form action="createShipment" method="post">
				<g:hiddenField name="id" value="${shipmentInstance?.id}"/>
				<g:render template="../shipment/summary" />	
				<g:render template="flowHeader" model="['currentState':'Tracking']"/>
				
				<div class="dialog box">
					<table>
	                    <tbody>		

	                    	<g:if test="${!shipmentWorkflow?.isExcluded('carrier')}">
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><warehouse:message
										code="shipping.traveler.label" /></label></td>
									<td valign="top" style="width: 30%;">
										<g:autoSuggest id="carrier" name="carrier" jsonUrl="${request.contextPath }/json/findPersonByName" 
											width="300"
											valueId="${shipmentInstance?.carrier?.id}" 
											valueName="${shipmentInstance?.carrier?.name}"/>		
											
										<g:link action="createShipment" event="addPerson" params="[target:'carrier']">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'user_add.png')}" alt="Add a person" class="middle"/>
										</g:link>
											
									</td>
								</tr>
							</g:if>	
							<g:if test="${!shipmentWorkflow?.isExcluded('shipmentMethod.shipper')}">
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><warehouse:message
										code="shipping.freightForwarder.label" /></label></td>
									<td valign="top" style="width: 30%;">
									
										<g:selectShipper id="shipperInput" 
											name="shipperInput.id" class="comboBox" value="${shipmentInstance?.shipmentMethod?.shipper?.id }" noSelection="['null':'']"/>
										
										<g:link action="createShipment" event="addShipper" params="[target:'shipper']">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry_add.png')}" alt="Add a shipper" class="middle"/>
										</g:link>	
									</td>
								</tr>
							</g:if>
							<g:if test="${!shipmentWorkflow?.isExcluded('recipient')}">
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><warehouse:message
										code="shipping.recipient.label" /></label></td>
									<td valign="top" style="width: 30%;">
										<g:autoSuggest id="recipient" name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName" 
											width="300"
											valueId="${shipmentInstance?.recipient?.id}" 
											valueName="${shipmentInstance?.recipient?.name}"/>		
											
										<g:link action="createShipment" event="addPerson" params="[target:'recipient']">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'user_add.png')}" alt="Add a person" class="middle"/>
										</g:link>
									</td>
								</tr>
							</g:if>
							
							<!-- list all the reference numbers valid for this workflow -->
							<g:each var="referenceNumberType" in="${shipmentWorkflow?.referenceNumberTypes}">
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><format:metadata obj="${referenceNumberType}" /></label></td>
									<td valign="top" style="width: 30%;">
										<g:textField name="referenceNumbersInput.${referenceNumberType?.id}" 
											size="20" class="text" value="${shipmentInstance?.referenceNumbers?.find({it.referenceNumberType.id == referenceNumberType.id})?.identifier}" /> 
									</td>
								</tr>
							</g:each>
											
							<g:if test="${!shipmentWorkflow?.isExcluded('statedValue')}">									
								<tr class="prop">
									<td valign="top" class="name"><label><warehouse:message
										code="shipping.statedValue.label" /></label></td>
									<td valign="top"
										class=" ${hasErrors(bean: shipmentInstance, field: 'statedValue', 'errors')}"
										nowrap="nowrap">
											<g:textField name="statedValue" value="${formatNumber(format: '##,##0.00', number: shipmentInstance.statedValue)}" 
												class="text" size="10"/> 
											<span class="fade"><warehouse:message code="shipping.statedValueExplanation.message"/></span>
									</td>
								</tr>	
							</g:if>			
							<g:if test="${!shipmentWorkflow?.isExcluded('totalValue')}">									
								<tr class="prop">
									<td valign="top" class="name"><label><warehouse:message
										code="shipping.totalValue.label" /></label></td>
									<td valign="top"
										class=" ${hasErrors(bean: shipmentInstance, field: 'totalValue', 'errors')}"
										nowrap="nowrap">
											<g:textField name="totalValue" value="${formatNumber(format: '##,##0.00', number: shipmentInstance.totalValue)}" 
											 	class="text" size="10"/> 
											<span class="fade"><warehouse:message code="shipping.totalValueExplanation.message"/></span>
									</td>
								</tr>	
							</g:if>			
							<g:if test="${!shipmentWorkflow?.isExcluded('additionalInformation')}">
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><warehouse:message
										code="default.comments.label"/></label></td>
									<td valign="top" style="width: 30%;">
										<g:textArea name="additionalInformation" value="${shipmentInstance?.additionalInformation}" cols="80" rows="3"/>
									</td>
								</tr>	
							</g:if>					
	                    </tbody>
               		</table>
				</div>
				<div class="buttons">
					<button name="_eventId_back">&lsaquo; <warehouse:message code="default.button.back.label"/></button>	
					<button name="_eventId_next"><warehouse:message code="default.button.next.label"/> &rsaquo;</button> 
					<button name="_eventId_save"><warehouse:message code="default.button.saveAndExit.label"/></button>
					<button name="_eventId_cancel"><warehouse:message code="default.button.cancel.label"/></button>						
				</div>
				
			</g:form>
		</div>
	</body>
</html>
