
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="default.show.label" args="[entityName]" /></title>        
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="shipping.deleteShipment.label"/></content>
</head>

<body>    
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
				
		<table>
			<tr>
				<td width="75%">
					<g:form action="deleteShipment" method="post">				
						<fieldset>
							<g:render template="summary"/>						
							<g:hiddenField name="id" value="${shipmentInstance?.id}" />
							<g:hiddenField name="version" value="${shipmentInstance?.version}" />

							<div id="details" class="section">
								<table>
									<tbody>
										<%-- 
										<tr class="prop">
											<td valign="top" class="name"><label><warehouse:message
												code="shipping.shipmentNumber.label" /></label>
											</td>
											<td colspan="3" valign="top"
												class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
												<span style="line-height: 1.5em">${shipmentInstance?.shipmentNumber}</span>
											</td>
										</tr>
										--%>
										<tr class="prop">
											<td valign="middle" class="name"><label><warehouse:message
												code="default.type.label" default="Type" /></label></td>
											<td valign="middle" class="value" nowrap="nowrap">
												<format:metadata obj="${shipmentInstance?.shipmentType}"/>
											</td>
										</tr>							
										<tr class="prop">
											<td valign="top" class="name"><label><warehouse:message
												code="default.name.label" default="Name" /></label>
											</td>
											<td colspan="3" valign="top"
												class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
												${shipmentInstance?.name}
											</td>
										</tr>									
										<tr class="prop">
											<td class="name"></td>
											<td class="value">
												<warehouse:message code="shipping.confirm.deleteShipment.message"/>
												<div class="buttons">
													<button type="submit" class="positive"><img
													src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
													 /> <warehouse:message code="default.button.delete.label"/></button>
													
													<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
														 /> <warehouse:message code="default.button.cancel.label"/></g:link>
													
												</div>
												
											</td>
										</tr>
									</tbody>
								</table>
							</div>
						</fieldset>
					</g:form>
				</td>		
			</tr>
		</table>
	</div>
</body>
</html>
