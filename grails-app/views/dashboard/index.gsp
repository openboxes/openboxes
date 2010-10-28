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
    		.widgetLarge { width: 94%; border: 1px solid lightgrey; height: 75px; margin: 15px; }
			.widgetSmall { width: 45%; float: left; border: 1px solid lightgrey;  margin: 15px; }    		
    		.widgetHeader { background-color: #F3F7ED; padding: 10px; font-weight: bold; font-size: 110%;} 
    		.widgetContent { padding: 10px; } 
    	</style>
    	
    	<script type="text/javascript">
	    	
    	</script>
    
		<div class="body">		
	    	<div id="dashboard">				
	    	
	    		<div class="widgetLarge">
			    	<div class="widgetHeader">Welcome to OpenBoxes!</div>
	    			<div class="widgetContent">
						<g:if test="${!session.user}">
							<p>Welcome! Please <a class="home" href="${createLink(uri: '/auth/login')}">login</a> to gain access</p>
						</g:if>
						<g:else>
							<p class="large" align="justify">				
								<span class="greeting">Welcome, <b>${session.user.firstName} ${session.user.lastName}</b>!</span>
								You are logged into the <b>${session.warehouse?.name}</b> warehouse. 
							</p>
						</g:else>				
					</div>
				</div>
				
				
				<div class="widgetSmall">					
					<div class="widgetHeader"><u>SHIPPING</u> from ${session.warehouse.name}</div>
	    			<div class="widgetContent">
	    				<div id="outgoingShipmentSummary">
		    				<g:if test="${!outgoingShipmentsByStatus}">
   								<div style="text-align: center; padding: 10px;" class="fade">
   									(no outgoing shipments)
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
										<g:each var="entry" in="${outgoingShipmentsByStatus}" status="i">											
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><g:link controller="shipment" action="listOutgoing" params="['eventStatus':entry.key, 'activityType':'SHIPPING']">${entry.key.name}</g:link></td>
												<td>${entry.value.objectList.size}</td>
											</tr>	
											
								    	</g:each>
							    	</tbody>
							    	<tfoot>
								    	<tr>
								    		<th>Total</th>
								    		<th>${allOutgoingShipments.size()}</td>
								    	</tr>
							    	</tfoot>
						    	</table>
						    </g:else>
						</div>
	    			</div>
				</div>				
				
				<div class="widgetSmall">
					<div class="widgetHeader"><u>RECEIVING</u> to ${session.warehouse.name}</div>
	    			<div class="widgetContent">	    					    			
	    				<div id="incomingShipmentSummary">	
		    				<g:if test="${!incomingShipmentsByStatus}">
   								<div style="text-align: center; padding: 10px;" class="fade">
   									(no incoming shipments)
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
												<td><g:link controller="shipment" action="listIncoming" params="['eventStatus':entry.key, 'activityType':'RECEIVING']">${entry.key.name}</g:link></td>
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

				
				<div class="widgetSmall">
					<div class="widgetHeader">Recent <u>SHIPPING</u> from ${session.warehouse.name}</div>
	    			<div class="widgetContent">
	    				<div id="mostRecentOutgoingShipments">		    				
	    					<table>	    				
	    						<thead>
	    							<tr>
	    								<th>Status</th>
	    								<th>Name</th>
	    								<th>Ship To</th>
	    								<th>Ship On</th>
	    							</tr>
	    						</thead>
								<tbody>	    				
		   							<g:if test="${!outgoingShipments}">
		   								<tr>
											<td colspan="4">
				   								<div style="text-align: center; padding: 10px;" class="fade">
				   									(no recent shipments)
				   								</div>
											</td>
		   								</tr>
		   							</g:if>
			    					<g:else>
										<g:each in="${outgoingShipments}" var="shipmentInstance" status="i">										
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td>
													<g:link controller="shipment" action="listOutgoing" params="['eventType.id':shipment?.mostRecentStatus?.id]"></g:link>
													${shipmentInstance?.mostRecentEvent?.eventType?.eventStatus?.name}
												</td>									
												<td><g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance.name }</g:link></td>
												<td>${shipmentInstance.destination.name }</td>
												<td>
													<g:relativeDate date="${shipmentInstance.expectedShippingDate}"/>
													<span class="fade">
														<g:formatDate date="${shipmentInstance.expectedShippingDate}" format="MMM dd"/>
													</span>
												</td>
											</tr>										
										</g:each>
									</g:else>
									<tr>
										<th colspan="4" style="text-align:right">
											<g:link class="new" controller="createShipment" action="suitcase">
												<img src="${createLinkTo(dir: 'images/icons/silk/', file: 'add.png') }" style="vertical-align:middle;"/>
												<g:message code="shipment.create.label" default="new suitcase" />
											</g:link>
										</th>
									</tr>
								</tbody>
							</table>
						</div>
	    			</div>
				</div>
				
				<div class="widgetSmall">
					<div class="widgetHeader">Recent <u>RECEIVING</u> to ${session.warehouse.name}</div>
	    			<div class="widgetContent">
	    				<div id="mostRecentIncomingShipments">
		    				<table>
	    						<thead>
	    							<tr>
	    								<th>Status</th>
	    								<th>Name</th>
	    								<th>Ship From</th>
	    								<th>Ship On</th>
	    							</tr>
	    						</thead>
	    						<tbody>
				    				<g:if test="${!incomingShipments}">
				    					<tr>
				    						<td colspan="4">
				   								<div style="text-align: center;" class="fade">
				   									(no recent shipments)
				   								</div>
				   							</td>
				   						</tr>
		   							</g:if>
			    					<g:else>
										<g:each in="${incomingShipments}" var="shipment" status="i">
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><g:link controller="shipment" action="listIncoming" params="['eventType.id':shipment?.mostRecentStatus?.id]">
													</g:link>${shipment?.mostRecentEvent?.eventType?.eventStatus?.name}</td>
												<td><g:link controller="shipment" action="showDetails" id="${shipment.id}">${shipment.name }</g:link></td>
												<td>${shipment?.origin?.name }</td>
												<td><g:formatDate date="${shipment.expectedShippingDate}" format="MMM dd"/></td>
											</tr>
										</g:each>
									</g:else>
								</tbody>
							</table>
						</div>
	    			</div>
				</div>
				
				
				
	    	</div>
		</div>
    </body>
</html>

