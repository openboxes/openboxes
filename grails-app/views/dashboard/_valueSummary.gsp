<g:hasRoleFinance>
<div class="box">
    <h2>
        <warehouse:message code="inventory.value.label" default="Stock Value"/>
    </h2>
    <div class="widget-content" style="padding:0; margin:0">

        <div class="tabs">
            <ul>
                <li>
                    <a href="#value-summary"><warehouse:message code="default.summary.label" default="Summary"/></a>
                </li>
                <li>
                    <a href="#value-details"><warehouse:message code="default.details.label" default="Details"/></a>
                </li>
            </ul>
            <div id="value-summary">
                <table class="zebra">
                    <thead>
                    <tr class="prop odd">
                        <td colspan="3">
                            <label class="fade lastUpdated"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></label>
                        </td>
                    </tr>
                    </thead>
                    <tbody>
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
                    <tr>
                        <td>
                            <img src="${createLinkTo(dir:'images/icons/silk/chart_pie.png')}" class="middle"/>
                        </td>
                        <td>
                            Products with pricing data
                        </td>
                        <td class="right">
                            <span id="progressSummary">
                                <img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
                            </span>
                            (<small id="progressPercentage">0%</small>)
                        </td>
                    </tr>
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
            <div id="value-details">
                <div class="widget-content" style="padding:0; margin:0">
                    <table id="stockValueDetailsTable">
                        <thead>
                        <th>${g.message(code: "product.productCode.label")}</th>
                        <th>${g.message(code: "product.label")}</th>
                        <th>${g.message(code: "inventory.value.label", default: "Value")}</th>
                        </thead>
                        <tbody>

                        </tbody>
                        <tfoot>
                        <tr>
                            <th colspan="3">
                                <div class="fade">All values in ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}</div>
                            </th>
                        </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function() {
        $(".tabs").tabs(
            {
                cookie: {
                    // store cookie for a day, without, it would be a session cookie
                    expires: 1
                }
            }
        );
    });

    $(window).load(function(){
        loadData();
        loadTableData();
    });

    function refresh() {
        loadData();
        $('#stockValueDetailsTable').dataTable().ajax.reload();
    }

    function formatPercentage(x) {
        if (x) {
            return x.toFixed(0) + "%"
        }
        else {
            return 0 + "%"
        }
    }

    function formatCurrency(x) {
        var currencyCode = "${grailsApplication.config.openboxes.locale.defaultCurrencyCode}";
        return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",") + " " + currencyCode;
    }

    function showError() {
        $("#errorMessage").append("There was an error.").toggle();
    }

    function loadData() {
        $.ajax({
            dataType: "json",
            timeout: 120000,
            url: "${request.contextPath}/json/getTotalStockValue?location.id=${session.warehouse.id}",
            success: function (data) {
                console.log(data);
                var value = data.totalStockValue?formatCurrency(data.totalStockValue.toFixed(0)):0;
                var progress = data.hitCount / data.totalCount
                var progressSummary = data.hitCount + " out of " + data.totalCount;
                var progressPercentage = progress*100;
                var lastUpdated = data.lastUpdated;
                $(".lastUpdated").html(lastUpdated);


                $('#totalStockValue').html(value);

                $("#totalStockSummary").html("Pricing data is available for " + formatPercentage(progressPercentage)  + " of all products");
                $('#progressSummary').html(progressSummary);
                $( "#progressbar" ).progressbar({ value: progressPercentage });
                $( "#progressPercentage").html("<span title='" + progressSummary + "'>" + formatPercentage(progressPercentage) + "</span>");

            },
            error: function(xhr, status, error) {
                $('#totalStockValue').html('ERROR');
                $("#totalStockSummary").html('Unable to calculate total value due to error: ' + error + " " + status + " " + xhr);
            }
        });
    }

    function loadTableData() {

        var dataTable = $('#stockValueDetailsTable').dataTable({
            "bProcessing": true,
            "sServerMethod": "GET",
            "iDisplayLength": 25,
            "bSearch": false,
            "bJQueryUI": true,
            "bAutoWidth": true,
            "bScrollInfinite": true,
            "bScrollCollapse": true,
            "sScrollY": 150,
            "sPaginationType": "two_button",
            "sAjaxSource": "${request.contextPath}/json/getStockValueByProduct",
            "fnServerParams": function (data) {},
            "fnServerData": function (sSource, aoData, fnCallback) {
                $.ajax({
                    "dataType": 'json',
                    "type": "GET",
                    "url": sSource,
                    "data": aoData,
                    "success": fnCallback,
                    "timeout": 120000,   // optional if you want to handle timeouts (which you should)
                    "error": handleAjaxError // this sets up jQuery to give me errors
                });
            },
            "oLanguage": {
                "sZeroRecords": "No records found",
                "sProcessing": "<img alt='spinner' src='${request.contextPath}/images/spinner.gif' /> Loading... "
            },
            "aLengthMenu": [
                [5, 10, 25, 100, 1000, -1],
                [5, 10, 25, 100, 1000, "All"]
            ],
            "aoColumns": [
                {"mData": "productCode", "sWidth": "1%"}, // 2
                {"mData": "productName"}, // 3
                {"mData": "totalValue", "sWidth": "5%", "sType": 'numeric'} // 5
            ],
            "bUseRendered": false,
            "aaSorting": [[2, "desc"]],
            "fnRowCallback": function (nRow, aData, iDisplayIndex) {
                $('td:eq(1)', nRow).html('<a href="${request.contextPath}/inventoryItem/showStockCard/' + aData["id"] + '">' +
                        aData["productName"] + '</a>');
                return nRow;
            }

        });
    }

    function handleAjaxError( xhr, status, error ) {
        if ( status === 'timeout' ) {
            alert( 'The server took too long to send the data.' );
        }
        else {
            // User probably refreshed page or clicked on a link, so this isn't really an error
            if(xhr.readyState == 0 || xhr.status == 0) {
                return;
            }

            if (xhr.responseText) {
                var error = eval("(" + xhr.responseText + ")");
                alert("An error occurred on the server.  Please contact your system administrator.\n\n" + error.errorMessage);
            } else {
                alert('An unknown error occurred on the server.  Please contact your system administrator.');
            }
        }
        console.log(dataTable);
        dataTable.fnProcessingDisplay( false );
    }

</script>
</g:hasRoleFinance>

