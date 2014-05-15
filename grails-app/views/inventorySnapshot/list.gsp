<html>
<head>
    <title>Inventory Snapshots</title>
    <meta name="layout" content="analytics" />


</head>

<div id="body">
    <h2>${session.warehouse.name}</h2>


    <table id="dataTable" class="display" cellspacing="0" width="100%">
        <thead>
            <tr>
                <th>Product</th>
                <th>Product group</th>
                <th>QoH</th>
            </tr>
        </thead>
        <tbody>

        </tbody>
        <tfoot>

        </tfoot>
    </table>
</div>

<r:script disposition="defer">
    $( document ).ready(function() {

        var dataTable = $('#dataTable').dataTable( {
            "bProcessing": true,
            "sServerMethod": "GET",
            "iDisplayLength": 12,
            "bScrollCollapse": true,
            "bJQueryUI": false,
            "bAutoWidth": true,
            "sPaginationType": "full_numbers",
            "sAjaxSource": "${request.contextPath}/json/getInventorySnapshotsByDate",
            "fnServerParams": function ( data ) {
                console.log("server data " + data);
                var locationId = $("#locationId").val();
                var date = $("#date").val();
                data.push({ name: "location.id", value: locationId });
                data.push({ name: "date", value: date })

            },
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                $.ajax( {
                    "dataType": 'json',
                    "type": "GET",
                    "url": sSource,
                    "data": aoData,
                    "success": fnCallback,
                    "timeout": 30000,   // optional if you want to handle timeouts (which you should)
                    "error": handleAjaxError // this sets up jQuery to give me errors
                } );
            },
//            "fnServerData": function ( sSource, aoData, fnCallback ) {
//                $.getJSON( sSource, aoData, function (json) {
//                    console.log(json);
//                    fnCallback(json);
//                });
//            },
            "oLanguage": {
                "sZeroRecords": "No records found",
                "sProcessing": "<img alt='spinner' src='${request.contextPath}/images/spinner-large.gif' /><br/>Loading..."
            },
            "fnInitComplete": fnInitComplete,
            //"iDisplayLength" : -1,
            "aLengthMenu": [
                [25, 50, 100, 500, 1000, -1],
                [25, 50, 100, 500, 1000, "All"]
            ],
            "aoColumns": [
                //{ "mData": "date" }, // 0
                //{ "mData": "location" }, // 1
                { "mData": "product" }, // 2
                { "mData": "productGroup" }, // 2
                { "mData": "quantityOnHand" } // 2
                //
                //{ "mData": "id", "bSearchable": false, "bVisible": false },
                //{ "mData": "inventoryLevelId", "bSearchable": false, "bVisible": false },
                //{ "mData": "status" }, // 0
                //{ "mData": "name" }, // 1
                //{ "mData": "productCodes" }, // 2
                //{ "mData": "minQuantity" }, // 3
                //{ "mData": "reorderQuantity" }, // 4
                //{ "mData": "maxQuantity" }, // 5
                //{ "mData": "onHandQuantity" }, //6
                //{ "mData": "totalValue" }, // 7
                //{ "mData": "hasProductGroup" },  // 8
                //{ "mData": "hasInventoryLevel" } // 9
                //{ "mData": "numProducts" }, // 2
                //{ "mData": "inventoryStatus" }, // 3

            ]
        });
        dataTable.removeClass( 'display' ).addClass('table table-striped table-bordered');

        $('#dataTable tbody').on( 'click', 'tr', function () {
            $(this).toggleClass('selected');
        });

        $('#do-btn').click( function (event) {
            event.preventDefault();
            console.log($("#dataTable tbody tr.selected"));
            //alert( dataTable.rows('.selected').data().length +' row(s) selected' );
        });


        function refreshData(event) {
            console.log("refreshData");
            event.preventDefault();
            //var dataTable = $('#dataTable').dataTable();
            dataTable.fnClearTable();
            dataTable.fnReloadAjax('${request.contextPath}/json/getInventorySnapshotsByDate');
            //dataTable.fnDraw();
        }

        function handleAjaxError( xhr, status, error ) {
            console.log("handleAjaxError");
            console.log(xhr);
            console.log(status);
            console.log(error);
            if ( status === 'timeout' ) {
                alert( 'The server took too long to send the data.' );
            }
            else {
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


        function fnInitComplete(obj1, obj2, obj3) {
            // no op
        }

        $(".datepicker").datepicker({ autoclose: true });
        $('.datepicker').datepicker('setDate', new Date(2014, 3, 28));
        $('.datepicker').datepicker('update');
        //$('#dob').val('');



        //$('#startDate').datepicker();
        //$('#endDate').datepicker();
        //$('#location\\.id').chosen({disable_search_threshold: 30});
        //$("#date").change( function (event) {
        //    event.preventDefault();
        //    refreshData();
        //});

        $('#refresh-btn').click( function (event) {
            refreshData(event);
        });





    });

</r:script>

</html>