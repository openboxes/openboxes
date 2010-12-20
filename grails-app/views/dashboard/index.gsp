<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${message(code: 'default.dashboard.label', default: 'Dashboard')}</title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">${message(code: 'default.dashboard.label', default: 'Dashboard')}</content>
    </head>
    <body>        
    
    	<style>
    		.widgetLarge { width: 90%; border: 1px solid lightgrey; height: 75px; margin: 5px; }
			.widgetSmall { width: 400px; float: left; border: 1px solid lightgrey;  margin: 5px; }    		
    		.widgetHeader { background-color: #FAFAFA; padding: 10px; font-weight: bold; font-size: 110%;} 
    		.widgetContent { padding: 10px; } 
    	</style>
    	
		<div class="body">		
	    	<div id="dashboard">				
	    		<div class="widgetLarge">
			    	<div class="widgetHeader"><g:message code="dashboard.welcome.label"/></div>
	    			<div class="widgetContent">
						<g:if test="${!session.user}">
							<p>
								<g:message code="dashboard.notLoggedIn.label" />
							</p>
						</g:if>
						<g:else>
							<p class="large" align="justify">				
								<span class="greeting">
									<g:message code="dashboard.greeting.label" args="[session.user.name, session.warehouse.name]" />
																
								</span>	 
							</p>
						</g:else>				
					</div>
				</div>
				
				
				<div class="widgetSmall">					
					<div class="widgetHeader"><g:message code="shipping.summary.label" args="[session.warehouse.name]"/></div>
	    			<div class="widgetContent">
	    				<div id="outgoingShipmentSummary">
		    				<g:if test="${!outgoingShipmentsByStatus}">
   								<div style="text-align: center; padding: 10px;" class="fade">
   									(<g:message code="shipping.noRecent.label"/>)
   								</div>
   							</g:if>	    		
   							<g:else>			
					    		<table>				    			
					    			<thead>
					    				<tr>
					    					<th><g:message code="shipping.status.label"/></th>
					    					<th><g:message code="shipping.total.label"/></th>
					    				</tr>
					    			</thead>	    			
					    			<tbody>
										<g:each var="entry" in="${outgoingShipmentsByStatus}" status="i">											
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><g:link controller="shipment" action="listShipping" params="['eventStatus':entry.key, 'activityType':'SHIPPING']">${entry.key.name}</g:link></td>
												<td>${entry.value.objectList.size}</td>
											</tr>	
											
								    	</g:each>
							    	</tbody>
							    	<tfoot>
								    	<tr>
								    		<th><g:message code="shipping.total.label"/></th>
								    		<th>${allOutgoingShipments.size()}</td>
								    	</tr>
							    	</tfoot>
						    	</table>
						    </g:else>
						</div>
	    			</div>
				</div>				
				
				<div class="widgetSmall">
					<div class="widgetHeader"><g:message code="receiving.summary.label" args="[session.warehouse.name]" default="Receiving" /></div>
	    			<div class="widgetContent">	    					    			
	    				<div id="incomingShipmentSummary">	
		    				<g:if test="${!incomingShipmentsByStatus}">
   								<div style="text-align: center; padding: 10px;" class="fade">
   									(<g:message code="receiving.noRecent.label"/>)
   								</div>
   							</g:if>	    		
   							<g:else>			
					    		<table>			
					    			<thead>
					    				<tr>
					    					<th>Status</th>
					    					<th>Count</th>
					    				</tr>
					    			</thead>	    			
					    			<tbody>
										<g:each var="entry" in="${incomingShipmentsByStatus}" status="i">	 
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><g:link controller="shipment" action="listReceiving" params="['eventStatus':entry.key, 'activityType':'RECEIVING']">${entry.key.name}</g:link></td>
												<td>${entry.value.objectList.size}</td>
											</tr>	
											
								    	</g:each>
							    	</tbody>
							    	<tfoot>
								    	<tr>
								    		<th>Total</th>							    		
								    		<th>${allIncomingShipments.size()}</td>
								    	</tr>
							    	</tfoot>
						    	</table>
						    </g:else>
						</div>
	    			</div>
				</div>								

				<br clear="all"/>


<%-- 
				
				<div class="widgetSmall">
					<div class="widgetHeader"><g:message code="shipping.mostRecent.label" args="[session.warehouse.name]" /></div>
	    			<div class="widgetContent">
	    				<div id="mostRecentShipping">		    				
	    				
  							<g:if test="${!outgoingShipments}">  				
   								<div style="text-align: center; padding: 10px;" class="fade">
   									(<g:message code="shipping.noRecent.label"/>)
   								</div>
	    					</g:if>
	    					<g:else>
		    					<table>	    				
		    						<thead>
		    							<tr>
		    								<th><g:message code="shipping.status.label"  /></th>
		    								<th><g:message code="shipping.name.label" /></th>
		    								<th><g:message code="shipping.to.label" /></th>
		    								<th><g:message code="shipping.date.label" /></th>
		    							</tr>
		    						</thead>
									<tbody>	    				
										<g:each in="${outgoingShipments}" var="shipmentInstance" status="i">										
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td>
													<g:link controller="shipment" action="listShipping" params="['eventType.id':shipment?.mostRecentStatus?.id]"></g:link>
													${shipmentInstance?.mostRecentEvent?.eventType?.eventStatus?.name}
												</td>									
												<td><g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}">${shipmentInstance?.name }</g:link></td>
												<td>${shipmentInstance?.destination?.name }</td>
												<td nowrap>
													<g:formatDate date="${shipmentInstance?.expectedShippingDate}" format="MMM dd"/>
													<span class="fade">
														(<g:relativeDate date="${shipmentInstance?.expectedShippingDate}"/>)
													</span>
												</td>
											</tr>										
										</g:each>
										<tr>
											<th colspan="4" style="text-align:left;">
												<g:link class="new" controller="createShipment" action="suitcase">
													<img src="${createLinkTo(dir: 'images/icons/silk/', file: 'add.png') }" style="vertical-align:middle;"/>
													<g:message code="suitcase.create.label" default="Create Suitcase" />
												</g:link>
											</th>
										</tr>
									</tbody>
								</table>
							</g:else>
						</div>
	    			</div>
				</div>
				
				<div class="widgetSmall">
					<div class="widgetHeader"><g:message code="receiving.mostRecent.label" args="[session.warehouse.name]" /></div>
	    			<div class="widgetContent">
	    				<div id="mostRecentReceiving">
		    				<g:if test="${!incomingShipments}">
   								<div style="text-align: center; padding: 10px;" class="fade">
   									( <g:message code="receiving.noRecent.label"/>)
   								</div>
   							</g:if>
	    					<g:else>
	    				
			    				<table>
		    						<thead>
		    							<tr>
		    								<th><g:message code="receiving.status.label"  /></th>
		    								<th><g:message code="receiving.name.label"  /></th>
		    								<th><g:message code="receiving.from.label"  /></th>
		    								<th><g:message code="receiving.date.label"  /></th>
		    							</tr>
		    						</thead>
		    						<tbody>
										<g:each in="${incomingShipments}" var="shipmentInstance" status="i">
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><g:link controller="shipment" action="listReceiving" params="['eventType.id':shipmentInstance?.mostRecentStatus?.id]">
													</g:link>${shipmentInstance?.mostRecentEvent?.eventType?.eventStatus?.name}</td>
												<td><g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}">${shipmentInstance?.name }</g:link></td>
												<td>${shipmentInstance?.origin?.name }</td>
												<td nowrap>
													<g:formatDate date="${shipmentInstance?.expectedShippingDate}" format="MMM dd"/>
													<span class="fade">
														(<g:relativeDate date="${shipmentInstance?.expectedShippingDate}"/>)
													</span>												
												</td>
											</tr>
										</g:each>
									</tbody>
								</table>
							</g:else>
						</div>
	    			</div>
				</div>
--%>
				
	    	</div>
		</div>
    </body>
</html>

