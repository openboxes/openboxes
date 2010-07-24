
<%@ page import="org.pih.warehouse.shipping.ContainerType"%>
<%@ page import="org.pih.warehouse.shipping.Document"%>
<%@ page import="org.pih.warehouse.shipping.DocumentType"%>
<%@ page import="org.pih.warehouse.shipping.EventType"%>
<%@ page import="org.pih.warehouse.core.Location"%>
<%@ page import="org.pih.warehouse.core.Organization"%>
<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.shipping.ReferenceNumberType"%>
<%@ page import="org.pih.warehouse.shipping.Shipment"%>
<%@ page import="org.pih.warehouse.user.User"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">
		New Event: <b>${shipmentInstance?.name}</b>
		<span style="color: #aaa; font-size: 0.8em; padding-left: 20px;">
			Created: <g:formatDate date="${shipmentInstance?.dateCreated}" format="dd MMM yyyy hh:mm" /> |
			Updated: <g:formatDate date="${shipmentInstance?.lastUpdated}" format="dd MMM yyyy hh:mm" />
		</span>
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


		<g:form action="addEvent" method="POST">
			<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
			<table>
				<tbody>
					<tr class="prop">
                           <td valign="top" class="name"><label><g:message code="event.eventDate.label" default="Event Date" /></label></td>                            
                           <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventDate', 'errors')}">
                                  <g:jqueryDatePicker name="eventDate" value="" format="" />
                              </td>
                       </tr>  	          
					<tr class="prop">
                           <td valign="top" class="name"><label><g:message code="event.eventType.label" default="Event Type" /></label></td>                            
                           <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventType', 'errors')}">
                                  <g:select id="eventTypeId" name='eventTypeId' noSelection="${['':'Select one ...']}" 
                                  	from='${EventType.list()}' optionKey="id" optionValue="optionValue"></g:select>
                              </td>
                       </tr>  	          
						<tr class="prop">
                           <td valign="top" class="name"><label><g:message code="event.eventDate.label" default="Location" /></label></td>                            
                           <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'location', 'errors')}">
							<g:select id="eventLocationId" name='eventLocationId' noSelection="${['':'Select one ...']}" 
								from='${Location.list()}' optionKey="id" optionValue="name">
								</g:select>									
                              </td>
                       </tr>  	          
                       <tr class="prop">
                       		<td></td>
                       
                       </tr>
                       <tr>
					    <td colspan="2">
							<div class="buttons" style="text-align: right;">
								<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Add Event</button>
								<g:link controller="dashboard" action="index" class="negative"> <img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="Cancel" /> Cancel </g:link>
							</div>
					    </td>					                        
                       </tr>         
                   </tbody>
               </table>
	    </g:form>
	</div>
</body>
</html>
