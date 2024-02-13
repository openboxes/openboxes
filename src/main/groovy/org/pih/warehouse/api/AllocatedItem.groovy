package org.pih.warehouse.api

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem

class AllocatedItem {
    Location binLocation
    InventoryItem inventoryItem
    Integer quantityAllocated
}
