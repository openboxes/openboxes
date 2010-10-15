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
					<div class="widgetHeader">Recent Shipments from ${session.warehouse.name}</div>
	    			<div class="widgetContent">
	    				<div id="mostRecentOutgoingShipments">		    				
   							<g:if test="${!outgoingShipments}">
   								<div style="text-align: center; padding: 10px;" class="fade">
   									(no recent shipments)
   								</div>
   							</g:if>
	    					<g:else>
		    					<table>	    				
		    						<thead>
		    							<tr>
		    								<th>Status</th>
		    								<th>Name</th>
		    								<th>Ship Date</th>
		    							</tr>
		    						</thead>
		    						<tbody>
										<g:each in="${outgoingShipments}" var="shipment" status="i">										
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><g:link controller="shipment" action="listOutgoing" params="['eventType.id':shipment?.mostRecentStatus?.id]">
													</g:link>${shipment?.mostRecentEvent?.eventType?.eventStatus?.name}</td>									
												<td><g:link controller="shipment" action="showDetails" id="${shipment.id}">${shipment.name }</g:link></td>
												<td><g:formatDate date="${shipment.expectedShippingDate}" format="MMM dd"/></td>
											</tr>										
										</g:each>
									</tbody>
								</table>
							</g:else>
						</div>
	    			</div>
				</div>
				
				<div class="widgetSmall">
					<div class="widgetHeader">Recent Shipments to ${session.warehouse.name}</div>
	    			<div class="widgetContent">
	    				<div id="mostRecentIncomingShipments">
		    				<g:if test="${!incomingShipments}">
   								<div style="text-align: center; padding: 10px;" class="fade">
   									(no recent shipments)
   								</div>
   							</g:if>
	    					<g:else>
			    				<table>
		    						<thead>
		    							<tr>
		    								<th>Status</th>
		    								<th>Name</th>
		    								<th>Ship Date</th>
		    							</tr>
		    						</thead>
		    						<tbody>
										<g:each in="${incomingShipments}" var="shipment" status="i">
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><g:link controller="shipment" action="listIncoming" params="['eventType.id':shipment?.mostRecentStatus?.id]">
													</g:link>${shipment?.mostRecentEvent?.eventType?.eventStatus?.name}</td>
												<td><g:link controller="shipment" action="showDetails" id="${shipment.id}">${shipment.name }</g:link></td>
												<td><g:formatDate date="${shipment.expectedShippingDate}" format="MMM dd"/></td>
											</tr>
										</g:each>
									</tbody>
								</table>	    			
							</g:else>
						</div>
	    			</div>
				</div>
				
				<br clear="all"/>
				
				<div class="widgetSmall">
					<div class="widgetHeader">Summary: Shipments from ${session.warehouse.name}</div>
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
					<div class="widgetHeader">Summary: Shipments to ${session.warehouse.name}</div>
	    			<div class="widgetContent">
	    					    			
	    				<hr/>
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
				
	    	</div>
		</div>
    </body>
</html>

