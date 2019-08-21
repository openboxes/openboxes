<div class="box">
    <h2>
        <warehouse:message code="dashboard.activity.label" args="[session.warehouse.name]"/>
    </h2>

	<div class="widget-content" style="padding: 0; margin: 0">
		<div id="activity-summary">
			<table id="recentActivity" class="dataTable">
                <thead>
                    <th>Type</th>
                    <th>Label</th>
                    <th>Last Updated</th>
                </thead>
                <tbody></tbody>
			</table>
		</div>
	</div>
</div>	
<script>

    $(function() {
        $('.nailthumb-container img').nailthumb({width : 20, height : 20});
    });

    $(window).load(function(){

        $('#recentActivity').dataTable( {
            "bProcessing": true,
            "sServerMethod": "GET",
            "iDisplayLength": 25,
            "bSort": false,
            "bSearch": false,
            "bScrollInfinite": true,
            "bScrollCollapse": true,
            "sScrollY": 150,
            "bJQueryUI": true,
            "bAutoWidth": true,
            "sPaginationType": "full_numbers",
            "sAjaxSource": "${request.contextPath}/json/getDashboardActivity",
            "fnServerParams": function ( data ) {},
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                $.ajax( {
                    "dataType": 'json',
                    "type": "GET",
                    "url": sSource,
                    "data": aoData,
                    "success": fnCallback,
                    "timeout": 15000,   // optional if you want to handle timeouts (which you should)
                    "error": handleAjaxError // this sets up jQuery to give me errors
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
                { "mData": "type", "sWidth": "0%" },
                { "mData": "label", "sWidth": "70%" },
                { "mData": "lastUpdated", "sWidth": "30%" }

            ],
            "bUseRendered": false,
            "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
                $('td:eq(0)', nRow).html('<img src="${request.contextPath}/images/icons/silk/' + aData["type"] + '.png" />');
                return nRow;
            }

        });


    });

    function handleAjaxError( xhr, status, error ) {
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
