package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.Requisition

/**
 * One row of the stock history table. Built per TransactionEntry by
 * StockHistoryAssembler#computeStockHistoryRows.
 *
 * transactionYear / transactionMonth are pre-formatted strings used by the page
 * model's groupBy. requisitionId / orderId are pre-resolved scalars so the GSP
 * looks up StockHistoryRequisitionDto / StockHistoryOrderDto via the display
 * context maps without ever materialising the entity (and triggering eager
 * Picklist - see notes on getTransactionRefIds).
 */
class StockHistoryRowDto {

    String transactionYear
    String transactionMonth
    TransactionCode transactionCode
    Transaction transaction

    /**
     * Always null in the current build - kept solely so that
     * _showStockHistoryPrintable.gsp's `stockHistoryEntry?.requisition?.X`
     * references continue to safe-nav to null instead of throwing
     * MissingPropertyException on the typed POGO. Those branches in the printable
     * template are vestigial (the link they emit has empty id / labels), but
     * removing them is out of scope for the performance refactor.
     */
    Requisition requisition

    String requisitionId
    String orderId

    LocalTransferInfo localTransferInfo

    Location binLocation
    Location destinationBinLocation
    InventoryItem inventoryItem

    String comments
    Integer quantity
    Integer balance

    boolean isDebit
    boolean isCredit
    boolean isInternal
    boolean isBaseline
    boolean isSameTransaction
    boolean showDetails
}
