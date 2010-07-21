
<%@ page import="org.pih.warehouse.shipping.ShipmentEvent" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipmentEvent.label', default: 'ShipmentEvent')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.create.label" args="[entityName]" /></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>    
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${shipmentEventInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${shipmentEventInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="targetLocation"><g:message code="shipmentEvent.targetLocation.label" default="Target Location" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentEventInstance, field: 'targetLocation', 'errors')}">
                                    <g:select name="targetLocation.id" from="${org.pih.warehouse.core.Location.list()}" optionKey="id" value="${shipmentEventInstance?.targetLocation?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description"><g:message code="shipmentEvent.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentEventInstance, field: 'description', 'errors')}">
                                    <g:textField name="description" value="${shipmentEventInstance?.description}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="shipmentStatus"><g:message code="shipmentEvent.shipmentStatus.label" default="Shipment Status" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentEventInstance, field: 'shipmentStatus', 'errors')}">
                                    <g:select name="shipmentStatus.id" from="${org.pih.warehouse.shipping.ShipmentStatus.list()}" optionKey="id" value="${shipmentEventInstance?.shipmentStatus?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated"><g:message code="shipmentEvent.dateCreated.label" default="Date Created" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentEventInstance, field: 'dateCreated', 'errors')}">
                                    <g:datePicker name="dateCreated" precision="day" value="${shipmentEventInstance?.dateCreated}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="eventDate"><g:message code="shipmentEvent.eventDate.label" default="Event Date" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentEventInstance, field: 'eventDate', 'errors')}">
                                    <g:datePicker name="eventDate" precision="day" value="${shipmentEventInstance?.eventDate}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="eventLocation"><g:message code="shipmentEvent.eventLocation.label" default="Event Location" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentEventInstance, field: 'eventLocation', 'errors')}">
                                    <g:select name="eventLocation.id" from="${org.pih.warehouse.core.Location.list()}" optionKey="id" value="${shipmentEventInstance?.eventLocation?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="eventType"><g:message code="shipmentEvent.eventType.label" default="Event Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentEventInstance, field: 'eventType', 'errors')}">
                                    <g:select name="eventType.id" from="${org.pih.warehouse.shipping.EventType.list()}" optionKey="id" value="${shipmentEventInstance?.eventType?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastUpdated"><g:message code="shipmentEvent.lastUpdated.label" default="Last Updated" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentEventInstance, field: 'lastUpdated', 'errors')}">
                                    <g:datePicker name="lastUpdated" precision="day" value="${shipmentEventInstance?.lastUpdated}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="shipment"><g:message code="shipmentEvent.shipment.label" default="Shipment" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentEventInstance, field: 'shipment', 'errors')}">
                                    <g:select name="shipment.id" from="${org.pih.warehouse.shipping.Shipment.list()}" optionKey="id" value="${shipmentEventInstance?.shipment?.id}"  />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
