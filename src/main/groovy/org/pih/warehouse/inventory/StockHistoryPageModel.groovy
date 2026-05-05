package org.pih.warehouse.inventory

/**
 * Everything the stock history templates (showStockHistory.gsp / printStockHistory.gsp)
 * need from the assembler in one bag, so the controller stays a thin routing step.
 *
 * groupedStockHistoryList is computed lazily because only the show template uses it -
 * the print template iterates the flat stockHistory.stockHistoryList instead.
 */
class StockHistoryPageModel {

    StockCardCommand commandInstance
    StockHistoryResult stockHistory
    StockHistoryDisplayContext displayContext

    private Map groupedStockHistoryListCache

    Map getGroupedStockHistoryList() {
        if (groupedStockHistoryListCache == null) {
            Map grouped = [:]
            stockHistory.stockHistoryList.groupBy { it.transactionYear }.each { year, history ->
                grouped.get(year, [:]) << history.groupBy { it.transactionMonth }
            }
            groupedStockHistoryListCache = grouped
        }
        return groupedStockHistoryListCache
    }
}
