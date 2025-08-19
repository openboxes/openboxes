package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product
import org.springframework.context.ApplicationEvent

class RefreshInventoryTransactionsSummaryEvent extends ApplicationEvent {

    String transactionId

    Map<Product, List<TransactionEntry>> entriesByProduct

    Date transactionDate

    String inventoryId

    Integer quantitySum

    String transactionTypeId

    Boolean isDelete

    RefreshInventoryTransactionsSummaryEvent(Transaction source) {
        super(source)
        transactionId = source.id
        entriesByProduct = source.transactionEntries?.groupBy { it.inventoryItem?.product }
        inventoryId = source.inventory.id
        transactionTypeId = source.transactionType.id
        transactionDate = source.transactionDate
    }

    RefreshInventoryTransactionsSummaryEvent(Transaction source, boolean isDelete) {
        super(source)
        transactionId = source.id
        transactionTypeId = source.transactionType.id
        this.isDelete = isDelete
    }
}
