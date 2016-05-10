<div class="box">
    <h2><warehouse:message code="dashboard.productSummary.label" default="Product Summary"/></h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="alertSummary" class="list">

    		<table class="zebra">
                <thead>
                    <tr class="prop odd">
                        <td colspan="3">
                            <label class="fade lastUpdated"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></label>
                        </td>
                    </tr>
                </thead>
    			<tbody>
                <tr class="prop">
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/information.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"information")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listInStock" target="_blank">
                            <label><warehouse:message code="inventory.listInStock.label" default="Items that are currently stocked"/></label>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="inStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">

                    </td>
                    <td>
                        <img src="${createLinkTo(dir:'images/icons/silk/heart.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"Information")}'/>
                        <g:link controller="inventory" action="listHealthyStock" target="_blank">
                            <warehouse:message code="inventory.listHealthyStock.label" default="Healthy stock"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="healthyStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                    </td>
                    <td>
                        <img src="${createLinkTo(dir:'images/icons/silk/accept.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"Information")}'/>
                        <g:link controller="inventory" action="listOverStock" target="_blank">
                            <warehouse:message code="inventory.listOverStock.label" default="Items that are over stocked"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="overStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                    </td>
                    <td>
                        <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warnings.label",default:"Warning")}'/>
                        <g:link controller="inventory" action="listReorderStock" target="_blank">
                            <warehouse:message code="inventory.listReorderStock.label" default="Items that are below reorder level"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="reorderStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                    </td>
                    <td class="indent">
                        <img src="${createLinkTo(dir:'images/icons/silk/exclamation.png')}" class="middle" title='${warehouse.message(code:"inventory.warnings.label",default:"Warning")}'/>
                        <g:link controller="inventory" action="listLowStock" target="_blank">
                            <warehouse:message code="inventory.listLowStock.label" default="Items that are below minimum level"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="lowStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/information.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"information")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listQuantityOnHandZero" target="_blank">
                            <label><warehouse:message code="inventory.listQuantityOnHandZero.label" default="Items that have QoH equal to zero"/></label>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="onHandQuantityZeroCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>


                <tr>
                    <td class="center" style="width: 1%">
                    </td>
                    <td>
                        <img src="${createLinkTo(dir:'images/icons/silk/stop.png')}" class="middle" title='${warehouse.message(code:"inventory.alerts.label",default:"Critical")}'/>
                        <g:link controller="inventory" action="listOutOfStock" target="_blank">
                            <warehouse:message code="inventory.listOutOfStock.label" default="Items that have stocked out"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="outOfStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>




				</tbody>
                <tfoot>

                    <tr>
                        <th class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/sum.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"information")}'/>
                        </th>
                        <th>
                            <g:link controller="inventory" action="listTotalStock" target="_blank">
                                <label><warehouse:message code="default.total.label" /></label>
                            </g:link>
                        </th>
                        <th class="right">
                            <div id="totalStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

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
            timeout: 120000,
            url: "${request.contextPath}/json/getDashboardAlerts?location.id=${session.warehouse.id}",
            //data: data,
            success: function (data) {
                console.log(data);
                var outOfStockCount = data.outOfStock?data.outOfStock:0;
                var lowStockCount = data.lowStock?data.lowStock:0;
                var reorderStockCount = data.reorderStock?data.reorderStock:0;
                var inStockCount = data.inStock?data.inStock:0;
                var overStockCount = data.overStock?data.overStock:0;
                var healthyStockCount = data.healthyStock?data.healthyStock:0;
                var totalStockCount = data.totalStock?data.totalStock:0;
                var onHandQuantityZeroCount = data.onHandQuantityZero?data.onHandQuantityZero:0;
                //var reconditionedStockCount = data.reconditionedStock?data.reconditionedStock:0;

                var keys = Object.keys(data);
                for (var i = 0; i < keys.length; i++) {
                    console.log(data[keys[i]]);
                }


                $("#healthyStockCount").html("<a href='${request.contextPath}/inventory/listHealthyStock' target='_blank'>" + healthyStockCount + "</a>");
                $('#outOfStockCount').html("<a href='${request.contextPath}/inventory/listOutOfStock' target='_blank'>" + outOfStockCount + "</a>");
                $('#lowStockCount').html("<a href='${request.contextPath}/inventory/listLowStock' target='_blank'>" + lowStockCount + "</a>");
                $('#reorderStockCount').html("<a href='${request.contextPath}/inventory/listReorderStock' target='_blank'>" + reorderStockCount + "</a>");
                $('#inStockCount').html("<a href='${request.contextPath}/inventory/listInStock' target='_blank'>" + inStockCount + "</a>");
                $('#overStockCount').html("<a href='${request.contextPath}/inventory/listOverStock' target='_blank'>" + overStockCount + "</a>");

                $('#totalStockCount').html("<a href='${request.contextPath}/inventory/listTotalStock' target='_blank'>" + totalStockCount + "</a>");
                $('#onHandQuantityZeroCount').html("<a href='${request.contextPath}/inventory/listQuantityOnHandZero' target='_blank'>" + onHandQuantityZeroCount + "</a>");
                //$('#reconditionedStockCount').html("<a href='${request.contextPath}/inventory/listReconditionedStock' target='_blank'>" + reconditionedStockCount + "</a>");
                //$('#outOfStockCountClassA').html(data.outOfStockClassA?data.outOfStockClassA:0);
                //$('#outOfStockCountClassB').html(data.outOfStockClassB?data.outOfStockClassB:0);
                //$('#outOfStockCountClassC').html(data.outOfStockClassC?data.outOfStockClassC:0);
                //$('#outOfStockCountClassNone').html(data.outOfStockClassNone?data.outOfStockClassNone:0);

            },
            error: function(xhr, status, error) {
                console.log(xhr);
                console.log(status);
                console.log(error);
                $('#outOfStockCount').html("ERROR " + error);
                $('#reorderStockCount').html("ERROR " + error);
                $('#healthyStockCount').html("ERROR " + error);
                $('#lowStockCount').html("ERROR " + error);
                $('#overStockCount').html("ERROR " + error);
                $('#inStockCount').html("ERROR " + error);
                //$('#reconditionedStockCount').html("ERROR " + error);
                $('#onHandQuantityZeroCount').html("ERROR " + error);
                $('#totalStockCount').html("ERROR " + error);

            }
        });
    });
</script>