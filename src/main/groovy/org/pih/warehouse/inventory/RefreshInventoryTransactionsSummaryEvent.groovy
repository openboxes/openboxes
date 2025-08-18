package org.pih.warehouse.inventory

import org.springframework.context.ApplicationEvent

class RefreshInventoryTransactionsSummaryEvent extends ApplicationEvent {

    String transactionId

    Map<String, List<TransactionEntry>> products

    Date transactionDate

    String inventoryId

    Integer quantitySum

    String transactionTypeId

    Boolean isDelete

    RefreshInventoryTransactionsSummaryEvent(Transaction source) {
        super(source)
        transactionId = source.id
        products = source.transactionEntries?.groupBy { it.inventoryItem?.product?.id }
        inventoryId = source.inventory.id
        transactionTypeId = source.transactionType.id
        transactionDate = source.transactionDate
    }

    RefreshInventoryTransactionsSummaryEvent(Transaction source, boolean isDelete) {
        super(source)
        transactionId = source.id
        this.isDelete = isDelete
    }
}
