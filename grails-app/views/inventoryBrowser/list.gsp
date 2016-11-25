<html>
<head>
    <title>Inventory Snapshots</title>
    <meta name="layout" content="analytics" />
</head>

<div id="error" class="error">

</div>

<div id="body">
    <h2>${session.warehouse.name}
    <small>
        <span id="processingTime"></span> |
        <span id="totalValue"></span>
    </small>
    </h2>
    <hr/>
    <table id="dataTable" class="box">
        <thead>
        <tr>
            <th>Product Group ID</th>
            <th>Inventory Level ID</th>
            <th>Status</th>
            <th>Name</th>
            <th>Product codes</th>
            <th>Has Product Group</th>
            <th>Has Inventory Level</th>
            <th>Minimum</th>
            <th>Reorder</th>
            <th>Maximum</th>
            <th>QoH</th>
            <th>Unit Price</th>
            <th >Total</th>
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

        //$("#dataTable").dataTable();

        var locationId = $("#locationId").val();

        var dataTable = $('#dataTable').dataTable( {
            "bProcessing": true,
            "bServerSide": false,
            "sServerMethod": "GET",
            "bScrollCollapse": true,
            "bJQueryUI": true,
            "sPaginationType": "full_numbers",
            "sAjaxSource": "${request.contextPath}/json/getQuantityOnHandByProductGroup",
            "fnServerParams": function ( data ) {
                console.log("BEGIN fnServerParams");
                data.push({ "name": "location.id", "value": locationId });
                console.log(data);
                $(".status-filter").each(function(index, value) {
                    if (this.checked) {
                        console.log(this.name + "=" + this.value + " is checked! " + this.checked);
                        data.push({ "name": this.name, "value": this.value });
                    }
                });
                console.log(data);
                console.log("END fnServerParams");
            },
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                $.ajax( {
                    "dataType": 'json',
                    "type": "POST",
                    "url": sSource,
                    "data": aoData,
                    "success": function(json) {
                        console.log(json);
                        $("#processingTime").html(json.processingTime);
                        $("#totalValue").html("Total value of selected items $" + json.totalValueFormatted);
                        fnCallback(json);
                    },
                    "timeout": 30000,   // optional if you want to handle timeouts (which you should)
                    "error": handleAjaxError // this sets up jQuery to give me errors
                });

            },

            "oLanguage": {
                "sProcessing": "<img alt='spinner' src='${request.contextPath}/images/spinner-large.gif' /><br/>Loading..."
            },
            "iDisplayLength" : 10,
            "aLengthMenu": [
                [5, 10, 25, 50, 100, 500, 1000, -1],
                [5, 10, 25, 50, 100, 500, 1000, "All"]
            ],
            "aoColumns": [
                { "mData": "id", "bSearchable": false, "bVisible": false },
                { "mData": "inventoryLevelId", "bSearchable": false, "bVisible": false },
                { "mData": "status" }, // 0
                { "mData": "name", "sWidth": "50%" }, // 1
                { "mData": "productCodes" }, // 2
                { "mData": "hasProductGroup" },  // 8
                { "mData": "hasInventoryLevel" }, // 9
                { "mData": "minQuantity" }, // 3
                { "mData": "reorderQuantity" }, // 4
                { "mData": "maxQuantity" }, // 5
                { "mData": "onHandQuantity" }, //6
                { "mData": "unitPriceFormatted", "sClass": "right" },
                { "mData": "totalValueFormatted", "sClass": "right" } // 7
                //{ "mData": "numProducts" }, // 2
                //{ "mData": "inventoryStatus" }, // 3

            ],
            "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
                console.log(aData);
                switch(aData["status"]){
                    case 'IN_STOCK_OBSOLETE':
                    case 'IN_STOCK':
                        $(nRow).css('color', 'green')
                        break;
                    case 'NOT_STOCKED':
                        $(nRow).css('color', 'grey')
                        break;
                    case 'STOCK_OUT_OBSOLETE':
                    case 'STOCK_OUT':
                        $(nRow).css('color', 'red')
                        break;
                    case 'LOW_STOCK':
                        $(nRow).css('color', 'orange')
                        break;
                    case 'REORDER':
                        $(nRow).css('color', 'yellow;')
                        break;
                    case 'IDEAL_STOCK':
                        $(nRow).css('color', 'green')
                        break;
                    case 'OVERSTOCK':
                        $(nRow).css('color', 'blue')
                        break;
                    case 'INVALID':
                        $(nRow).css('color', 'grey')
                        break;
                }
                if (aData["id"]) {
                    $('td:eq(1)', nRow).html('<a href="${request.contextPath}/productGroup/edit/' + aData['id'] + '" target="_blank">' + aData['name'] + '</a>');
                }
                if (aData["inventoryLevelId"]) {
                    $('td:eq(5)', nRow).html('<a href="${request.contextPath}/inventoryLevel/edit/' + aData['inventoryLevelId'] + '" target="_blank">' + aData['minQuantity'] + '</a>');
                    $('td:eq(6)', nRow).html('<a href="${request.contextPath}/inventoryLevel/edit/' + aData['inventoryLevelId'] + '" target="_blank">' + aData['reorderQuantity'] + '</a>');
                    $('td:eq(7)', nRow).html('<a href="${request.contextPath}/inventoryLevel/edit/' + aData['inventoryLevelId'] + '" target="_blank">' + aData['maxQuantity'] + '</a>');
                }
                return nRow;
            }

        });

        // Add some CSS styles to the table
        dataTable.removeClass( 'display' ).addClass('table table-striped table-bordered');

        function handleAjaxError( xhr, textStatus, error ) {
            if ( textStatus === 'timeout' ) {
                alert( 'The server took too long to send the data.' );
            }
            else {
                alert( 'An error occurred on the server. Please submit a bug report using the link under the Help menu.' );
            }
            dataTable.fnProcessingIndicator( false );
        }

        // Toggle checkboxes
        $("#status-0").click(function() {
            $('input:checkbox').not(this).prop('checked', this.checked);
        });

        // Refresh datatable with data from the server
        $('#refresh-btn').click( function (event) {
            event.preventDefault();
            dataTable.fnClearTable();
            dataTable.fnReloadAjax('${request.contextPath}/json/getQuantityOnHandByProductGroup');
            //console.log(dataTable);
            //dataTable.fnDraw();
        } );

        $('#cancel-btn').click( function (event) {
            event.preventDefault();
            xhr.abort();
        } );

        // AJAX Call to retrieve the count/percentage for each inventory status in the sidebar
        var xhr = $.ajax({
            dataType: "json",
            timeout: 30000,
            url: "${request.contextPath}/json/getSummaryByProductGroup?location.id=${session.warehouse.id}",
            //data: data,
            success: function (data) {

                console.log("Loading data ...");
                console.log(data);
                $.each(data, function( key, value ) {
                    console.log(key);
                    console.log(value);
                    $("#badge-status-" + key).html(value.numProductGroups);
                    $("#badge-percentage-" + key).html(Math.round(value.percentage * 100) + "%");
                });
                //$("#reportContent").html(data);
                // {"lowStock":103,"reorderStock":167,"overStock":38,"totalStock":1619,"reconditionedStock":54,"stockOut":271,"inStock":1348}
                //$('#lowStockCount').html(data.lowStock?data.lowStock:0);
                $("#status-spinner").hide();
            },
            error: function(xhr, status, error) {
                console.log(xhr);
                console.log(status);
                console.log(error);
                $("#status-spinner").hide();
            }
        });

    });


    jQuery.fn.dataTableExt.oApi.fnProcessingIndicator = function ( oSettings, onoff ) {
        if ( typeof( onoff ) == 'undefined' ) {
            onoff = true;
        }
        this.oApi._fnProcessingDisplay( oSettings, onoff );
    };
</r:script>

</html>