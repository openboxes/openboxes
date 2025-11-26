package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.product.availability.AvailableItemKey
import org.pih.warehouse.inventory.product.availability.AvailableItemMap
import org.pih.warehouse.product.Product

class InventoryBaselineTransactionCommand<T> {
    Location facility
    T sourceObject
    Collection<Product> products
    AvailableItemMap availableItems
    Date transactionDate
    String comment
    Map<AvailableItemKey, String> transactionEntriesComments = [:]
    Boolean validateTransactionDates = true
    Boolean disableRefresh = false
}
