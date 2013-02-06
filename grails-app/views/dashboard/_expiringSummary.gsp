<div class="widget-large">
	<div class="widget-header">
		<h2><warehouse:message code="inventory.expiring.label" default="Expiring inventory"/></h2>
	</div>	    			
	<div class="widget-content" style="padding:0; margin:0">	    					    			
		<div id="alertSummary">	

	    		<table>
	    			<tbody>
						<tr class="even">
							<td class="center" style="width: 1%">
								<img src="${createLinkTo(dir:'images/icons/silk/clock.png')}" class="middle"/>
							</td>
							<td>
								<g:link controller="inventory" action="listExpiringStock" params="[threshold:30]">
									Items that will expire within 30 days
								</g:link>
							</td>
							<td>
								${expiringStockWithin30Days?.size() }
							</td>
						</tr>
						<tr class="odd">
							<td class="center" style="width: 1%">
								<img src="${createLinkTo(dir:'images/icons/silk/clock.png')}" class="middle"/>
							</td>
							<td>
								<g:link controller="inventory" action="listExpiringStock" params="[threshold:90]">
									Items that will expire within 90 days
								</g:link>
							</td>
							<td>
								${expiringStockWithin90Days?.size() }
							
							</td>
						</tr>
						<tr class="even">
							<td class="center" style="width: 1%">
								<img src="${createLinkTo(dir:'images/icons/silk/clock.png')}" class="middle"/>
							</td>
							<td>
								<g:link controller="inventory" action="listExpiringStock" params="[threshold:180]">
									Items that will expire within 180 days
								</g:link>
							</td>
							<td>
								${expiringStockWithin180Days?.size() }
							</td>
						</tr>
						<tr class="odd">
							<td class="center" style="width: 1%">
								<img src="${createLinkTo(dir:'images/icons/silk/clock.png')}" class="middle"/>
							</td>
							<td>
								<g:link controller="inventory" action="listExpiringStock" params="[threshold:365]">
									Items that will expire within 365 days
								</g:link>
							</td>
							<td>
								${expiringStockWithin365Days?.size() }
							</td>
						</tr>
					</tbody>
				</table>
		</div>
	</div>
</div>