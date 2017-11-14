<div class="box">
    <h2>
        <warehouse:message code="dashboard.binLocationSummary.label" />
        <img id="binLocationSummary-spinner" class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
    </h2>
    <div id="binLocationSummaryWidget" class="widget-content" style="padding:0; margin:0">
        <table id="binLocationSummaryTable" class="zebra">
            <tbody></tbody>
            <tfoot></tfoot>
        </table>
        <div id="binLocationSummary-loading">
            <div class="fade center empty">Loading ...</div>
        </div>
	</div>
</div>

<script>
    $(window).load(function(){

        $.ajax({
            dataType: "json",
            timeout: 120000,
            url: "${request.contextPath}/json/getBinLocationSummary?location.id=${session.warehouse.id}",
            success: function (data) {
                console.log("data: ", data);

                // Add each row of data to the table
                $.each(data, function(index) {
                    var row = data[index];

                    if (row.status == "inStock")
                        row.imgSrc = "${createLinkTo(dir:'images/icons/silk/accept.png')}";
                    else if (row.status == "outOfStock")
                        row.imgSrc = "${createLinkTo(dir:'images/icons/silk/exclamation.png')}";

                    row.reportUrl = "${request.contextPath}/report/showBinLocationReport?location.id=${session.warehouse.id}&status=" + row.status;

                    appendBodyRow(row);
                });

                // Add totals row to footer
                var totalCount = data.reduce(function(sum, row) { return sum + row.count; }, 0);
                var imgSrc = "${createLinkTo(dir:'images/icons/silk/asterisk.png')}";
                var reportUrl = "${request.contextPath}/report/showBinLocationReport?location.id=${session.warehouse.id}";
                var tableFooter = $('#binLocationSummaryTable tfoot');
                appendFooterRow({imgSrc: imgSrc, label: 'Total', count: totalCount, reportUrl: reportUrl});

                // Remove loading indicators
                $("#binLocationSummary-spinner").hide();
                $("#binLocationSummary-loading").hide();

            },
            error: function(xhr, status, error) {

                var errorMessage = "<p class='error'>An unexpected error has occurred on the server.  Please contact your system administrator.</p>";
                if (xhr.responseText) {
                    var error = JSON.parse(xhr.responseText);
                    errorMessage = errorMessage += "<code>" + error.errorMessage + "</code>"
                }

                $("#binLocationSummaryWidget").html(errorMessage);
                $("#binLocationSummary-spinner").hide();
                $("#binLocationSummary-loading").hide();

            }
        });
    });

    function appendBodyRow(row, error) {
        if (row) {
            var table = $('#binLocationSummaryTable tbody');
            table.append('<tr>' +
                '<td width="1%"><img src="' + row.imgSrc + '"/></td>' +
                '<td><a href="' + row.reportUrl + '">' + row.label + '</a></td>' +
                '<td class="right"><a href="' + row.reportUrl + '">' + row.count + '</a></td></tr>');
        }
    }
    function appendFooterRow(row, error) {
        if (row) {
            var table = $('#binLocationSummaryTable tfoot');
            table.append('<tr>' +
                '<th colspan="2"><a href="' + row.reportUrl + '">' + row.label + '</a></th>' +
                '<th class="right"><a href="' + row.reportUrl + '">' + row.count + '</a></th></tr>');
        }
    }


</script>