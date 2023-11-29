package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location

class QuantityPickedByLocationAndProductDto {
    Location binLocation
    InventoryItem inventoryItem
    Integer quantityAllocated
}
