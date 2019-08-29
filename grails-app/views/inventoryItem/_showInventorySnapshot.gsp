<%@ page import="org.pih.warehouse.product.Product" %>
<style>
    #placeholder { width: 100%; height: 400px; border: 0px solid black; }
    .legend table { width: auto; }

</style>

<div id="content" class="box">
    <h2>Inventory Snapshot</h2>
    <g:set var="minQuantity" value="${product?.getInventoryLevel(session.warehouse.id)?.minQuantity?:0}"/>
    <g:set var="reorderQuantity" value="${product?.getInventoryLevel(session.warehouse.id)?.reorderQuantity?:0}"/>
    <g:set var="maxQuantity" value="${product?.getInventoryLevel(session.warehouse.id)?.maxQuantity?:0}"/>
    <input type="hidden" id="productId" name="productId" value="${product.id}"/>
    <input type="hidden" id="locationId" name="locationId" value="${session.warehouse.id}"/>
    <input type="hidden" id="minQuantity" name="minQuantity" value="${minQuantity}"/>
    <input type="hidden" id="reorderQuantity" name="reorderQuantity" value="${reorderQuantity}"/>
    <input type="hidden" id="maxQuantity" name="maxQuantity" value="${maxQuantity}"/>

    <table>
        <tr class="prop">
            <td class="name middle right">
                <label for="numMonths">
                    <warehouse:message code="default.duration.label" default="Duration"/></label>
            </td>
            <td class="value middle">
                <g:select id="numMonths" name="numMonths" value="${params.numMonths?:12}" class="chzn-select-deselect"
                          from="[1:'Last 1 month', 2:'Last 2 months',3:'Last 3 months',6:'Last 6 months',9:'Last 9 months',12:'Last 12 months',18:'Last 18 months',24:'Last 2 years',36:'Last 3 years',48:'Last 4 years',60:'Last 5 years',60:'Last 5 years',120:'Last 10 years']" optionKey="key" optionValue="value"></g:select>
            </td>
        </tr>
        <tr class="prop">
            <td class="name top right">
                <label><g:message code="inventoryLevel.label"/></label>
            </td>
            <!-- e6beff, aaffc3 fffac8, ffd8b1, fabebe -->
            <td class="value middle">

                <g:if test="${maxQuantity}">
                    <div style="background-color: #fffac8">
                        <label><g:message code="inventory.listOverStock.label" default="Overstock"/></label>
                        Greater than ${maxQuantity}
                    </div>
                </g:if>
                <g:if test="${reorderQuantity || maxQuantity || minQuantity}">
                    <div style="background-color: #aaffc3">
                        <label><g:message code="inventoryLevel.idealQuantity.label" default="Ideal"/></label>
                        <g:if test="${reorderQuantity && maxQuantity}"> Between ${reorderQuantity} and ${maxQuantity}</g:if>
                        <g:elseif test="${minQuantity && maxQuantity}"> Between ${minQuantity} and ${maxQuantity}</g:elseif>
                        <g:elseif test="${reorderQuantity}">Greater than ${reorderQuantity}</g:elseif>
                        <g:elseif test="${minQuantity}">Greater than ${minQuantity}</g:elseif>
                        <g:else>Less than ${maxQuantity}</g:else>
                    </div>
                </g:if>
                <g:if test="${reorderQuantity}">
                    <div style="background-color: #ffd8b1">
                        <label><g:message code="inventoryLevel.reorderQuantity.label"/></label>
                        <g:if test="${minQuantity && reorderQuantity}"> Between ${minQuantity} and ${reorderQuantity}</g:if>
                        <g:elseif test="${reorderQuantity}">Less than ${reorderQuantity}</g:elseif>
                        <g:elseif test="${minQuantity}">Less than ${minQuantity}</g:elseif>
                        <g:else>Less than ${minQuantity}</g:else>
                    </div>
                </g:if>
                <g:if test="${minQuantity}">
                    <div style="background-color: #fabebe">
                        <label><g:message code="inventoryLevel.minimumQuantity.label"/></label>
                        Less than ${minQuantity}
                    </div>
                </g:if>
            </td>
        </tr>
    </table>


    <div class="demo-container">
        <div id="placeholder" class="demo-placeholder" style="height:400px; padding: 10px"></div>
    </div>
    <div class="right" style="margin:5px;">
        <g:isSuperuser>
            <g:remoteLink controller="inventorySnapshot" action="triggerCalculateQuantityOnHandJob"
                          class="button icon reload"
                          params="['product.id':product.id,'location.id':session.warehouse.id]">Refresh data</g:remoteLink>
        </g:isSuperuser>
    </div>
</div>

<script src="${createLinkTo(dir:'js/flot/', file:'jquery.flot.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/flot/', file:'jquery.flot.time.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/flot/', file:'jquery.flot.canvas.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/flot/', file:'jquery.flot.resize.js')}" type="text/javascript" ></script>

<script type="text/javascript">

    $( document ).ready(function() {

        $("#numMonths").change(function() {
            var numMonths = $(this).val();
            plotGraph(numMonths);
        });
        plotGraph(12);

        $('<div id="tooltip"></div>').css( {
            position: 'absolute',
            display: 'none',
            border: '1px solid #fdd',
            padding: '5px',
            'background-color': '#fff',
            opacity: 0.80
        }).appendTo("body");
    });

    var minQuantity = $("#minQuantity").val();
    var reorderQuantity = $("#reorderQuantity").val();
    var maxQuantity = $("#maxQuantity").val();
    var markings = [];
    // below minimum
    if (minQuantity >= 0) {
        markings.push(
            {yaxis: {from: 0, to: minQuantity}, color: "#fabebe", lineWidth: 0});
        markings.push(
            {yaxis: {from: minQuantity, to: minQuantity}, color: "red", lineWidth: 0});
    }
    // below reorder
    if (reorderQuantity > 0) {
      markings.push(
          {yaxis: {from: minQuantity, to: reorderQuantity}, color: "#ffd8b1", lineWidth: 0});
      markings.push(
          {yaxis: {from: reorderQuantity, to: reorderQuantity}, color: "orange", lineWidth: 1});
    }
    if (maxQuantity > 0) {
      // ideal quantity
      markings.push(
          {yaxis: {from: reorderQuantity, to: maxQuantity}, color: "#aaffc3", lineWidth: 0});
      markings.push(
          {yaxis: {from: maxQuantity, to: maxQuantity }, color: "green", lineWidth: 1});
      // overstock
      markings.push(
          {yaxis: {from: maxQuantity}, color: "#fffac8", lineWidth: 0});
    }

      var options = {
        series: {
          points: {
            show: true,
            fill: false
          },
          lines: {
            show: true,
            fill: false,
            barWidth: 1.0,
            align: "center",
            label: {
              show: true
            }
          }
        },
        xaxis: {
          mode: "time",
          tickLength: 0
        },
        yaxis: {
          //min: 0,
          lines: {show: true}
        },
        legend: {
          show: false
        },
        crosshair: {
          mode: "x"
        },
        grid: {
          color: "#999999",
          backgroundColor: {colors: ["#aaffc3", "#aaffc3"]},
          hoverable: true,
          autoHighlight: true,
          clickable: true,
          markings: markings
        }
      };

      var myGraph;

      function plotGraph(numMonths) {
        var placeholder = $("#placeholder");
        var locationId = $("#locationId").val();
        var productId = $("#productId").val();
        var chartData = [];
        $.ajax({
          dataType: "json",
          url: "${request.contextPath}/json/getQuantityOnHandByMonth",
          data: {'location.id': locationId, 'product.id': productId, numMonths: numMonths},
          success: function (resp) {
            chartData.push(resp);
            myGraph = $.plot(placeholder, chartData, options);
          },
          error: function (xhr, status, error) {
            console.log(xhr, status, error);
          }
        });
        placeholder.bind("plothovercleanup", function (event, pos, item) {
          $("#tooltip").hide();
        });
        placeholder.bind("plotclick", function (event, pos, item) {
          if (item) {
            myGraph.highlight(item.series, item.datapoint);
          }
        });
        placeholder.bind("plothover", function (event, pos, item) {
          if (item) {
            var x = item.datapoint[0].toFixed(2), y = item.datapoint[1].toFixed(2);
            showTooltip(item.pageX, item.pageY, y);
          } else {
            $("#tooltip").hide();
          }
        });
      }

      function showTooltip(pageX, pageY, contents) {
        $("#tooltip").html(contents).css({top: pageY, left: pageX + 20}).fadeIn(200);
      }



</script>
