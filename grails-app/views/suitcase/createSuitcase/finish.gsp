

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
			<fieldset>
				<legend>Confirmation</legend>
				<g:hiddenField name="id" value="${shipmentInstance?.id}"/>				
				<div class="">				
					<table>
						<tbody>							
							<tr class="prop">
	                            <td valign="top" class="name"></td>                            
	                            <td valign="top" class="value">
									Congratulations, your suitcase has been sent!  <br/><br/>
									
									<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}">Go to shipment</g:link>
                                </td>
	                        </tr>  	        
	                        
	                        <%-- 
							<tr class="prop">
								<td class=""></td>
								<td class="">
									<div class="buttons">
									    <g:submitButton name="finish" value="Finish"></g:submitButton>
									    <g:submitButton name="back" value="Back"></g:submitButton>
									    <g:submitButton name="cancel" value="Cancel"></g:submitButton>
								    </div>
								</td>
							</tr>
							--%>
						</tbody>
					</table>										
				</div>
			</fieldset>
		</g:form>		
	</div>
</body>
</html>