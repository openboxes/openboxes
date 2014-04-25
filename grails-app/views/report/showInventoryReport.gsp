<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />

    <title><warehouse:message code="report.showInventoryReport.label" default="Inventory report" /></title>
    <!-- DataTables CSS -->
    <link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">

    <!-- jQuery
    <script type="text/javascript" charset="utf8" src="http://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.8.2.min.js"></script>
    -->

    <!-- DataTables -->
    <script type="text/javascript" charset="utf8" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js"></script>


</head>
<body>

    <input type="hidden" id="locationId" name="locationId" value="${session.warehouse.id}"/>
    <div class="body">
        <div class="yui-gf">
            <div class="yui-u first">
                <div class="box">
                    <h2>Filters</h2>
                    <div id="filterContent">
                        <div class="filter-list-item">
                            <label>Status</label>
                        </div>
                        <g:each in="${['NOT_STOCKED','STOCK_OUT','LOW_STOCK','REORDER','IN_STOCK','IDEAL_STOCK','OVERSTOCK','INVALID']}" var="status" status="i">
                            <div class="filter-list-item">
                                <g:checkBox id="status-${i}" name="status" value="${status}" checked="${false}" class="status-filter"/>
                                <label for="status-${i}">${status}</label>
                            </div>
                        </g:each>
                        <div class="filter-list-item">
                            <button id="refresh-btn" class="btn-large">Refresh</button>
                        </div>

                    </div>
                </div>
            </div>
            <div class="yui-u">
                <div class="box">
                    <h2>Results</h2>
                    <div id="dataTableDiv">
                        <table id="dataTable">
                            <thead>
                            <tr>
                                <th>Product Group ID</th>
                                <th>Inventory Level ID</th>
                                <th>Status</th>
                                <th>Name</th>
                                <th>Product codes</th>
                                <th>Type</th>
                                <th>Min</th>
                                <th>Reorder</th>
                                <th>Max</th>
                                <th>QoH</th>
                                <th>Total Value</th>
                                <th>Has Product Group</th>
                                <th>Has Inventory Level</th>
                            </tr>
                            </thead>
                        </table>

                    </div>


                </div>
            </div>
        </div>
    </div>


    <script type="text/javascript">
        $.fn.dataTableExt.oApi.fnReloadAjax = function ( oSettings, sNewSource, fnCallback, bStandingRedraw ) {
            // DataTables 1.10 compatibility - if 1.10 then versionCheck exists.
            // 1.10s API has ajax reloading built in, so we use those abilities
            // directly.
            if ( $.fn.dataTable.versionCheck ) {
                var api = new $.fn.dataTable.Api( oSettings );

                if ( sNewSource ) {
                    api.ajax.url( sNewSource ).load( fnCallback, !bStandingRedraw );
                }
                else {
                    api.ajax.reload( fnCallback, !bStandingRedraw );
                }
                return;
            }

            if ( sNewSource !== undefined && sNewSource !== null ) {
                oSettings.sAjaxSource = sNewSource;
            }

            // Server-side processing should just call fnDraw
            if ( oSettings.oFeatures.bServerSide ) {
                this.fnDraw();
                return;
            }

            this.oApi._fnProcessingDisplay( oSettings, true );
            var that = this;
            var iStart = oSettings._iDisplayStart;
            var aData = [];

            this.oApi._fnServerParams( oSettings, aData );

            oSettings.fnServerData.call( oSettings.oInstance, oSettings.sAjaxSource, aData, function(json) {
                /* Clear the old information from the table */
                that.oApi._fnClearTable( oSettings );

                /* Got the data - add it to the table */
                var aData =  (oSettings.sAjaxDataProp !== "") ?
                        that.oApi._fnGetObjectDataFn( oSettings.sAjaxDataProp )( json ) : json;

                for ( var i=0 ; i<aData.length ; i++ )
                {
                    that.oApi._fnAddData( oSettings, aData[i] );
                }

                oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();

                that.fnDraw();

                if ( bStandingRedraw === true )
                {
                    oSettings._iDisplayStart = iStart;
                    that.oApi._fnCalculateEnd( oSettings );
                    that.fnDraw( false );
                }

                that.oApi._fnProcessingDisplay( oSettings, false );

                /* Callback user function - for event handlers etc */
                if ( typeof fnCallback == 'function' && fnCallback !== null ) {
                    fnCallback( oSettings );
                }
            }, oSettings );
        };



        $( document ).ready(function() {

            //$("#dataTable").dataTable();

            var locationId = $("#locationId").val();

            var dataTable = $('#dataTable').dataTable( {
                "bProcessing": true,
                "sServerMethod": "GET",
                "sAjaxSource": "${request.contextPath}/json/getQuantityOnHandByProductGroup",
                "fnServerParams": function ( data ) {
                    data.push({ "name": "location.id", "value": locationId });
                    console.log(data);
                    $(".status-filter").each(function(index, value) {
                        if (this.checked) {
                            data.push({ "name": this.name, "value": this.value });
                        }
                    });

                },
                "iDisplayLength" : -1,
                "aLengthMenu": [
                    [25, 50, 100, 500, 1000, -1],
                    [25, 50, 100, 500, 1000, "All"]
                ],
                "aoColumns": [
                    { "mData": "id", "bSearchable": false, "bVisible": false },
                    { "mData": "inventoryLevelId", "bSearchable": false, "bVisible": false },
                    { "mData": "status" }, // 0
                    { "mData": "name" }, // 1
                    //{ "mData": "numProducts" }, // 2
                    { "mData": "productCodes" }, // 2
                    { "mData": "inventoryStatus" }, // 3
                    { "mData": "minQuantity" }, // 4
                    { "mData": "reorderQuantity" }, // 5
                    { "mData": "maxQuantity" }, // 6
                    { "mData": "onHandQuantity" }, //7
                    { "mData": "totalValue" }, // 8
                    { "mData": "hasProductGroup" },  // 9
                    { "mData": "hasInventoryLevel" } // 10

                ],
                "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
                    console.log(aData);
                    switch(aData["status"]){
                        case 'IN_STOCK':
                            $(nRow).css('color', 'green')
                            break;
                        case 'NOT_STOCKED':
                            $(nRow).css('color', 'grey')
                            break;
                        case 'STOCK_OUT':
                            $(nRow).css('color', 'red')
                            break;
                        case 'LOW_STOCK':
                            $(nRow).css('color', 'orange')
                            break;
                        case 'REORDER':
                            $(nRow).css('color', '#eed7b0;')
                            break;
                        case 'IDEAL_STOCK':
                            $(nRow).css('color', 'green')
                            break;
                        case 'OVERSTOCK':
                            $(nRow).css('color', 'blue')
                            break;
                        case 'INVALID':
                            $(nRow).css('color', 'green')
                            break;
                    }
                    if (aData["id"]) {
                        $('td:eq(1)', nRow).html('<a href="/openboxes/productGroup/edit/' + aData["id"] + '" target="_blank">' + aData["name"] + '</a>');
                    }
                    if (aData["inventoryLevelId"]) {
                        $('td:eq(4)', nRow).html('<a href="/openboxes/inventoryLevel/edit/' + aData["inventoryLevelId"] + '" target="_blank">' + aData["minQuantity"] + '</a>');
                        $('td:eq(5)', nRow).html('<a href="/openboxes/inventoryLevel/edit/' + aData["inventoryLevelId"] + '" target="_blank">' + aData["reorderQuantity"] + '</a>');
                        $('td:eq(6)', nRow).html('<a href="/openboxes/inventoryLevel/edit/' + aData["inventoryLevelId"] + '" target="_blank">' + aData["maxQuantity"] + '</a>');
                    }
                    return nRow;
                }
            });


            $('#refresh-btn').click( function (event) {
                event.preventDefault();
                dataTable.fnClearTable();
                dataTable.fnReloadAjax('${request.contextPath}/json/getQuantityOnHandByProductGroup');
            } );

            /*
            $(':checkbox').click(function(event) {
                //dataTable.fnReloadAjax('${request.contextPath}/json/getQuantityOnHandByProductGroup');

                var $this = $(this);
                // $this will contain a reference to the checkbox
                if ($this.is(':checked')) {
                    // the checkbox was checked
                    //var dataTable = $("#dataTable").dataTable();
                    dataTable.fnClearTable();
                    //dataTable.fnDraw();
                    //dataTable.fnAddData();
                    //alert('refresh on check');
                } else {
                    // the checkbox was unchecked
                    //var dataTable = $("#dataTable").dataTable();
                    dataTable.fnClearTable();
                    //dataTable.fnDraw();
                    //dataTable.fnAddData();
                    //alert('refresh uncheck');
                }
            });
            */




            /*
            $.ajax({
                dataType: "json",
                timeout: 60000,
                url: "${request.contextPath}/json/getQuantityOnHandByProductGroup?location.id=${session.warehouse.id}",
                //data: data,
                success: function (data) {
                    console.log(data);
                    $("#reportContent").html(data);
                    // {"lowStock":103,"reorderStock":167,"overStock":38,"totalStock":1619,"reconditionedStock":54,"stockOut":271,"inStock":1348}
                    //$('#lowStockCount').html(data.lowStock?data.lowStock:0);

                },
                error: function(xhr, status, error) {
                    console.log(xhr);
                    console.log(status);
                    console.log(error);

                }
            });
            */
        });

    </script>

</body>
</html>