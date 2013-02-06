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
						<td rowspan="2" width="40%">
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
					
					
				</table>
	    	</div>
		</div>
		
    </body>
</html>

