<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${warehouse.message(code: 'default.dashboard.label', default: 'Dashboard')}</title>
    </head>
    <body>        
		<div class="body">		
		
			<g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>		
	    	<div id="dashboard">
	    		<table>
					<tr>
						<td width="30%">
                            <g:render template="requisitionSummary" model="[requisitions:requisitions]"/>
                            <g:render template="receiptSummary"/>
                            <g:render template="shipmentSummary"/>
                            <g:render template="indicatorSummary"/>
						</td>
                        <td width="30%">
                            <g:render template="valueSummary"/>
                            <g:render template="alertSummary"/>
                            <g:render template="inventorySummary"/>
                            <g:render template="expiringSummary"/>
                        </td>
						<td width="40%">
                            <g:render template="activitySummary"/>
                            <g:render template="tagSummary" model="[tags:tags]"/>
						</td>
					
					</tr>
				</table>
	    	</div>
		</div>

        <script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
        <script type="text/javascript">

            $(window).load(function(){
                $(".spinner").click(function() {
                    $(this).hide();
                });
            });

        </script>
		
    </body>
</html>

