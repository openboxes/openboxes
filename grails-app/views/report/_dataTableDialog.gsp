<label class="internal-transfers-toggle" title="${g.message(code: 'report.transaction.internalTransfer.hint', default: 'A transfer between two bin locations of this facility (e.g. a putaway or stock transfer). It moves stock between bins without changing the balance, so it isn\'t counted in the credits and debits of the report.')}">
    <input type="checkbox" id="dialogIncludeInternalTransfers"/>
    <g:message code="report.transaction.includeInternalTransfers.label" default="Include internal transfers"/>
</label>
<table id="dialogDataTable" class="dataTable">
    <thead>
        <tr>
            <th><g:message code="default.dateTime.label" default="Date/Time"/></th>
            <th><g:message code="transaction.transactionType.label" default="Type"/></th>
            <th><g:message code="transaction.label" default="Transaction"/></th>
            <th><g:message code="default.reference.label" default="Reference"/></th>
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
        // The label of each type of document a transaction can come from (orders use their order type)
        var referenceLabels = {
            REQUISITION: "${g.message(code: 'requisition.label', default: 'Request')}",
            SHIPMENT: "${g.message(code: 'shipment.label', default: 'Shipment')}",
            ORDER: "${g.message(code: 'order.label', default: 'Order')}"
        };
        var balanceLabels = {
            BALANCE_OPENING: "${g.message(code: 'report.transaction.openingBalanceRow.label', default: 'Opening Balance')}",
            BALANCE_CLOSING: "${g.message(code: 'report.transaction.closingBalanceRow.label', default: 'Closing Balance')}"
        };
        // "Ghost" rows: changes in quantity on hand that no transaction recorded
        var ghostLabels = {
            UNRECORDED_CHANGE: "${g.message(code: 'report.transaction.unrecordedChange.label', default: 'Unrecorded change')}",
            UNRECONCILED: "${g.message(code: 'report.transaction.unreconciled.label', default: 'Unexplained difference')}"
        };
        var ghostHints = {
            UNRECORDED_CHANGE: "${g.message(code: 'report.transaction.unrecordedChange.hint', default: 'The difference between the count below and the balance before it, i.e. a change that was not recorded as a transaction (e.g. a loss, or a transaction that was deleted after the count). These add up to the Differences column.')}",
            UNRECONCILED: "${g.message(code: 'report.transaction.unreconciled.hint', default: 'The transactions above do not add up to the closing balance. This is the rest of the difference.')}"
        };
        var countHint = "${g.message(code: 'report.transaction.countQuantity.hint', default: 'Counted quantity. The balance is set to the counted quantity instead of adding to it.')}";
        var internalTransferHint = $('.internal-transfers-toggle').attr('title');

        // Hide internal transfers unless "Include internal transfers" is checked. The filter is added once, since this
        // dialog is loaded each time a product is clicked, and only applies to this table.
        if (!$.fn.dataTableExt.internalTransferFilterAdded) {
            $.fn.dataTableExt.internalTransferFilterAdded = true;
            $.fn.dataTableExt.afnFiltering.push(function (oSettings, aData, iDataIndex) {
                return oSettings.nTable.id !== 'dialogDataTable'
                    || !oSettings.aoData[iDataIndex]._aData.internal
                    || $('#dialogIncludeInternalTransfers').is(':checked');
            });
        }
        $('#dialogIncludeInternalTransfers').change(function () {
            $('#dialogDataTable').dataTable().fnDraw();
        });

        // Until the transactions have loaded, the empty table shows that they're loading instead of "No records found"
        var loadingMessage = "<img alt='spinner' src=\"${resource(dir: 'images', file: 'spinner.gif')}\" /> ${g.message(code: 'default.loading.label', default: 'Loading...')}";
        var emptyTableMessage = "${g.message(code: 'default.dataTable.noData.label', default: 'No data available in table')}";
        var zeroRecordsMessage = "${g.message(code: 'default.dataTable.noRecordsFound.label', default: 'No records found')}";
        function showEmptyTableMessages(oSettings) {
            oSettings.oLanguage.sEmptyTable = emptyTableMessage;
            oSettings.oLanguage.sZeroRecords = zeroRecordsMessage;
        }

        $('#dialogDataTable').dataTable({
            "bProcessing": false,  // The empty table shows the loading message
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
            "fnServerData": function ( sSource, aoData, fnCallback, oSettings ) {
              const sSourceParsed = sSource.replaceAll("&amp;", "&");
                $.ajax({
                    "dataType": 'json',
                    "type": "POST",
                    "url": sSourceParsed,
                    "data": aoData,
                    "success": function (json) {
                        showEmptyTableMessages(oSettings);
                        fnCallback(json);
                    },
                    "timeout": 30000,
                    "error": function (xhr, status, error) {
                        showEmptyTableMessages(oSettings);
                        oSettings.oInstance.fnDraw();
                        handleAjaxError(xhr, status, error);
                    }
                })
            },
            "oLanguage": {
              "sEmptyTable": loadingMessage,
              "sInfoEmpty": "${g.message(code: 'default.dataTable.showingZeroEntries.label', default: 'Showing 0 to 0 of 0 entries')}",
              "sInfo": "${g.message(code: 'default.dataTable.showing.label', 'Showing')} " +
                "_START_" +
                " ${g.message(code: 'default.dataTable.to.label', default: 'to')} " +
                "_END_" +
                " ${g.message(code: 'default.dataTable.of.label', default: 'of')} " +
                "_TOTAL_" +
                " ${g.message(code: 'default.dataTable.entries.label', default: 'entries')}",
              "sSearch": "${g.message(code: 'default.dataTable.search.label', default: 'Search:')}",
              "sZeroRecords": loadingMessage,
              "sProcessing": "<img alt='spinner' src=\"${resource(dir: 'images', file: 'spinner.gif')}\" /> ${g.message(code: 'default.loading.label', default: 'Loading...')}",
            },
            "aoColumns": [
                // Every column has a default, because some rows (e.g. the ghost rows) don't have every field
                {"mData": "transactionDate", "sClass": "date-cell", "sWidth": "140px", "sDefaultContent": ""},
                {"mData": "transactionTypeName", "sDefaultContent": ""},
                {"mData": "transactionNumber", "sWidth": "110px", "sDefaultContent": ""},
                {"mData": "reference", "sWidth": "170px", "sDefaultContent": ""},
                {"mData": "lotNumber", "sWidth": "110px", "sDefaultContent": ""},
                {"mData": "binLocation", "sWidth": "110px", "sDefaultContent": ""},
                {"mData": "quantity", "sClass": "right", "sWidth": "80px", "sDefaultContent": ""},
                {"mData": "balance", "sClass": "right", "sWidth": "80px", "sDefaultContent": ""}
            ],
            // DataTables doesn't filter the first draw after loading the data when sorting is off, so redraw to hide
            // the internal transfers
            "fnInitComplete": function () {
                this.fnDraw();
            },
            "fnDrawCallback": function () {
                // Dim a date that's the same as the row above, and draw a line above the first row of each day, so
                // that it's easier to see where each day starts
                var previousDate = null;
                $('#dialogDataTable tbody tr').each(function () {
                    var date = $(this).find('td.date-cell .date').text();
                    $(this).toggleClass('new-day', previousDate !== null && date !== previousDate);
                    $(this).find('td.date-cell .date').toggleClass('same-date', date === previousDate);
                    previousDate = date;
                });
            },
            "fnRowCallback": function (nRow, aData) {
                var cells = $('td', nRow);
                // The time is lighter than the date, so that the dates are easier to scan down the column
                cells.eq(0).empty().append(
                    $('<span class="date">').text(aData.transactionDate),
                    ' ',
                    $('<span class="time">').text(aData.transactionTime)
                );

                if (ghostLabels[aData.transactionCode]) {
                    $(nRow).addClass('ghost-row').attr('title', ghostHints[aData.transactionCode]);
                    cells.eq(1).text(ghostLabels[aData.transactionCode]);
                    // No transaction or reference, but the lot and bin that the change was in (if it's known)
                    cells.slice(2, 4).empty();
                    cells.eq(4).text(aData.lotNumber || '');
                    cells.eq(5).text(aData.binLocation || '');
                    var ghostQuantityCell = cells.eq(6).removeClass('credit debit zero counted');
                    if (aData.quantityChange > 0) {
                        ghostQuantityCell.text('+' + aData.quantityChange.toLocaleString('en-US')).addClass('credit');
                    } else {
                        renderQuantity(ghostQuantityCell, Math.abs(aData.quantityChange), 'debit');
                    }
                    renderQuantity(cells.eq(7), aData.balance);
                    return nRow;
                }

                // Opening and closing balances only have a balance
                if (balanceLabels[aData.transactionCode]) {
                    $(nRow).addClass('balance-row');
                    cells.eq(1).text(balanceLabels[aData.transactionCode]);
                    cells.slice(2, 7).empty();
                    renderQuantity(cells.eq(7), aData.balance);
                    return nRow;
                }

                // Internal transfers are dimmed, since they don't change the balance
                $(nRow).toggleClass('internal-row', !!aData.internal);
                if (aData.internal) {
                    $(nRow).attr('title', internalTransferHint);
                }

                if (aData.transactionId) {
                    cells.eq(2).empty().append($('<a target="_blank">')
                        .attr('href', transactionUrl + aData.transactionId)
                        .text(aData.transactionNumber || aData.transactionId));
                }

                // The document the transaction came from, e.g. "Putaway › 1234"
                var referenceCell = cells.eq(3).empty().removeAttr('title');
                if (aData.reference) {
                    var referenceText = (aData.reference.typeName || referenceLabels[aData.reference.type]) + " \u203a " +
                        (aData.reference.number || '');
                    referenceCell.attr('title', referenceText).append($('<a target="_blank" class="ellipsis">')
                        .attr('href', "${request.contextPath}" + aData.reference.url)
                        .text(referenceText));
                }

                // Credits are shown as +n and debits as (n). Counts set the balance, so they're dimmed.
                var quantityCell = cells.eq(6).removeClass('credit debit zero counted').removeAttr('title');
                if (aData.quantityChange == null) {
                    quantityCell.text(Number(aData.quantity).toLocaleString('en-US')).addClass('counted').attr('title', countHint);
                } else if (aData.quantityChange > 0) {
                    quantityCell.text('+' + aData.quantityChange.toLocaleString('en-US')).addClass('credit');
                } else {
                    renderQuantity(quantityCell, Math.abs(aData.quantityChange), 'debit');
                }
                renderQuantity(cells.eq(7), aData.balance);
                return nRow;
            }
        });
    });

</script>
