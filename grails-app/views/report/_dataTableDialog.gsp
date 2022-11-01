<table id="dialogDataTable" class="dataTable">
    <thead>
        <tr>
            <th><g:message code="default.date.label" default="Date"/></th>
            <th><g:message code="default.time.label" default="Time"/></th>
            <th><g:message code="transaction.transactionType.label" default="Type"/></th>
            <th><g:message code="transactionType.transactionCode.label" default="Transaction Code"/></th>
            <th><g:message code="default.quantity.label" default="Quantity"/></th>
            <th><g:message code="default.balance.label" default="Balance"/></th>
        </tr>
    </thead>
    <tbody>

    </tbody>
    <tfoot>
    <tr>
        <th><g:message code="default.date.label" default="Date"/></th>
        <th><g:message code="default.time.label" default="Time"/></th>
        <th><g:message code="transaction.transactionType.label" default="Type"/></th>
        <th><g:message code="transactionType.transactionCode.label" default="Transaction Code"/></th>
        <th><g:message code="default.quantity.label" default="Quantity"/></th>
        <th><g:message code="default.balance.label" default="Balance"/></th>
    </tr>
    </tfoot>

</table>

<script>

    $(document).ready(function() {
        $('#dialogDataTable').dataTable({
            "bProcessing": true,
            "bSort": false,
            "iDisplayLength": 100,
            "bSearch": false,
            "bScrollCollapse": true,
            "bJQueryUI": true,
            "bAutoWidth": true,
            "bScrollInfinite": true,
            "sScrollY": 400,
            "sPaginationType": "two_button",
            "sAjaxSource": "${url}",
            "fnServerData": function ( sSource, aoData, fnCallback ) {
              const sSourceParsed = sSource.replaceAll("&amp;", "&");
                $.ajax({
                    "dataType": 'json',
                    "type": "POST",
                    "url": sSourceParsed,
                    "data": aoData,
                    "success": fnCallback,
                    "timeout": 30000,
                    "error": handleAjaxError
                })
            },
            "fnServerParams": function (data) {
                $("#locationId").val();
                $("#startDate").val();
                $("#endDate").val();
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
            },
            "aoColumns": [
                {"mData": "transactionDate" },
                {"mData": "transactionTime" },
                {"mData": "transactionTypeName"},
                {"mData": "transactionCode"},
                {"mData": "quantity", "sType": 'numeric'},
                {"mData": "balance", "sType": 'numeric'}
            ]
        });
    });

</script>
