<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Add New Event</content>
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
				<td width="75%">
					<g:form action="saveEvent" method="POST">
						<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
						<g:hiddenField name="eventId" value="${eventInstance?.id}" />
						<fieldset>			
							
							<g:render template="summary"/>

						
							<table>
								<tbody>
									<tr class="prop">
			                           <td valign="top" class="name"><label><warehouse:message code="event.eventType.label" default="Event Type" /></label></td>                            
			                           <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventType', 'errors')}">
											<g:if test="${!eventInstance?.eventType}">												
												<g:select id="eventType.id" name='eventType.id' noSelection="${['':'Select one ...']}" 
													from='${org.pih.warehouse.core.EventType.list()}' optionKey="id" 
													optionValue="name" value="${eventInstance.eventType}" >
												</g:select>
											</g:if>
											<g:else>
												<g:hiddenField name="eventType.id" value="${eventInstance?.eventType?.id}"/>
												${eventInstance?.eventType?.name}
											</g:else>
			                       		</td>
									</tr> 								
								
									<tr class="prop">
				                           <td valign="top" class="name"><label><warehouse:message code="event.eventDate.label" default="Event Date" /></label></td>                            
				                           <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventDate', 'errors')}">
				                                  <g:jqueryDatePicker name="eventDate" value="${eventInstance?.eventDate}" format="MM/dd/yyyy" />
				                              </td>
				                       </tr>  	          
										 	          
										<tr class="prop">
				                           <td valign="top" class="name"><label><warehouse:message code="event.eventDate.label" default="Location" /></label></td>                            
				                           <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'location', 'errors')}">
											<g:select id="eventLocation.id" name='eventLocation.id' noSelection="${['':'Select one ...']}" 
												from='${org.pih.warehouse.core.Location.list()}' optionKey="id" optionValue="name"
												value="${eventInstance?.eventLocation?.id}">
												</g:select>									
				                              </td>
				                       </tr>  	          
				                       <tr class="prop">
				                       		<td></td>
				                       
				                       </tr>
				                       <tr>
									    <td class="name"></td>
									    <td class="value">
											<div class="buttons" style="text-align: left;">
												<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Save Event</button>
												<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}" class="negative"> <img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="Cancel" /> Cancel </g:link>
											</div>
									    </td>					                        
				                       </tr>         
				                   </tbody>
				               </table>
				         </fieldset>	       
					</g:form>
				</td>
				<td width="20%">
					<g:render template="sidebar" />				
				</td>								
			</tr>
		</table>
	</div>
</body>
</html>
