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
                                <warehouse:message code="inventory.listExpiringStock.label" args="[30]" default="Items that will expire within {0} days"/>

							</g:link>
						</td>
						<td class="right">
							<div id="expiringIn30DaysStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
						</td>
					</tr>
					<tr class="odd">
						<td class="center" style="width: 1%">
							<img src="${createLinkTo(dir:'images/icons/silk/clock.png')}" class="middle"/>
						</td>
						<td>
							<g:link controller="inventory" action="listExpiringStock" params="[threshold:90]">
                                <warehouse:message code="inventory.listExpiringStock.label" args="[90]" default="Items that will expire within {0} days"/>
							</g:link>
						</td>
						<td class="right">
							<div id="expiringIn90DaysStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
						
						</td>
					</tr>
					<tr class="even">
						<td class="center" style="width: 1%">
							<img src="${createLinkTo(dir:'images/icons/silk/clock.png')}" class="middle"/>
						</td>
						<td>
							<g:link controller="inventory" action="listExpiringStock" params="[threshold:180]">
                                <warehouse:message code="inventory.listExpiringStock.label" args="[180]" default="Items that will expire within {0} days"/>
							</g:link>
						</td>
						<td class="right">
							<div id="expiringIn180DaysStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
							
						</td>
					</tr>						
				</tbody>
			</table>
		</div>
	</div>
</div>