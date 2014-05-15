<style>
    #placeholder { width: 100%; height: 400px; }
    .legend table { width: auto; }

</style>

<div id="content" class="box">
    <h2>Inventory Snapshot</h2>
    <input type="hidden" id="productId" name="productId" value="${product.id}"/>
    <input type="hidden" id="locationId" name="locationId" value="${session.warehouse.id}"/>

    <table style="width: auto;">
        <tr>
            <td class="middle right">
                <label for="numMonths">
                    <warehouse:message code="default.duration.label" default="Num of months"/></label>
            </td>
            <td class="middle">
                <g:select id="numMonths" name="numMonths" value="${params.numMonths?:12}" from="[6:'Last 6 months',12:'Last 12 months',18:'Last 18 months',24:'Last 24 months']" optionKey="key" optionValue="value"></g:select>
            </td>
        </tr>

    </table>


    <div class="demo-container">
        <div id="placeholder" class="demo-placeholder" style="height:300px;"></div>
    </div>

</div>



<%--<script src="${createLinkTo(dir:'js/flot/', file:'jquery.js')}" type="text/javascript" ></script>--%>
<script src="${createLinkTo(dir:'js/flot/', file:'jquery.flot.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/flot/', file:'jquery.flot.categories.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/flot/', file:'jquery.flot.resize.js')}" type="text/javascript" ></script>

<script type="text/javascript">

    $(function() {

        $("#numMonths").change(function() {
            var numMonths = $(this).val();
            plotGraph(numMonths);
        });


//        var options = {
//            lines: { show: true },
//            points: { show: true },
//            xaxis: { tickDecimals: 0, tickSize: 1 }
//        };
        var options = {
            series: {
                points: { show: true },
                lines: {
                    show: true,
                    barWidth: 0.3,
                    align: "center",
                    label: {show: true}
                }
            },
            xaxis: {
                mode: "categories",
                tickLength: 0
            },
            legend: {show: true},
            crosshair: {
                mode: "x"
            },
            grid: {
                hoverable: true,
                autoHighlight: true,
                clickable: true,
                markings: [
                    {yaxis: {from: 10, to: 10}, color: "#F7977A"},
                    {yaxis: {from: 25, to: 25}, color: "#FDC68A"},
                    {yaxis: {from: 50, to: 50}, color: "#82CA9D"}
                ]
            }
        }


        //$.plot(placeholder, data, options);
        plotGraph(12);

        function plotGraph(numMonths) {
            var placeholder = $("#placeholder");
            var locationId = $("#locationId").val();
            var productId = $("#productId").val();
            var chartData = []; //[["January", 10], ["February", 8], ["March", 4], ["April", 13], ["May", 17], ["June", 9]];


            $.ajax({
                dataType: "json",
                url: "${request.contextPath}/json/getQuantityOnHandByMonth",
                data: { 'location.id': locationId, 'product.id': productId, numMonths: numMonths  },
                success: function (resp) {
                    console.log(resp);
                    chartData.push(resp);
                    console.log(chartData);
                    $.plot(placeholder, chartData, options);
                },
                error: function(xhr, status, error) {
                    alert("error");
                    console.log(xhr);
                    console.log(status);
                    console.log(error);


                }
            });


        }
    });
</script>