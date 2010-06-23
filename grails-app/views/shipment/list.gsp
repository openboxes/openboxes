
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
<%-- 
		<g:javascript library="prototype" />
		<g:javascript>
			function clearShipment(e) { $('shipmentContent').value=''; }	
			function showSpinner(visible) { $('spinner').style.display = visible ? "inline" : "none"; }
		</g:javascript>
--%>

    </head>
    <body>
        <div class="body" width="90%">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            
            <div class="list">
				<div>	
	            	<ul>
	            		<li class="first active"><g:link class="browse" action="list" id="all">Show all shipments (${shipmentInstanceTotal})</g:link></li>
	            		<li><g:link class="browse" action="list" id="incoming">Shipping from ${session.warehouse.name} (${incomingShipmentCount})</g:link></li>
	            		<li><g:link class="browse" action="list" id="outgoing">Delivering to ${session.warehouse.name} (${outgoingShipmentCount})</g:link></li>
	            	</ul>
	            </div>
	            <br clear="all"/>


                <table>
                    <thead>
                        <tr>                        
                            <g:sortableColumn property="shipmentStatus" title="${message(code: 'shipment.status.label', default: 'Status')}" />                            
                            <g:sortableColumn property="expectedShippingDate" title="${message(code: 'shipment.expectedShippingDate.label', default: 'Expected Shipping Date')}" />                        
                            <g:sortableColumn property="trackingNumber" title="${message(code: 'shipment.trackingNumber.label', default: 'Tracking Number')}" />                        
                            <th><g:message code="shipment.origin.label" default="Origin" /></th>                   	    
                            <th><g:message code="shipment.destination.label" default="Destination" /></th>                   	    
                        </tr>
                    </thead>
                    <tbody>
	                    <g:each in="${shipmentInstanceList}" status="i" var="shipmentInstance">
	                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" style="height: 50px;">
	                            <td style="vertical-align: middle; text-align: center;" nowrap="true">
	                            	<g:link action="show" id="${shipmentInstance.id}" alt="show">
	                            		<g:if test="${shipmentInstance?.shipmentStatus}">${fieldValue(bean: shipmentInstance, field: "shipmentStatus.name")}</g:if>
	                            		<g:else>No status</g:else>
									</g:link>
	                            </td>
	                            <td style="vertical-align: middle; text-align: center;" nowrap="true"><g:formatDate date="${shipmentInstance.expectedShippingDate}" format="dd MMM yyyy" /></td>
	                            <td style="vertical-align: middle; text-align: center;">${fieldValue(bean: shipmentInstance, field: "trackingNumber")}</td>
	                            <td style="vertical-align: middle; text-align: center;">${fieldValue(bean: shipmentInstance, field: "origin")}</td>
	                            <td style="vertical-align: middle; text-align: center;">${fieldValue(bean: shipmentInstance, field: "destination")}</td>
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
