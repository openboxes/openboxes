<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <title><g:message code="report.binLocationReport.label" default="Bin Location Report"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
</head>
<body>
<div class="body">

    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${reportInstance}">
        <div class="errors">
            <g:renderErrors bean="${reportInstance}" as="list" />
        </div>
    </g:hasErrors>

    <div class="button-bar">
        <g:link controller="dashboard" action="index" class="button"><g:message code="default.button.backTo.label" args="['Dashboard']"/></g:link>
    </div>

    <div class="yui-gf">
        <div class="yui-u first">

            <div class="box">
                <h2 class="middle"><g:message code="default.filters.label"/></h2>
                <g:form controller="report" action="showBinLocationReport" method="GET">
                    <div class="filters">
                        <div class="prop">
                            <div class="filter-list-item">
                                <label>${warehouse.message(code:'location.label')}</label>
                                <g:selectLocation id="location" name="location.id" class="chzn-select-deselect"
                                                  value="${params?.location?.id?:session?.warehouse?.id}" noSelection="['':'']" data-placeholder=" " />
                            </div>
                            <div class="filter-list-item">
                                <label>${warehouse.message(code:'default.status.label')}</label>
                                <g:select name="status" class="chzn-select-deselect" from="${statuses}"
                                    optionKey="status" optionValue="${{ it.label }}" value="${params.status}"
                                    noSelection="['':g.message(code:'default.all.label')]" data-placeholder=" " />
                            </div>
                        </div>

                        <div class="buttons">

                            <button name="button" value="run" class="button"><g:message code="default.button.run.label"/></button>
                            <button name="button" value="download" class="button"><g:message code="default.button.download.label"/></button>
                            <%--
                                <g:link controller="report" action="clearBinLocationCache" class="button">
                                    <g:message code="default.button.reset.label"/>
                                </g:link>
                                <button class="button" id="refresh-btn">
                                    <g:message code="default.button.refresh.label"/>
                                </button>
                            --%>
                        </div>
                    </div>
                </g:form>
            </div>

        </div>
        <div class="yui-u">

            <g:hasErrors bean="${command}">
                <div class="errors">
                    <g:renderErrors bean="${command}" as="list" />
                </div>
            </g:hasErrors>


            <div class="box">
                <h2 class="middle">
                    <g:message code="report.binLocationReport.label" default="Bin Location Report"/>
                    <%--
                    <g:if test="${binLocations}">
                        <small>Returned ${binLocations.size()} results in ${elapsedTime/1000} seconds</small>
                    </g:if>
                    --%>
                </h2>
                <div class="dialog">

                    <table id="binLocationReportTable">
                        <thead>
                            <tr class="prop">
                                <th class="center"><g:message code="default.status.label"/></th>
                                <th class="center"><g:message code="product.productCode.label"/></th>
                                <th class="center"><g:message code="product.label"/></th>
                                <th class="center"><g:message code="location.binLocation.label"/></th>
                                <th class="center"><g:message code="inventoryItem.lotNumber.label"/></th>
                                <th class="center"><g:message code="inventoryItem.expirationDate.label"/></th>
                                <th class="center"><g:message code="default.quantity.label"/></th>
                                <th class="center"><g:message code="default.uom.label"/></th>
                                <th class="center"><g:message code="productSupplier.unitCost.label"/></th>
                                <th class="center"><g:message code="product.totalValue.label"/></th>
                            </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript" charset="utf8" src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/jquery.dataTables.js"></script>
<script>
    $(document).ready(function() {

        $('#binLocationReportTable').dataTable( {
            "bProcessing": true,
            "sServerMethod": "GET",
            "iDisplayLength": 15,
            "bSearch": false,
            "bScrollCollapse": true,
            "bJQueryUI": true,
            "bAutoWidth": true,
            "sPaginationType": "full_numbers",
            "sAjaxSource": "${request.contextPath}/json/getBinLocationReport",
            "fnServerParams": function ( data ) {
                console.log("server params ");
                data.push({ name: "location.id", value: "${params?.location?.id}"});
                data.push({ name: "status", value: "${params.status}"});
                //var locationId = $("#location").val();
                //data.push({ name: "location.id", value: locationId });
            },
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                console.log("fnServerData", aoData);
                aoData.push( { "name": "test1", "value": "value1" } );
                $.ajax( {
                    "dataType": 'json',
                    "type": "GET",
                    "url": sSource,
                    "data": aoData,
                    "success": fnCallback,
                    "timeout": 120000,   // optional if you want to handle timeouts (which you should)
                    "error": handleAjaxError // this sets up jQuery to give me errors
                } );
            },
            "oLanguage": {
                "sZeroRecords": "No records found",
                "sProcessing": "<img alt='spinner' src='${request.contextPath}/images/spinner.gif' /> <br/><br/> Loading... "
            },
            //"fnInitComplete": fnInitComplete,
            //"iDisplayLength" : -1,
            "aLengthMenu": [
                [5, 15, 25, 100, 1000, -1],
                [5, 15, 25, 100, 1000, "All"]
            ],
            "aoColumns": [
                { "mData": "status", "sWidth": "1%" },
                { "mData": "productCode", "sWidth": "1%" },
                { "mData": "productName", "sWidth": "20%" },
                { "mData": "binLocation", "sWidth": "5%"  },
                { "mData": "lotNumber", "sWidth": "5%"  },
                { "mData": "expirationDate", "sWidth": "5%"  },
                { "mData": "quantity", "sWidth": "5%"  },
                { "mData": "unitOfMeasure", "sWidth": "1%" },
                { "mData": "unitCost", "sWidth": "1%" },
                { "mData": "totalValue", "sWidth": "1%", "sType":"currency" }

            ],
            "bUseRendered": false,
            "aaSorting": [[ 3, "desc" ], [4, "desc"]],
            "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
                //$('td:eq(2)', nRow).html('<a href="${request.contextPath}/inventoryItem/showStockCard/' + aData["id"] + '">' +
                //    aData["productName"] + '</a>');
                return nRow;
            }

        });

        $("#refresh-btn").click(function(event) {
            event.preventDefault();
            var dataTable = $("#binLocationReportTable").dataTable();
            console.log(dataTable);
            //dataTable._fnAjaxUpdate();
            //dataTable.fnDestroy();
            dataTable.fnDraw();
            //$('#binLocationReportTable').dataTable();
            //dataTable.fnDraw();
        });
    });

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
//            if (xhr.responseText) {
//                var error = eval("(" + xhr.responseText + ")");
//                alert("An error occurred on the server.  Please contact your system administrator.\n\n" + error.errorMessage);
//            } else {
//                alert('An unknown error occurred on the server.  Please contact your system administrator.');
//            }
        }
        //console.log(dataTable);
        //dataTable.fnProcessingDisplay( false );
    }

</script>

</body>
</html>
