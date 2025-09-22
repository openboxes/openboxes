package org.pih.warehouse.inventory.product.availability

import groovy.transform.EqualsAndHashCode

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product

/**
 * A key on [bin location + inventory item] for uniquely identifying an available item record
 */
@EqualsAndHashCode(excludes=["inventoryItem", "binLocation"])
class AvailableItemKey {

    Location binLocation
    InventoryItem inventoryItem
    String key

    AvailableItemKey(AvailableItem availableItem) {
        this(availableItem?.binLocation, availableItem?.inventoryItem)
    }

    AvailableItemKey(Location binLocation, InventoryItem inventoryItem) {
        this.binLocation = binLocation
        this.inventoryItem = inventoryItem
        key = asKey(binLocation, inventoryItem)
    }

    String asKey(Location binLocation, InventoryItem inventoryItem) {
        return "${binLocation?.id}-${inventoryItem?.id}"
    }

    boolean equals(Location binLocation, InventoryItem inventoryItem) {
        return key == asKey(binLocation, inventoryItem)
    }

    boolean isForProduct(Product product) {
        return inventoryItem?.productId == product.id
    }
}
