<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'shipment.label', default: 'Shipping')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">
			<warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
			<g:if test="${params.groupBy}">
				<div class="left">
					<style>
						.selected { disabled: true; }
					</style>
				
					<h1>Shipments originating at ${session.warehouse.name}</h1>
					<br/>
				
					<label>Group by:</label>
					
					<g:if test="${params.groupBy=='expectedShippingDate'}">
						<span class="selected">
							<warehouse:message code="shipment.expectedShippingDate.label" default="Expected Shipping"/>
						</span>
					</g:if>
					<g:else>
						<g:link controller="shipment" action="listShippingByDate" params="['groupBy':'expectedShippingDate']">
							<warehouse:message code="shipment.expectedShippingDate.label" default="Expected shipping"/>
						</g:link>
					</g:else>
					|
					<g:if test="${params.groupBy=='dateCreated'}">
						<span class="selected">
							<warehouse:message code="shipment.dateCreated.label" default="Created"/>
						</span>
					</g:if>
					<g:else>
						<g:link controller="shipment" action="listShippingByDate" params="['groupBy':'dateCreated']">
							<warehouse:message code="shipment.dateCreated.label" default="Created"/>
						</g:link>
					</g:else>
					|
					<g:if test="${params.groupBy=='lastUpdated'}">
						<span class="selected">
							<warehouse:message code="shipment.lastUpdated.label" default="Modified"/>
						</span>
					</g:if>
					<g:else>
						<g:link controller="shipment" action="listShippingByDate" params="['groupBy':'lastUpdated']">
							<warehouse:message code="shipment.lastUpdated.label" default="Modified"/>
						</g:link>
					</g:else>
					
				</div>	
			</g:if>

            <div class="list">
				<g:each var="key" in="${shipmentInstanceMap.keySet()}">	                    
					<h2>
						${key}
					</h2>	      
					<table>
	                    <thead>
	                        <tr>   
								<g:sortableColumn property="shipmentType" title="${warehouse.message(code: 'shipment.shipmentType.label', default: 'Type')}" />
	                            <g:sortableColumn property="shipmentNumber" title="${warehouse.message(code: 'shipment.shipmentNumber.label', default: 'Shipment')}" />								
	                            <g:sortableColumn property="destination" title="${warehouse.message(code: 'shipment.destination.label', default: 'Destination')}" />
	                       		<th><a href="">${warehouse.message(code: 'shipment.status.label', default: 'Status')}</a></th>
	                         	<th><a href="">${warehouse.message(code: 'shipment.documents.label', default: 'Documents')}</a></th>                   
	                        </tr>
	                    </thead>
	                   
	                   	<tbody>
		                    <g:each var="shipmentInstance" in="${shipmentInstanceMap.get(key)}" status="i">
									<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
										<td width="3%" style="text-align: center">
											<img src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
											alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />		
										</td>										
										<td width="15%">
											<g:link action="showDetails" id="${shipmentInstance.id}">
												${fieldValue(bean: shipmentInstance, field: "name")}
											</g:link>						
											<span class="fade">											
												&nbsp;modified											
												<g:prettyDateFormat date="${shipmentInstance?.lastUpdated}" /> 
											</span>
																																			
										</td>
										<td width="10%" align="center">
											${fieldValue(bean: shipmentInstance, field: "destination.name")}
										</td>
										<td width="10%">
											
											<g:if test="${!shipmentInstance.events}">
												Created 
												<span class="fade"><g:prettyDateFormat date="${shipmentInstance?.dateCreated}" /></span>
											</g:if>
											<g:else>
												<div>
													${shipmentInstance?.mostRecentEvent?.eventType?.eventCode}													
													<span class="fade"><g:prettyDateFormat date="${shipmentInstance.mostRecentEvent.eventDate}" /></span>
												</div>
											</g:else>											
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
	                    </tbody>
					</table>
				</g:each>
            </div>
        </div>		
    </body>
</html>
