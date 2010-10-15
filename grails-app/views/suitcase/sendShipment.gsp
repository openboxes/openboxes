
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
						<g:message code="shipment.wizard.step${suitcaseCommand?.stepNumber}.label" default="Step ${params.stepNumber}" />
					</legend>				
				
					<g:form action="step1" method="post">
		                <g:hiddenField name="id" value="${suitcaseCommand?.shipment?.id}" />
		                <g:hiddenField name="stepNumber" value="${suitcaseCommand?.stepNumber}" />
		                <g:hiddenField name="version" value="${suitcaseCommand?.shipment?.version}" />
					
	                	<div class="prop">
						
						</div>					
						<div class="prop">		
							<div class="buttons" style="text-align: right;">								
							<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" alt="next" /> Send</button>								
							</div>					
						</div>	
					</g:form>

				</fieldset>
			</div>
		</div>
	</div>

											
</body>
</html>
