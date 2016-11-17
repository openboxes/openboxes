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
						<td width="33%">
                            <g:render template="requisitionSummary" model="[requisitions:requisitions]"/>
                            <g:render template="receiptSummary"/>
                            <g:render template="shipmentSummary"/>
                            <g:render template="requisitionItemSummary"/>
                            <g:render template="indicatorSummary"/>
                        </td>
                        <td width="33%">

                            <%--<g:render template="valueSummary"/>--%>
                            <g:render template="productSummary"/>
                            <%--<g:render template="genericProductSummary"/>--%>
                            <g:render template="expiringSummary"/>
                        </td>
						<td width="33%">
                            <g:render template="activitySummary"/>
                            <g:render template="tagSummary" model="[tags:tags]"/>
						</td>
					
					</tr>
				</table>
	    	</div>
		</div>
        <script type="text/javascript" charset="utf8" src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/jquery.dataTables.js"></script>
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

