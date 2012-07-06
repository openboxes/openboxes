
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="request.enterRequestDetails.label"/></title>
</head>
<body>
	<div class="nav">
		<span class="linkButton"><a href="${createLinkTo(dir:'')}"><warehouse:message code="default.home.label"/></a>
		</span>
	</div>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${requestInstance}">
			<div class="errors">
				<g:renderErrors bean="${requestInstance}" as="list" />
			</div>
		</g:hasErrors>
		<g:each var="requestItem" in="${requestItems}" status="i">
			<g:hasErrors bean="${requestItem}">
				<div class="errors">
					<g:renderErrors bean="${requestItem}" as="list" />
				</div>
			</g:hasErrors>
		</g:each>
		
		<g:set var="requestingLocation" value="${requestInstance.destination?:session.warehouse}"/>
		
		<g:form action="createRequest" method="post">
			<div class="dialog">
			
				<fieldset>
            		<g:render template="/request/summary" model="[requestInstance:requestInstance]"/>
			
					<table>
						<tbody>
						
							<tr class='prop'>
								<td valign='top' class='name'><label for='description'><warehouse:message code="default.description.label"/>:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:request,field:'description','errors')}'>
									<input type="text" name='description' class="text" value="${requestInstance?.description?.encodeAsHTML()}" size="60"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'><warehouse:message code="request.from.label"/>:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:requestInstance,field:'origin','errors')}'>
									<g:selectRequestSupplier name="origin.id" class="comboBox text"
										optionKey="id" value="${requestInstance?.origin?.id}" 
										noSelection="['null':'...']"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination"><warehouse:message code="request.for.label"/>:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:requestInstance,field:'destination','errors')}'>
									<format:metadata obj="${requestingLocation}"/>
									<g:hiddenField name="destination.id" value="${requestingLocation?.id}"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='requestedBy'><warehouse:message code="request.requestedBy.label"/>:</label></td>
								<td valign='top'
									class='value ${hasErrors(bean:requestInstance,field:'requestedBy','errors')}'>
									${requestInstance?.requestedBy?.name}
								</td>
							</tr>
						</tbody>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton> 
						&nbsp;
						
						<g:link action="createRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
					</div>
				</fieldset>
			</div>
		</g:form>
	</div>
	<g:comboBox />	
</body>
</html>