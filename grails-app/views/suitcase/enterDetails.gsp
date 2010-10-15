
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
				
		<div class="dialog">
		
			<g:render template="progressBar"/>
			
			<div id="wizard" class="dialog">			
				<fieldset>
					<legend>
						<g:message code="shipment.wizard.step${params?.stepNumber}.label" default="Step ${params.stepNumber}" />
					</legend>				
				
					<g:form action="processDetails" method="post">
		                <g:hiddenField name="id" value="${shipmentInstance?.id}" />
		                <g:hiddenField name="stepNumber" value="${params?.stepNumber}" />
		                <g:hiddenField name="version" value="${shipmentInstance?.version}" />					
					
					
						<div class="prop">	
							<label><g:message code="shipment.suitcaseCount.label" default="How many suitcases?" /></label>
							<g:select name="suitcaseCount" from="${1..5}" value="${suitcaseCommand?.suitcaseCount}"
						          noSelection="['':'']"/>
						</div>
						<div class="prop">	
							<label><g:message code="shipment.origin.label" default="Where is it coming from?" /></label>											
	                       	${session.warehouse.name}
	                       	<g:hiddenField name="origin.id" value="${session.warehouse.id}" />						
						</div>
						<div class="prop">	
							<label><g:message code="shipment.destination.label" default="Where is it going?" /></label>											
                           	<g:autoSuggest id="destination" name="destination"
								valueId="${suitcaseCommand?.shipment?.destination?.id}" valueName="${suitcaseCommand?.shipment?.destination?.name}"
								jsonUrl="/warehouse/json/findWarehouseByName" width="300" />						
						</div>	
						<div class="prop">
							<label><g:message code="shipment.initialStatus.label" default="What is the status?" /></label>
							<g:select name="eventType.id" from="${eventTypes}" optionKey="id" optionValue="${{it?.name}}" value="" noSelection="['0':'']" />
						</div>
						<div class="prop">
                           	<label><g:message code="shipment.traveler.label" default="Who is the traveler?" /></label>
							<g:autoSuggest id="traveler" name="traveler" jsonUrl="/warehouse/json/findPersonByName" 
								valueId="${suitcaseCommand?.traveler?.id}" 
								valueName="${suitcaseCommand?.traveler?.name}"/>												
						</div>
						<div class="prop">
                           	<label><g:message code="shipment.expectedShippingDate.label" default="When is it expected to be shipped?" /></label>
							<g:jqueryDatePicker id="expectedShippingDate" name="expectedShippingDate"
								value="${suitcaseCommand?.expectedShippingDate}" format="MM/dd/yyyy"/>							
						</div>
						<div class="prop">
                           	<label><g:message code="shipment.expectedDeliveryDate.label" default="When is it expected to be delivered?" /></label>
							<g:jqueryDatePicker id="expectedDeliveryDate" name="expectedDeliveryDate"
								value="${suitcaseCommand?.expectedDeliveryDate}" format="MM/dd/yyyy"/>							
						</div>
						<div class="prop">
                           	<label><g:message code="shipment.flightNumber.label" default="Airline and flight number" /></label>
							<g:textField name="flightNumber" value="${suitcaseCommand?.flightNumber}" /> 
							<span class="fade">e.g. AA 1920</span>
						
						</div>
						<div class="prop">
                           	<label><g:message code="shipment.value.label" default="Total value" /></label>
							<g:textField name="totalValue" value="${suitcaseCommand?.totalValue}" />
						</div>
						<div class="prop">		
							<div class="buttons" style="text-align: right;">
								<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" alt="next" /> Next</button>											
							</div>					
						</div>	
					</g:form>

				</fieldset>

			</div>
		</div>
	</div>

											
</body>
</html>
