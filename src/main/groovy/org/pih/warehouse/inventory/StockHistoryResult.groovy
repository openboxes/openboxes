package org.pih.warehouse.inventory

/**
 * Output of the stock history main loop: the per-row data for the GSP plus the
 * running totals shown in the page footer.
 */
class StockHistoryResult {

    List<StockHistoryRowDto> stockHistoryList = []

    int totalDebit = 0
    int totalCredit = 0
    int totalBalance = 0
    int totalCount = 0
}
