<div class="box">
    <h2><warehouse:message code="inventory.alerts.label" default="Alerts"/></h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="alertSummary">	

    		<table class="zebra">
    			<tbody>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/exclamation.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listOutOfStock">
                                <warehouse:message code="inventory.listOutOfStock.label" default="Items that have stocked out"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="outOfStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listLowStock">
                                <warehouse:message code="inventory.listLowStock.label" default="Items that are below minimum level"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="lowStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                        </td>
                    </tr>

					<tr>
						<td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/creditcards.png')}" class="middle"/>
						</td>
						<td>
                            <g:link controller="inventory" action="listReorderStock">
                                <warehouse:message code="inventory.listReorderStock.label" default="Items that are below reorder level"/>
							</g:link>
						</td>
						<td class="right">
							<div id="reorderStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
						</td>
					</tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/package.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listOverStock">
                                <warehouse:message code="inventory.listOverStock.label" default="Items that are over stocked"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="overStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                        </td>
                    </tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/box.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listQuantityOnHandZero">
                                <warehouse:message code="inventory.listQuantityOnHandZero.label" default="Items that have QoH equal to zero"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="onHandQuantityZeroCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

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
            success: function (data) {
                console.log(data);
                var outOfStockCount = data.outOfStock?data.outOfStock:0;
                var lowStockCount = data.lowStock?data.lowStock:0;
                var overStockCount = data.outOfStock?data.outOfStock:0;
                var reconditionedStockCount = data.outOfStock?data.outOfStock:0;
                var totalStockCount = data.outOfStock?data.outOfStock:0;
                var totalStockCount = data.outOfStock?data.outOfStock:0;

                $("#outOfStockCount").html("<a href='${request.contextPath}/inventory/listOutOfStock>" + count + "</a>");
                $('#lowStockCount').html();
                $('#overStockCount').html(data.overStock?data.overStock:0);
                $('#reconditionedStockCount').html(data.reconditionedStock?data.reconditionedStock:0);
                $('#totalStockCount').html(data.totalStock?data.totalStock:0);
                $('#inStockCount').html(data.inStock?data.inStock:0);
                $('#onHandQuantityZeroCount').html(data.onHandQuantityZero?data.onHandQuantityZero:0);
                $('#outOfStockCount').html();
                $('#reorderStockCount').html(data.reorderStock?data.reorderStock:0);

            },
            error: function(xhr, status, error) {
                console.log(xhr);
                console.log(status);
                console.log(error);
                $('#lowStockCount').html("ERROR " + error);
                $('#overStockCount').html("ERROR " + error);
                $('#reconditionedStockCount').html("ERROR " + error);
                $('#onHandQuantityZeroCount').html("ERROR " + error);
                $('#totalStockCount').html("ERROR " + error);
                $('#inStockCount').html("ERROR " + error);
                $('#outOfStockCount').html("ERROR " + error);
                $('#reorderStockCount').html("ERROR " + error);

            }
        });
    });
</script>