package org.pih.warehouse.inboundSortation

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product

class PutawayContext {
    Location facility
    Product product
    InventoryItem inventoryItem
    String lotNumber
    Date expirationDate
    Location currentBinLocation
    Location preferredBin
    Integer quantity
}
