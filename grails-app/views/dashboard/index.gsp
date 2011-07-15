<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${message(code: 'default.dashboard.label', default: 'Dashboard')}</title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">${message(code: 'default.dashboard.label', default: 'Dashboard')}</content>
    </head>
    <body>        
		<div class="body">				
	    	<div id="dashboard">				
	    		<%-- 
	    		<div class="widget-large">
			    	<div class="widget-header"><g:message code="dashboard.welcome.label"/></div>
	    			<div class="widget-content">
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
				--%>
				
				<!--  Show the last five transactions? -->
				<%-- 
				<div class="widget-small">
					<div class="widget-header"><g:message code="transaction.summary.label" args="[session.warehouse.name]"/></div>
	    			<div class="widget-content">
	    				<div id="transaction-summary">
	    				
	    				</div>
	    			</div>
	    		</div>
				--%>
				
				<!--  Show recent shipments/receipts -->
				<div class="widget-small">
					<div class="widget-header"><h2><g:message code="shipping.summary.label" args="[session.warehouse.name]"/></h2></div>
	    			<div class="widget-content">
	    				<div id="shippingsummary">
		    				<g:if test="${!outgoingShipmentsByStatus}">
   								<div style="text-align: center; padding: 10px;" class="fade">
   									(<g:message code="shipping.noRecent.label"/>)
   								</div>
   							</g:if>	    		
   							<g:else>			
					    		<table >
					    			<thead>
					    				<tr class="">
					    					<td colspan="2">
					    						
					    						Shipping from ${session.warehouse.name }
					    					</td>
					    				</tr>
					    			</thead>				    			
					    			<tbody>
										<g:each var="entry" in="${outgoingShipmentsByStatus}" status="i">											
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td>${entry.key.name}</td>
												<td style="text-align: center;"><g:link controller="shipment" action="listShipping" params="['status':entry.key.name]">${entry.value.objectList.size}</g:link></td>
											</tr>	
											
								    	</g:each>
							    	</tbody>
							    	<tfoot>
								    	<tr style="border-top: 1px solid lightgrey">
								    		<th style="text-align: right;"><g:message code="shipping.total.label"/></th>
								    		<th style="text-align: center;"><g:link controller="shipment" action="listShipping" params="">${allOutgoingShipments.size()}</g:link></th>
								    	</tr>
							    	</tfoot>
						    	</table>
						    </g:else>
						</div>
	    			</div>

	    			<div class="widget-content">	    					    			
	    				<div id="receivingsummary">	
		    				<g:if test="${!incomingShipmentsByStatus}">
   								<div style="text-align: center; padding: 10px;" class="fade">
   									(<g:message code="receiving.noRecent.label"/>)
   								</div>
   							</g:if>	    		
   							<g:else>			
					    		<table>
						    		<thead>
					    				<tr class="">
					    					<td colspan="2">
					    						Receiving into ${session.warehouse.name }
					    					</td>
					    				</tr>
					    			</thead>				
					    			<tbody>
										<g:each var="entry" in="${incomingShipmentsByStatus}" status="i">	 
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td>${entry.key.name}</td>
												<td style="text-align: center;"><g:link controller="shipment" action="listReceiving" params="['status':entry.key.name]">
													${entry.value.objectList.size}</g:link></td>
											</tr>	
								    	</g:each>
							    	</tbody>
							    	<tfoot>
								    	<tr style="border-top: 1px solid lightgrey">
								    		<th style="text-align: right;"><g:message code="shipping.total.label"/></td>							    		
								    		<th style="text-align: center;"><g:link controller="shipment" action="listReceiving" params="">${allIncomingShipments.size()}</g:link></td>
								    	</tr>
							    	</tfoot>
						    	</table>
						    </g:else>
						</div>
	    			</div>
	    			<br clear="all"/>
	    			
				</div>	
<%-- 				

				
				<div class="widget-small">
					<div class="widget-header"><g:message code="shipping.mostRecent.label" args="[session.warehouse.name]" /></div>
	    			<div class="widget-content">
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
													${shipmentInstance?.mostRecentEvent?.eventType?.eventCode?.name}
												</td>									
												<td><g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}">${shipmentInstance?.name }</g:link></td>
												<td>${shipmentInstance?.destination?.name }</td>
												<td nowrap>
													<format:date obj="${shipmentInstance?.expectedShippingDate}"/>
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
				
				<div class="widget-small">
					<div class="widget-header"><g:message code="receiving.mostRecent.label" args="[session.warehouse.name]" /></div>
	    			<div class="widget-content">
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
													</g:link>${shipmentInstance?.mostRecentEvent?.eventType?.eventCode?.name}</td>
												<td><g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}">${shipmentInstance?.name }</g:link></td>
												<td>${shipmentInstance?.origin?.name }</td>
												<td nowrap>
													<format:date obj="${shipmentInstance?.expectedShippingDate}"/>
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

