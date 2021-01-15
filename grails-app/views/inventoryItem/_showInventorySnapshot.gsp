<%@ page import="org.pih.warehouse.product.Product" %>
<style>
    #placeholder { width: 100%; height: 400px; border: 0px solid black; }
    .legend table { width: auto; }
</style>
<div id="content" class="box">
    <h2><g:message code="inventorySnapshot.label" default="Inventory Snapshot"/></h2>
    <g:set var="inventoryLevel" value="${product?.getInventoryLevel(session.warehouse.id)}"/>
    <g:set var="minQuantity" value="${inventoryLevel?.minQuantity?:0}"/>
    <g:set var="reorderQuantity" value="${inventoryLevel?.reorderQuantity?:0}"/>
    <g:set var="maxQuantity" value="${inventoryLevel?.maxQuantity?:0}"/>
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
                <g:set var="monthLabel" value="${g.message(code: 'default.time.unit.month.label', default: 'Month').toLowerCase()}" />
                <g:set var="monthsLabel" value="${g.message(code: 'default.time.unit.months.label', default: 'Months').toLowerCase()}" />
                <g:set var="yearsLabel" value="${g.message(code: 'default.time.unit.years.label', default: 'Years').toLowerCase()}" />
                <g:select
                    id="numMonths"
                    name="numMonths"
                    value="${params.numMonths?:1}"
                    class="chzn-select-deselect"
                    from="[
                      1: g.message(code: 'default.last.label', args: [1, monthLabel]),
                      2: g.message(code: 'default.last.label', args: [2, monthsLabel]),
                      3: g.message(code: 'default.last.label', args: [3, monthsLabel]),
                      6: g.message(code: 'default.last.label', args: [6, monthsLabel]),
                      9: g.message(code: 'default.last.label', args: [9, monthsLabel]),
                      12: g.message(code: 'default.last.label', args: [12, monthsLabel]),
                      18: g.message(code: 'default.last.label', args: [18, monthsLabel]),
                      24: g.message(code: 'default.last.label', args: [2, yearsLabel]),
                      36: g.message(code: 'default.last.label', args: [3, yearsLabel]),
                      48: g.message(code: 'default.last.label', args: [4, yearsLabel]),
                      60: g.message(code: 'default.last.label', args: [5, yearsLabel]),
                      120: g.message(code: 'default.last.label', args: [10, yearsLabel])
                    ]"
                    optionKey="key"
                    optionValue="value"
                />
            </td>
        </tr>
        <tr class="prop">
            <td class="name top right">
                <label><g:message code="inventoryLevels.label"/></label>
            </td>
            <!-- e6beff, aaffc3 fffac8, ffd8b1, fabebe -->
            <td class="value middle">
                <g:if test="${!reorderQuantity && !maxQuantity && !minQuantity}">
                    <g:message code="default.none.label"/>
                </g:if>
                <table>
                    <thead>
                        <tr>
                            <th><g:message code="inventoryLevel.status.label"/></th>
                            <th><g:message code="inventoryLevel.range.label" default="Range"/></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:if test="${maxQuantity}">
                        <tr style="background-color: #fffac8">
                            <td>
                                <label><g:message code="inventory.listOverStock.label" default="Overstock"/></label>
                            </td>
                            <td>
                                Greater than ${maxQuantity}
                            </td>
                        </tr>
                    </g:if>
                    <g:if test="${reorderQuantity || maxQuantity || minQuantity}">
                        <tr style="background-color: #aaffc3">
                            <td><label><g:message code="inventoryLevel.idealQuantity.label" default="Ideal"/></label></td>
                            <td>
                                <g:if test="${reorderQuantity && maxQuantity}"> Between ${reorderQuantity} and ${maxQuantity}</g:if>
                                <g:elseif test="${minQuantity && maxQuantity}"> Between ${minQuantity} and ${maxQuantity}</g:elseif>
                                <g:elseif test="${reorderQuantity}">Greater than ${reorderQuantity}</g:elseif>
                                <g:elseif test="${minQuantity}">Greater than ${minQuantity}</g:elseif>
                                <g:else>Less than ${maxQuantity}</g:else>

                            </td>
                        </tr>
                    </g:if>
                    <g:if test="${reorderQuantity}">
                        <tr style="background-color: #ffd8b1">
                            <td>
                                <label><g:message code="inventoryLevel.reorderQuantity.label"/></label>
                            </td>
                            <td>
                                <g:if test="${minQuantity && reorderQuantity}"> Between ${minQuantity} and ${reorderQuantity}</g:if>
                                <g:elseif test="${reorderQuantity}">Less than ${reorderQuantity}</g:elseif>
                                <g:elseif test="${minQuantity}">Less than ${minQuantity}</g:elseif>
                                <g:else>Less than ${minQuantity}</g:else>
                            </td>
                        </tr>
                    </g:if>
                    <g:if test="${minQuantity}">
                        <tr style="background-color: #fabebe">
                            <td><label><g:message code="inventoryLevel.minimumQuantity.label"/></label></td>
                            <td>Less than ${minQuantity}</td>
                        </tr>
                    </g:if>
                </tbody>
                </table>
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label><g:message code="default.chart.label" default="Chart"/></label>
            </td>
            <td class="value">
                <div class="demo-container">
                    <div id="placeholder" class="demo-placeholder" style="height:400px; padding: 10px"></div>
                </div>
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label><g:message code="default.data.label" default="Data"/></label>
            </td>
            <td class="value">
                <table id="inventorySnapshotsTable" class="dataTable">
                    <thead>
                        <tr>
                            <th><g:message code="default.date.label" default="Date" /></th>
                            <th><g:message code="product.code.label" default="Product Code" /></th>
                            <th><g:message code="location.binLocation.label" default="Bin Location" /></th>
                            <th><g:message code="inventory.lotNumber.label" default="Lot Number" /></th>
                            <th><g:message code="inventory.expires.label" default="Expiration Date" /></th>
                            <th><g:message code="default.qoh.label" default="QoH" /></th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                    <tfoot>
                    <th colspan="5" class="right"><g:message code="default.total.label"/></th>
                    <th></th>
                    </tfoot>
                </table>
            </td>
        </tr>
    </table>
</div>
<div class="buttons">
%{--    FIXME Commented out because it does not properly recalculate inventory snapshot for each date --}%
%{--    <g:isSuperuser>--}%
%{--        <g:remoteLink class="button"--}%
%{--                      controller="inventorySnapshot" action="triggerRefreshInventorySnapshotJob"--}%
%{--                      params="['product.id':product.id,'location.id':session.warehouse.id]" onSuccess="javascript:refreshPage();">--}%
%{--            <img src="${resource(dir:'images/icons/silk',file:'reload.png')}" />&nbsp;--}%
%{--            ${g.message(code: "default.reload.label", args: [g.message(code: "default.data.label")])}--}%
%{--        </g:remoteLink>--}%
%{--    </g:isSuperuser>--}%
    <g:if test="${inventoryLevel}">
        <g:set var="redirectUrl">${g.createLink(controller: "inventoryItem", action: "showStockCard", id: product?.id)}</g:set>
        <a href class="button btn-show-dialog"
            data-title="${g.message(code: 'default.edit.label', args: [g.message(code:'inventoryLevel.label')])}"
            data-url="${request.contextPath}/inventoryLevel/dialog/${inventoryLevel?.id}?redirectUrl=${redirectUrl}">
            <img src="${resource(dir:'images/icons/silk',file:'pencil.png')}" />&nbsp;
            ${g.message(code: 'default.edit.label', args: [g.message(code:'inventoryLevel.label')])}
        </a>
    </g:if>
</div>

<script src="${resource(dir:'js/flot/', file:'jquery.flot.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/flot/', file:'jquery.flot.time.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/flot/', file:'jquery.flot.canvas.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/flot/', file:'jquery.flot.resize.js')}" type="text/javascript" ></script>

<script type="text/javascript">

    $( document ).ready(function() {

        $("#numMonths").change(function() {
            var numMonths = $(this).val();
            plotGraph(numMonths);
        });

        $('<div id="tooltip"></div>').css( {
            position: 'absolute',
            display: 'none',
            border: '1px solid #fdd',
            padding: '5px',
            'background-color': '#fff',
            opacity: 0.80
        }).appendTo("body");

        plotGraph(1);

        // Initialize data table with the most recent inventory snapshot data point
        var locationId = $("#locationId").val();
        var productId = $("#productId").val();
        getInventorySnapshots(productId, locationId, null)
    });

    var minQuantity = $("#minQuantity").val();
    var reorderQuantity = $("#reorderQuantity").val();
    var maxQuantity = $("#maxQuantity").val();
    var yAxisMax = maxQuantity;

    // e6beff, aaffc3 fffac8, ffd8b1, fabebe
    var markings = [];

    // below stockout
    markings.push(
        {yaxis: {from: 0, to: 0}, color: "#fabebe", lineWidth: 0});

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
          min: 0,
          lines: {
            show: true
          },
        },
        legend: {
          show: false
        },
        crosshair: {
          mode: "x"
        },
        grid: {
          //color: "#999999",
          backgroundColor: {colors: ["#aaffc3", "#aaffc3"]},
          hoverable: true,
          autoHighlight: true,
          clickable: true,
          markings: markings
        }
      };

      var myGraph;

      function refreshPage() {
        location.reload()
      }

      function createDataTable(data) {
        $("#inventorySnapshotsTable").dataTable({
          "aaData": data,
          "bProcessing": true,
          "bDestroy": true,
          "bJQueryUI": true,
          "iDisplayLength": -1,
          "aoColumns": [
            {"mData": "date"},
            {"mData": "productCode"},
            {"mData": "binLocation"},
            {"mData": "lotNumber"},
            {"mData": "expirationDate"},
            {"mData": "quantityOnHand"},
          ],
          "oLanguage": {
            "sEmptyTable": "${g.message(code: 'default.dataTable.noData.label', default: 'No data available in table')}",
            "sInfoEmpty": "${g.message(code: 'default.dataTable.showingZeroEntries.label', default: 'Showing 0 to 0 of 0 entries')}",
            "sLengthMenu": "${g.message(code: 'default.dataTable.show.label', 'Show')}"
              + " _MENU_ "
              + "${g.message(code: 'default.dataTable.entries.label', 'entries')}",
            "sInfo": "${g.message(code: 'default.dataTable.showing.label', 'Showing')} " +
              "_START_" +
              " ${g.message(code: 'default.dataTable.to.label', default: 'to')} " +
              "_END_" +
              " ${g.message(code: 'default.dataTable.of.label', default: 'of')} " +
              "_TOTAL_" +
              " ${g.message(code: 'default.dataTable.entries.label', default: 'entries')}",
            "sSearch": "${g.message(code: 'default.dataTable.search.label', default: 'Search:')}",
            "sZeroRecords": "${g.message(code: 'default.dataTable.search.label', default: 'No records found')}",
            "sProcessing": "<img alt='spinner' src='${request.contextPath}/images/spinner.gif' /> ${g.message(code: 'default.loading.label', default: 'Loading...')}",
            "sInfoFiltered": "(${g.message(code: 'default.dataTable.filteredFrom.label', default: 'filtered from')}"
              + " _MAX_ "
              + "${g.message(code: 'default.dataTable.totalEntries.label', default: 'total entries')})"
          },
          "aaSorting": [[ 4, "desc" ]],
          "fnFooterCallback": function (nRow, aaData, iStart, iEnd, aiDisplay) {
            console.log(aaData);
            var totalQoH = 0;
            for (var i = iStart; i < iEnd; i++) {
              totalQoH += aaData[aiDisplay[i]].quantityOnHand;
            }
            var nCells = nRow.getElementsByTagName('th');
            nCells[1].innerHTML = totalQoH;
          }
        }).show();
      }

      function getInventorySnapshots(productId, locationId, date) {
        $.ajax({
          dataType: "json",
          url: "${request.contextPath}/json/getInventorySnapshotDetails",
          data: {'location.id': locationId, 'product.id': productId, date: date},
          success: function (resp) {
            createDataTable(resp.data);
          },
          error: function (xhr, status, error) {
            console.log(xhr, status, error);
          }
        });
      }

      function showTooltip(pageX, pageY, contents) {
        $("#tooltip").html(contents).css({top: pageY, left: pageX + 20}).fadeIn(200);
      }

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
            //myGraph.highlight(item.series, item.datapoint);
            var x = item.datapoint[0].toFixed(2), y = item.datapoint[1].toFixed(2);
            var date = new Date(item.datapoint[0]);
            getInventorySnapshots(productId, locationId, date.toLocaleDateString())
          }
        });
        placeholder.bind("plothover", function (event, pos, item) {
          if (item) {
            var x = item.datapoint[0].toFixed(2), y = item.datapoint[1].toFixed(2);
            var date = new Date(item.datapoint[0]);
            var tooltip = date.toLocaleDateString() + "<br/><b>" + y + "</b>";

            showTooltip(item.pageX, item.pageY, tooltip);
          } else {
            $("#tooltip").hide();
          }
        });
      }
</script>
