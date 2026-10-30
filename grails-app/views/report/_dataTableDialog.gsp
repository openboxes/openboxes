<table id="dialogDataTable" class="dataTable">
    <thead>
        <tr>
            <th><g:message code="default.dateTime.label" default="Date/Time"/></th>
            <th><g:message code="transaction.transactionType.label" default="Type"/></th>
            <th><g:message code="transaction.label" default="Transaction"/></th>
            <th><g:message code="inventory.lotNumber.label" default="Lot number"/></th>
            <th><g:message code="location.binLocation.label" default="Bin Location"/></th>
            <th class="right"><g:message code="default.quantity.label" default="Quantity"/></th>
            <th class="right"><g:message code="default.balance.label" default="Balance"/></th>
        </tr>
    </thead>
    <tbody>

    </tbody>
</table>

<script>

    $(document).ready(function() {
        var transactionUrl = "${request.contextPath}/inventory/showTransaction/";
        var balanceLabels = {
            BALANCE_OPENING: "${g.message(code: 'report.transaction.openingBalanceRow.label', default: 'Opening Balance')}",
            BALANCE_CLOSING: "${g.message(code: 'report.transaction.closingBalanceRow.label', default: 'Closing Balance')}"
        };
        var countHint = "${g.message(code: 'report.transaction.countQuantity.hint', default: 'Counted quantity. The balance is set to the counted quantity instead of adding to it.')}";

        $('#dialogDataTable').dataTable({
            "bProcessing": true,
            "bSort": false,
            "iDisplayLength": 100,
            "bSearch": false,
            "bScrollCollapse": true,
            "bJQueryUI": true,
            "bAutoWidth": false,
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
                {"mData": "transactionDate", "sWidth": "120px"},
                {"mData": "transactionTypeName"},
                {"mData": "transactionNumber", "sWidth": "110px", "sDefaultContent": ""},
                {"mData": "lotNumber", "sWidth": "110px", "sDefaultContent": ""},
                {"mData": "binLocation", "sWidth": "110px", "sDefaultContent": ""},
                {"mData": "quantity", "sClass": "right", "sWidth": "80px", "sDefaultContent": ""},
                {"mData": "balance", "sClass": "right", "sWidth": "80px"}
            ],
            "fnRowCallback": function (nRow, aData) {
                var cells = $('td', nRow);
                cells.eq(0).text(aData.transactionDate + " " + aData.transactionTime);

                // Opening and closing balances only have a balance
                if (balanceLabels[aData.transactionCode]) {
                    $(nRow).addClass('balance-row');
                    cells.eq(1).text(balanceLabels[aData.transactionCode]);
                    cells.slice(2, 6).empty();
                    renderQuantity(cells.eq(6), aData.balance);
                    return nRow;
                }

                if (aData.transactionId) {
                    cells.eq(2).empty().append($('<a target="_blank">')
                        .attr('href', transactionUrl + aData.transactionId)
                        .text(aData.transactionNumber || aData.transactionId));
                }

                // Credits are shown as +n and debits as (n). Counts set the balance, so they're dimmed.
                var quantityCell = cells.eq(5).removeClass('credit debit zero counted').removeAttr('title');
                if (aData.quantityChange == null) {
                    quantityCell.text(Number(aData.quantity).toLocaleString('en-US')).addClass('counted').attr('title', countHint);
                } else if (aData.quantityChange > 0) {
                    quantityCell.text('+' + aData.quantityChange.toLocaleString('en-US')).addClass('credit');
                } else {
                    renderQuantity(quantityCell, Math.abs(aData.quantityChange), 'debit');
                }
                renderQuantity(cells.eq(6), aData.balance);
                return nRow;
            }
        });
    });

</script>
