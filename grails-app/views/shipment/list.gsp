
<%@ page import="org.pih.warehouse.Shipment" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>		
		<g:javascript library="prototype" />
		<g:javascript>
			function clearShipment(e) { $('shipmentContent').value=''; }	
			function showSpinner(visible) { $('spinner').style.display = visible ? "inline" : "none"; }
		</g:javascript>
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
                            <g:sortableColumn property="id" title="${message(code: 'shipment.id.label', default: 'View')}" />                        
                            <g:sortableColumn property="trackingNumber" title="${message(code: 'shipment.trackingNumber.label', default: 'Tracking Number')}" />                        
                            <g:sortableColumn property="expectedShippingDate" title="${message(code: 'shipment.expectedShippingDate.label', default: 'Expected Shipping Date')}" />                        
                            <g:sortableColumn property="expectedDeliveryDate" title="${message(code: 'shipment.expectedDeliveryDate.label', default: 'Actual Shipping Date')}" />                        
                            <th><g:message code="shipment.source.label" default="Source" /></th>                   	    
                            <th><g:message code="shipment.target.label" default="Target" /></th>                   	    
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${shipmentInstanceList}" status="i" var="shipmentInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="show" id="${shipmentInstance.id}">view</g:link></td>                        
                            <td>${fieldValue(bean: shipmentInstance, field: "trackingNumber")}</td>
                            <td><g:formatDate date="${shipmentInstance.expectedShippingDate}" /></td>
                            <td><g:formatDate date="${shipmentInstance.expectedDeliveryDate}" /></td>
                            <td>${fieldValue(bean: shipmentInstance, field: "source")}</td>
                            <td>${fieldValue(bean: shipmentInstance, field: "target")}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${shipmentInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
