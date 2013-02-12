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
								Items that have expired
							</g:link>
						</td>
						<td class="right">
							<div id="expiredStockCount"></div>
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
						<td class="right">
							<div id="lowStockCount"></div>
							
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
						<td class="right">
							<div id="reorderStockCount"></div>
						</td>
					</tr>						
				</tbody>
			</table>
		</div>
	</div>
</div>
<script type="text/javascript">

$(function() { 		
	$('#lowStockCount').load('${request.contextPath}/json/getLowStockCount?location.id=${session.warehouse.id}');
	$('#reorderStockCount').load('${request.contextPath}/json/getReorderStockCount?location.id=${session.warehouse.id}');
	$('#expiredStockCount').load('${request.contextPath}/json/getExpiredStockCount?location.id=${session.warehouse.id}');
})
</script>

