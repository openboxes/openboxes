
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
					<div style="padding-bottom: 10px;">
						<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
						 &nbsp; &raquo; &nbsp; 
						<span style="font-size: 90%">Add Comment</span>
					</div>					
				</td>
			</tr>		
			<tr>
				<td width="75%">
					<fieldset>
						<g:render template="summary" />

						
							<g:form action="sendShipment" method="POST">
								<g:hiddenField name="id" value="${shipmentInstance?.id}" />
								<table>
									<tbody>
									
									<tr class="prop">
										<td valign="top" class="name" style="width: 10%;"><label><g:message
											code="shipment.carrier.label" default="Carrier" /></label></td>
										<td valign="top" style="width: 30%;">
											<g:autoSuggest name="carrier" jsonUrl="/warehouse/shipment/findPersonByName" 
												width="150" 
												valueId="${shipmentInstance?.carrier?.id}" 
												valueName="${shipmentInstance?.carrier?.email}"/>		
												
										</td>
									</tr>
									<tr class="prop">
										<td class="name"  style="width: 10%;">
											<label><g:message code="shipment.destination.label" default="Recipient" /></label>
										</td>
										<td class="value" style="width: 30%;">		
											<g:autoSuggest name="recipient" jsonUrl="/warehouse/shipment/findPersonByName"
												width="150"
												valueId="${shipmentInstance?.recipient?.id}"
												valueName="${shipmentInstance?.recipient?.email}"/>
	
										</td>
									</tr>						
									
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.actualShippingDate.label" default="Shipping date" /></label></td>
										<td valign="top"
											class=" ${hasErrors(bean: shipmentInstance, field: 'actualShippingDate', 'errors')}"
											nowrap="nowrap">
												<g:jqueryDatePicker name="actualShippingDate"
											value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy"/>
										</td>
									</tr>											
									<tr class="prop">
			                            <td valign="top" class="name"><label><g:message code="comment.comment.label" default="Comment" /></label></td>                            
			                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'comment', 'errors')}">
		                                    <g:textArea name="comment" cols="60" rows="5"/>
		                                </td>
			                        </tr>  	        
											
										<tr class="prop">
											<td valign="top" class="name"></td>
											<td valign="top" class="value">
												<div class="buttons">
													<button type="submit" class="positive"><img
														src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
														alt="save" /> Send Shipment</button>
													<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}" class="negative">
														<img
															src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
															alt="Cancel" /> Cancel </g:link>
												</div>				
											</td>
										</tr>
								</tbody>
							</table>
						</g:form>
					</fieldset>
				</td>
				<td width="20%">
					<g:render template="sidebar" />						
				</td>				
			</tr>
		</table>

			
			
	</div>
</body>
</html>
