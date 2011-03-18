
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'receiving.label', default: 'Receiving')}" />
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

			<h1>Shipments destined for ${session.warehouse.name}</h1>

			 <g:form action="listReceiving" method="post">
           		<h3>Type:  <g:select name="shipmentType"
								from="${org.pih.warehouse.shipping.ShipmentType.list()}"
								optionKey="id" optionValue="name" value="${shipmentType}" 
								noSelection="['':'--All--']" />&nbsp;&nbsp;    
           
           		Origin:  <g:select name="origin" 
           							from="${org.pih.warehouse.core.Location.list().sort()}"
           							optionKey="id" optionValue="name" value="${origin}" 
           							noSelection="['':'--All--']" />&nbsp;&nbsp;
           								
           		Status:  <g:select name="status" 
           					   from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
           					   optionKey="name" optionValue="name" value="${status}" 
           					   noSelection="['':'--All--']" />&nbsp;&nbsp;	
           					   
           		from <g:jqueryDatePicker id="statusStartDate" name="statusStartDate"
												value="${statusStartDate}" format="MM/dd/yyyy"/>
				to <g:jqueryDatePicker id="statusEndDate" name="statusEndDate"
												value="${statusEndDate}" format="MM/dd/yyyy"/>
           							
				<g:submitButton name="filter" value="Filter"/>
				</h3>  
            </g:form>
            
            <br/>
            
            <g:if test="${shipments.size()==0}">
           		<div>
           			<g:if test="${shipmentType || origin || status || statusStartDate || statusEndDate}">
           				There are no shipments matching your conditions.
           			</g:if>
           			<g:else>
   		        		There are no shipments destined for ${session.warehouse.name}.
            		</g:else>
           		</div>
           	</g:if>
           	
			<g:else>
            <div class="list">                            			      
				<table>
                    <thead>
                        <tr>   
                        	<th>${message(code: 'shipment.shipmentType.label', default: 'Type')}</th>
                            <th>${message(code: 'shipment.shipment.label', default: 'Shipment')}</th>							
                            <th>${message(code: 'shipment.origin.label', default: 'Origin')}</th>
                         	<th>${message(code: 'shipment.status.label', default: 'Status')}</th>
                         	<th>${message(code: 'shipment.documents.label', default: 'Documents')}</th>
                        </tr>
                    </thead>
                   
                   	<tbody>
						<g:each var="shipmentInstance" in="${shipments}" status="i">
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
									${fieldValue(bean: shipmentInstance, field: "origin.name")}
								</td>
								<td width="10%">												
									${shipmentInstance?.status.name}
									<g:if test="${shipmentInstance?.status.date}">
									 - <g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${shipmentInstance?.status.date}"/>
									 </g:if>	
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
            </div>
            </g:else>
        </div>		
    </body>
</html>
