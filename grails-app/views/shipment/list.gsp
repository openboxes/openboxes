
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

				<div>
					
	            	<ul>
	            		<li class="first"><span class="large">Showing:</span></li>
	            		<li class="first"><b>All Shipments</b> </li>
	            		<li><a href="">Incoming (0)</a></li>
	            		<li><a href="">Outgoing (0)</a></li>
	            	</ul>
	            </div>
	            <br clear="all"/>
	            <br/>

                <table>
                    <thead>
                        <tr>                        
                            <g:sortableColumn property="id" title="${message(code: 'shipment.id.label', default: 'View')}" />    
                            <g:sortableColumn property="shipmentStatus" title="${message(code: 'shipment.status.label', default: 'Status')}" />                            
                            <g:sortableColumn property="expectedShippingDate" title="${message(code: 'shipment.expectedShippingDate.label', default: 'Expected Shipping Date')}" />                        
                            <g:sortableColumn property="trackingNumber" title="${message(code: 'shipment.trackingNumber.label', default: 'Tracking Number')}" />                        
                            <th><g:message code="shipment.origin.label" default="Origin" /></th>                   	    
                            <th><g:message code="shipment.destination.label" default="Destination" /></th>                   	    
                        </tr>
                    </thead>
                    <tbody>
	                    <g:each in="${shipmentInstanceList}" status="i" var="shipmentInstance">
	                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'} higher">
	                            <td><span class="menuButton"><g:link class="list" action="show" id="${shipmentInstance.id}" alt="show"></g:link></span></td>                        
	                            <td nowrap="true"><span class="rounded">${fieldValue(bean: shipmentInstance, field: "shipmentStatus.name")}</span></td>
	                            <td nowrap="true"><g:formatDate date="${shipmentInstance.expectedShippingDate}" /></td>
	                            <td>${fieldValue(bean: shipmentInstance, field: "trackingNumber")}</td>
	                            <td>${fieldValue(bean: shipmentInstance, field: "origin")}</td>
	                            <td>${fieldValue(bean: shipmentInstance, field: "destination")}</td>
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
