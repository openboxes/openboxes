<style>
#dataTable_filter { margin: 5px;}
#dataTable_length { margin: 5px; }
#dataTable_info { margin: 5px; }
#dataTable_paginate { margin: 0px; }

</style>


<div class="box">
    <h2>

        <div class="action-menu" style="position:absolute;top:5px;right:5px">
            <button class="action-btn">
                <img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>
            </button>
            <div class="actions">
                <div class="action-menu-item">
                    <g:link controller="dashboard" action="downloadFastMoversAsCsv">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
                        <warehouse:message code="dashboard.downloadFastMoversAsCsv.label" default="Download fast movers as CSV"/>
                    </g:link>

                </div>
            </div>
        </div>
        <warehouse:message code="dashboard.fastMovers.label" default="Fast Movers"/>
    </h2>
	<div id="fastMoversWidget" class="widget-content" style="padding:0;margin:0">
        <table id="fastMoversDataTable">
            <thead>
                <th>ID</th>
                <th>Rank</th>
                <th>Code</th>
                <th>Product</th>
                <th>Count</th>
                <th>Requested</th>
                <th>On Hand</th>
            </thead>
            <tbody>

            </tbody>
        </table>
	</div>
</div>
<script type="text/javascript" charset="utf8" src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/jquery.dataTables.js"></script>
<script>
    $(window).load(function(){

        var dataTable = $('#fastMoversDataTable').dataTable( {
            "bProcessing": true,
            "sServerMethod": "GET",
            "iDisplayLength": 25,
            "bScrollInfinite": true,
            "bScrollCollapse": true,
            "sScrollY": 150,
            "bJQueryUI": true,
            "sPaginationType": "full_numbers",
            "sAjaxSource": "${request.contextPath}/json/getFastMovers",
            "fnServerParams": function ( data ) {
                data.push({ name: "location.id", value: $("#currentLocationId").val() });
            },
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                $.ajax( {
                    "dataType": 'json',
                    "type": "GET",
                    "url": sSource,
                    "data": aoData,
                    "success": fnCallback,
                    "timeout": 120000,   // optional if you want to handle timeouts (which you should)
                    "error": handleFastMoversAjaxError // this sets up jQuery to give me errors
                } );
            },
            "oLanguage": {
                "sZeroRecords": "No records found",
                "sProcessing": "Loading ... <img alt='spinner' src='${request.contextPath}/images/spinner.gif' />"
            },
            "aLengthMenu": [
                [5, 10, 25, 100, 1000, -1],
                [5, 10, 25, 100, 1000, "All"]
            ],
            "aoColumns": [

                { "mData": "id", "bVisible":false }, // 0
                { "mData": "rank", "sWidth": "1%" }, // 1
                { "mData": "productCode", "bVisible":false }, // 2
                { "mData": "name" }, // 3
                { "mData": "requisitionCount", "sWidth": "5%"  }, // 4
                { "mData": "quantityRequested", "sWidth": "5%"  }, // 5
                { "mData": "quantityOnHand", "sWidth": "5%"  } // 5

            ],
            "bUseRendered": false,
            "aaSorting": [[ 4, "desc" ], [5, "desc"]],
            "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
                $('td:eq(1)', nRow).html('<a href="${request.contextPath}/inventoryItem/showStockCard/' + aData["id"] + '">' +
                        aData["productCode"] + " " + aData["name"] + '</a>');
                return nRow;
            }

        });


    });

    function handleFastMoversAjaxError( xhr, status, error ) {
        if ( status === 'timeout' ) {
            alert( 'The server took too long to send the data.' );
        }
        else {
            // User probably refreshed page or clicked on a link, so this isn't really an error
            if(xhr.readyState == 0 || xhr.status == 0) {
                return;
            }

            var errorMessage = "<p class='error'>An unexpected error has occurred on the server.  Please contact your system administrator.</p>";

            if (xhr.responseText) {
                var error = JSON.parse(xhr.responseText);
                errorMessage = errorMessage += "<code>" + error.errorMessage + "</code>"
            }
            $("#fastMoversWidget").html(errorMessage);
        }
    }

</script>