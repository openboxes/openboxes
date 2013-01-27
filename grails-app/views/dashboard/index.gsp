<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${warehouse.message(code: 'default.dashboard.label', default: 'Dashboard')}</title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">${warehouse.message(code: 'default.dashboard.label', default: 'Dashboard')}</content>
	    <script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
		
    </head>
    <body>        
		<div class="body">		
		
			<g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>		
				
	    	<div id="dashboard">
	    		
	    		<table>
	    			<tr>
	    				<td>
							<g:render template="inventorySummary"/>
							
							<g:render template="shipmentSummary"/>
							<g:render template="receiptSummary"/>
							
							<%-- 
							<g:render template="orderSummary"/>
							--%>
						</td>
						<td width="40%">
							<g:if test='${activityList }'>
								<g:render template="activitySummary"/>
							</g:if>						
						</td>
					</tr>
				</table>
	    	</div>
		</div>
		
    </body>
</html>

