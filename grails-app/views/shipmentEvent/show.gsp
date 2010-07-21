
<%@ page import="org.pih.warehouse.shipping.ShipmentEvent" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipmentEvent.label', default: 'ShipmentEvent')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.show.label" args="[entityName]" /></content>
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
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipmentEvent.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: shipmentEventInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipmentEvent.targetLocation.label" default="Target Location" /></td>
                            
                            <td valign="top" class="value"><g:link controller="location" action="show" id="${shipmentEventInstance?.targetLocation?.id}">${shipmentEventInstance?.targetLocation?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipmentEvent.description.label" default="Description" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: shipmentEventInstance, field: "description")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipmentEvent.shipmentStatus.label" default="Shipment Status" /></td>
                            
                            <td valign="top" class="value"><g:link controller="shipmentStatus" action="show" id="${shipmentEventInstance?.shipmentStatus?.id}">${shipmentEventInstance?.shipmentStatus?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipmentEvent.dateCreated.label" default="Date Created" /></td>
                            
                            <td valign="top" class="value"><g:formatDate date="${shipmentEventInstance?.dateCreated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipmentEvent.eventDate.label" default="Event Date" /></td>
                            
                            <td valign="top" class="value"><g:formatDate date="${shipmentEventInstance?.eventDate}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipmentEvent.eventLocation.label" default="Event Location" /></td>
                            
                            <td valign="top" class="value"><g:link controller="location" action="show" id="${shipmentEventInstance?.eventLocation?.id}">${shipmentEventInstance?.eventLocation?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipmentEvent.eventType.label" default="Event Type" /></td>
                            
                            <td valign="top" class="value"><g:link controller="eventType" action="show" id="${shipmentEventInstance?.eventType?.id}">${shipmentEventInstance?.eventType?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipmentEvent.lastUpdated.label" default="Last Updated" /></td>
                            
                            <td valign="top" class="value"><g:formatDate date="${shipmentEventInstance?.lastUpdated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipmentEvent.shipment.label" default="Shipment" /></td>
                            
                            <td valign="top" class="value"><g:link controller="shipment" action="show" id="${shipmentEventInstance?.shipment?.id}">${shipmentEventInstance?.shipment?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${shipmentEventInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
