<div class="widget-small">
					<div class="widget-header"><h2><warehouse:message code="order.summary.label" args="[session.warehouse.name]"/></h2></div>	    			
 			<div class="widget-content">	    					    			
 				<div id="receivingsummary">	
  				<g:if test="${!incomingOrdersByStatus}">
							<div style="text-align: left; padding: 10px;" class="fade">
								(<warehouse:message code="order.noRecent.label"/>)
							</div>
						</g:if>	    		
						<g:else>			
	    		<table>
	    			<thead>
	    				<tr class="odd">
	    					<th colspan="2" class="left">
								<warehouse:message code="order.ordersInto.label" args="[session.warehouse.name]"/>
	    					</th>
	    				</tr>
	    			</thead>
	    			<tbody>
						<g:each var="entry" in="${incomingOrdersByStatus}" status="i">	 
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
								<td>
									<g:link controller="order" action="list" params="['status':entry?.key]">
										${format.metadata(obj:entry.key)} 
									</g:link>
								</td>
								<td style="text-align: center;">
									<g:link controller="order" action="list" params="['status':entry?.key]">
										${entry.value.objectList.size}
									</g:link>
								</td>
							</tr>	
				    	</g:each>
			    	</tbody>
			    	<tfoot>
				    	<tr style="border-top: 1px solid lightgrey">
				    		<td style="text-align: left;">
				    			<warehouse:message code="shipping.total.label"/>
				    		</td>							    		
				    		<td style="text-align: center;">
				    			<g:link controller="order" action="list" params="">
				    				${incomingOrders.size()}
				    			</g:link>
				    		</td>
				    	</tr>
			    	</tfoot>
		    	</table>
		    </g:else>
		</div>
		</div>
		<br clear="all"/>	    			
</div>	