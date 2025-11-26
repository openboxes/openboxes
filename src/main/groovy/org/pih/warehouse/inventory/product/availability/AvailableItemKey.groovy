package org.pih.warehouse.inventory.product.availability

import groovy.transform.EqualsAndHashCode

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product

/**
 * A key on [productId + bin location name + lot number] for uniquely identifying an available item record.
 *
 * We opt to key on these fields (instead of on [bin location id + inventory item id]) to support the case
 * where we're in the process of creating the bin or lot and so don't have an id field for them yet.
 */
@EqualsAndHashCode
class AvailableItemKey {

    String productId
    String productLot
    String binLocationName

    AvailableItemKey(AvailableItem availableItem) {
        this(availableItem?.binLocation, availableItem?.inventoryItem)
    }

    AvailableItemKey(Location binLocation, InventoryItem inventoryItem) {
        this(inventoryItem?.productId as String, inventoryItem?.lotNumber, binLocation?.name)
    }

    AvailableItemKey(String productId, String productLot, String binLocationName) {
        this.productId = productId
        this.productLot = productLot
        this.binLocationName = binLocationName
    }

    boolean equals(Location binLocation, InventoryItem inventoryItem) {
        return equals(inventoryItem?.productId as String, inventoryItem?.lotNumber, binLocation?.name)
    }

    boolean equals(String productId, String productLot, String binLocationName) {
        return this.productId == productId &&
                this.productLot == productLot &&
                this.binLocationName == binLocationName
    }

    boolean isForProduct(Product product) {
        return productId == product.id
    }
}
