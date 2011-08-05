
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="request.enterRequestDetails.label"/></title>
</head>
<body>
	<div class="nav">
		<span class="menuButton"><a href="${createLinkTo(dir:'')}"><warehouse:message code="default.home.label"/></a>
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
									<input type="text" name='description' value="${requestInstance?.description?.encodeAsHTML()}" size="30"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'><warehouse:message code="request.from.label"/>:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:requestInstance,field:'origin','errors')}'>
									<g:select name="origin.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" optionKey="id" value="${requestInstance?.origin?.id}" noSelection="['':'']"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination"><warehouse:message code="request.for.label"/>:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:requestInstance,field:'destination','errors')}'>
									<g:if test="${requestInstance.destination }">
										${requestInstance?.destination?.name }
										<g:hiddenField name="destination.id" value="${requestInstance?.destination?.id}"/>									
									</g:if>
									<g:else>
										${session.warehouse?.name }
										<g:hiddenField name="destination.id" value="${session.warehouse?.id}"/>									
									</g:else>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='requestedBy'><warehouse:message code="request.requestedBy.label"/>:</label></td>
								<td valign='top'
									class='value ${hasErrors(bean:requestInstance,field:'requestedBy','errors')}'>
									<%-- 
									<g:select class="combobox" name="requestedBy.id" from="${org.pih.warehouse.core.Person.list().sort{it.lastName}}" optionKey="id" value="${request?.requestedBy?.id}" noSelection="['':'']"/>
									--%>
									${requestInstance?.requestedBy?.name}
								</td>
							</tr>
							<%-- 
							<tr class='prop'>
								<td valign='top' class='name'><label for='dateOrdered'><warehouse:message code="request.orderedOn.label"/>:</label></td>
								<td valign='top'
									class='value ${hasErrors(bean:request,field:'dateOrdered','errors')}'>								
									<g:jqueryDatePicker 
										id="dateOrdered" 
										name="dateOrdered" 
										value="${request?.dateOrdered }" 
										format="MM/dd/yyyy"
										size="8"
										showTrigger="false" />								
								</td>
							</tr>							
							--%>
						</tbody>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton> 
						<g:link action="createRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
					</div>
					
					
				</fieldset>
			</div>
		</g:form>
	</div>
	<g:comboBox />	
</body>
</html>