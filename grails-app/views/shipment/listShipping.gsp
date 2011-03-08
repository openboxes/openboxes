
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipping')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">
			<g:message code="default.list.label" args="[entityName]" /></content>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>

			<h1>Shipments originating at ${session.warehouse.name}</h1>

            <div class="list">
				<g:if test="${shipmentInstanceMap.size()==0}">
            		<div class="message">
            			<g:if test="${eventType?.name}">
            				There are no shipments with status <b>${eventType?.eventCode?.status}</b>.
            			</g:if>
            			<g:else>
    		        		There are no shipments matching your conditions.
	            		</g:else>
            		</div>
            	</g:if>

				<g:each var="entry" in="${shipmentInstanceMap}">	                    
					<h2><b>${entry.key.name}</b> Shipments (${entry.value.objectList.size})</h2>
						      
					<table>
	                    <thead>
	                        <tr>   
								<g:sortableColumn property="shipmentType" title="${message(code: 'shipment.shipmentType.label', default: 'Type')}" />
	                            <g:sortableColumn property="shipmentNumber" title="${message(code: 'shipment.shipmentNumber.label', default: 'Shipment')}" />								
	                            <g:sortableColumn property="destination" title="${message(code: 'shipment.destination.label', default: 'Destination')}" />
	                        	<g:sortableColumn property="expectedShippingDate"  title="${message(code: 'shipment.expectedShippingDate.label', default: 'Expected Shipping Date')}" />
	                         	<!-- include actual shipping date? -->
	                         	<th><a href="">${message(code: 'shipment.status.label', default: 'Status')}</a></th>
	                         	<th><a href="">${message(code: 'shipment.documents.label', default: 'Documents')}</a></th>
	                        </tr>
	                    </thead>
	                   
	                   	<tbody>
		                    <g:each var="shipmentList" in="${entry.value}">
								<g:each var="shipmentInstance" in="${shipmentList.objectList}" status="i">
									<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
										<td width="3%" style="text-align: center">
											<img src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
											alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />		
										</td>										
										<td width="10%">
											<g:link action="showDetails" id="${shipmentInstance.id}">
												${fieldValue(bean: shipmentInstance, field: "name")}
											</g:link>																														
										</td>
										<td width="10%" align="center">
											${fieldValue(bean: shipmentInstance, field: "destination.name")}
										</td>
										<td width="10%" align="center">
											<g:formatDate format="dd/MMM/yyyy" date="${shipmentInstance?.expectedShippingDate}"/>
										</td>
										<td width="10%">												
											${shipmentInstance?.mostRecentEvent?.eventType?.eventCode?.status} - <g:formatDate format="dd/MMM/yyyy" date="${shipmentInstance?.mostRecentEvent?.eventDate}"/>									
										</td>
										<td width="15%">
											<g:if test="${!shipmentInstance.documents}"><span class="fade">(empty)</span></g:if>
											<g:else>
												<g:each in="${shipmentInstance.documents}" var="document" status="j">
													<div id="document-${document.id}">
														<img src="${createLinkTo(dir:'images/icons/',file:'document.png')}" alt="Document" style="vertical-align: middle"/>
														<g:link controller="document" action="download" id="${document.id}">${document?.documentType?.name} (${document?.filename})</g:link>
													</div>
												</g:each>							
											</g:else>
										</td>
			                        </tr>
								</g:each>                    		
	                    	</g:each>	                    	         
	                    </tbody>
					</table>
					<br/>
					<br/>
				</g:each>
            </div>
        </div>		
    </body>
</html>
