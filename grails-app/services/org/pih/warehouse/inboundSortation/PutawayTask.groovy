package org.pih.warehouse.inboundSortation

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product

class PutawayTask {
    Location facility
    Product product
    InventoryItem inventoryItem
    Location currentBinLocation
    Location putawayLocation
    Integer quantity
}
