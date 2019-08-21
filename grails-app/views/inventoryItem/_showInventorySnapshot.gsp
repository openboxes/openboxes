<%@ page import="org.pih.warehouse.product.Product" %>
<style>
    #placeholder { width: 100%; height: 400px; border: 0px solid black; }
    .legend table { width: auto; }

</style>

<div id="content" class="box">
    <h2>Inventory Snapshot</h2>
    <input type="hidden" id="productId" name="productId" value="${product.id}"/>
    <input type="hidden" id="locationId" name="locationId" value="${session.warehouse.id}"/>
    <input type="hidden" id="minQuantity" name="minQuantity" value="${product?.getInventoryLevel(session.warehouse.id)?.minQuantity?:0}"/>
    <input type="hidden" id="reorderQuantity" name="reorderQuantity" value="${product?.getInventoryLevel(session.warehouse.id)?.reorderQuantity?:0}"/>
    <input type="hidden" id="maxQuantity" name="maxQuantity" value="${product?.getInventoryLevel(session.warehouse.id)?.maxQuantity?:0}"/>

    <table style="width: auto;">
        <tr>
            <td class="middle right">
                <label for="numMonths">
                    <warehouse:message code="default.duration.label" default="Duration"/></label>
            </td>
            <td class="middle">
                <g:select id="numMonths" name="numMonths" value="${params.numMonths?:12}"
                          from="[1:'Last 1 month', 2:'Last 2 months',3:'Last 3 months',6:'Last 6 months',9:'Last 9 months',12:'Last 12 months',18:'Last 18 months',24:'Last 2 years',36:'Last 3 years',48:'Last 4 years',60:'Last 5 years',60:'Last 5 years',72:'Last 6 years',84:'Last 7 years',96:'Last 8 years',108:'Last 9 years',120:'Last 10 years']" optionKey="key" optionValue="value"></g:select>
            </td>
        </tr>

    </table>

    <div class="demo-container">
        <div id="placeholder" class="demo-placeholder" style="height:400px; padding: 10px"></div>
    </div>
    <div class="right" style="margin:5px;">
        <g:remoteLink controller="inventorySnapshot" action="triggerCalculateQuantityOnHandJob"
                      class="button icon reload"
                      params="['product.id':product.id,'location.id':session.warehouse.id]">Refresh data</g:remoteLink>

    </div>

</div>

<script src="${createLinkTo(dir:'js/flot/', file:'jquery.flot.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/flot/', file:'jquery.flot.categories.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/flot/', file:'jquery.flot.canvas.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/flot/', file:'jquery.flot.resize.js')}" type="text/javascript" ></script>

<script type="text/javascript">

    $( document ).ready(function() {

        $("#numMonths").change(function() {
            var numMonths = $(this).val();
            plotGraph(numMonths);
        });

        plotGraph(12);


    });

    var minQuantity = $("#minQuantity").val();
    var reorderQuantity = $("#reorderQuantity").val();
    var maxQuantity = $("#maxQuantity").val();
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
        yaxis: { min: 1000 },
        legend: {show: true},
        crosshair: {
            mode: "x"
        },
        grid: {
            hoverable: true,
            autoHighlight: true,
            clickable: true,
            markings: [
                {yaxis: {from: minQuantity, to: minQuantity}, color: "#F7977A", lineWidth: 2},
                {yaxis: {from: reorderQuantity, to: reorderQuantity}, color: "#FDC68A", lineWidth: 2},
                {yaxis: {from: maxQuantity, to: maxQuantity}, color: "#82CA9D", lineWidth: 2}
            ]
        }
    }

    var myGraph;

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
                myGraph = $.plot(placeholder, chartData, options);
            },
            error: function(xhr, status, error) {
                alert("error");
                console.log(xhr);
                console.log(status);
                console.log(error);


            }
        });
    }

    function downloadGraph() {
        var graph = $("#placeholder");
        var myCanvas = graph.getCanvas();
        var image = myCanvas.toDataURL();
        image = image.replace("image/png","image/octet-stream");
        document.location.href=image;

    }

</script>
