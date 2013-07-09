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
                            <g:render template="inventorySummary"/>
							<g:render template="alertSummary"/>
                            <g:render template="requisitionSummary" model="[requisitions:requisitions]"/>
                            <g:render template="tagSummary" model="[tags:tags]"/>
						</td>
						<td width="30%">
							<g:render template="expiringSummary"/>
                            <g:render template="indicatorSummary"/>
                            <g:render template="shipmentSummary"/>
                            <g:render template="receiptSummary"/>
						</td>
						<td width="40%">
                            <g:render template="activitySummary"/>
						</td>
					
					</tr>
				</table>
	    	</div>
		</div>

        <script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
        <script src="${createLinkTo(dir:'js/jquery.tagcloud', file:'jquery.tagcloud.js')}" type="text/javascript" ></script>
        <script type="text/javascript">
            //$(function() {
            $(window).load(function(){
                //getIndicator("#reconditionedStockCount", "${request.contextPath}/json/getReconditionedStockCount?location.id=${session.warehouse.id}", 1000, "error");
                $('#reconditionedStockCount').load('${request.contextPath}/json/getReconditionedStockCount?location.id=${session.warehouse.id}');
                $('#totalStockCount').load('${request.contextPath}/json/getTotalStockCount?location.id=${session.warehouse.id}');
                $('#inStockCount').load('${request.contextPath}/json/getInStockCount?location.id=${session.warehouse.id}');
                $('#outOfStockCount').load('${request.contextPath}/json/getOutOfStockCount?location.id=${session.warehouse.id}');
                $('#lowStockCount').load('${request.contextPath}/json/getLowStockCount?location.id=${session.warehouse.id}');
                $('#overStockCount').load('${request.contextPath}/json/getOverStockCount?location.id=${session.warehouse.id}');
                $('#reorderStockCount').load('${request.contextPath}/json/getReorderStockCount?location.id=${session.warehouse.id}');
                $('#expiredStockCount').load('${request.contextPath}/json/getExpiredStockCount?location.id=${session.warehouse.id}');
                $('#expiringIn30DaysStockCount').load('${request.contextPath}/json/getExpiringStockCount?location.id=${session.warehouse.id}&daysUntilExpiry=30');
                $('#expiringIn90DaysStockCount').load('${request.contextPath}/json/getExpiringStockCount?location.id=${session.warehouse.id}&daysUntilExpiry=90');
                $('#expiringIn180DaysStockCount').load('${request.contextPath}/json/getExpiringStockCount?location.id=${session.warehouse.id}&daysUntilExpiry=180');
                $('#expiringIn365DaysStockCount').load('${request.contextPath}/json/getExpiringStockCount?location.id=${session.warehouse.id}&daysUntilExpiry=365');



                $(".spinner").click(function() {
                    $(this).hide();
                });

                $("#tagcloud a").tagcloud({
                    size: {
                        start: 10,
                        end: 25,
                        unit: 'px'
                    },
                    color: {
                        start: "#CDE",
                        end: "#FS2"
                    }
                });

            });

            /*
            function getIndicator(id, url, timeout, error) {
                var value = $.ajax({
                    url: url,
                    type: "GET",
                    dataType: "json",
                    timeout: timeout,
                    success: function(response) { console.log(response); },
                    error: function(x, t, m) {
                        if(t==="timeout") {
                            console.log("got timeout");
                        } else {
                            console.log(t);
                        }
                    }
                });
                console.log(value);
                $(id).val(value);
            }
            */

        </script>
		
    </body>
</html>

