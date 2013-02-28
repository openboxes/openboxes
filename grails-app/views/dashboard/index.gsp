<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${warehouse.message(code: 'default.dashboard.label', default: 'Dashboard')}</title>
	    <script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
		
    </head>
    <body>        
		<div class="body">		
		
			<g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>		
	    	<div id="dashboard">
	    		<table>
	    			<%-- 
	    			<tr>
	    			
	    				<td colspan="2">
							<g:render template="inventorySummary"/>
						<td>						
					</tr>
					--%>

					<tr>
						<td>
							<g:render template="alertSummary"/>
						</td>
						<td>
							<g:render template="expiringSummary"/>
						</td>
						<td rowspan="3" width="40%">
							<g:if test='${activityList }'>
								<g:render template="activitySummary"/>
							</g:if>						
						
						</td>
					
					</tr>
					<tr>
						<td>
							<g:render template="shipmentSummary"/>
						</td>
						<td>
							<g:render template="receiptSummary"/>
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<g:render template="tagSummary"/>
						</td>
					</tr>
					
					
				</table>
	    	</div>
		</div>

<script type="text/javascript">

$(function() { 		
	$('#lowStockCount').load('${request.contextPath}/json/getLowStockCount?location.id=${session.warehouse.id}');
	$('#reorderStockCount').load('${request.contextPath}/json/getReorderStockCount?location.id=${session.warehouse.id}');
	$('#expiredStockCount').load('${request.contextPath}/json/getExpiredStockCount?location.id=${session.warehouse.id}');
	$('#expiringIn30DaysStockCount').load('${request.contextPath}/json/getExpiringStockCount?location.id=${session.warehouse.id}&daysUntilExpiry=30');
	$('#expiringIn90DaysStockCount').load('${request.contextPath}/json/getExpiringStockCount?location.id=${session.warehouse.id}&daysUntilExpiry=90');
	$('#expiringIn180DaysStockCount').load('${request.contextPath}/json/getExpiringStockCount?location.id=${session.warehouse.id}&daysUntilExpiry=180');

	$(".spinner").click(function() { 
		$(this).hide();
	});

});
</script>
		
    </body>
</html>

