<div class="box">
    <h2>
        <g:message code="dashboard.genericProductSummary.label"/>
    </h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="genericProductSummary">
    		<table class="zebra">
    			<tbody>
                <tr class="prop">
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/accept.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"Information")}'/>
                    </td>

                    <td>
                        <g:link controller="dashboard" action="downloadGenericProductSummaryAsCsv" params="[status:'IN_STOCK']">
                            <warehouse:message code="inventory.listInStock.label" />
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="IN_STOCK" class="indicator"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/accept.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"Information")}'/>
                    </td>

                    <td>
                        <g:link controller="dashboard" action="downloadGenericProductSummaryAsCsv" params="[status:'OVERSTOCK']">
                            <warehouse:message code="inventory.listOverStock.label" />
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="OVERSTOCK" class="indicator"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warning.label",default:"Warning")}'/>
                    </td>

                    <td>
                        <g:link controller="dashboard" action="downloadGenericProductSummaryAsCsv" params="[status:'REORDER']">
                            <warehouse:message code="inventory.listReorderStock.label" default="Items that are below reorder level"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="REORDER" class="indicator"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>

                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warning.label",default:"Warning")}'/>
                    </td>

                    <td>
                        <g:link controller="dashboard" action="downloadGenericProductSummaryAsCsv" params="[status:'LOW_STOCK']">
                            <warehouse:message code="inventory.listLowStock.label" default="Items that are below minimum level"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="LOW_STOCK" class="indicator"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>


                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warning.label",default:"Warning")}'/>
                    </td>
                    <td>
                        <g:link controller="dashboard" action="downloadGenericProductSummaryAsCsv" params="[status:'STOCK_OUT_OBSOLETE']">
                            <warehouse:message code="inventory.listOutOfStockObsolete.label" default="Stocked out, but obsolete"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="STOCK_OUT_OBSOLETE" class="indicator"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>

				</tbody>
                <tfoot>
                    <tr>
                        <th class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/sum.png')}" class="middle" title='${warehouse.message(code:"default.total.label",default:"Total")}'/>
                        </th>
                        <th>
                            <g:link controller="dashboard" action="downloadGenericProductSummaryAsCsv" params="[status:'ALL']">
                                <warehouse:message code="default.total.label" />
                            </g:link>
                        </th>
                        <th class="right">
                            <div id="TOTAL"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                        </th>
                    </tr>

                </tfoot>
			</table>
		</div>
	</div>
</div>

<script>
    $(window).load(function(){

        // Sort the rows in reverse
        $("#genericProductSummary table tbody").each(function(elem,index){
            var arr = $.makeArray($("tr",this).detach());
            arr.reverse();
            $(this).append(arr);
        });

        // Pull the data from the server
        $.ajax({
            dataType: "json",
            timeout: 120000,
            url: "${request.contextPath}/json/getGenericProductSummary?location.id=${session.warehouse.id}",
            success: function (data) {
                console.log(data);
                var totalCount =0
                $(".indicator").each(function( index ) {
                    var status = this.id;
                    var count = (data.genericProductByStatusMap[status])?data.genericProductByStatusMap[status]:0;
                    totalCount += count;
                    $("#" + status).html("<a href='${request.contextPath}/dashboard/downloadGenericProductSummaryAsCsv?status=" + status + "'>" + count + "</a>");
                });
                $("#TOTAL").html("<a href='${request.contextPath}/dashboard/downloadGenericProductSummaryAsCsv?status=ALL'>" + totalCount + "</a>");
            },
            error: function(xhr, status, error) {
                var errorMessage = "An unexpected error has occurred";
                if (xhr.responseText) {
                    var errorJson = JSON.parse(xhr.responseText);
                    errorMessage += ":\n" + errorJson.errorMessage;
                }

                var errorHtml = "<img src='${createLinkTo(dir:'images/icons/silk/exclamation.png')}' title='" + errorMessage +"'/>";
                $(".indicator").each(function( index ) {
                    $("#" + this.id).html(errorHtml);
                });
                $("#TOTAL").html(errorHtml);
            }
        });
    });
</script>