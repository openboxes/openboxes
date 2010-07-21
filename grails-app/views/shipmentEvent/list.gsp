
<%@ page import="org.pih.warehouse.shipping.ShipmentEvent" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipmentEvent.label', default: 'ShipmentEvent')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
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
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'shipmentEvent.id.label', default: 'Id')}" />
                        
                            <th><g:message code="shipmentEvent.targetLocation.label" default="Target Location" /></th>
                   	    
                            <g:sortableColumn property="description" title="${message(code: 'shipmentEvent.description.label', default: 'Description')}" />
                        
                            <th><g:message code="shipmentEvent.shipmentStatus.label" default="Shipment Status" /></th>
                   	    
                            <g:sortableColumn property="dateCreated" title="${message(code: 'shipmentEvent.dateCreated.label', default: 'Date Created')}" />
                        
                            <g:sortableColumn property="eventDate" title="${message(code: 'shipmentEvent.eventDate.label', default: 'Event Date')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${shipmentEventInstanceList}" status="i" var="shipmentEventInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${shipmentEventInstance.id}">${fieldValue(bean: shipmentEventInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: shipmentEventInstance, field: "targetLocation")}</td>
                        
                            <td>${fieldValue(bean: shipmentEventInstance, field: "description")}</td>
                        
                            <td>${fieldValue(bean: shipmentEventInstance, field: "shipmentStatus")}</td>
                        
                            <td><g:formatDate date="${shipmentEventInstance.dateCreated}" /></td>
                        
                            <td><g:formatDate date="${shipmentEventInstance.eventDate}" /></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${shipmentEventInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
