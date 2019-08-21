<div class="box">
    <h2><warehouse:message code="dashboard.productSummary.label" /></h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="productSummary" class="list">

    		<table class="zebra">
    			<tbody>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/exclamation.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"information")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listQuantityOnHandZero" target="_blank">
                            <warehouse:message code="inventory.listQuantityOnHandZero.label" default="Items that have QoH equal to zero"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="onHandQuantityZeroCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warnings.label",default:"Warning")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listLowStock" target="_blank">
                            <g:message code="inventory.listLowStock.label"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="lowStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warnings.label",default:"Warning")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listReorderStock" target="_blank">
                            <g:message code="inventory.listReorderStock.label" />
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="reorderStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle" title='${warehouse.message(code:"inventory.warning.label",default:"Warning")}'/>

                    </td>
                    <td>
                        <g:link controller="inventory" action="listOverStock" target="_blank">
                            <g:message code="inventory.listOverStock.label"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="overStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                    </td>
                </tr>
                <tr>
                    <td class="center" style="width: 1%">
                        <img src="${createLinkTo(dir:'images/icons/silk/accept.png')}" class="middle" title='${warehouse.message(code:"inventory.information.label",default:"information")}'/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="listInStock" target="_blank">
                            <g:message code="inventory.listInStock.label"/>
                        </g:link>
                    </td>
                    <td class="right">
                        <div id="inStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

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

        // Sort the rows in reverse
        $("#productSummary table tbody").each(function(elem,index){
            var arr = $.makeArray($("tr",this).detach());
            arr.reverse();
            $(this).append(arr);
        });

        // Pull the data from the server
        $.ajax({
            dataType: "json",
            timeout: 120000,
            url: "${request.contextPath}/json/getDashboardAlerts?location.id=${session.warehouse.id}",
            success: function (data) {
                console.log(data);
                var inStockCount = data.inStock?data.inStock:0;
                var lowStockCount = data.lowStock?data.lowStock:0;
                var reorderStockCount = data.reorderStock?data.reorderStock:0;
                var overStockCount = data.overStock?data.overStock:0;
                var totalStockCount = data.totalStock?data.totalStock:0;
                var onHandQuantityZeroCount = data.onHandQuantityZero?data.onHandQuantityZero:0;

                $('#inStockCount').html("<a href='${request.contextPath}/inventory/listInStock' target='_blank'>" + inStockCount + "</a>");
                $('#lowStockCount').html("<a href='${request.contextPath}/inventory/listLowStock' target='_blank'>" + lowStockCount + "</a>");
                $('#reorderStockCount').html("<a href='${request.contextPath}/inventory/listReorderStock' target='_blank'>" + reorderStockCount + "</a>");
                $('#overStockCount').html("<a href='${request.contextPath}/inventory/listOverStock' target='_blank'>" + overStockCount + "</a>");
                $('#totalStockCount').html("<a href='${request.contextPath}/inventory/listTotalStock' target='_blank'>" + totalStockCount + "</a>");
                $('#onHandQuantityZeroCount').html("<a href='${request.contextPath}/inventory/listQuantityOnHandZero' target='_blank'>" + onHandQuantityZeroCount + "</a>");

            },
            error: function(xhr, status, error) {
                var errorMessage = "An unexpected error has occurred";
                if (xhr.responseText) {
                    var errorJson = JSON.parse(xhr.responseText);
                    errorMessage += ":\n" + errorJson.errorMessage;
                }

                var errorHtml = "<img src='${createLinkTo(dir:'images/icons/silk/exclamation.png')}' title='" + errorMessage +"'/>";
                $('#reorderStockCount').html(errorHtml);
                $('#lowStockCount').html(errorHtml);
                $('#inStockCount').html(errorHtml);
                $('#overStockCount').html(errorHtml);
                $('#onHandQuantityZeroCount').html(errorHtml);
            }
        });
    });
</script>