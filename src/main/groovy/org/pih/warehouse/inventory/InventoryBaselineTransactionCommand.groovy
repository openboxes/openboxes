package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.product.availability.AvailableItemKey
import org.pih.warehouse.inventory.product.availability.AvailableItemMap
import org.pih.warehouse.product.Product

class InventoryBaselineTransactionCommand<T> implements Validateable {
    Location facility
    T sourceObject
    Collection<Product> products
    AvailableItemMap availableItems
    Date transactionDate
    String comment
    Map<AvailableItemKey, String> transactionEntriesComments = [:]
    Boolean validateTransactionDates = true
    Boolean disableRefresh = false
    TransactionSource transactionSource

    static constraints = {
        sourceObject(nullable: true)
        availableItems(nullable: true)
        transactionDate(nullable: true)
        comment(nullable: true)
        transactionEntriesComments(nullable: true)
        // TODO: (?) should probably become required in the future if we cover all places where transaction sources should be created
        transactionSource(nullable: true)
    }
}
