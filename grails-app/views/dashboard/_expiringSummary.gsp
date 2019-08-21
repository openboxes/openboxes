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
                            <g:link controller="inventory" action="listExpiringStock" params="[status:'greaterThan365Days']">
                                <warehouse:message code="inventory.listGreaterThan365Days.label" args="[365]" default="Expires after {0} days"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="greaterThan365Days"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                        </td>
                    </tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/', file: 'error.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listExpiringStock" params="[status:'within365Days']">
                                <warehouse:message code="inventory.listExpiringStock.label" args="[365]" default="Items that will expire within {0} days"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="within365Days"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                        </td>
                    </tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk', file: 'error.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listExpiringStock" params="[status:'within180Days']">
                                <warehouse:message code="inventory.listExpiringStock.label" args="[180]" default="Items that will expire within {0} days"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="within180Days"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listExpiringStock" params="[status:'within90Days']">
                                <warehouse:message code="inventory.listExpiringStock.label" args="[90]" default="Items that will expire within {0} days"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="within90Days"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>

                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listExpiringStock" params="[status:'within30Days']">
                                <warehouse:message code="inventory.listExpiringStock.label" args="[30]" default="Items that will expire within {0} days"/>

                            </g:link>
                        </td>
                        <td class="right">
                            <div id="within30Days"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                        </td>
                    </tr>

                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/exclamation.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listExpiredStock" params="[status:'expired']">
                                <warehouse:message code="inventory.listExpiredStock.label" default="Items that have expired"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="expired">
                                <img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
                            </div>
                        </td>
                    </tr>


                </tbody>
                <tfoot>
                <tr>
                    <th class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/sum.png')}" class="middle"
                             title='${warehouse.message(code:"inventory.information.label",default:"information")}'/>
                    </th>
                    <th>
                        <g:link controller="inventory" action="listTotalStock" target="_blank">
                            <g:message code="default.total.label" />
                        </g:link>
                    </th>
                    <th class="right">
                        <div id="totalExpiring"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </th>
                </tr>
                </tfoot>
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
            success: function (data) {
                console.log(data);
                // Expiration
                $('#expired').html(data.expired?data.expired:0);
                $('#within30Days').html(data.within30Days?data.within30Days:0);
                $('#within60Days').html(data.within60Days?data.within60Days:0);
                $('#within90Days').html(data.within90Days?data.within90Days:0);
                $('#within180Days').html(data.within180Days?data.within180Days:0);
                $('#within365Days').html(data.within365Days?data.within365Days:0);
                $('#greaterThan365Days').html(data.greaterThan365Days?data.greaterThan365Days:0);
                $('#totalExpiring').html(data.totalExpiring?data.totalExpiring:0);


            },
            error: function(xhr, status, error) {
                var errorMessage = "An unexpected error has occurred";
                if (xhr.responseText) {
                    var errorJson = JSON.parse(xhr.responseText);
                    errorMessage += ":\n" + errorJson.errorMessage;
                }

                var errorHtml = "<img src='${createLinkTo(dir:'images/icons/silk/exclamation.png')}' title='" + errorMessage +"'/>";
                $('#expired').html(errorHtml);
                $('#within30Days').html(errorHtml);
                $('#within60Days').html(errorHtml);
                $('#within90Days').html(errorHtml);
                $('#within180Days').html(errorHtml);
                $('#within365Days').html(errorHtml);
                $('#greaterThan365Days').html(errorHtml);
                $('#totalExpiring').html(errorHtml);
            }
        });
    });
</script>