<table id="dialogDataTable" class="dataTable">
    <thead>
        <tr>
            <th><g:message code="default.date.label"/></th>
            <th><g:message code="default.time.label"/></th>
            <th><g:message code="transaction.transactionType.label"/></th>
            <th><g:message code="transactionType.transactionCode.label"
                           default="Transaction Code"/></th>
            <th><g:message code="default.quantity.label"/></th>
            <th><g:message code="default.balance.label" default="Balance"/></th>
        </tr>
    </thead>
    <tbody>

    </tbody>
    <tfoot>
    <tr>
        <th><g:message code="default.date.label"/></th>
        <th><g:message code="default.time.label"/></th>
        <th><g:message code="transaction.transactionType.label"/></th>
        <th><g:message code="transactionType.transactionCode.label" default="Transaction Code"/></th>
        <th><g:message code="default.quantity.label"/></th>
        <th><g:message code="default.balance.label" default="Balance"/></th>
    </tr>
    </tfoot>

</table>

<script>

    $(document).ready(function() {
        $('#dialogDataTable').dataTable({
            "bProcessing": true,
            "bSort": false,
            "iDisplayLength": 25,
            "bSearch": false,
            "bScrollCollapse": true,
            "bJQueryUI": true,
            "bAutoWidth": true,
            "bScrollInfinite": true,
            "sScrollY": 400,
            "sPaginationType": "two_button",
            "sAjaxSource": "${url}",
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                $.ajax({
                    "dataType": 'json',
                    "type": "POST",
                    "url": sSource,
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
                "sZeroRecords": "No records found",
                "sProcessing": "<img alt='spinner' src='${request.contextPath}/images/spinner.gif' /> Loading... "
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
