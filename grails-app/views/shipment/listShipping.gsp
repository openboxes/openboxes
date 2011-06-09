
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipping')}" />
        <title>Shipments from <b>${session.warehouse.name}</b></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
			<table>            	
            	<tr>
            		<td style="border: 1px solid lightgrey; background-color: #f5f5f5;">
			            <g:form action="listShipping" method="post">
			            
			            	<table >
			            		<tr>
						           	<td class="filter-list-item">
						           		<label class="block">Type</label> 
						           		<g:select name="shipmentType"
														from="${org.pih.warehouse.shipping.ShipmentType.list()}"
														optionKey="id" optionValue="name" value="${shipmentType}" 
														noSelection="['':'--All--']" />&nbsp;&nbsp;    
									</td>
						           	<td class="filter-list-item">
							           	<label class="block">Destination </label>
							           	<g:select name="destination" 
							           							from="${org.pih.warehouse.core.Location.list().sort()}"
							           							optionKey="id" optionValue="name" value="${destination}" 
							           							noSelection="['':'--All--']" />&nbsp;&nbsp;
									</td>
						           	<td class="filter-list-item">
							           	<label class="block">Status</label> 
							           	<g:select name="status" 
							           					   from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
							           					   optionKey="name" optionValue="name" value="${status}" 
							           					   noSelection="['':'--All--']" />&nbsp;&nbsp;	
									</td>
						           	<td class="filter-list-item">
							           	<label class="block">From</label> 
							           	<g:jqueryDatePicker id="statusStartDate" name="statusStartDate"
																			value="${statusStartDate}" format="MM/dd/yyyy"/>
									</td>
						           	<td class="filter-list-item">
										<label class="block">To</label> 
										<g:jqueryDatePicker id="statusEndDate" name="statusEndDate"
																			value="${statusEndDate}" format="MM/dd/yyyy"/>
									</td>
						           	<td class="filter-list-item" style="height: 100%; vertical-align: bottom">
										<button name="filter">
											<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;
											Filter </button>
									</td>
								</tr>
							</table>
			            </g:form>
            		</td>
            	</tr>
            	<tr>
					<td>            
			            <g:if test="${shipments.size()==0}">
			           		<div>
			           			<g:if test="${shipmentType || destination || status || statusStartDate || statusEndDate}">
			           				There are no shipments matching your conditions.
			           			</g:if>
			           			<g:else>
			   		        		There are no shipments originating at <b>${session.warehouse.name}</b>.
			            		</g:else>
			            		&nbsp;
			            		<g:link controller="shipment" action="listShipping"><g:message code="shipment.startOver.label"  default="Start over "/></g:link>
			           		</div>
			           	</g:if>
			            
			            <g:else>
				            <div class="list">
								<table>
				                    <thead>
				                        <tr class="odd">   
				                         	<th>${message(code: 'shipment.actions.label', default: 'Actions')}</th>
				                        	<th>${message(code: 'shipment.shipmentType.label', default: 'Type')}</th>
				                            <th>${message(code: 'shipment.shipment.label', default: 'Shipment')}</th>							
				                            <th>${message(code: 'shipment.destination.label', default: 'Destination')}</th>
				                        	<th>${message(code: 'shipment.expectedShippingDate.label', default: 'Shipping Date')}</th>
				                         	<th>${message(code: 'shipment.status.label', default: 'Status')}</th>
				                        </tr>
				                    </thead>
				                   	<tbody>
										<g:each var="shipmentInstance" in="${shipments}" status="i">
											<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">            
												<td>
													<div class="action-menu">
														<button class="action-btn">
															<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
															<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
														</button>
														<div class="actions" style="position: absolute; display: none;">
															<g:render template="listShippingMenuItems" model="[shipmentInstance:shipmentInstance]"/>															
														</div>
													</div>	
												</td>
												<td width="3%" style="text-align: center">
													<img src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
													alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />		
												</td>										
												<td>
													<g:link action="showDetails" id="${shipmentInstance.id}">
														${fieldValue(bean: shipmentInstance, field: "name")}
													</g:link>				
												</td>
												<td align="center">
													${fieldValue(bean: shipmentInstance, field: "destination.name")}
												</td>
												<td align="center">
													<format:date obj="${shipmentInstance?.expectedShippingDate}"/>
												</td>
												<td>												
													${shipmentInstance?.status.name}
													<g:if test="${shipmentInstance?.status.date}">
													 - <format:date obj="${shipmentInstance?.status.date}"/>
													 </g:if>									
												</td>
					                        </tr>
										</g:each>                    		
				                    </tbody>
								</table>
				            </div>
			            </g:else>
					</td>
				</tr>
			</table>
        </div>		
        
    </body>
</html>
