<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <title><g:message code="report.inventoryDetails.label" default="Inventory Details Report"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
</head>
<body>
<div class="body">

    <g:if test="${flash.message}">
        <div class="message" role="status" aria-label="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${reportInstance}">
        <div class="errors" role="alert" aria-label="error-message">
            <g:renderErrors bean="${reportInstance}" as="list" />
        </div>
    </g:hasErrors>

    <div class="button-bar">
        <g:link controller="dashboard" action="index" class="button"><g:message code="default.button.backTo.label" args="['Dashboard']"/></g:link>
    </div>

    <div class="yui-gf">
        <div class="box p-2">
            <g:message
                    code="report.inventoryDetails.instructions.label"
                    default='In the Inventory Details report, you find information about the stock at your location for all products of your inventory, including details of the lots in stock with their bin location. Use the filters to exclude "Out of Stock" products. Use this detailed view to find information on a specific product, with Quantities, Lot numbers, and Bin location details.'
            />
        </div>
        <div class="yui-u first">
            <div class="box">
                <h2 class="middle"><g:message code="default.filters.label"/></h2>
                <g:form name="showBinLocationReportForm" controller="report" action="showBinLocationReport" method="GET">
                    <div class="filters">
                        <div class="prop">
                            <div class="filter-list-item">
                                <label>${warehouse.message(code:'location.label')}</label>
                                <g:selectLocation
                                    id="location"
                                    name="location.id"
                                    class="chzn-select-deselect"
                                    value="${params?.location?.id?:session?.warehouse?.id}"
                                    noSelection="['':'']"
                                    data-placeholder=" "
                                />
                            </div>
                            <div class="filter-list-item">
                                <label>${warehouse.message(code:'default.status.label')}</label>
                                <g:select
                                    name="status"
                                    class="chzn-select-deselect"
                                    from="${statuses}"
                                    optionValue="${{g.message(code: 'binLocationSummary.' + it + '.label', default: it)}}"
                                    value="${params.status}"
                                    noSelection="['':g.message(code:'default.all.label')]" data-placeholder=" "
                                />
                            </div>
                        </div>
                        <div class="buttons">
                            <button name="button" value="run" class="button">
                                <img src="${resource(dir:'images/icons/silk',file:'play_green.png')}" />&nbsp;
                                <g:message code="report.runReport.label"/>
                            </button>
                            <span class="action-menu" style="margin-left: 15px">
                                <button class="action-btn button">
                                    <img src="${resource(dir:'images/icons/silk',file:'page_white_excel.png')}" />&nbsp;
                                    ${warehouse.message(code: 'default.button.download.label')}
                                    <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
                                </button>
                                <div class="actions">
                                    <div class="action-menu-item">
                                        <a href="#" class="download-btn" data-download-action="downloadStockReport">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'page_white_excel.png')}" />
                                            <g:message code="default.download.label" args="[g.message(code: 'default.report.label', default: 'Report')]"/>
                                        </a>
                                    </div>
                                    <div class="action-menu-item">
                                        <a href="#" class="download-btn" data-download-action="downloadStockMovement">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'page_white_excel.png')}" />
                                            <g:message code="default.download.label" args="[g.message(code: 'stockMovement.label')]"/>
                                        </a>
                                    </div>
                                </div>
                            </span>
                        </div>
                    </div>
                </g:form>
            </div>

        </div>
        <div class="yui-u">

            <g:hasErrors bean="${command}">
                <div class="errors" role="alert" aria-label="error-message">
                    <g:renderErrors bean="${command}" as="list" />
                </div>
            </g:hasErrors>


            <div class="box">
                <h2 class="middle">
                    <g:message code="report.inventoryDetails.label" default="Inventory Details Report"/>
                </h2>
                <div class="dialog">

                    <table id="binLocationReportTable">
                        <thead>
                            <tr class="prop">
                                <th class="center"><g:message code="default.status.label"/></th>
                                <th class="center"><g:message code="product.productCode.label"/></th>
                                <th class="center"><g:message code="product.label"/></th>
                                <th class="center"><g:message code="location.zone.label"/></th>
                                <th class="center"><g:message code="location.binLocation.label"/></th>
                                <th class="center"><g:message code="inventoryItem.lotNumber.label"/></th>
                                <th class="center"><g:message code="inventoryItem.expirationDate.label"/></th>
                                <th class="center"><g:message code="default.quantityOnHand.label"/></th>
                                <th class="center"><g:message code="default.quantityAvailable.label"/></th>
                                <th class="center"><g:message code="default.uom.label"/></th>
                                <th class="center"><g:message code="product.unitCost.label"/></th>
                                <th class="center"><g:message code="product.totalValue.label"/></th>
                            </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="loading">Loading...</div>
<script type="text/javascript" charset="utf8" src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/jquery.dataTables.js"></script>
<script>
    $(document).ready(function() {

        $(".download-btn").click(function(event){
          console.log(event);
          console.log($(this));
            event.preventDefault();
            $(".loading").show();
            try {
                var action = $(this).data("download-action");
                var formField = $("<input>").attr({id: "downloadActionInput", "type": "hidden", name: "downloadAction", value: action});
                console.log($("form[name='showBinLocationReportForm']"));
                $("form[name='showBinLocationReportForm']").append(formField).submit();
                $("#downloadActionInput").remove();

            } finally {
                $(".loading").hide();
            }
        });

        $('#binLocationReportTable').dataTable( {
            "bProcessing": false,
            "sServerMethod": "GET",
            "iDisplayLength": 100,
            "bSearch": false,
            "bScrollCollapse": true,
            "bScrollInfinite": true,
            "bJQueryUI": true,
            "sScrollY": 500,
            "bAutoWidth": true,
            "sPaginationType": "full_numbers",
            "sAjaxSource": "${request.contextPath}/json/getBinLocationReport",
            "fnServerParams": function ( data ) {
                console.log("server params ");
                data.push({ name: "location.id", value: "${params?.location?.id}"});
                data.push({ name: "status", value: "${params.status}"});
            },
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                console.log("fnServerData", aoData);
                $.ajax( {
                    "dataType": 'json',
                    "type": "GET",
                    "url": sSource,
                    "data": aoData,
                    "success": fnCallback,
                    "timeout": 120000,   // optional if you want to handle timeouts (which you should)
                    "error": handleAjaxError, // this sets up jQuery to give me errors
                    beforeSend : function(){
                        $(".loading").show();
                    },
                    complete: function(){
                       $(".loading").hide();
                    },
                } );
            },
            "oLanguage": {
              "sEmptyTable": "${g.message(code: 'default.dataTable.noData.label', default: 'No data available in table')}",
              "sInfoEmpty": "${g.message(code: 'default.dataTable.showingZeroEntries.label', default: 'Showing 0 to 0 of 0 entries')}",
              "sInfo": "${g.message(code: 'default.dataTable.showing.label', 'Showing')} " +
                "_START_" +
                " ${g.message(code: 'default.dataTable.to.label', default: 'to')} " +
                "_END_" +
                " ${g.message(code: 'default.dataTable.of.label', default: 'of')} " +
                "_TOTAL_" +
                " ${g.message(code: 'default.dataTable.entries.label', default: 'entries')}",
              "sSearch": "${g.message(code: 'default.dataTable.search.label', default: 'Search:')}",
              "sZeroRecords": "${g.message(code: 'default.dataTable.noRecordsFound.label', default: 'No records found')}",
              "sProcessing": "<img alt='spinner' src=\"${resource(dir: 'images', file: 'spinner.gif')}\" /> ${g.message(code: 'default.loading.label', default: 'Loading...')}",
              "sInfoFiltered": "(${g.message(code: 'default.dataTable.filteredFrom.label', default: 'filtered from')} " +
                "_MAX_" +
                " ${g.message(code: 'default.dataTable.totalEntries.label', default: 'total entries')})"
            },
            "aLengthMenu": [
                [5, 15, 25, 100, 1000, -1],
                [5, 15, 25, 100, 1000, "All"]
            ],
            "aoColumns": [
                { "mData": "status", "sWidth": "1%" },
                { "mData": "productCode", "sWidth": "1%" },
                { "mData": "productName", "sWidth": "20%" },
                { "mData": "zone", "sWidth": "4%"  },
                { "mData": "binLocation", "sWidth": "4%"  },
                { "mData": "lotNumber", "sWidth": "5%"  },
                { "mData": "expirationDate", "sWidth": "4%"  },
                { "mData": "quantity", "sWidth": "4%"  },
                { "mData": "quantityAvailableToPromise", "sWidth": "4%"  },
                { "mData": "unitOfMeasure", "sWidth": "1%" },
                { "mData": "unitCost", "sWidth": "1%" },
                { "mData": "totalValue", "sWidth": "1%", "sType":"currency" }

            ],
            "bUseRendered": false,
            "aaSorting": [[ 3, "desc" ], [4, "desc"]],
            "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
                const productLabel = aData?.displayName ?? aData?.productName;
                $('td:eq(1)', nRow).html('<a href="${request.contextPath}/inventoryItem/showStockCard/' + aData["id"] + '">' + aData["productCode"] + '</a>');
                $('td:eq(2)', nRow).html('<a href="${request.contextPath}/inventoryItem/showStockCard/' + aData["id"] + '">' + productLabel + ' ' +
                  renderHandlingIcons(aData) +  '</a>');
                // If we display translated name, we want to have tooltip with original name of the product
                if (aData?.displayName) {
                  $('td:eq(2)', nRow).attr('title', aData?.productName);
                }
                if (aData["lotStatus"] === "RECALLED") {
                  $(nRow).removeClass().css('backgroundColor', '#ffcccb').attr("title", "This lot has been recalled");
                }
                if (aData["isOnHold"]) {
                  $(nRow).removeClass().css('backgroundColor', '#fca714').attr("title", "This bin has been restricted");
                }
            }

        });

        $("#refresh-btn").click(function(event) {
            event.preventDefault();
            var dataTable = $("#binLocationReportTable").dataTable();
            dataTable.fnDraw();
        });
    });

    function renderHandlingIcons(data) {
      var iconElements = ""
      $.each(data.handlingIcons, function( index, value ) {
        var color = value?.color ? value.color : 'inherit';
        iconElements += '<i class="fa ' + value.icon + '" ' + 'style="color:' + color + '" ></i>&nbsp;'
      });
      return iconElements
    }

    function handleAjaxError( xhr, status, error ) {
        console.log("ajax error");

        if ( status === 'timeout' ) {
            alert( 'The server took too long to send the data.' );
        }
        else {
            // User probably refreshed page or clicked on a link, so this isn't really an error
            if(xhr.readyState == 0 || xhr.status == 0) {
                return;
            }
            alert("An error occurred on the server.  Please contact your system administrator.");
        }
    }

</script>

</body>
</html>
