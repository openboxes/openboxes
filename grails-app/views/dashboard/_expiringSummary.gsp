<div class="box">
	<h2><warehouse:message code="dashboard.expirationSummary.label" /></h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="expirationSummary">

    		<table class="zebra">
    			<tbody>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/', file: 'accept.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listExpiringStock" params="[threshold:365]">
                                <warehouse:message code="inventory.listExpiringStock.label" args="[365]" default="Items that will expire within {0} days"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="expiringIn365DaysStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                        </td>
                    </tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk', file: 'error.png')}" class="middle"/>
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
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle"/>
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

                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle"/>
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

                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/exclamation.png')}" class="middle"/>
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


                </tbody>
			</table>
		</div>
	</div>
</div>
<script>
    $(window).load(function(){

        // Pull the data from the server
        $.ajax({
            dataType: "json",
            timeout: 120000,
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
                $('#expiringEverStockCount').html(data.within365Days?data.within365Days:0);

            },
            error: function(xhr, status, error) {
                var errorMessage = "An unexpected error has occurred";
                if (xhr.responseText) {
                    var errorJson = JSON.parse(xhr.responseText);
                    errorMessage += ":\n" + errorJson.errorMessage;
                }

                var errorHtml = "<img src='${createLinkTo(dir:'images/icons/silk/exclamation.png')}' title='" + errorMessage +"'/>";
                $('#expiredStockCount').html(errorHtml);
                $('#expiringIn30DaysStockCount').html(errorHtml);
                $('#expiringIn60DaysStockCount').html(errorHtml);
                $('#expiringIn90DaysStockCount').html(errorHtml);
                $('#expiringIn180DaysStockCount').html(errorHtml);
                $('#expiringIn365DaysStockCount').html(errorHtml);
                $('#expiringEverStockCount').html(errorHtml);
            }
        });
    });
</script>