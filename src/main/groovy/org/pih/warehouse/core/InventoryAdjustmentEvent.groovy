package org.pih.warehouse.core

import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.product.Product
import org.springframework.context.ApplicationEvent

class InventoryAdjustmentEvent extends ApplicationEvent {
    List<Product> products
    Location facility

    // If it is single line adjustment, then the baseline will be null
    Transaction baselineTransaction
    // If there is no adjustment transaction, then it is an inventory snapshot
    Transaction adjustmentTransaction

    InventoryAdjustmentEvent(List<Product> products, Location facility,
                             Transaction baselineTransaction, Transaction adjustmentTransaction) {
        super(baselineTransaction?.id ?: adjustmentTransaction?.id)
        this.baselineTransaction = baselineTransaction
        this.adjustmentTransaction = adjustmentTransaction
        this.products = products
        this.facility = facility
    }
}
