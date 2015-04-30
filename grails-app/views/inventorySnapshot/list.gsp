<html>
<head>
    <title>Inventory Snapshots</title>
    <meta name="layout" content="analytics" />
</head>

<div id="body">

    <div class="button-bar pull-right">
        <a id="download-button" href="#" data-link="${g.createLink(controller:'inventorySnapshot', action:'download')}" class="btn btn-default">Download</a>
    </div>

    <h1 class="title">Current Stock <small>${session.warehouse.name}</small> <small>${params?.date}</small></h1>


    <table id="dataTable" class="display" cellspacing="0" width="100%">
        <thead>
            <tr>
                <%--
                <th>Date</th>
                <th>Location</th>--%>
                <th>Product</th>
                <th>Category</th>
                <th>Product group</th>
                <th>Tags</th>
                <th>QoH</th>
                <th>UoM</th>
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

        $("#download-button").click(function(event) {
            event.preventDefault();
            var link = $(this).data("link");
            var location = $("#locationId").val();
            var date = $("#date").val();
            link += "?date=" + date + "&location.id=" + location;
            window.location.href = link;

        });

        var dataTable = $('#dataTable').dataTable( {
            "bProcessing": true,
            "sServerMethod": "GET",
            "iDisplayLength": 10,
            "bScrollCollapse": true,
            "bJQueryUI": false,
            "bAutoWidth": true,
            "sAjaxSource": "${request.contextPath}/inventorySnapshot/findByDateAndLocation",
            "fnServerParams": function ( data ) {
                data.push({ name: "location.id", value: $("#locationId").val() });
                data.push({ name: "date", value: $("#date").val() })
                console.log("server post data ");
                console.log(data);
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
                [10, 25, 50, 100, 500, 1000, -1],
                [10, 25, 50, 100, 500, 1000, "All"]
            ],
            "aoColumns": [
               // { "mData": "date" }, // 0
               // { "mData": "location" }, // 1
                { "mData": "product" }, // 2
                { "mData": "category" }, // 2
                { "mData": "productGroup" }, // 2
                { "mData": "tags" }, // 2
                { "mData": "quantityOnHand" }, // 2
                { "mData": "unitOfMeasure" } // 2
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
            console.log("refreshing data ");
            console.log(event);
            event.preventDefault();
            //var dataTable = $('#dataTable').dataTable();
            dataTable.fnClearTable();
            dataTable.fnReloadAjax('${request.contextPath}/inventorySnapshot/findByDateAndLocation');
            dataTable.fnDraw();
        }

        function triggerServerUpdate(event) {
            console.log("trigger server update");
            console.log(event);
            var response = $.ajax( {
                "dataType": 'json',
                "type": "POST",
                "async": false,
                "url": "${request.contextPath}/inventorySnapshot/update",
                "data": $('form').serialize(),
                "timeout": 30000,   // optional if you want to handle timeouts (which you should)
                "success": handleAjaxSuccess,
                "error": handleAjaxError // this sets up jQuery to give me errors
            } );

            alert(response.responseText);
        }


        function handleAjaxSuccess() {
            console.log("success");
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

        function fnCallback() {
            console.log("data response success");
        }

        function fnInitComplete(obj1, obj2, obj3) {
            // no op
        }

        $(".datepicker").datepicker({ autoclose: true }).on('changeDate', onDateChange);
        //$('.datepicker').datepicker('update', new Date());

        //$('#startDate').datepicker();
        //$('#endDate').datepicker();
        //$('#location\\.id').chosen({disable_search_threshold: 30});
        function onDateChange(event) {
            console.log("date picker change");
            console.log(event);
            refreshData(event);
        }

        //$('#refresh-btn').click( function (event) {
        //    event.preventDefault();
        //
        //    refreshData(event);
        //});


        $("#trigger-btn").click( function(event) {
            event.preventDefault();
            alert("This may take some time ...");
            triggerServerUpdate(event);
        });





    });

</r:script>

</html>