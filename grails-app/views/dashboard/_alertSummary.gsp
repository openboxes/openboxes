<div class="widget-large">
	<div class="widget-header">
		<h2><warehouse:message code="inventory.alerts.label" default="Alerts"/></h2>
	</div>	    			
	<div class="widget-content" style="padding:0; margin:0">	    					    			
		<div id="alertSummary">	

    		<table>
    			<tbody>
					<tr class="even">
						<td class="center" style="width: 1%">
							<img src="${createLinkTo(dir:'images/icons/silk/bell.png')}" class="middle"/>
						</td>
						<td>
							<g:link controller="inventory" action="listExpiredStock">
                                <warehouse:message code="inventory.listExpiredStock.label" default="Items that have expired"/>
							</g:link>
						</td>
						<td class="right">
							<div id="expiredStockCount">
								<img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
							</div>
						</td>
					</tr>
    			
					<tr class="odd">
						<td class="center" style="width: 1%">
							<img src="${createLinkTo(dir:'images/icons/silk/box.png')}" class="middle"/>
						</td>
						<td>
							<g:link controller="inventory" action="listLowStock">
                                <warehouse:message code="inventory.listLowStock.label" default="Items that have stocked out"/>
							</g:link>
						</td>
						<td class="right">
							<div id="lowStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
							
						</td>
					</tr>
					 
					<tr class="even">
						<td class="center" style="width: 1%">
							<img src="${createLinkTo(dir:'images/icons/silk/reload.png')}" class="middle"/>
						</td>
						<td>
                            <warehouse:message code="inventory.listReorderStock.label" default="Items that are below reorder level"/>
							<g:link controller="inventory" action="listReorderStock"></g:link>
						</td>
						<td class="right">
							<%-- 
							<div id="reorderStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
							--%>
							<warehouse:message code="default.notAvailable.label"/>
						</td>
					</tr>
											
				</tbody>
			</table>
		</div>
	</div>
</div>

