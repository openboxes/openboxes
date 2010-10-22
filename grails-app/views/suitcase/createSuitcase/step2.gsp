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
		
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		
		<g:hasErrors bean="${shipmentInstance}">
            <div class="errors">
                <g:renderErrors bean="${shipmentInstance}" as="list" />
            </div>
        </g:hasErrors>		

		<g:form action="createSuitcase">
			<g:hiddenField name="id" value="${shipmentInstance?.id}" />
			<g:hiddenField name="version" value="${shipmentInstance?.version}" />
	
			<fieldset>
				<legend>Step 2. Detailed Information</legend>			
				<table>
					<tbody>
					
						<tr class="prop">
							<td valign="top" class="name"><label><g:message
								code="shipment.name.label" default="Shipment Number" /></label>
							</td>
							<td colspan="3" valign="top"
								class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
								${shipmentInstance?.shipmentNumber}
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
							<td valign="top" class="name"><label><g:message
								code="shipment.name.label" default="Name" /></label>
							</td>
							<td colspan="3" valign="top"
								class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
								${shipmentInstance?.name}
							</td>
						</tr>									
						<tr class="prop">
							<td valign="top" class="name"><label>Route</label></td>
							<td valign="top"
								class="value">
									${shipmentInstance?.origin?.name}
									&nbsp;							
									<img src="${createLinkTo(dir:'images/icons/silk',file: 'arrow_right.png')}" />
									&nbsp;							
									${shipmentInstance?.destination?.name}
							</td>
						</tr>
						<tr>
							<td colspan="2"><hr/></td>
						</tr>					
						<tr class="prop">
							<td valign="top" class="name"><label><g:message
								code="shipment.expectedShippingDate.label" default="Expected shipping date" /></label></td>
							<td valign="top"
								class=" ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}"
								nowrap="nowrap">
									<g:jqueryDatePicker id="expectedShippingDate" name="expectedShippingDate"
								value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy"/>
							</td>
						</tr>		
						<tr class="prop">
							<td valign="top" class="name"><label><g:message
								code="shipment.expectedShippingDate.label" default="Expected arrival date" /></label></td>
							<td valign="top"
								class=" ${hasErrors(bean: shipmentInstance, field: 'expectedDeliveryDate', 'errors')}"
								nowrap="nowrap">
									<g:jqueryDatePicker id="expectedDeliveryDate" name="expectedDeliveryDate"
								value="${shipmentInstance?.expectedDeliveryDate}" format="MM/dd/yyyy"/>
							</td>
						</tr>		
						<tr>
							<td colspan="2"><hr/></td>
						</tr>					
						
							
										
						<tr class="prop">	
							<td valign="top" class="name">
								<label><g:message code="shipment.suitcaseCount.label" default="How many suitcases?" /></label>
							</td>
							<td valign="top" class="value">							
								<g:select name="suitcaseCount" from="${1..5}" value="${params?.suitcaseCount ? params.suitcaseCount : 1}" disabled="true"
							          noSelection="['':'']" "/> &nbsp; <span class="fade">NOTE: System currently supports one (1) suitcase per shipment.</span>
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label><g:message
								code="shipment.totalValue.label" default="Total value of contents" /></label></td>
							<td valign="top"
								class=" ${hasErrors(bean: shipmentInstance, field: 'totalValue', 'errors')}"
								nowrap="nowrap">
									<g:textField 
										name="totalValue" 
										value="${formatNumber(number: (shipmentInstance?.totalValue)?shipmentInstance?.totalValue:0.00, format: '#,##0.00')}" 
										size="7" />  <span class="fade">(USD $#,###.##)</span>
							</td>
						</tr>		
						
						
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
							<td valign="top" class="name"><label><g:message
								code="shipment.totalValue.label" default="Flight Information" /></label></td>
							<td valign="top"
								class=" ${hasErrors(bean: shipmentInstance, field: 'flightInformation', 'errors')}"
								nowrap="nowrap">
									<g:textField name="flightInformation" value="${shipmentInstance?.flightInformation}" /> 
									<span class="fade">(e.g. AA 2292)</span>
							</td>
						</tr>		
						
						
						<%-- 
						<tr class="prop">
							<td valign="top" class="name"><label><g:message
								code="shipment.contents.label" default="Contents" /></label>
							</td>
							<td colspan="3" valign="top"
								class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
								<g:each var="containerInstance" in="${shipmentInstance?.containers}">
									${containerInstance?.containerType?.name} ${containerInstance?.name} &nbsp
								</g:each>
							</td>
						</tr>
						--%>	
						<%-- 								
						<tr class="prop">
							<td valign="top" class="name" style="width: 10%;"><label><g:message
								code="shipment.shipper.label" default="Origin" /></label>
							</td>
							<td valign="top" style="width: 30%; line-height: 1.5em">											
								
									${shipmentInstance?.origin?.name}
									<g:hiddenField name="origin.id" value="${shipmentInstance?.origin?.id}" />
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name" style="width: 10%;"><label><g:message
								code="shipment.shipper.label" default="Destination" /></label>
							</td>
							<td valign="top" style="width: 30%; line-height: 1.5em">											
								<g:autoSuggest id="destination" name="destination" 
									jsonUrl="/warehouse/json/findWarehouseByName" 
									valueId="${shipmentInstance?.destination?.id}" 
									valueName="${shipmentInstance?.destination?.name}"/>												
							</td>
						</tr>						

						--%>
						<%--
						<tr class="prop">
							<td class="name"  style="width: 10%;">
								<label><g:message code="shipment.recipient.label" default="Recipient" /></label>
							</td>
							<td class="value" style="width: 30%;">		
								<g:if test="${shipmentInstance?.recipient}">
									<g:autoSuggest id="recipient" name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
										valueId="${shipmentInstance?.recipient?.id}" 
										valueName="${shipmentInstance?.recipient?.firstName} ${shipmentInstance?.recipient?.lastName}"/>												
								</g:if>
								<g:else>
									<g:autoSuggest id="recipient" name="recipient" jsonUrl="/warehouse/json/findPersonByName" width="200" />	
								</g:else>	
							</td>
						</tr>
						--%>						
						
						
						<tr class="prop">
							<td class=""></td>
							<td>
								<div class="">
								    <g:submitButton name="next" value="Next"></g:submitButton>
								    <g:submitButton name="back" value="Back"></g:submitButton>
								    <g:link action="createSuitcase" event="cancel" id="${shipmentInstance?.id}">Cancel</g:link>
								</div>
	
							</td>
						</tr>
					</tbody>
				</table>
			</fieldset>
		</g:form>
	</div>
</body>
</html>