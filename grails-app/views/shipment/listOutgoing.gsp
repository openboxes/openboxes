
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipping')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
		<content tag="pageTitle">
			<img src="${createLinkTo(dir:'images/icons/silk/',file: 'lorry.png')}"
				valign="top" style="vertical-align: middle;" />
			<g:message code="default.list.label" args="[entityName]" />
		</content>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <%-- 
            <div class="dialog">
				<g:form method="get" action="listOutgoing">
	            	<fieldset>
	            		<legend>Filter by</legend>				            
						<table>
							<tr class="prop">
								<td class="name">
									<label>Search</label>
								</td>
								<td>
									<g:textField name="searchQuery" value="${params.searchQuery}"/>										
								</td>										
							</tr>
						</table>				
					</fieldset>
				</g:form>            
            </div>
            --%>
            <div class="list">
				<g:each var="entry" in="${shipmentInstanceMap}">	        
					<div style="padding: 10px; font-weight: bold;" ><h1>${entry.key}</h1></div>	                    	
					<table>
	                    <thead>
	                        <tr>   
								<g:sortableColumn property="shipmentType" title="${message(code: 'shipment.shipmentType.label', default: 'Type')}" />
	                            <g:sortableColumn property="shipmentNumber" title="${message(code: 'shipment.shipmentNumber.label', default: 'Shipment')}" />								
	                            <g:sortableColumn property="status" title="${message(code: 'shipment.status.label', default: 'Status')}" />                            
	                            <g:sortableColumn property="eventDate" title="${message(code: 'shipment.eventDate.label', default: 'Event Date')}" />                            
	                            <g:sortableColumn property="destination" title="${message(code: 'shipment.destination.label', default: 'Destination')}" />
	                            <g:sortableColumn property="documents" title="${message(code: 'shipment.documents.label', default: 'Documents')}" />                            
	                        </tr>
	                    </thead>
	                   
	                   	<tbody>
		                    <g:each var="shipmentList" in="${entry.value}">
								<g:each var="shipmentInstance" in="${shipmentList.objectList}" status="i">
									<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
										<td width="5%" align="center">
											<img src="${createLinkTo(dir:'images/icons',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
											alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />		
										</td>										
										<td width="20%">
											<g:link action="showDetails" id="${shipmentInstance.id}">
												${fieldValue(bean: shipmentInstance, field: "name")}
											</g:link>																														
										</td>
										<td width="10%">
											<g:if test="${!shipmentInstance.events}"></g:if>
											<g:else>
												<div>
													${shipmentInstance.mostRecentEvent.eventType.name}
													
												</div>									
											</g:else>											
										</td>
										<td width="10%">
											<g:if test="${!shipmentInstance.events}"></g:if>
											<g:else>										
												<g:formatDate format="MMM dd yyyy" date="${shipmentInstance.mostRecentEvent.eventDate}"/>
											</g:else>
											
										</td>
										<td width="10%" align="center">
											${fieldValue(bean: shipmentInstance, field: "destination.name")}											
										</td>
										<td width="15%">
											<g:if test="${!shipmentInstance.documents}"><span class="fade">(empty)</span></g:if>
											<g:else>
												<g:each in="${shipmentInstance.documents}" var="document" status="j">
													<div id="document-${document.id}">
														<img src="${createLinkTo(dir:'images/icons/',file:'document.png')}" alt="Document" style="vertical-align: middle"/>
														<g:link controller="document" action="download" id="${document.id}">
															${document?.filename}
														</g:link>
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
