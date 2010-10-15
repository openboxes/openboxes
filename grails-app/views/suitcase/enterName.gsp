
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
		
		<g:hasErrors bean="${suitcaseCommand}">
            <div class="errors">
                <g:renderErrors bean="${suitcaseCommand}" as="list" />
            </div>
        </g:hasErrors>		
				
		<div class="dialog">
	              		
	        <g:render template="progressBar"/>
	        
			<div id="wizard" class="dialog">			
				<fieldset>
					<legend>
						<g:message code="shipment.wizard.step${suitcaseCommand?.stepNumber}.label" default="Step ${suitcaseCommand.stepNumber}" />
					</legend>				
				
					<g:form action="processName" method="post">
		                <g:hiddenField name="id" value="${suitcaseCommand?.id}" />
		                <g:hiddenField name="stepNumber" value="${suitcaseCommand?.stepNumber}" />
					
	                	<div class="prop">
							<span class="left">				
								<g:if test="suitcaseCommand?.shipmentType">
									<img src="${createLinkTo(dir:'images/icons/shipmentType', file: suitcaseCommand?.shipmentType?.name?.toLowerCase() + '.png')}"
										style="vertical-align: middle;"/><br/>
										
										<g:hiddenField name="shipmentType.id" value="${suitcaseCommand?.shipment?.shipmentType?.id}" />
										<span class="fade">${suitcaseCommand?.shipmentType?.name}</span>
								</g:if>	
								<g:else>
									<label><g:message code="shipment.shipmentType.label" default="Shipment Type" /></label>	
									<g:each var="shipmentType" in="${org.pih.warehouse.shipping.ShipmentType.list()}">
										<span class="radio"><input type="radio" name="shipmentType.id" value="${suitcaseCommand?.shipmentType.id}" 
											${shipmentType?.id == suitcaseCommand?.shipmentType?.id ? 'checked' : ''}/> ${suitcaseCommand?.shipmentType?.name}
										</span>
									</g:each>
								</g:else>
							</span>
							<span class="right">				
								<label><g:message code="shipment.name.label" default="Nickname" /></label>
								<g:textField name="name" size="30" value="${suitcaseCommand?.name}" />
							</span>		
						</div>					
						<br clear="all"/>	
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
