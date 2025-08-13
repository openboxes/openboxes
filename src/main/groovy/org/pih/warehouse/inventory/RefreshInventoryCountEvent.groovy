package org.pih.warehouse.inventory

import org.springframework.context.ApplicationEvent

class RefreshInventoryCountEvent extends ApplicationEvent {

    Inventory inventory
    List<String> productIds
    Date transactionDate
    String transactionId
    String transactionTypeId

    RefreshInventoryCountEvent(Transaction source) {
        super(source)
        this.inventory = source.inventory
        this.productIds = source.associatedProducts
        this.transactionDate = source.transactionDate
        this.transactionId = source.id
        this.transactionTypeId = source.transactionType.id
    }

    RefreshInventoryCountEvent(Transaction source, boolean isDelete) {
        super(source)
        this.transactionId = source.id
    }
}
