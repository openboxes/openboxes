package org.pih.warehouse.inventory

import org.springframework.context.ApplicationEvent

class RefreshCycleCountReportViewsEvent extends ApplicationEvent {

    Inventory inventory
    List<String> productIds
    Date transactionDate
    String transactionId
    String transactionTypeId

    RefreshCycleCountReportViewsEvent(Transaction source) {
        super(source)
        this.inventory = source.inventory
        this.productIds = source.associatedProducts
        this.transactionDate = source.transactionDate
        this.transactionId = source.id
        this.transactionTypeId = source.transactionType.id
    }

    RefreshCycleCountReportViewsEvent(Transaction source, boolean isDelete) {
        super(source)
        this.transactionId = source.id
    }
}
