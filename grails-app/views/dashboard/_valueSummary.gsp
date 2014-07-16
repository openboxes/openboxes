<g:isUserAdmin>
<div class="box">
    <h2><warehouse:message code="inventory.value.label" default="Stock value"/></h2>
    <div class="widget-content" style="padding:0; margin:0">
        <div id="alertSummary">

            <table class="zebra">
                <tbody>
                    <tr>
                        <td>
                            <img src="${createLinkTo(dir:'images/icons/silk/sum.png')}" class="middle"/>
                        </td>
                        <td>
                            <div># of products with pricing information</div>
                        </td>
                        <td>
                            <div id="progressSummary" class="right">
                                <img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <img src="${createLinkTo(dir:'images/icons/silk/chart_pie.png')}" class="middle"/>
                        </td>
                        <td>
                            <div>Percentage of products with pricing information</div>
                        </td>
                        <td>
                            <div id="progressPercentage" class="right">
                                <img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/money.png')}" class="middle"/>
                        </td>
                        <td>
                            <warehouse:message code="inventory.totalStockValue.label" default="Total value of inventory"/>
                        </td>
                        <td class="right">
                            <div id="totalStockValue">
                                <img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
                            </div>
                        </td>
                    </tr>
                <%--
                    <tr>
                        <td colspan="3">
                            <div id="progressbar"></div>
                        </td>
                    </tr>
                    --%>
                </tbody>
                <tfoot>
                    <tr class="odd">
                        <td colspan="3">
                            <span id="totalStockSummary" class="fade"></span>
                        </td>
                    </tr>

                </tfoot>
            </table>
        </div>
    </div>
</div>
<script type="text/javascript">

    $(window).load(function(){
        $( "#progressbar" ).progressbar({ value: 0 });
        //$( "#progressPercentage").html('')
        $.ajax({
            dataType: "json",
            timeout: 120000,
            url: "${request.contextPath}/json/getTotalStockValue?location.id=${session.warehouse.id}",
            //data: data,
            success: function (data) {
                console.log(data);
                var value = data.totalStockValue?formatCurrency(data.totalStockValue.toFixed(0)):0;
                var progress = data.hitCount / data.totalCount
                $('#totalStockValue').html(value);

                if (progress < 0.1) {
                    $("#totalStockSummary").html("* Pricing data is available for less than 10% of all products");
                }
                else if (progress < 0.25) {
                    $("#totalStockSummary").html("* Pricing data is available for less than 25% of all products");
                }
                else if (progress < 0.5) {
                    $("#totalStockSummary").html("* Pricing data is available for less than 50% of all products");
                }
                else if (progress < 0.75) {
                    $("#totalStockSummary").html("* Pricing data is available for less than 75% of all products");
                }
                else if (progress < 0.90) {
                    $("#totalStockSummary").html("* Pricing data is available for less than 90% of all products");
                }
                else {
                    $("#totalStockSummary").html("* Pricing data is available for more than 90% of all products");
                }
                var progressSummary = data.hitCount + " out of " + data.totalCount;
                $('#progressSummary').html(progressSummary);
                var progressPercentage = progress*100;
                $( "#progressbar" ).progressbar({ value: progressPercentage });
                $( "#progressPercentage").html("<span title='" + progressSummary + "'>" + formatPercentage(progressPercentage) + "</span>");

            },
            error: function(xhr, status, error) {
                //console.log(xhr);
                //console.log(status);
                //console.log(error);
                $('#totalStockValue').html('ERROR');
                $("#totalStockSummary").html('Unable to calculate total value due to error: ' + error + " " + status + " " + xhr);
            }
        });
    });
    function formatPercentage(x) {
        return x.toFixed(0) + "%"
    }

    function formatCurrency(x) {
        return "$" + x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }
</script>
</g:isUserAdmin>

