
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
		<g:form action="createRequest" method="post">
			<div class="dialog">
           		<g:render template="/requisition/summary" model="[requestInstance:requestInstance]"/>
           		<g:render template="header" model="['state':'enterRequestDetails']"/>
				<table>
					<tbody>
						
						<tr class='prop'>
							<td valign='middle' class='name'>
								<label for='status'><warehouse:message code="request.status.label"/></label>
							</td>
							<td valign='middle' class='value ${hasErrors(bean:requestInstance,field:'status','errors')} ${hasErrors(bean:requestInstance,field:'dateRequested','errors')}'>
								<format:metadata obj="${requestInstance.status}"/>
								<warehouse:message code="default.asof.label"/>									
								<format:datetime obj="${requestInstance?.dateRequested}"></format:datetime>
							</td>
						</tr>					
						
					
						<%-- 
						<tr class='prop'>
							<td valign='top' class='name'><label for='description'><warehouse:message code="default.description.label"/></label>
							</td>
							<td valign='top' class='value ${hasErrors(bean:request,field:'description','errors')}'>
								<input type="text" name='description' class="text" value="${requestInstance?.description?.encodeAsHTML()}" size="60"/>
							</td>
						</tr>
						--%>
						<tr class='prop'>
							<td valign='middle' class='name'>
								<label for='origin'><warehouse:message code="request.from.label"/></label>
							</td>
							<td valign='middle' class='value ${hasErrors(bean:requestInstance,field:'origin','errors')}'>
								
								<%-- 
								<g:selectRequestOrigin name="origin.id" class="text"
									optionKey="id" value="${requestingLocation?.id}" 
									noSelection="['null':'']"/>									
								--%>
								<g:set var="requestingLocation" value="${requestInstance.origin?:session.warehouse}"/>
								<format:metadata obj="${requestingLocation}"/>
								<g:hiddenField name="origin.id" value="${requestingLocation?.id}"/>
									
							</td>
						</tr>
						<g:if test="${requestInstance?.name }">
							<tr class='prop'>
								<td class='name middle'>
									<label for='status'><warehouse:message code="default.description.label"/></label>
								</td>
								<td class='value middle ${hasErrors(bean:requestInstance,field:'name','errors')}'>
									<g:textField name="name" value="${requestInstance?.name }" class="text" size="80"/>
								</td>
							</tr>					
						</g:if>						
						
						<tr class='prop'>
							<td class="name middle">
								<label for="destination"><warehouse:message code="request.for.label"/></label>
							</td>
							<td class='value middle ${hasErrors(bean:requestInstance,field:'destination','errors')}'>
								<%-- 
								<format:metadata obj="${requestingLocation}"/>
								<g:hiddenField name="destination.id" value="${requestingLocation?.id}"/>
								--%>
								<g:selectRequestDestination name="destination.id" class="text"
									placeholder="Destination"
									optionKey="id" value="${requestInstance?.destination?.id}" 
									noSelection="['null':'']"/>									
								
								<g:autoSuggestString 
									id="recipientProgram" 
									name="recipientProgram" 
									class="text"
									placeholder="Program"
									jsonUrl="${request.contextPath }/json/findPrograms" 
									value="${requestInstance?.recipientProgram}"
									label="${requestInstance?.recipientProgram}"/> 
									
								<g:autoSuggest 
									id="recipient" 
									name="recipient"  
									styleClass="text"
									jsonUrl="${request.contextPath }/json/findPersonByName"
									placeholder="Recipient"
									valueId="${requestInstance?.recipient?.id}" 
									valueName="${requestInstance?.recipient?.name}"/>	
							
							</td>
						</tr>
						<tr class='prop'>
							<td class='name middle'>
								<label for='requestedBy'><warehouse:message code="request.requestedBy.label"/></label>
							</td>
							<td
								class='value middle ${hasErrors(bean:requestInstance,field:'requestedBy','errors')}'>
								<g:set var="requestedBy" value="${requestInstance?.requestedBy?:session?.user }"/>
								<g:autoSuggest id="requestedBy" name="requestedBy" jsonUrl="${request.contextPath }/json/findPersonByName" 
									styleClass="text"
									placeholder="Requested by"
									valueId="${requestedBy?.id}" 
									valueName="${requestedBy?.name}"/>	
								
							</td>
						</tr>
						<%-- 
						<tr class='prop'>
							<td valign='top' class='name'>
								<label for='requestedBy'><warehouse:message code="request.createdBy.label"/></label>
							</td>
							<td valign='top'
								class='value ${hasErrors(bean:requestInstance,field:'createdBy','errors')}'>
								
								${requestInstance?.createdBy?.name}
								
							</td>
						</tr>
						--%>
						
					</tbody>
				</table>

				<div class="buttons">
					<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton> 
					&nbsp;
					
					<g:link action="createRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
				</div>
			</div>
		</g:form>
	</div>
</body>
</html>