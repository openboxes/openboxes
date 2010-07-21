
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Incoming Shipments')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
            	<!-- 
						Shipment name
						Shipment ID (six digits)
						To
						Last event
						Date of last event
						Location of last event            	
            	 -->
                <table>
                    <thead>
                        <tr>   
                            <g:sortableColumn property="shipmentNumber" title="${message(code: 'shipment.shipmentNumber.label', default: 'Shipment Number')}" />
                            <g:sortableColumn property="name" title="${message(code: 'shipment.name.label', default: 'Name')}" />
                            <g:sortableColumn property="origin.name" title="${message(code: 'shipment.origin.label', default: 'From')}" />
                            <g:sortableColumn property="events" title="${message(code: 'shipment.events.label', default: 'Most Recent Event')}" />
							<g:sortableColumn property="documents" title="${message(code: 'shipment.documents.label', default: 'Documents')}" />                            
                        </tr>
                    </thead>
                    <tbody>
	                    <g:each in="${shipmentInstanceList}" status="i" var="shipmentInstance">
	                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
								<td align="center" width="5%"><g:link action="showDetails" id="${shipmentInstance.id}">${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}</g:link></td>
								<td align="center">${fieldValue(bean: shipmentInstance, field: "name")}</td>
								<td align="center">
									${fieldValue(bean: shipmentInstance, field: "origin.name")}
								</td>
								<td>
									<g:if test="${!shipmentInstance.events}">No events</g:if>									
									<g:else>
							
					
									</g:else>																		

								</td>
								<td>
								
								</td>
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
