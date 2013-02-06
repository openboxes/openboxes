<div class="widget-large">
	<div class="widget-header">
		<h2><warehouse:message code="inventory.alerts.label" default="Alerts"/></h2>
	</div>	    			
	<div class="widget-content" style="padding:0; margin:0">	    					    			
		<div id="alertSummary">	

			<g:if test="${!outgoingShipmentsByStatus}">
				<div style="text-align: left; padding: 10px;" class="fade">
					(<warehouse:message code="shipping.noRecent.label"/>)
				</div>
			</g:if>	    		
			<g:else>	
	    		<table>
	    			<tbody>
						<tr class="even">
							<td class="center" style="width: 1%">
								<img src="${createLinkTo(dir:'images/icons/silk/bell.png')}" class="middle"/>
							</td>
							<td>
								<g:link controller="inventory" action="listExpiredStock">
									Items that have expired
								</g:link>
							</td>
							<td>
								${expiredStock.size() }
							</td>
						</tr>
	    			
						<tr class="odd">
							<td class="center" style="width: 1%">
								<img src="${createLinkTo(dir:'images/icons/silk/box.png')}" class="middle"/>
							</td>
							<td>
								<g:link controller="inventory" action="listLowStock">
									Items that have stocked out
								</g:link>
							</td>
							<td>
								${lowStock?.size() }
							</td>
						</tr>
						<tr class="even">
							<td class="center" style="width: 1%">
								<img src="${createLinkTo(dir:'images/icons/silk/reload.png')}" class="middle"/>
							</td>
							<td>
								<g:link controller="inventory" action="listReorderStock">
									Items that are below reorder level
								</g:link>
							</td>
							<td>
								${reorderStock?.size() }
							</td>
						</tr>						
					</tbody>
				</table>
			</g:else>	
		</div>
	</div>
</div>