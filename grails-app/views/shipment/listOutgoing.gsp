
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
                <table id="listOutgoing">
                	<thead>
                        <tr>   
                            <g:sortableColumn property="shipmentNumber" title="${message(code: 'shipment.shipmentNumber.label', default: 'Shipment Number')}" />
                            <g:sortableColumn property="name" title="${message(code: 'shipment.name.label', default: 'Name')}" />
                            <g:sortableColumn property="status" title="${message(code: 'shipment.status.label', default: 'Most Recent Status')}" />
                            <g:sortableColumn property="destination.name" title="${message(code: 'shipment.destination.label', default: 'Expected to ship')}" />
							<g:sortableColumn property="documents" title="${message(code: 'shipment.documents.label', default: 'Documents')}" />
							<g:sortableColumn property="lastModified" title="${message(code: 'shipment.lastModified.label', default: 'Last Modified')}" />
                        </tr>
                        
                    </thead>	                    
					<g:each var="entry" in="${shipmentInstanceMap}">	                    
	                    <tbody>
		                    <g:each var="shipmentList" in="${entry.value}">
								<g:each var="shipmentInstance" in="${shipmentList.objectList}" status="i">
									<tr >            
										<td>
											<g:if test="${i == 0}">
												<b>${shipmentInstance.mostRecentStatus}</b>
											</g:if>											
										</td>								
										<td align="center">
											<g:link action="showDetails" id="${shipmentInstance.id}">${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}</g:link>
												${fieldValue(bean: shipmentInstance, field: "name")}
											<br/>
										</td>
										<td>
											<g:if test="${!shipmentInstance.events}"><span class="fade">(empty)</span></g:if>									
											<g:else>
												<div>												
													${shipmentInstance.mostRecentEvent.eventType.name}<br/>
													<span class="fade">
														${shipmentInstance.mostRecentEvent.eventLocation.name}
														&nbsp; | &nbsp;
														<g:formatDate format="dd MMM yyyy" date="${shipmentInstance.mostRecentEvent.eventDate}"/>   
														
													</span>
												</div>									
											</g:else>
										</td>
										<td>				
											<g:if test="${shipmentInstance.expectedShippingDate}">										
												Shipping<br/>
												<span class="fade">
													${fieldValue(bean: shipmentInstance, field: "destination.name")} &nbsp; | &nbsp;											
													<g:formatDate format="dd MMM yyyy" date="${shipmentInstance.expectedShippingDate}"/>  
												</span>												
											</g:if>
											<g:else>
												<span class="fade">
													${fieldValue(bean: shipmentInstance, field: "destination.name")} &nbsp; | &nbsp;											
												</span>
											</g:else>
										</td>
										<td>
											<g:if test="${!shipmentInstance.documents}"><span class="fade">(empty)</span></g:if>
											<g:else>
												<g:each in="${shipmentInstance.documents}" var="document" status="j">
													<div id="document-${document.id}">
														<img src="${createLinkTo(dir:'images/icons/',file:'document.png')}" alt="Document" style="vertical-align: middle"/>
														<g:link controller="document" action="download" id="${document.id}">${document?.documentType?.name}</g:link>
													</div>
												</g:each>							
											</g:else>
										</td>
										<td>
											<span class="fade"><g:formatDate format="dd MMM yyyy hh:mm a" date="${shipmentInstance.lastUpdated}"/></span>
										</td>
										
	                    			</tr>
	                    		</g:each>
	                    		<tr>
	                    			<td>&nbsp; <!-- separates shipment status groups --></td>
	                    		</tr>	                    		
	                    	</g:each>
						</tbody>
					</g:each>
   				</table>
	        </div>
            
            <!-- 
            <div class="paginateButtons">
                <g:paginate total="${shipmentInstanceTotal}" />
            </div>
             -->
        </div>
    </body>
</html>
