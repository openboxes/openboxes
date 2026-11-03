<%-- A summary of the product's period, like its row in the report: opening + credits - debits + differences = closing.
     Filled in by renderDialogSummary when the transactions have loaded. --%>
<div class="dialog-summary">
    <div class="dialog-summary-equation"></div>
</div>
<%-- The filter set by the summary or the By type tab. Moved into the table's search row when the table is created. --%>
<span class="dialog-filter" style="display: none">
    <g:message code="report.transaction.filter.showing.label" default="Showing"/>:
    <strong class="dialog-filter-label"></strong>
    <a href="#" class="dialog-filter-clear"><g:message code="default.clear.label" default="Clear"/></a>
</span>
<div id="dialogTabs">
    <%-- On the right of the tab bar. Starts the same as the report that was run, so that the summary matches the
         product's row. --%>
    <label class="internal-transfers-toggle" title="${g.message(code: 'report.transaction.internalTransfer.hint', default: 'A transfer between two bin locations of this facility (e.g. a putaway or stock transfer). It moves stock between bins without changing the balance, so it isn\'t counted in the credits and debits of the report.')}">
        <input type="checkbox" id="dialogIncludeInternalTransfers" ${params.includeInternalTransfers == 'true' ? 'checked="checked"' : ''}/>
        <g:message code="report.transaction.includeInternalTransfers.label" default="Include internal transfers"/>
        <span class="internal-transfers-count"></span>
    </label>
    <ul>
        <li><a href="#dialogTransactionsTab"><g:message code="report.transaction.transactionsTab.label" default="Transactions"/> <span class="dialog-tab-count"></span></a></li>
        <li><a href="#dialogByTypeTab"><g:message code="report.transaction.byTypeTab.label" default="By type"/></a></li>
    </ul>
    <div id="dialogTransactionsTab">
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
    </div>
    <%-- The transactions grouped by type. Filled in by renderDialogByType. Click a type to see its transactions. --%>
    <div id="dialogByTypeTab">
        <table class="dialog-by-type">
            <thead>
                <tr>
                    <th><g:message code="transaction.transactionType.label" default="Type"/></th>
                    <th class="right"><g:message code="transactions.label" default="Transactions"/></th>
                    <th class="right"><g:message code="default.credits.label" default="Credits"/></th>
                    <th class="right"><g:message code="default.debits.label" default="Debits"/></th>
                    <th class="right"><g:message code="report.transaction.net.label" default="Net"/></th>
                </tr>
            </thead>
            <tbody></tbody>
            <tfoot></tfoot>
        </table>
    </div>
</div>

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

        var labels = {
            opening: "${g.message(code: 'report.transaction.openingBalance.label', default: 'Opening')}",
            credits: "${g.message(code: 'default.credits.label', default: 'Credits')}",
            debits: "${g.message(code: 'default.debits.label', default: 'Debits')}",
            differences: "${g.message(code: 'report.transaction.differences.label', default: 'Differences')}",
            closing: "${g.message(code: 'report.transaction.closingBalance.label', default: 'Closing')}",
            transactions: "${g.message(code: 'report.transaction.summary.transactions.label', default: 'Transactions')}",
            counts: "${g.message(code: 'report.transaction.summary.counts.label', default: 'Counts')}",
            internalTransfers: "${g.message(code: 'report.transaction.summary.internalTransfers.label', default: 'Internal transfers')}",
            internalCounted: "${g.message(code: 'report.transaction.summary.counted.label', default: 'counted')}",
            internalNotCounted: "${g.message(code: 'report.transaction.summary.notCounted.label', default: 'not counted')}",
            countsType: "${g.message(code: 'report.transaction.byType.counts.label', default: 'Counts')}",
            countsHint: "${g.message(code: 'report.transaction.byType.counts.hint', default: 'Counts set the balance instead of adding to it. Their differences are in the Unrecorded change row.')}",
            internalType: "${g.message(code: 'report.transaction.byType.internalTransfers.label', default: 'Internal transfers')}",
            total: "${g.message(code: 'default.total.label', default: 'Total')}",
            filterHint: "${g.message(code: 'report.transaction.summary.filter.hint', default: 'Click to show only these rows')}"
        };

        // The rows of the dialog, and the filter that the summary and the By type tab can set on them (null for none)
        var dialogRows = [];
        var dialogFilter = null;

        function isIncludingInternalTransfers() {
            return $('#dialogIncludeInternalTransfers').is(':checked');
        }

        function isCountedRow(row) {
            return row.quantityChange != null && (!row.internal || isIncludingInternalTransfers());
        }

        function isGhostRow(row) {
            return !!ghostLabels[row.transactionCode];
        }

        function isTransactionRow(row) {
            return !!row.transactionId && row.transactionCode !== 'PRODUCT_INVENTORY';
        }

        function formatNumber(value) {
            return Number(value || 0).toLocaleString('en-US');
        }

        function formatSigned(value) {
            return (value > 0 ? '+' : value < 0 ? '−' : '±') + formatNumber(Math.abs(value));
        }

        function countTransactions(rows) {
            var ids = {};
            rows.forEach(function (row) { ids[row.transactionId] = true; });
            return Object.keys(ids).length;
        }

        // Shows only the rows that pass the given test (and the opening and closing balances). Clicking the same filter
        // again clears it.
        function setDialogFilter(key, label, test) {
            dialogFilter = (dialogFilter && dialogFilter.key === key) ? null : { key: key, label: label, test: test };
            $('.dialog-filter').toggle(!!dialogFilter);
            $('.dialog-filter-label').text(dialogFilter ? dialogFilter.label : '');
            $('.dialog-summary [data-filter]').removeClass('active')
                .filter(function () { return dialogFilter && $(this).data('filter') === dialogFilter.key; }).addClass('active');
            $('#dialogTabs').tabs('select', 0);
            $('#dialogDataTable').dataTable().fnDraw();
        }

        // Hide internal transfers unless "Include internal transfers" is checked, and the rows that don't pass the
        // filter. The filter function is added once, since this dialog is loaded each time a product is clicked, and
        // only applies to this table. It calls the dialog's current filter, so that it uses this dialog's state.
        window.transactionReportDialogFilter = function (row) {
            if (balanceLabels[row.transactionCode]) {
                return true;
            }
            if (row.internal && !isIncludingInternalTransfers()) {
                return false;
            }
            return !dialogFilter || dialogFilter.test(row);
        };
        if (!$.fn.dataTableExt.internalTransferFilterAdded) {
            $.fn.dataTableExt.internalTransferFilterAdded = true;
            $.fn.dataTableExt.afnFiltering.push(function (oSettings, aData, iDataIndex) {
                return oSettings.nTable.id !== 'dialogDataTable'
                    || window.transactionReportDialogFilter(oSettings.aoData[iDataIndex]._aData);
            });
        }

        // Opening + credits - debits + differences = closing, like the product's row in the report. Credits and debits
        // include internal transfers only when they're included (they cancel out either way).
        function renderDialogSummary() {
            var opening = 0, closing = 0, credits = 0, debits = 0, differences = 0;
            dialogRows.forEach(function (row) {
                if (row.transactionCode === 'BALANCE_OPENING') {
                    opening = row.balance;
                } else if (row.transactionCode === 'BALANCE_CLOSING') {
                    closing = row.balance;
                } else if (isGhostRow(row)) {
                    differences += row.quantityChange;
                } else if (isCountedRow(row)) {
                    if (row.quantityChange > 0) {
                        credits += row.quantityChange;
                    } else {
                        debits -= row.quantityChange;
                    }
                }
            });

            // Read like a bank statement: the opening balance, plus credits, minus debits, plus or minus differences,
            // equals the closing balance. The balances are set apart from the period's activity between them, and the
            // activity carries its sign (instead of the report's parentheses, which would read as a double negative).
            // Zeros are grey.
            var balance = function (label, value) {
                return $('<span class="dialog-stat dialog-stat-balance">').append(
                    $('<span class="dialog-stat-label">').text(label),
                    $('<span class="dialog-stat-value">').text(formatNumber(value)));
            };
            var signed = function (value) {
                return $('<span class="dialog-stat-value">').text(value ? formatSigned(value) : '0')
                    .addClass(value > 0 ? 'credit' : value < 0 ? 'debit' : 'zero');
            };
            // Credits, debits and differences show only their rows when clicked
            var activity = function (key, label, value, test) {
                return $('<a href="#" class="dialog-stat dialog-summary-figure">').attr('title', labels.filterHint).attr('data-filter', key)
                    .append($('<span class="dialog-stat-label">').text(label), signed(value))
                    .click(function (event) {
                        event.preventDefault();
                        setDialogFilter(key, label, test);
                    });
            };
            $('.dialog-summary-equation').empty().append(
                balance(labels.opening, opening),
                activity('credits', labels.credits, credits, function (row) { return isCountedRow(row) && !isGhostRow(row) && row.quantityChange > 0; }),
                activity('debits', labels.debits, -debits, function (row) { return isCountedRow(row) && !isGhostRow(row) && row.quantityChange < 0; }),
                activity('differences', labels.differences, differences, function (row) { return isGhostRow(row); }),
                $('<span class="dialog-stat-operator">').text('='),
                balance(labels.closing, closing)
            );

            // The number of transactions (and counts) in the table goes in the Transactions tab, with the breakdown in
            // its tooltip, and the number of internal transfers next to the option that includes them
            var transactions = countTransactions(dialogRows.filter(function (row) { return isTransactionRow(row) && !row.internal; }));
            var counts = countTransactions(dialogRows.filter(function (row) { return row.transactionCode === 'PRODUCT_INVENTORY'; }));
            var internal = countTransactions(dialogRows.filter(function (row) { return row.internal; }));
            var parts = [labels.transactions + ': ' + transactions, labels.counts + ': ' + counts];
            if (internal) {
                parts.push(labels.internalTransfers + ': ' + internal + ' (' +
                    (isIncludingInternalTransfers() ? labels.internalCounted : labels.internalNotCounted) + ')');
            }
            $('.dialog-tab-count').text('(' + (transactions + counts + (isIncludingInternalTransfers() ? internal : 0)) + ')')
                .closest('a').attr('title', parts.join(' · '));
            $('.internal-transfers-count').text(internal ? '(' + internal + ')' : '');
            if (dialogFilter) {
                $('.dialog-summary [data-filter]').filter(function () { return $(this).data('filter') === dialogFilter.key; }).addClass('active');
            }
        }

        // The transactions grouped by type, with the counts, the unrecorded changes and the internal transfers in
        // their own rows. Clicking a type shows its transactions.
        function renderDialogByType() {
            var groups = {}, order = [];
            var addTo = function (key, label, test, row) {
                if (!groups[key]) {
                    groups[key] = { key: key, label: label, test: test, rows: [], credits: 0, debits: 0 };
                    order.push(key);
                }
                var group = groups[key];
                group.rows.push(row);
                if (row.quantityChange > 0) {
                    group.credits += row.quantityChange;
                } else if (row.quantityChange < 0) {
                    group.debits -= row.quantityChange;
                }
            };
            dialogRows.forEach(function (row) {
                if (balanceLabels[row.transactionCode]) {
                    return;
                }
                if (isGhostRow(row)) {
                    addTo('ghost', ghostLabels.UNRECORDED_CHANGE, function (r) { return isGhostRow(r); }, row);
                } else if (row.transactionCode === 'PRODUCT_INVENTORY') {
                    addTo('counts', labels.countsType, function (r) { return r.transactionCode === 'PRODUCT_INVENTORY'; }, row);
                } else if (row.internal) {
                    addTo('internal', labels.internalType, function (r) { return !!r.internal; }, row);
                } else {
                    var name = row.transactionTypeName;
                    addTo('type:' + name, name, function (r) { return !r.internal && r.transactionTypeName === name; }, row);
                }
            });

            // Types that add stock first, then types that remove it, largest first; then the special rows
            var special = ['internal', 'counts', 'ghost'];
            var types = order.filter(function (key) { return special.indexOf(key) < 0; }).map(function (key) { return groups[key]; });
            types.sort(function (a, b) { return (b.credits - b.debits) - (a.credits - a.debits); });
            special.forEach(function (key) {
                if (groups[key]) {
                    types.push(groups[key]);
                }
            });

            var tbody = $('#dialogByTypeTab tbody').empty();
            // The unrecorded changes are one figure (Differences) in the summary, so they only have a net quantity here,
            // and the credits and debits columns add up to the summary's
            var totalCredits = 0, totalDebits = 0, totalDifferences = 0, totalTransactions = 0;
            types.forEach(function (group) {
                var isCounts = group.key === 'counts';
                var isGhost = group.key === 'ghost';
                var notCounted = group.key === 'internal' && !isIncludingInternalTransfers();
                if (isGhost) {
                    totalDifferences += group.credits - group.debits;
                } else if (!isCounts && !notCounted) {
                    totalCredits += group.credits;
                    totalDebits += group.debits;
                }
                if (!isCounts && !isGhost && !notCounted) {
                    totalTransactions += countTransactions(group.rows);
                }
                var tr = $('<tr>').toggleClass('not-counted', notCounted).toggleClass('special', special.indexOf(group.key) >= 0)
                    .attr('title', isCounts ? labels.countsHint : group.key === 'internal' ? internalTransferHint : isGhost ? ghostHints.UNRECORDED_CHANGE : labels.filterHint)
                    .append(
                        $('<td>').append($('<a href="#">').text(group.label)),
                        $('<td class="right">').text(isGhost ? formatNumber(group.rows.length) : countTransactions(group.rows)),
                        $('<td class="right credit">').text(isCounts || isGhost || !group.credits ? '' : '+' + formatNumber(group.credits)),
                        // .debit adds the parentheses (openboxes.css), so it's only set when there's a quantity
                        $('<td class="right">').toggleClass('debit', !isCounts && !isGhost && !!group.debits)
                            .text(isCounts || isGhost || !group.debits ? '' : formatNumber(group.debits)),
                        $('<td class="right">').text(isCounts ? '' : formatSigned(group.credits - group.debits))
                    );
                tr.find('a').click(function (event) {
                    event.preventDefault();
                    if (group.key === 'internal' && !isIncludingInternalTransfers()) {
                        $('#dialogIncludeInternalTransfers').prop('checked', true).change();
                    }
                    setDialogFilter(group.key, group.label, group.test);
                });
                tbody.append(tr);
            });
            $('#dialogByTypeTab tfoot').empty().append($('<tr>').append(
                $('<td>').text(labels.total),
                $('<td class="right">').text(totalTransactions),
                $('<td class="right credit">').text(totalCredits ? '+' + formatNumber(totalCredits) : ''),
                $('<td class="right">').toggleClass('debit', !!totalDebits).text(totalDebits ? formatNumber(totalDebits) : ''),
                $('<td class="right">').text(formatSigned(totalCredits - totalDebits + totalDifferences))
            ));
        }

        $('#dialogIncludeInternalTransfers').change(function () {
            renderDialogSummary();
            renderDialogByType();
            $('#dialogDataTable').dataTable().fnDraw();
        });

        $('.dialog-filter-clear').click(function (event) {
            event.preventDefault();
            if (dialogFilter) {
                setDialogFilter(dialogFilter.key);
            }
        });

        $('#dialogTabs').tabs({
            // The table was hidden while another tab was shown, so its header no longer lines up with its rows
            show: function (event, ui) {
                if (ui.index === 0 && $.fn.dataTable.fnIsDataTable($('#dialogDataTable')[0])) {
                    $('#dialogDataTable').dataTable().fnAdjustColumnSizing(false);
                }
            }
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
                        dialogRows = (json && json.aaData) || [];
                        renderDialogSummary();
                        renderDialogByType();
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
                $('#dialogDataTable_wrapper .fg-toolbar:first').prepend($('.dialog-filter'));
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
