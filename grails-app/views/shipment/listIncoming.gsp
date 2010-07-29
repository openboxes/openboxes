
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Incoming Shipments')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">
			<img src="${createLinkTo(dir:'images/icons/silk/',file: 'lorry.png')}"
			valign="top" style="vertical-align: middle;" /> 
			<g:message code="default.list.label" args="[entityName]" /></content>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            
            <div class="dialog">
				<g:form method="get" action="listIncoming">
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
            
            	<!-- 
						Shipment name
						Shipment ID (six digits)
						To
						Last event
						Date of last event
						Location of last event            	
            	 -->
				
            <div class="list">
				<g:each var="entry" in="${shipmentInstanceMap}">	                    
					<div style="padding: 10px; font-weight: bold;" >${entry.key}</div>	                    	
					<table>
	                    <thead>
	                        <tr>   
	                            <g:sortableColumn property="shipmentNumber" title="${message(code: 'shipment.shipmentNumber.label', default: 'Shipment Number')}" />
	                            <g:sortableColumn property="name" title="${message(code: 'shipment.name.label', default: 'Name')}" />
	                            <g:sortableColumn property="origin.name" title="${message(code: 'shipment.origin.label', default: 'From')}" />
	                            <g:sortableColumn property="events" title="${message(code: 'shipment.events.label', default: 'Most Recent Event')}" />
								<g:sortableColumn property="documents" title="${message(code: 'shipment.documents.label', default: 'Documents')}" />                            
	                            <g:sortableColumn property="mostRecentStatus" title="${message(code: 'shipment.status.label', default: 'Most Recent Status')}" />
	                        </tr>
	                    </thead>
	                   
	                   	<tbody>
		                    <g:each var="shipmentList" in="${entry.value}">
								<g:each var="shipmentInstance" in="${shipmentList.objectList}" status="i">
									<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
										<td align="center" width="15%"><g:link action="showDetails" id="${shipmentInstance.id}">${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}</g:link></td>
										<td align="center">${fieldValue(bean: shipmentInstance, field: "name")}</td>
										<td align="center">
											${fieldValue(bean: shipmentInstance, field: "origin.name")}
										</td>
										<td>
											<g:if test="${!shipmentInstance.events}"></g:if>									
											<g:else>
												<div>
													${shipmentInstance.mostRecentEvent.eventType.name}<br/>
													<span style="font-size: 0.8em; color: #aaa;">
														<g:formatDate format="dd MMM yyyy" date="${shipmentInstance.mostRecentEvent.eventDate}"/> |  
														${shipmentInstance.mostRecentEvent.eventLocation.name}</span>
												</div>									
											</g:else>
										</td>
										<td nowrap="nowrap">								
											<g:if test="${!shipmentInstance.events}"></g:if>
											<g:else>
												<g:each in="${shipmentInstance.documents}" var="document" status="j">
													<div id="document-${document.id}">
														<img src="${createLinkTo(dir:'images/icons/',file:'document.png')}" alt="Document" style="vertical-align: absmiddle"/>
														<g:link controller="document" action="download" id="${document.id}">${document?.filename}</g:link>
													</div>
												</g:each>							
											</g:else>
										</td>
										<td>
											${shipmentInstance.mostRecentStatus}
										</td>
			                        </tr>
								</g:each>                    		
	                    	</g:each>	                    	         
	                    </tbody>
					</table>
				</g:each>
            </div>
<!--              
            <div class="paginateButtons">
                <g:paginate total="${shipmentInstanceTotal}" />
            </div>
-->            
        </div>		
    </body>
</html>
