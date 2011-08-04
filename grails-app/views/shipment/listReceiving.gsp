
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'receiving.label', default: 'Receiving')}" />
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

			<h1>Shipments destined for ${session.warehouse.name}</h1>

			 <g:form action="listReceiving" method="post">
           		<h3><warehouse:message code="default.type.label"/>:  <g:select name="shipmentType"
								from="${org.pih.warehouse.shipping.ShipmentType.list()}"
								optionKey="id" optionValue="name" value="${shipmentType}" 
								noSelection="['':warehouse.message(code:'default.all.label')]" />&nbsp;&nbsp;    
           
           		<warehouse:message code="default.destination.label"/>:  <g:select name="origin" 
           							from="${org.pih.warehouse.core.Location.list().sort()}"
           							optionKey="id" optionValue="name" value="${origin}" 
           							noSelection="['':warehouse.message(code:'default.all.label')]" />&nbsp;&nbsp;
           								
           		<warehouse:message code="default.status.label"/>:  <g:select name="status" 
           					   from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
           					   optionKey="name" optionValue="${{warehouse.message(code:it.name)}}" value="${status}" 
           					   noSelection="['':warehouse.message(code:'default.all.label')]" />&nbsp;&nbsp;	
           					   
           		<warehouse:message code="default.from.label"/>: <g:jqueryDatePicker id="statusStartDate" name="statusStartDate"
												value="${statusStartDate}" format="MM/dd/yyyy"/>
				<warehouse:message code="default.to.label"/>: <g:jqueryDatePicker id="statusEndDate" name="statusEndDate"
												value="${statusEndDate}" format="MM/dd/yyyy"/>
           							
				<g:submitButton name="filter" value="${warehouse.message(code:'default.button.filter.label')}"/>
				</h3>  
            </g:form>
            
            <br/>
            
            <g:if test="${shipments.size()==0}">
           		<div>
           			<g:if test="${shipmentType || origin || status || statusStartDate || statusEndDate}">
           				<warehouse:message code="shipping.noShipmentsMatchingConditions.message"/>
           			</g:if>
           			<g:else>
   		        		<warehouse:message code="shipping.noShipmentsDestinedFor.message"/> <b>${session.warehouse.name}</b>.
            		</g:else>
           		</div>
           	</g:if>
           	
			<g:else>
            <div class="list">                            			      
				<table>
                    <thead>
                        <tr>   
                        	
                        	<th>${warehouse.message(code: 'default.type.label')}</th>
                            <th>${warehouse.message(code: 'shipping.shipment.label')}</th>							
                            <th>${warehouse.message(code: 'default.origin.label')}</th>
                         	<th>${warehouse.message(code: 'shipping.expectedDeliveryDate.label')}</th>
                         	<th>${warehouse.message(code: 'default.status.label')}</th>
                        </tr>
                    </thead>
                   
                   	<tbody>
						<g:each var="shipmentInstance" in="${shipments}" status="i">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
								<td width="3%" style="text-align: center">
									<img src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + format.metadata(obj:shipmentInstance?.shipmentType) + '.png')}"
									alt="${format.metadata(obj:shipmentInstance?.shipmentType)}" style="vertical-align: middle; width: 24px; height: 24px;" />		
								</td>										
								<td>
									<g:link action="showDetails" id="${shipmentInstance.id}">
										${fieldValue(bean: shipmentInstance, field: "name")}
									</g:link>																														
								</td>
								<td align="center">
									${fieldValue(bean: shipmentInstance, field: "origin.name")}
								</td>
								
								<td align="center">
									<g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${shipmentInstance?.expectedDeliveryDate}"/>
								</td>								
								<td>												
									<warehouse:message code="${shipmentInstance?.status.name}"/>
									<g:if test="${shipmentInstance?.status.date}">
									 - <format:date obj="${shipmentInstance?.status.date}"/>
									 </g:if>	
								</td>
								<%-- 
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
								--%>
	                        </tr>
						</g:each>                    			                    	         
                    </tbody>
				</table>
            </div>
            </g:else>
        </div>		
    </body>
</html>
