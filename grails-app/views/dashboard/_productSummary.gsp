<div class="box">
    <h2><warehouse:message code="inventory.productSummary.label" default="Inventory status by product"/></h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="alertSummary" class="list">

    		<table class="zebra">
    			<tbody>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${resource(dir:'images/icons/silk/exclamation.png')}" class="middle" title='${warehouse.message(code:"inventory.alerts.label",default:"Critical")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listOutOfStock" target="_blank">
                            <warehouse:message code="inventory.listOutOfStock.label" default="Items that have stocked out"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="outOfStockCount"><img class="spinner" src="${resource(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>

                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${resource(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warnings.label",default:"Warning")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listLowStock" target="_blank">
                            <warehouse:message code="inventory.listLowStock.label" default="Items that are below minimum level"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="lowStockCount"><img class="spinner" src="${resource(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${resource(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warnings.label",default:"Warning")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listReorderStock" target="_blank">
                            <warehouse:message code="inventory.listReorderStock.label" default="Items that are below reorder level"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="reorderStockCount"><img class="spinner" src="${resource(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${resource(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warning.label",default:"Warning")}'/>

                    </td>
                    <td>
                        <g:link controller="inventory" action="listOverStock" target="_blank">
                            <warehouse:message code="inventory.listOverStock.label" default="Items that are over stocked"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="overStockCount"><img class="spinner" src="${resource(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${resource(dir:'images/icons/silk/accept.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"information")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listTotalStock" target="_blank">
                            <warehouse:message code="inventory.listTotalStock.label" default="Items that have ever been stocked"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="totalStockCount"><img class="spinner" src="${resource(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>

                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${resource(dir:'images/icons/silk/accept.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"information")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listInStock" target="_blank">
                            <warehouse:message code="inventory.listInStock.label" default="Items that are currently stocked"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="inStockCount"><img class="spinner" src="${resource(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${resource(dir:'images/icons/silk/information.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"information")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listQuantityOnHandZero" target="_blank">
                            <warehouse:message code="inventory.listQuantityOnHandZero.label" default="Items that have QoH equal to zero"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="onHandQuantityZeroCount"><img class="spinner" src="${resource(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>

                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${resource(dir:'images/icons/silk/information.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"information")}'/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listReconditionedStock" target="_blank">
                                <warehouse:message code="inventory.listReconditionedStock.label" default="Items that need to be reconditioned"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="reconditionedStockCount"><img class="spinner" src="${resource(dir:'images/spinner.gif')}" class="middle"/></div>

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
            url: "${request.contextPath}/json/getDashboardAlerts?location.id=${session.warehouse.id}",
            //data: data,
            success: function (data) {
                console.log(data);
                var outOfStockCount = data.outOfStock?data.outOfStock:0;
                var lowStockCount = data.lowStock?data.lowStock:0;
                var reorderStockCount = data.reorderStock?data.reorderStock:0;
                var inStockCount = data.inStock?data.inStock:0;
                var overStockCount = data.overStock?data.overStock:0;

                var totalStockCount = data.totalStock?data.totalStock:0;
                var onHandQuantityZeroCount = data.onHandQuantityZero?data.onHandQuantityZero:0;
                var reconditionedStockCount = data.reconditionedStock?data.reconditionedStock:0;


                $("#outOfStockCount").html("<a href='${request.contextPath}/inventory/listOutOfStock' target='_blank'>" + outOfStockCount + "</a>");
                $('#outOfStockCount').html("<a href='${request.contextPath}/inventory/listOutOfStock' target='_blank'>" + outOfStockCount + "</a>");
                $('#lowStockCount').html("<a href='${request.contextPath}/inventory/listLowStock' target='_blank'>" + lowStockCount + "</a>");
                $('#reorderStockCount').html("<a href='${request.contextPath}/inventory/listReorderStock' target='_blank'>" + reorderStockCount + "</a>");
                $('#inStockCount').html("<a href='${request.contextPath}/inventory/listInStock' target='_blank'>" + inStockCount + "</a>");
                $('#overStockCount').html("<a href='${request.contextPath}/inventory/listOverStock' target='_blank'>" + overStockCount + "</a>");

                $('#totalStockCount').html("<a href='${request.contextPath}/inventory/listTotalStock' target='_blank'>" + totalStockCount + "</a>");
                $('#onHandQuantityZeroCount').html("<a href='${request.contextPath}/inventory/listQuantityOnHandZero' target='_blank'>" + onHandQuantityZeroCount + "</a>");
                $('#reconditionedStockCount').html("<a href='${request.contextPath}/inventory/listReconditionedStock' target='_blank'>" + reconditionedStockCount + "</a>");
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
                $('#lowStockCount').html("ERROR " + error);
                $('#overStockCount').html("ERROR " + error);
                $('#inStockCount').html("ERROR " + error);
                $('#reconditionedStockCount').html("ERROR " + error);
                $('#onHandQuantityZeroCount').html("ERROR " + error);
                $('#totalStockCount').html("ERROR " + error);

            }
        });
    });
</script>