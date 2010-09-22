<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${message(code: 'default.dashboard.label', default: 'Dashboard')}</title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">${message(code: 'default.dashboard.label', default: 'My Dashboard')}</content>
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
					<div class="widgetHeader">Shipments: Most Recent Outgoing</div>
	    			<div class="widgetContent">
	    				<div><b>From:</b> ${session.warehouse.name }</div>	    			
	    				<hr/>
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
		    								<th>Date</th>
		    								<th>Name</th>
		    								<th>Status</th>
		    							</tr>
		    						</thead>
		    						<tbody>
										<g:each in="${outgoingShipments}" var="shipment" status="i">										
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><g:formatDate date="${shipment.expectedShippingDate}" format="MMM dd yyyy"/></td>
												<td><g:link controller="shipment" action="showDetails" id="${shipment.id}">${shipment.name }</g:link></td>
												<td><g:link controller="shipment" action="listOutgoing" params="['eventType.id':shipment?.mostRecentStatus?.id]">${shipment.mostRecentStatus}</g:link></td>									
											</tr>										
										</g:each>
									</tbody>
								</table>
							</g:else>
						</div>
	    			</div>
				</div>
				
				<div class="widgetSmall">
					<div class="widgetHeader">Shipments: Most Recent Incoming</div>
	    			<div class="widgetContent">
	    				<div><b>To:</b> ${session.warehouse.name}</div>
	    				<hr/>
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
		    								<th>Date</th>
		    								<th>Name</th>
		    								<th>Status</th>
		    							</tr>
		    						</thead>
		    						<tbody>
										<g:each in="${incomingShipments}" var="shipment" status="i">
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><g:formatDate date="${shipment.expectedShippingDate}" format="MMM dd yyyy"/></td>
												<td><g:link controller="shipment" action="showDetails" id="${shipment.id}">${shipment.name }</g:link></td>
												<td><g:link controller="shipment" action="listIncoming" params="['eventType.id':shipment?.mostRecentStatus?.id]">${shipment.mostRecentStatus}</g:link></td>
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
					<div class="widgetHeader">Shipments: Outgoing Summary</div>
	    			<div class="widgetContent">
	    				<div><b>From:</b> ${session.warehouse.name }</div>	    			
	    				<hr/>
	    				<div id="outgoingShipmentBreakdown">	
				    		<table>				    			
				    			<tbody>
									<g:each var="entry" in="${outgoingShipmentsByStatus}" status="i">	 
										
										<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
											<td><g:link controller="shipment" action="listOutgoing" params="['eventType.id':entry.key.id]">${entry.key.name}</g:link></td>
											<td><span class="fade">${entry.key.description}</span></td>
											<td>${entry.value.objectList.size}</td>
										</tr>	
										
							    	</g:each>
						    	</tbody>
						    	<tfoot>
							    	<tr>
							    		<th colspan="2">Total</th>
							    		<th>${allOutgoingShipments.size()}</td>
							    	</tr>
						    	</tfoot>
					    	</table>
						</div>
	    			</div>
				</div>				
				
				<div class="widgetSmall">
					<div class="widgetHeader">Shipments: Incoming Summary </div>
	    			<div class="widgetContent">
	    				<div><b>To:</b> ${session.warehouse.name}</div>	    			
	    				<hr/>
	    				<div id="incomingShipmentBreakdown">	
				    		<table>				    			
				    			<tbody>
									<g:each var="entry" in="${incomingShipmentsByStatus}" status="i">	 
										<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
											<td><g:link controller="shipment" action="listIncoming" params="['eventType.id':entry.key.id]">${entry.key.name}</g:link></td>
											<td><span class="fade">${entry.key.description}</span></td>
											<td>${entry.value.objectList.size}</td>
										</tr>	
										
							    	</g:each>
						    	</tbody>
						    	<tfoot>
							    	<tr>
							    		<th colspan="2">Total</th>							    		
							    		<th>${allIncomingShipments.size()}</td>
							    	</tr>
						    	</tfoot>
					    	</table>
						</div>
	    			</div>
				</div>								
				
	    	</div>
		</div>
    </body>
</html>

