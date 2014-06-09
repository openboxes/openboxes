<div class="box">
    <h2><warehouse:message code="inventory.genericProductSummary.label" default="Inventory status by generic product"/></h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="alertSummary">
    		<table class="zebra">
    			<tbody>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/exclamation.png')}" class="middle" title='${warehouse.message(code:"inventory.alerts.label",default:"Critical")}'/>
                    </td>

                    <td>
                        <g:link controller="json" action="listByProductGroup" params="[status:'STOCK_OUT']">
                            <warehouse:message code="inventory.listOutOfStock.label" default="Items that have stocked out"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="STOCK_OUT" class="indicator"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warning.label",default:"Warning")}'/>
                    </td>
                    <td>
                        <g:link controller="json" action="listByProductGroup" params="[status:'STOCK_OUT_OBSOLETE']">
                            <warehouse:message code="inventory.listOutOfStockObsolete.label" default="Stocked out, but obsolete"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="STOCK_OUT_OBSOLETE" class="indicator"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warning.label",default:"Warning")}'/>
                    </td>

                    <td>
                        <g:link controller="json" action="listByProductGroup" params="[status:'LOW_STOCK']">
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
                        <g:link controller="json" action="listByProductGroup" params="[status:'REORDER']">
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
                        <g:link controller="json" action="listByProductGroup" params="[status:'OVERSTOCK']">
                            <warehouse:message code="inventory.listOverStock.label" />
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="OVERSTOCK" class="indicator"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/accept.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"Information")}'/>
                    </td>

                    <td>
                        <g:link controller="json" action="listByProductGroup" params="[status:'IN_STOCK']">
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
                        <g:link controller="json" action="listByProductGroup" params="[status:'IN_STOCK_OBSOLETE']">
                            <warehouse:message code="inventory.listInStockObsolete.label" default="In stock, but obsolete"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="IN_STOCK_OBSOLETE" class="indicator"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>




				</tbody>
                <tfoot>
                    <tr>
                        <th class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/sum.png')}" class="middle" title='${warehouse.message(code:"default.total.label",default:"Total")}'/>
                        </th>
                        <th>
                            <g:link controller="json" action="listByProductGroup" params="[status:'ALL']">
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

        $.ajax({
            dataType: "json",
            timeout: 60000,
            url: "${request.contextPath}/json/getProductGroupAlerts?location.id=${session.warehouse.id}",
            success: function (data) {
                console.log(data);
                var totalCount =0
                $(".indicator").each(function( index ) {
                    var status = this.id;
                    var count = (data.productGroupByStatusMap[status])?data.productGroupByStatusMap[status].length:0;
                    totalCount += count;
                    var countLink =

                    $("#" + status).html("<a href='${request.contextPath}/json/listByProductGroup?status=" + status + "'>" + count + "</a>");
                });
                $("#TOTAL").html("<a href='${request.contextPath}/json/listByProductGroup?status=ALL'>" + totalCount + "</a>");
            },
            error: function(xhr, status, error) {

            }
        });
    });
</script>