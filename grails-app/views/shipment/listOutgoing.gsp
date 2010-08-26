
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Outgoing Shipments')}" />
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
            <div class="list">
				<g:each var="entry" in="${shipmentInstanceMap}">	                    
					<div style="padding: 10px; font-weight: bold;" ><h1>${entry.key}</h1></div>	                    	
					<table>
	                    <thead>
	                        <tr>   
	                            <g:sortableColumn property="events" title="${message(code: 'shipment.events.label', default: 'Status')}" />
								<g:sortableColumn property="shipmentNumber" title="${message(code: 'shipment.shipmentNumber.label', default: 'Shipment Number')}" />
	                            <g:sortableColumn property="destination" title="${message(code: 'shipment.destination.label', default: 'Destination')}" />
	                            <g:sortableColumn property="documents" title="${message(code: 'shipment.documents.label', default: 'Documents')}" />                            
								<g:sortableColumn property="lastModified" title="${message(code: 'shipment.lastModified.label', default: 'Last Modified')}" />
	                        </tr>
	                    </thead>
	                   
	                   	<tbody>
		                    <g:each var="shipmentList" in="${entry.value}">
								<g:each var="shipmentInstance" in="${shipmentList.objectList}" status="i">
									<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
										<td width="10%">
											<g:if test="${!shipmentInstance.events}"></g:if>									
											<g:else>
												<div>
													<b>${shipmentInstance.mostRecentEvent.eventType.name}</b> &nbsp;
													<span style="font-size: 0.8em; color: #aaa;">
														<g:formatDate format="dd MMM yyyy" date="${shipmentInstance.mostRecentEvent.eventDate}"/></span>
												</div>									
											</g:else>
										</td>
										<td width="10%" align="center">
											<g:link action="showDetails" id="${shipmentInstance.id}">${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}</g:link>
											${fieldValue(bean: shipmentInstance, field: "name")}
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
														<g:link controller="document" action="download" id="${document.id}">${document?.documentType?.name} (${document?.filename})</g:link>
													</div>
												</g:each>							
											</g:else>
										</td>
										<td width="10%">
											<span class="fade"><g:formatDate format="dd MMM yyyy hh:mm a" date="${shipmentInstance.lastUpdated}"/></span>
										</td>									
			                        </tr>
								</g:each>                    		
	                    	</g:each>	                    	         
	                    </tbody>
					</table>
				</g:each>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${shipmentInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
