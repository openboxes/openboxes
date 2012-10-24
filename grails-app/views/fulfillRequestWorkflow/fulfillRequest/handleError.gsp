<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>System Error</title>

</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${command}">
			<div class="errors">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>
		
		
		<div class="dialog">
			<g:form action="fulfillRequest" autocomplete="false">
				<fieldset>				
					<g:render template="../request/summary" model="[requestInstance:command?.request]"/>				
					<g:render template="progressBar" model="['state':'handleError']"/>		
				
					<table>
						<tr>
							<td style="padding: 0; margin: 0;" class="middle">														
								<div style="padding: 25px; height: 300px;" class="left middle">
					
									<h2>
										<img src="${resource(dir: 'images/icons/silk', file: 'error.png')}" /> 
										<warehouse:message code="fulfillRequestWorkflow.handleError.message"/>
									</h2>
									<div class="error">
										<g:if test="${rootCauseException?.message }">
											<b>${rootCauseException?.class.name }</b> ${rootCauseException?.message }										
										</g:if>
									</div>
									<div class="box">
										<g:link controller="requisition" action="list">
											${warehouse.message(code: 'request.returnToList.label', default: 'Return to request list')} 
										</g:link>																			
									</div>
								</div>
							</td>
						</tr>
					</table>
				</fieldset>
			</g:form>
		</div>
	</div>
</body>
</html>