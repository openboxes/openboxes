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
        <script src="${createLinkTo(dir:'js/jquery.tagcloud', file:'jquery.tagcloud.js')}" type="text/javascript" ></script>
        <script type="text/javascript">
            //$(function() {
            $(window).load(function(){

                $.ajax({
                    dataType: "json",
                    timeout: 60000,
                    url: "${request.contextPath}/json/getDashboardAlerts?location.id=${session.warehouse.id}",
                    //data: data,
                    success: function (data) {
                        console.log(data);
                        // {"lowStock":103,"reorderStock":167,"overStock":38,"totalStock":1619,"reconditionedStock":54,"stockOut":271,"inStock":1348}
                        $('#lowStockCount').html(data.lowStock?data.lowStock:0);
                        $('#overStockCount').html(data.overStock?data.overStock:0);
                        $('#reconditionedStockCount').html(data.reconditionedStock?data.reconditionedStock:0);
                        $('#totalStockCount').html(data.totalStock?data.totalStock:0);
                        $('#inStockCount').html(data.inStock?data.inStock:0);
                        $('#onHandQuantityZeroCount').html(data.onHandQuantityZero?data.onHandQuantityZero:0);
                        $('#outOfStockCount').html(data.outOfStock?data.outOfStock:0);
                        $('#reorderStockCount').html(data.reorderStock?data.reorderStock:0);

                    },
                    error: function(xhr, status, error) {
                        console.log(xhr);
                        console.log(status);
                        console.log(error);
                        $('#lowStockCount').html("ERROR: " + error);
                        $('#overStockCount').html("ERROR: " + error);
                        $('#reconditionedStockCount').html("ERROR: " + error);
                        $('#onHandQuantityZeroCount').html("ERROR: " + error);
                        $('#totalStockCount').html("ERROR: " + error);
                        $('#inStockCount').html("ERROR: " + error);
                        $('#outOfStockCount').html("ERROR: " + error);
                        $('#reorderStockCount').html("ERROR: " + error);

                    }
                });

                $.ajax({
                    dataType: "json",
                    timeout: 60000,
                    url: "${request.contextPath}/json/getDashboardExpiryAlerts?location.id=${session.warehouse.id}",
                    //data: data,
                    success: function (data) {
                        console.log(data);
                        // Expiration
                        $('#expiredStockCount').html(data.expired?data.expired:0);
                        $('#expiringIn30DaysStockCount').html(data.within30Days?data.within30Days:0);
                        $('#expiringIn60DaysStockCount').html(data.within60Days?data.within60Days:0);
                        $('#expiringIn90DaysStockCount').html(data.within90Days?data.within90Days:0);
                        $('#expiringIn180DaysStockCount').html(data.within180Days?data.within180Days:0);
                        $('#expiringIn365DaysStockCount').html(data.within365Days?data.within365Days:0);

                    },
                    error: function(xhr, status, error) {
                        console.log(xhr);
                        console.log(status);
                        console.log(error);
                        // Expiration
                        $('#expiredStockCount').html("ERROR: " + error);
                        $('#expiringIn30DaysStockCount').html("ERROR: " + error);
                        $('#expiringIn60DaysStockCount').html("ERROR: " + error);
                        $('#expiringIn90DaysStockCount').html("ERROR: " + error);
                        $('#expiringIn180DaysStockCount').html("ERROR: " + error);
                        $('#expiringIn365DaysStockCount').html("ERROR: " + error);
                    }
                });

                $( "#progressbar" ).progressbar({ value: 0 });
                $( "#progressPercentage").html('Loading...')
                $.ajax({
                    dataType: "json",
                    timeout: 60000,
                    url: "${request.contextPath}/json/getTotalStockValue?location.id=${session.warehouse.id}",
                    //data: data,
                    success: function (data) {
                        console.log(data);
                        var value = data.totalStockValue?formatCurrency(data.totalStockValue.toFixed(0)):0;
                        var progress = data.hitCount / data.totalCount
                        $('#totalStockValue').html(value);
                        if (progress < 0.1) {
                            $("#totalStockSummary").html("* Pricing data is available for less than 10% of all products");
                        }
                        else if (progress < 0.25) {
                            $("#totalStockSummary").html("* Pricing data is available for less than 25% of all products");
                        }
                        else if (progress < 0.5) {
                            $("#totalStockSummary").html("* Pricing data is available for less than 50% of all products");
                        }
                        else if (progress < 0.75) {
                            $("#totalStockSummary").html("* Pricing data is available for less than 75% of all products");
                        }
                        else if (progress < 0.90) {
                            $("#totalStockSummary").html("* Pricing data is available for less than 90% of all products");
                        }
                        else {
                            $("#totalStockSummary").html("* Pricing data is available for more than 90% of all products");
                        }
                        var progressSummary = data.hitCount + " out of " + data.totalCount;
                        var progressPercentage = progress*100;
                        $( "#progressbar" ).progressbar({ value: progressPercentage });
                        $( "#progressPercentage").html("<span title='" + progressSummary + "'>" + formatPercentage(progressPercentage) + "</span>");

                    },
                    error: function(xhr, status, error) {
                        //console.log(xhr);
                        //console.log(status);
                        //console.log(error);
                        $('#totalStockValue').html('<span class="error" title="' + error + '">N/A</span>');
                        $("#totalStockSummary").html('Unable to calculate total value due to error: ' + error + " " + status + " " + xhr);
                    }
                });
                function formatPercentage(x) {
                    return x.toFixed(0) + "%"
                }

                function formatCurrency(x) {
                    return "$" + x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
                }

                //getIndicator("#reconditionedStockCount", "${request.contextPath}/json/getReconditionedStockCount?location.id=${session.warehouse.id}", 1000, "error");
                //$('#reconditionedStockCount').load('${request.contextPath}/json/getReconditionedStockCount?location.id=${session.warehouse.id}');
                //$('#totalStockCount').load('${request.contextPath}/json/getTotalStockCount?location.id=${session.warehouse.id}');
                //$('#inStockCount').load('${request.contextPath}/json/getInStockCount?location.id=${session.warehouse.id}');
                //$('#outOfStockCount').load('${request.contextPath}/json/getOutOfStockCount?location.id=${session.warehouse.id}');
                //$('#lowStockCount').load('${request.contextPath}/json/getLowStockCount?location.id=${session.warehouse.id}');
                //$('#overStockCount').load('${request.contextPath}/json/getOverStockCount?location.id=${session.warehouse.id}');
                //$('#reorderStockCount').load('${request.contextPath}/json/getReorderStockCount?location.id=${session.warehouse.id}');
                //$('#expiredStockCount').load('${request.contextPath}/json/getExpiredStockCount?location.id=${session.warehouse.id}');
                //$('#expiringIn30DaysStockCount').load('${request.contextPath}/json/getExpiringStockCount?location.id=${session.warehouse.id}&daysUntilExpiry=30');
                //$('#expiringIn90DaysStockCount').load('${request.contextPath}/json/getExpiringStockCount?location.id=${session.warehouse.id}&daysUntilExpiry=90');
                //$('#expiringIn180DaysStockCount').load('${request.contextPath}/json/getExpiringStockCount?location.id=${session.warehouse.id}&daysUntilExpiry=180');
                //$('#expiringIn365DaysStockCount').load('${request.contextPath}/json/getExpiringStockCount?location.id=${session.warehouse.id}&daysUntilExpiry=365');

                $(".spinner").click(function() {
                    $(this).hide();
                });

                $("#tagcloud a").tagcloud({
                    size: {
                        start:1.0,
                        end: 1.5,
                        unit: 'em'
                    },
                    color: {
                        start: "#aaa", // "#CDE"
                        end: "#F52"//"#FS2"
                    }
                });

            });


            function showIndicators() {
                console.log("test");
            }
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

