
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">
		Edit Shipment
	</content>
</head>

<body>
	<div class="body">
	
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>	



<table>
	<tr>
		<td colspan="2">		
			<table>
				<tr>
					<td>
						<div style="padding-bottom: 10px;">
							<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
							&raquo; <span style="font-size: 90%">Edit Details</span>
						</div>										
					</td>
				</tr>
			</table>		
		</td>	
	</tr>
	<tr>
		<td>
			<g:form action="update" method="post">
				<fieldset>			
					
					<g:render template="summary"/>
				
				
					<g:hiddenField name="id" value="${shipmentInstance?.id}" />
					<g:hiddenField name="version" value="${shipmentInstance?.version}" />
					<table>
						<tbody>
							<tr >
								<td>&nbsp;</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.expectedShippingDate.label" default="Expected shipping date" /></label></td>
								<td valign="top"
									class=" ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}"
									nowrap="nowrap">
										<g:jqueryDatePicker name="expectedShippingDate" 
											value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy" />
								</td>
							</tr>		
							<tr class="prop">
								<td class="name"></td>
								<td>
									<div class="buttons">
										<button type="submit" class="positive"><img 
											src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" /> Save</button>
										<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
											alt="Cancel" /> Cancel</g:link>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</fieldset>
			</g:form>
			
		</td>		
		<td width="20%">
			<g:render template="sidebar"/>	
		</td>				
	</tr>
</table>
			
			
	</div>
</body>
</html>
