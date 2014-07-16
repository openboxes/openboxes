<div class="box">
	<h2><warehouse:message code="inventory.expiring.label" default="Expiring inventory"/></h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="alertSummary">	

    		<table class="zebra">
    			<tbody>

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
                </tbody>
			</table>
		</div>
	</div>
</div>
<script>
    $(window).load(function(){
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

            },
            error: function(xhr, status, error) {
                console.log(xhr);
                console.log(status);
                console.log(error);
                // Expiration
                $('#expiredStockCount').html("ERROR " + error);
                $('#expiringIn30DaysStockCount').html("ERROR " + error);
                $('#expiringIn60DaysStockCount').html("ERROR " + error);
                $('#expiringIn90DaysStockCount').html("ERROR " + error);
                $('#expiringIn180DaysStockCount').html("ERROR " + error);
                $('#expiringIn365DaysStockCount').html("ERROR " + error);
            }
        });
    });
</script>