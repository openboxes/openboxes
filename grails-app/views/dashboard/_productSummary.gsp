<style>
    tr.data td { font-color: lightgrey; font-weight: normal; }
    tr.subtotal td { font-color: lightgrey; border-top: 0px solid black; font-weight: bold; }
    tr.total td { background-color: #f5f5f5; border-top: 1px solid black; font-weight: bold; }
    tr.a_normal td { background-color: #dff0d8; }
    tr.b_warning td { background-color: #fcf8e3; }
    tr.c_danger td  { background-color: #f2dede; }
</style>

<div class="box">
    <h2 id="productSummaryTitle" title="">
        <warehouse:message code="inventory.productSummary.label" default="Product Summary"/>
        <img id="product-summary-spinner" class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>

        <div class="right" style="padding: 5px;">
            <g:link controller="dashboard" action="flushCache" class="button icon reload">
                ${warehouse.message(code:'default.button.refresh.label', default: 'Refresh')}
            </g:link>
        </div>
    </h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="product-summary-details">
    		<table id="product-summary-table" class="table table-striped">
                <thead>
                    <tr>
                        <th></th>
                        <th>Status</th>
                        <th class="right" width="10%">Count</th>
                        <th class="right" width="20%">Amount (USD)</th>
                    </tr>
                </thead>
    			<tbody>
				</tbody>
			</table>
		</div>
	</div>
</div>

<script>


    $(document).ready(function(){
        $.ajax({
            dataType: "json",
            url: "${request.contextPath}/dashboard/productSummary?location.id=${session.warehouse.id}",
            //data: data,
            success: function (data) {
                console.log(data);
                if (!data.error) {
                    renderProductSummary(data.productSummary);
                    $("#productSummaryTitle").attr("title", "Database was automatically updated on " + data.lastUpdated);
                }
                else {
                    renderProductSummaryError(data);
                }

            },
            error: function(xhr, status, error) {
                var data = { message: message };
                renderProductSummaryError(data);
            }
        });
    });

    function renderProductSummary(data) {
        console.log(data);
        if (data.length == 0) {
            renderProductSummaryError({ message: "There's no data in the inventory item summary table." });
        }
        else {
            //$("#product-summary-spinner").show();

            $.each(data, function () {
                var productSummary = {
                    label: $(this).attr("label"),
                    code: $(this).attr("code"),
                    status: $(this).attr("status"),
                    subStatus: $(this).attr("subStatus"),
                    count: $(this).attr("count"),
                    cost: $(this).attr("cost"),
                    styleClass: $(this).attr("styleClass"),
                    url: $(this).attr("url")
                };

                console.log(productSummary);
                if (productSummary.styleClass != "subtotal") {
                    addProductSummary(productSummary);
                }
            });
        }
        $("#product-summary-spinner").hide();
    }


    function renderProductSummaryError(error) {
        $("#product-summary-error-template").tmpl(error).appendTo('#product-summary-table tbody');
        $("#product-summary-spinner").hide();
    }

    function addProductSummary(productSummary) {
        console.log($("#product-summary-table"))

        $("#product-summary-template").tmpl(productSummary).appendTo('#product-summary-table tbody');
    }


</script>

<script id="product-summary-template" type="x-jquery-tmpl">
    <tr class="prop {{= styleClass}} {{= code}} {{= status}}">
        <td class="center" style="width: 1%">
            <img src="{{= imgUrl}}" class="middle" title=""{{= title}}" />
        </td>
        <td>
            <a href="{{= url}}">{{= label}}</a>
        </td>
        <td class="right">
            <a href="{{= url}}">{{= count}}</a>
        </td>
        <td class="right">
            <a href="{{= url}}">{{= cost}}</a>
        </td>
    </tr>
</script>

<script id="product-summary-error-template" type="x-jquery-tmpl">
    <tr class="prop error">
        <td colspan="4" class="center">
            {{= message}}
        </td>
    </tr>
</script>
